package io.bitsquare.btc.actor.event;

/**
 * <p>Message to return block chain download progress for wallet.</p>
 */
public class DownloadStarted {

    private final String networkId;
    private final Integer totalBlocks;


    public DownloadStarted(String networkId, Integer totalBlocks) {
        this.networkId = networkId;
        this.totalBlocks = totalBlocks;
    }

    public String getNetworkId() {
        return networkId;
    }

    public Integer getTotalBlocks() {
        return totalBlocks;
    }
}
