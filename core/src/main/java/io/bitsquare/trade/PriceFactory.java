package io.bitsquare.trade;

import io.bitsquare.locale.CurrencyUtil;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Monetary;
import org.bitcoinj.utils.Fiat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceFactory {
    private static final Logger log = LoggerFactory.getLogger(PriceFactory.class);

    public static Price getPrice(String currencyCode, Monetary monetary) {
        if (CurrencyUtil.isCryptoCurrency(currencyCode) && monetary instanceof Coin)
            return new AltcoinPrice(currencyCode, (Coin) monetary);
        else if (monetary instanceof Fiat)
            return new FiatPrice((Fiat) monetary);
        else
            throw new IllegalArgumentException("Monetary object is not of correct type. monetary.class=" +
                    monetary.getClass().getSimpleName());
    }

    public static Price getPriceFromString(String currencyCode, String priceAsString) {
        Monetary monetary;
        if (CurrencyUtil.isCryptoCurrency(currencyCode))
            monetary = Coin.parseCoin(priceAsString);
        else
            monetary = Fiat.parseFiat(currencyCode, priceAsString);

        return getPrice(currencyCode, monetary);
    }

    public static Price getPriceFromLong(String currencyCode, long priceAsLong) {
        Monetary monetary;
        if (CurrencyUtil.isCryptoCurrency(currencyCode))
            monetary = Coin.valueOf(priceAsLong);
        else
            monetary = Fiat.valueOf(currencyCode, priceAsLong);

        return getPrice(currencyCode, monetary);
    }
}
