package io.bitsquare.trade.exceptions;

public class MarketPriceNoAvailableException extends Exception {
    public MarketPriceNoAvailableException() {
        super("Market price is not available");
    }
}
