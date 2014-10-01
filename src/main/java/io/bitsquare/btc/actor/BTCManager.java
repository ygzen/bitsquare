package io.bitsquare.btc.actor;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.kits.WalletAppKit;
import com.google.bitcoin.params.MainNetParams;
import com.google.bitcoin.params.RegTestParams;
import io.bitsquare.BitSquare;
import io.bitsquare.btc.actor.command.InitializeWallet;
import io.bitsquare.btc.actor.event.WalletInitialized;

import java.io.File;
import java.util.List;

public class BTCManager extends AbstractActor {

    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    private final ActorPeerWalletEventListener actorPeerWalletEventListener;

    public final static String NAME = "btcManager";

    public static Props getProps() {
        return Props.create(BTCManager.class);
    }

    private WalletAppKit walletAppKit;
    private Wallet wallet;


    public BTCManager() {

        actorPeerWalletEventListener = new ActorPeerWalletEventListener(context().system(), self());

        receive(ReceiveBuilder.
                        match(InitializeWallet.class, m -> {
                            log.debug("Received message: {}", m);
                            setupWallet(m.getNetworkId(), m.getWalletPrefix());
                        }).
//                        match(SendCoins.class, sc -> {
//                            log.debug("Received message: {}", sc);
//                            Address address = new Address(this.params, Hex.decode(sc.getAddress()));
//                            // Send TX and wait for confirmation
//                            Wallet.SendResult result = wallet.sendCoins(walletAppKit.peerGroup(), address,
//                                    sc.getValue());
//
//                            Futures.addCallback(result.broadcastComplete,
//                                    new FutureCallback<Transaction>() {
//                                        @Override
//                                        public void onSuccess(@Nullable Transaction transaction) {
//                                            context().sender().tell(new CoinsSent(sc, transaction.getHash()),
//                                                    context().self());
//                                        }
//
//                                        @Override
//                                        public void onFailure(Throwable throwable) {
//                                            if (throwable instanceof InsufficientMoneyException) {
//                                                context().sender().tell(new InsufficientCoins(sc),
//                                                        context().self());
//                                            }
//                                            else log.error(throwable.toString());
//                                        }
//                                    },
//                                    context().system().dispatcher());
//                        }).
        matchAny(o -> log.info("received unknown message")).build()
        );
    }

    private void setupWallet(String networkId, String walletPrefix) {

        log.debug("setupWallet");

        actorPeerWalletEventListener.setNetworkId(networkId);
        actorPeerWalletEventListener.addReceiver(sender());
        File dataDir = new File(System.getProperty("user.home") + "/Library/" + BitSquare.getAppName());
        NetworkParameters netParams = NetworkParameters.fromID(networkId);

        ActorRef sender = sender();

        // If seed is non-null it means we are restoring from backup.
        walletAppKit = new WalletAppKit(netParams, dataDir, walletPrefix) {
            @Override
            protected void onSetupCompleted() {

                walletAppKit.peerGroup().setBloomFilterFalsePositiveRate(0.00001);

                // setup wallet
                wallet = walletAppKit.wallet();
                wallet.addEventListener(actorPeerWalletEventListener);

                // Don't make the user wait for confirmations for now, as the intention is they're sending it
                // their own money!
                wallet.allowSpendingUnconfirmedTransactions();

                if (netParams == RegTestParams.get()) {
                    walletAppKit.peerGroup().setMinBroadcastConnections(1);
                } else {
                    walletAppKit.peerGroup().setMaxConnections(11);
                    walletAppKit.peerGroup().setMinBroadcastConnections(2);
                }

                // Send seed code back to sender of init message
                List<String> seedCode = walletAppKit.wallet().getKeyChainSeed().getMnemonicCode();

                sender.tell(new WalletInitialized(params.getId(), seedCode), self());
            }

        };

        if (netParams == RegTestParams.get()) {
            walletAppKit.connectToLocalHost();   // You should run a regtest mode bitcoind locally.
        }

        if (netParams == MainNetParams.get()) {
            // Checkpoints are block headers that ship inside our app: for a new user, we pick the last header
            // in the checkpoints file and then download the rest from the network. It makes things much faster.
            // Checkpoint files are made using the BuildCheckpoints tool and usually we have to download the
            // last months worth or more (takes a few seconds).
            walletAppKit.setCheckpoints(getClass().getResourceAsStream("checkpoints"));
            // As an example!
            // walletAppKit.useTor();
        }

        walletAppKit.setDownloadListener(actorPeerWalletEventListener)
                .setBlockingStartup(false)
                .restoreWalletFromSeed(null)
                .setUserAgent("BitSquare", "0.1");

        // Now start the appkit. This will take a second or two - we could show a temporary splash screen
        // or progress widget to keep the user engaged whilst we initialise, but we don't.
        walletAppKit.startAsync();
    }

    @Override
    public void postStop() throws Exception {
        log.debug("postStop");
        walletAppKit.stopAsync();
        super.postStop();
    }
}
