package io.bitsquare.trade;

import com.google.common.math.LongMath;
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

    public final Coin numeratorAsBitcoin; //numerator
    public final Altcoin denominatorAsAltcoin; //denominator

    /**
     * One altcoin is worth this amount of bitcoin.
     */
    public AltcoinPrice(String altcoinCurrencyCode, Coin numeratorAsBitcoin) {
        checkArgument(numeratorAsBitcoin.isPositive());
        checkArgument(altcoinCurrencyCode != null, "currency code required");
        this.numeratorAsBitcoin = numeratorAsBitcoin;
        this.denominatorAsAltcoin = Altcoin.valueOf(altcoinCurrencyCode, Altcoin.COIN_VALUE);
    }


    @Override
    public String getPriceAsString() {
        return numeratorAsBitcoin.toPlainString();
    }

    @Override
    public long getPriceAsLong() {
        return numeratorAsBitcoin.value;
    }

    public long getInvertedPriceAsLong() {
        log.error(denominatorAsAltcoin.toFriendlyString());
        log.error(Coin.COIN.toFriendlyString());
        log.error(numeratorAsBitcoin.toFriendlyString());
        log.error("COIN " + Coin.COIN.value);
        log.error("coin " + numeratorAsBitcoin.value);
        log.error("altcoin " + denominatorAsAltcoin.value);
        log.error("" + Coin.COIN.value / numeratorAsBitcoin.value);
        log.error("" + Coin.COIN.divide(numeratorAsBitcoin) * 10000);

        return Coin.COIN.divide(numeratorAsBitcoin) * 10000;
    }

    @Override
    public double getPriceAsDouble() {
        return (double) numeratorAsBitcoin.value / LongMath.pow(10, numeratorAsBitcoin.smallestUnitExponent());
    }

    @Override
    public String getCurrencyCode() {
        return denominatorAsAltcoin.currencyCode;
    }

    @Override
    public String getCurrencyCodePair() {
        return "BTC/" + denominatorAsAltcoin.currencyCode;
    }

    @Override
    public boolean isZero() {
        return numeratorAsBitcoin.isZero();
    }

    @Override
    public boolean isPositive() {
        return numeratorAsBitcoin.isPositive();
    }

    @Override
    public String toFriendlyString() {
        return numeratorAsBitcoin.toFriendlyString();
    }

    @Override
    public Altcoin getVolume(Coin amount) {
        // Use BigInteger because it's much easier to maintain full precision without overflowing.
        final BigInteger coinVal = BigInteger.valueOf(numeratorAsBitcoin.value);
        if (coinVal.compareTo(BigInteger.ZERO) == 0)
            return Altcoin.valueOf(denominatorAsAltcoin.currencyCode, 0);
        BigInteger converted = BigInteger.valueOf(amount.value).multiply(BigInteger.valueOf(denominatorAsAltcoin.value)).divide(coinVal);
        if (converted.compareTo(BigInteger.valueOf(Long.MAX_VALUE)) > 0
                || converted.compareTo(BigInteger.valueOf(Long.MIN_VALUE)) < 0)
            throw new ArithmeticException("Overflow");
        return Altcoin.valueOf(denominatorAsAltcoin.currencyCode, converted.longValue());
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

        checkArgument(volumeAsAltcoin.currencyCode.equals(denominatorAsAltcoin.currencyCode), "Currency mismatch: %s vs %s",
                volumeAsAltcoin.currencyCode, denominatorAsAltcoin.currencyCode);
        // Use BigInteger because it's much easier to maintain full precision without overflowing.
        final BigInteger converted = BigInteger.valueOf(volumeAsAltcoin.value)
                .multiply(BigInteger.valueOf(numeratorAsBitcoin.value))
                .divide(BigInteger.valueOf(denominatorAsAltcoin.value));

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

        if (numeratorAsBitcoin != null ? !numeratorAsBitcoin.equals(that.numeratorAsBitcoin) : that.numeratorAsBitcoin != null)
            return false;
        return !(denominatorAsAltcoin != null ? !denominatorAsAltcoin.equals(that.denominatorAsAltcoin) : that.denominatorAsAltcoin != null);

    }

    @Override
    public int hashCode() {
        int result = numeratorAsBitcoin != null ? numeratorAsBitcoin.hashCode() : 0;
        result = 31 * result + (denominatorAsAltcoin != null ? denominatorAsAltcoin.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Object other) {
        if (other instanceof AltcoinPrice)
            return numeratorAsBitcoin.compareTo(((AltcoinPrice) other).numeratorAsBitcoin);
        else
            return 0;
    }

    @Override
    public String toString() {
        return "AltcoinPrice{" +
                "coin=" + numeratorAsBitcoin +
                ", altcoin=" + denominatorAsAltcoin +
                '}';
    }
}

