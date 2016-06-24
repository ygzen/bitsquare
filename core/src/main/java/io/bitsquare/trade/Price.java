package io.bitsquare.trade;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Monetary;

public interface Price {
    Monetary getVolume(Coin amount);

    String getPriceAsString();

    double getPriceAsDouble();

    long getPriceAsLong();

    String getCurrencyCode();

    String getCurrencyCodePair();
}
