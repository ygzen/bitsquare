package io.bitsquare.trade.actor.Event;

public class OfferValidationFailed {
    private final String offerId;
    private final String message;

    public OfferValidationFailed(String offerId, String message) {
        this.offerId = offerId;
        this.message = message;
    }
}
