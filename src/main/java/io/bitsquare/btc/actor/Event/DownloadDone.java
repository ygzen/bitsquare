package io.bitsquare.btc.actor.event;

/**
 * <p>Message to return block chain download progress for wallet.</p>
 */
public class DownloadDone {

    private final String networkId;


    public DownloadDone(String networkId) {
        this.networkId = networkId;
    }

    public String getNetworkId() {
        return networkId;
    }
}
