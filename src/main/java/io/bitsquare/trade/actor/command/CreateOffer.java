package io.bitsquare.trade.actor.command;

import io.bitsquare.trade.Offer;

/**
 * <p>Message to create a new offer to buy or sell Bitcoins.</p>
 */
public class CreateOffer {

    private final Offer offer;

    public CreateOffer(Offer offer) {
        this.offer = offer;
    }

    public Offer getOffer() {
        return offer;
    }
}
