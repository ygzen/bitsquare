package io.bitsquare.trade;

import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Monetary;
import org.bitcoinj.utils.ExchangeRate;
import org.bitcoinj.utils.Fiat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.google.common.base.Preconditions.checkArgument;

// Price in Fiat per BTC 
public class FiatPrice extends ExchangeRate implements Price {
    private static final Logger log = LoggerFactory.getLogger(FiatPrice.class);

    // One bitcoin is worth this amount of fiat.
    public FiatPrice(Fiat fiat) {
        super(fiat);
    }

    @Override
    public String getPriceAsString() {
        return fiat.toPlainString();
    }

    @Override
    public long getPriceAsLong() {
        return fiat.value;
    }

    @Override
    public double getPriceAsDouble() {

        return 0;
    }

    @Override
    public String getCurrencyCode() {
        return fiat.getCurrencyCode();
    }

    @Override
    public String getCurrencyCodePair() {
        return fiat.currencyCode + "/BTC";
    }

    @Override
    public boolean isZero() {
        return getPriceAsLong() == 0;
    }

    @Override
    public Fiat getVolume(Coin amount) {
        return super.coinToFiat(amount);
    }

    @Override
    public Coin getAmountFromVolume(Monetary volume) {
        checkArgument(volume instanceof Fiat, "Volume need to be instance of Fiat. volume=" + volume);
        return super.fiatToCoin((Fiat) volume);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FiatPrice)) return false;

        FiatPrice that = (FiatPrice) o;

        if (coin != null ? !coin.equals(that.coin) : that.coin != null) return false;
        return !(fiat != null ? !fiat.equals(that.fiat) : that.fiat != null);

    }

    @Override
    public int hashCode() {
        int result = coin != null ? coin.hashCode() : 0;
        result = 31 * result + (fiat != null ? fiat.hashCode() : 0);
        return result;
    }


    @Override
    public int compareTo(Object other) {
        if (other instanceof FiatPrice)
            return fiat.compareTo(((FiatPrice) other).fiat);
        else
            return 0;
    }


}
