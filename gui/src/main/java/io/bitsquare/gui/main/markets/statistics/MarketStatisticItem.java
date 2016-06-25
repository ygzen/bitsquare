package io.bitsquare.gui.main.markets.statistics;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Monetary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;

public class MarketStatisticItem {
    private static final Logger log = LoggerFactory.getLogger(MarketStatisticItem.class);
    public final String currencyCode;
    public final int numberOfOffers;
    @Nullable
    public final Monetary spread;
    public final Coin totalAmount;

    public MarketStatisticItem(String currencyCode, int numberOfOffers, @Nullable Monetary spread, Coin totalAmount) {
        this.currencyCode = currencyCode;
        this.numberOfOffers = numberOfOffers;
        this.spread = spread;
        this.totalAmount = totalAmount;
    }
}
