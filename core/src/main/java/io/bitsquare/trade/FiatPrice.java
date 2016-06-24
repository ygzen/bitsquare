package io.bitsquare.trade;

import org.bitcoinj.core.Coin;
import org.bitcoinj.utils.ExchangeRate;
import org.bitcoinj.utils.Fiat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Price in Fiat per BTC 
public class FiatPrice extends ExchangeRate implements Price {
    private static final Logger log = LoggerFactory.getLogger(FiatPrice.class);

    @Override
    public Fiat getVolume(Coin amount) {
        return super.coinToFiat(amount);
    }

    // One bitcoin is worth this amount of fiat.
    public FiatPrice(Fiat fiat) {
        super(fiat);
    }
}
