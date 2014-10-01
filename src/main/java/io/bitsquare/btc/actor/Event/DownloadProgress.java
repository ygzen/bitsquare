package io.bitsquare.btc.actor.event;

import java.util.Date;

/**
 * <p>Message to return block chain download progress for wallet.</p>
 */
public class DownloadProgress {

    private final String networkId;
    private final Double percent;
    private final Integer blocksSoFar;
    private final Date date;

    public DownloadProgress(String networkId, Double percent, Integer blocksSoFar, Date date) {

        this.networkId = networkId;
        this.blocksSoFar = blocksSoFar;
        this.date = date;
        this.percent = percent;
    }

    public String getNetworkId() {
        return networkId;
    }

    public Double getPercent() {
        return percent;
    }

    public Integer getBlocksSoFar() {
        return blocksSoFar;
    }

    public Date getDate() {
        return date;
    }
}
