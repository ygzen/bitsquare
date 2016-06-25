package io.bitsquare.trade;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Monetary;

public interface Price extends Comparable {
    Monetary getVolume(Coin amount);

    Coin getAmountFromVolume(Monetary volume);

    String getPriceAsString();

    double getPriceAsDouble();

    long getPriceAsLong();

    String getCurrencyCode();

    String getCurrencyCodePair();

    boolean isZero();
}
