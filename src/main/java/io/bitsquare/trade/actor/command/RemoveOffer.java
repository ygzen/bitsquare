package io.bitsquare.trade.actor.command;

import io.bitsquare.trade.Offer;

/**
 * <p>Message to create a new offer to buy or sell Bitcoins.</p>
 */
public class RemoveOffer {

    private final Offer offer;

    public RemoveOffer(Offer offer) {
        this.offer = offer;
    }

    public Offer getOffer() {
        return offer;
    }
}
