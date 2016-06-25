package io.bitsquare.trade;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Monetary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigInteger;

import static com.google.common.base.Preconditions.checkArgument;

// Price in BTC per altcoin. It is inverted to the FiatPrice where Fiat/BTC is used.
public class AltcoinPrice implements Serializable, Price {
    private static final Logger log = LoggerFactory.getLogger(AltcoinPrice.class);

    public final Coin coin;
    public final Altcoin altcoin;

    /**
     * One altcoin is worth this amount of bitcoin.
     */
    public AltcoinPrice(String altcoinCurrencyCode, Coin coin) {
        checkArgument(coin.isPositive());
        checkArgument(altcoinCurrencyCode != null, "currency code required");
        this.coin = coin;
        this.altcoin = Altcoin.valueOf(altcoinCurrencyCode, Altcoin.COIN_VALUE);
    }


    @Override
    public String getPriceAsString() {
        return coin.toPlainString();
    }

    @Override
    public long getPriceAsLong() {
        return coin.value;
    }

    public long getInvertedPriceAsLong() {
        return Coin.COIN.divide(coin);
    }

    @Override
    public double getPriceAsDouble() {
        return 0;
    }

    @Override
    public String getCurrencyCode() {
        return altcoin.currencyCode;
    }

    @Override
    public String getCurrencyCodePair() {
        return "BTC/" + altcoin.currencyCode;
    }

    @Override
    public boolean isZero() {
        return getPriceAsLong() == 0;
    }

    @Override
    public Altcoin getVolume(Coin amount) {
        // Use BigInteger because it's much easier to maintain full precision without overflowing.
        final BigInteger coinVal = BigInteger.valueOf(coin.value);
        if (coinVal.compareTo(BigInteger.ZERO) == 0)
            return Altcoin.valueOf(altcoin.currencyCode, 0);
        BigInteger converted = BigInteger.valueOf(amount.value).multiply(BigInteger.valueOf(altcoin.value)).divide(coinVal);
        if (converted.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                || converted.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0)
            throw new ArithmeticException("Overflow");
        return Altcoin.valueOf(altcoin.currencyCode, converted.longValue());
    }

    /**
     * Convert a altCoin amount to a coin amount using this exchange rate.
     *
     * @throws ArithmeticException if the converted coin amount is too high or too low.
     */
    @Override
    public Coin getAmountFromVolume(Monetary volume) {
        checkArgument(volume instanceof Altcoin, "Volume need to be instance of Altcoin. volume=" + volume);
        Altcoin volumeAsAltcoin = (Altcoin) volume;

        checkArgument(volumeAsAltcoin.currencyCode.equals(altcoin.currencyCode), "Currency mismatch: %s vs %s",
                volumeAsAltcoin.currencyCode, altcoin.currencyCode);
        // Use BigInteger because it's much easier to maintain full precision without overflowing.
        final BigInteger converted = BigInteger.valueOf(volumeAsAltcoin.value)
                .multiply(BigInteger.valueOf(coin.value))
                .divide(BigInteger.valueOf(altcoin.value));

        if (converted.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                || converted.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0)
            throw new ArithmeticException("Overflow");
        try {
            return Coin.valueOf(converted.longValue());
        } catch (IllegalArgumentException x) {
            throw new ArithmeticException("Overflow: " + x.getMessage());
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AltcoinPrice)) return false;

        AltcoinPrice that = (AltcoinPrice) o;

        if (coin != null ? !coin.equals(that.coin) : that.coin != null) return false;
        return !(altcoin != null ? !altcoin.equals(that.altcoin) : that.altcoin != null);

    }

    @Override
    public int hashCode() {
        int result = coin != null ? coin.hashCode() : 0;
        result = 31 * result + (altcoin != null ? altcoin.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Object other) {
        if (other instanceof AltcoinPrice)
            return coin.compareTo(((AltcoinPrice) other).coin);
        else
            return 0;
    }

}

