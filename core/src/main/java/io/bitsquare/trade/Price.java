package io.bitsquare.trade;

import io.bitsquare.btc.pricefeed.MarketPrice;
import io.bitsquare.btc.pricefeed.PriceFeed;
import io.bitsquare.common.util.MathUtils;
import io.bitsquare.locale.CurrencyUtil;
import io.bitsquare.trade.exceptions.MarketPriceNoAvailableException;
import io.bitsquare.trade.offer.Offer;
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

    boolean isPositive();

    String toFriendlyString();

    static double getPercentagePriceFromPrice(PriceFeed priceFeed, String currencyCode, Offer.Direction direction, double price) throws MarketPriceNoAvailableException {
        MarketPrice marketPrice = priceFeed.getMarketPrice(currencyCode);
        if (marketPrice != null) {
            double marketPriceAsDouble = marketPrice.getPrice(direction == Offer.Direction.BUY ? PriceFeed.Type.ASK : PriceFeed.Type.BID);
            double relation = price / marketPriceAsDouble;
            double result;
            if (CurrencyUtil.isCryptoCurrency(currencyCode)) {
                result = direction == Offer.Direction.BUY ? relation - 1 : 1 - relation;
            } else
                result = direction == Offer.Direction.BUY ? 1 - relation : relation - 1;

            return MathUtils.roundDouble(result, 4);
        } else {
            throw new MarketPriceNoAvailableException();
        }
    }

    static double getPriceFromPercentagePrice(PriceFeed priceFeed, String currencyCode, Offer.Direction direction, double percentagePrice) throws MarketPriceNoAvailableException {
        MarketPrice marketPrice = priceFeed.getMarketPrice(currencyCode);
        if (marketPrice != null) {
            double marketPriceAsDouble = marketPrice.getPrice(direction == Offer.Direction.BUY ? PriceFeed.Type.ASK : PriceFeed.Type.BID);
            double factor;
            if (CurrencyUtil.isCryptoCurrency(currencyCode))
                factor = direction == Offer.Direction.BUY ? 1 + percentagePrice : 1 - percentagePrice;
            else
                factor = direction == Offer.Direction.BUY ? 1 - percentagePrice : 1 + percentagePrice;

            double targetPrice = marketPriceAsDouble * factor;
            return MathUtils.roundDouble(targetPrice, CurrencyUtil.isCryptoCurrency(currencyCode) ? 8 : 4);
        } else {
            throw new MarketPriceNoAvailableException();
        }
    }
}
