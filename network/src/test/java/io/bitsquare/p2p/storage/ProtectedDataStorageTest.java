package io.bitsquare.p2p.storage;

import io.bitsquare.common.UserThread;
import io.bitsquare.common.crypto.*;
import io.bitsquare.crypto.EncryptionService;
import io.bitsquare.p2p.NodeAddress;
import io.bitsquare.p2p.P2PService;
import io.bitsquare.p2p.TestUtils;
import io.bitsquare.p2p.network.NetworkNode;
import io.bitsquare.p2p.peers.PeerManager;
import io.bitsquare.p2p.storage.messages.RefreshTTLMessage;
import io.bitsquare.p2p.storage.mocks.MockData;
import io.bitsquare.p2p.storage.storageentry.ProtectedStorageEntry;
import io.bitsquare.storage.FileUtil;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

@Ignore
public class ProtectedDataStorageTest {
    private static final Logger log = LoggerFactory.getLogger(ProtectedDataStorageTest.class);

    final boolean useClearNet = true;
    private final Set<NodeAddress> seedNodes = new HashSet<>();
    private NetworkNode networkNode1;
    private PeerManager peerManager1;
    private EncryptionService encryptionService1, encryptionService2;
    private P2PDataStorage dataStorage1;
    private KeyPair storageSignatureKeyPair1, storageSignatureKeyPair2;
    private KeyRing keyRing1, keyRing2;
    private MockData mockData;
    private final int sleepTime = 100;
    private File dir1;
    private File dir2;

    @Before
    public void setup() throws InterruptedException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, CryptoException, SignatureException, InvalidKeyException {
        Security.addProvider(new BouncyCastleProvider());
        dir1 = File.createTempFile("temp_tests1", "");
        dir1.delete();
        dir1.mkdir();
        dir2 = File.createTempFile("temp_tests2", "");
        dir2.delete();
        dir2.mkdir();

        UserThread.setExecutor(Executors.newSingleThreadExecutor());
        P2PDataStorage.CHECK_TTL_INTERVAL_SEC = 500;

        keyRing1 = new KeyRing(new KeyStorage(dir1));

        storageSignatureKeyPair1 = keyRing1.getSignatureKeyPair();
        encryptionService1 = new EncryptionService(keyRing1);
        P2PService p2PService = TestUtils.getAndStartSeedNode(8001, useClearNet, seedNodes).getSeedNodeP2PService();
        networkNode1 = p2PService.getNetworkNode();
        peerManager1 = p2PService.getPeerManager();
        dataStorage1 = p2PService.getP2PDataStorage();

        // for mailbox
        keyRing2 = new KeyRing(new KeyStorage(dir2));
        storageSignatureKeyPair2 = keyRing2.getSignatureKeyPair();
        encryptionService2 = new EncryptionService(keyRing2);

        mockData = new MockData("mockData", keyRing1.getSignatureKeyPair().getPublic());
        Thread.sleep(sleepTime);
    }

    @After
    public void tearDown() throws InterruptedException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, CryptoException, SignatureException, InvalidKeyException {
        Thread.sleep(sleepTime);
        if (dataStorage1 != null) dataStorage1.shutDown();
        if (peerManager1 != null) peerManager1.shutDown();

        if (networkNode1 != null) {
            CountDownLatch shutDownLatch = new CountDownLatch(1);
            networkNode1.shutDown(() -> shutDownLatch.countDown());
            shutDownLatch.await();
        }

        Path path = Paths.get(TestUtils.test_dummy_dir);
        File dir = path.toFile();
        FileUtil.deleteDirectory(dir);
        FileUtil.deleteDirectory(dir1);
        FileUtil.deleteDirectory(dir2);
    }

    //@Test
    public void testAddAndRemove() throws InterruptedException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, CryptoException, SignatureException, InvalidKeyException, NoSuchProviderException {
        ProtectedStorageEntry data = dataStorage1.getProtectedData(mockData, storageSignatureKeyPair1);
        Assert.assertTrue(dataStorage1.add(data, null, null, true));
        Assert.assertEquals(1, dataStorage1.getMap().size());

        int newSequenceNumber = data.sequenceNumber + 1;
        byte[] hashOfDataAndSeqNr = Hash.getHash(new P2PDataStorage.DataAndSeqNrPair(data.getStoragePayload(), newSequenceNumber));
        byte[] signature = Sig.sign(storageSignatureKeyPair1.getPrivate(), hashOfDataAndSeqNr);
        ProtectedStorageEntry dataToRemove = new ProtectedStorageEntry(data.getStoragePayload(), data.ownerPubKey, newSequenceNumber, signature);
        Assert.assertTrue(dataStorage1.remove(dataToRemove, null, true));
        Assert.assertEquals(0, dataStorage1.getMap().size());
    }

    // @Test
    public void testTTL() throws InterruptedException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, CryptoException, SignatureException, InvalidKeyException, NoSuchProviderException {
        mockData.ttl = (int) (P2PDataStorage.CHECK_TTL_INTERVAL_SEC * 1.5);
        ProtectedStorageEntry data = dataStorage1.getProtectedData(mockData, storageSignatureKeyPair1);
        log.debug("data.date " + data.creationTimeStamp);
        Assert.assertTrue(dataStorage1.add(data, null, null, true));
        log.debug("test 1");
        Assert.assertEquals(1, dataStorage1.getMap().size());

        Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_SEC);
        log.debug("test 2");
        Assert.assertEquals(1, dataStorage1.getMap().size());

        Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_SEC * 2);
        log.debug("test 3 removed");
        Assert.assertEquals(0, dataStorage1.getMap().size());
    }

    /* //@Test
     public void testRePublish() throws InterruptedException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, CryptoException, SignatureException, InvalidKeyException, NoSuchProviderException {
         mockData.ttl = (int) (P2PDataStorage.CHECK_TTL_INTERVAL_MILLIS * 1.5);
         ProtectedData data = dataStorage1.getDataWithSignedSeqNr(mockData, storageSignatureKeyPair1);
         Assert.assertTrue(dataStorage1.add(data, null));
         Assert.assertEquals(1, dataStorage1.getMap().size());
         Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_MILLIS);
         log.debug("test 1");
         Assert.assertEquals(1, dataStorage1.getMap().size());
 
         data = dataStorage1.getDataWithSignedSeqNr(mockData, storageSignatureKeyPair1);
         Assert.assertTrue(dataStorage1.rePublish(data, null));
         Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_MILLIS);
         log.debug("test 2");
         Assert.assertEquals(1, dataStorage1.getMap().size());
 
         data = dataStorage1.getDataWithSignedSeqNr(mockData, storageSignatureKeyPair1);
         Assert.assertTrue(dataStorage1.rePublish(data, null));
         Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_MILLIS);
         log.debug("test 3");
         Assert.assertEquals(1, dataStorage1.getMap().size());
 
         Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_MILLIS);
         log.debug("test 4");
         Assert.assertEquals(1, dataStorage1.getMap().size());
 
         Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_MILLIS * 2);
         log.debug("test 5 removed");
         Assert.assertEquals(0, dataStorage1.getMap().size());
     }
 */
    @Test
    public void testRefreshTTL() throws InterruptedException, NoSuchAlgorithmException, CertificateException, KeyStoreException, IOException, CryptoException, SignatureException, InvalidKeyException, NoSuchProviderException {
        mockData.ttl = (int) (P2PDataStorage.CHECK_TTL_INTERVAL_SEC * 1.5);
        ProtectedStorageEntry data = dataStorage1.getProtectedData(mockData, storageSignatureKeyPair1);
        Assert.assertTrue(dataStorage1.add(data, null, null, true));
        Assert.assertEquals(1, dataStorage1.getMap().size());
        Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_SEC);
        log.debug("test 1");
        Assert.assertEquals(1, dataStorage1.getMap().size());

        RefreshTTLMessage refreshTTLMessage = dataStorage1.getRefreshTTLMessage(mockData, storageSignatureKeyPair1);
        Assert.assertTrue(dataStorage1.refreshTTL(refreshTTLMessage, null, true));
        Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_SEC);
        log.debug("test 2");
        Assert.assertEquals(1, dataStorage1.getMap().size());

        refreshTTLMessage = dataStorage1.getRefreshTTLMessage(mockData, storageSignatureKeyPair1);
        Assert.assertTrue(dataStorage1.refreshTTL(refreshTTLMessage, null, true));
        Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_SEC);
        log.debug("test 3");
        Assert.assertEquals(1, dataStorage1.getMap().size());

        Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_SEC);
        log.debug("test 4");
        Assert.assertEquals(1, dataStorage1.getMap().size());

        Thread.sleep(P2PDataStorage.CHECK_TTL_INTERVAL_SEC * 2);
        log.debug("test 5 removed");
        Assert.assertEquals(0, dataStorage1.getMap().size());
    }
}
