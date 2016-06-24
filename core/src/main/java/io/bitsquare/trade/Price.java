package io.bitsquare.trade;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Monetary;

public interface Price {

    Monetary getVolume(Coin amount);

}
