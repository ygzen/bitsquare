package io.bitsquare.btc.actor;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import com.google.bitcoin.core.*;
import com.google.bitcoin.script.Script;
import io.bitsquare.btc.actor.event.DownloadDone;
import io.bitsquare.btc.actor.event.DownloadProgress;
import io.bitsquare.btc.actor.event.DownloadStarted;

import javax.annotation.Nullable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ActorPeerWalletEventListener extends DownloadListener implements WalletEventListener {

    private final ActorRef sender;
    private final Map<Integer, ActorRef> receivers = new HashMap<Integer, ActorRef>();
    private final LoggingAdapter log;

    private String networkId;

    public ActorPeerWalletEventListener(ActorSystem actorSystem, ActorRef sender) {
        this.sender = sender;
        log = Logging.getLogger(actorSystem, sender);
    }

    public String getNetworkId() {
        return networkId;
    }

    public void setNetworkId(String networkId) {
        this.networkId = networkId;
    }

    public void addReceiver(ActorRef receiver) {
        receivers.put(receiver.path().uid(), receiver);
    }

    public void removeReceiver(ActorRef receiver) {
        receivers.remove(receiver.path().uid());
    }

    private void tellReceivers(Object message) {
        receivers.values().forEach(r -> r.tell(message, sender));
    }

    // PeerEventListener

    @Override
    public void onBlocksDownloaded(Peer peer, Block block, int blocksLeft) {
        log.debug("onBlocksDownloaded");
        super.onBlocksDownloaded(peer, block, blocksLeft);
    }

    @Override
    public void onChainDownloadStarted(Peer peer, int blocksLeft) {
        log.debug("onChainDownloadStarted");
        super.onChainDownloadStarted(peer, blocksLeft);
    }

    @Override
    public void onPeerConnected(Peer peer, int peerCount) {
        log.debug("onPeerConnected");
        super.onPeerConnected(peer, peerCount);
    }

    @Override
    public void onPeerDisconnected(Peer peer, int peerCount) {
        log.debug("onPeerDisconnected");
        super.onPeerDisconnected(peer, peerCount);
    }

    @Override
    public Message onPreMessageReceived(Peer peer, Message m) {
        super.onPreMessageReceived(peer, m);
        //log.debug("onPreMessageReceived");
        //self().tell("MSG",self());
        return m;
    }

    @Override
    public void onTransaction(Peer peer, Transaction t) {
        log.debug("onTransaction");
        super.onTransaction(peer, t);
    }

    @Nullable
    @Override
    public List<Message> getData(Peer peer, GetDataMessage m) {
        log.debug("getData");
        return super.getData(peer, m);
    }

    // DownloadListener

    @Override
    protected void progress(double pct, int blocksSoFar, Date date) {
        log.debug("download progress");

        super.progress(pct, blocksSoFar, date);
        DownloadProgress progress = new DownloadProgress(networkId, pct, blocksSoFar, date);
        tellReceivers(progress);
    }

    @Override
    protected void startDownload(int blocks) {
        log.debug("start download");

        super.startDownload(blocks);
        DownloadStarted started = new DownloadStarted(networkId, blocks);
        tellReceivers(started);
    }

    @Override
    protected void doneDownload() {
        log.debug("done download");

        super.doneDownload();
        tellReceivers(new DownloadDone(networkId));
    }

    // KeyChainEventListener

    @Override
    public void onKeysAdded(List<ECKey> keys) {
        log.debug("onKeysAdded");
    }

    // WalletEventListener

    @Override
    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        log.debug("onCoinsReceived");
    }

    @Override
    public void onCoinsSent(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
        log.debug("onCoinsSent");
    }

    @Override
    public void onReorganize(Wallet wallet) {
        log.debug("onReorganize");
    }

    @Override
    public void onTransactionConfidenceChanged(Wallet wallet, Transaction tx) {
        log.debug("onTransactionConfidenceChanged");
    }

    @Override
    public void onWalletChanged(Wallet wallet) {
        log.debug("onWalletChanged");
    }

    @Override
    public void onScriptsAdded(Wallet wallet, List<Script> scripts) {
        log.debug("onScriptsAdded");
    }
}
