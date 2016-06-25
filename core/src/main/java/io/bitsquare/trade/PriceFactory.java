package io.bitsquare.trade;

import io.bitsquare.locale.CurrencyUtil;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Monetary;
import org.bitcoinj.utils.Fiat;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PriceFactory {
    private static final Logger log = LoggerFactory.getLogger(PriceFactory.class);

    public static Price getPrice(@NotNull String currencyCode, @NotNull Monetary monetary) {
        if (!currencyCode.isEmpty()) {
            if (CurrencyUtil.isCryptoCurrency(currencyCode) && monetary instanceof Coin)
                return new AltcoinPrice(currencyCode, (Coin) monetary);
            else if (monetary instanceof Fiat)
                return new FiatPrice((Fiat) monetary);
            else
                throw new IllegalArgumentException("Monetary object is not of correct type. monetary.class=" +
                        monetary.getClass().getSimpleName());
        } else {
            throw new IllegalArgumentException("currencyCode must not be empty");
        }
    }

    public static Price getPriceFromString(@NotNull String currencyCode, @NotNull String priceAsString) {
        if (!currencyCode.isEmpty() && !priceAsString.isEmpty()) {
            Monetary monetary;
            if (CurrencyUtil.isCryptoCurrency(currencyCode))
                monetary = Coin.parseCoin(priceAsString);
            else
                monetary = Fiat.parseFiat(currencyCode, priceAsString);

            return getPrice(currencyCode, monetary);
        } else {
            throw new IllegalArgumentException("currencyCode and priceAsString must not be empty.\n" +
                    "currencyCode=" + currencyCode + ", priceAsString=" + priceAsString);
        }
    }

    public static Price getPriceFromLong(@NotNull String currencyCode, long priceAsLong) {
        if (!currencyCode.isEmpty()) {
            Monetary monetary;
            if (CurrencyUtil.isCryptoCurrency(currencyCode))
                monetary = Coin.valueOf(priceAsLong);
            else
                monetary = Fiat.valueOf(currencyCode, priceAsLong);

            return getPrice(currencyCode, monetary);
        } else {
            throw new IllegalArgumentException("currencyCode must not be empty");
        }
    }
}
