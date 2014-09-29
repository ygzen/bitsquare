package io.bitsquare.trade.actor.Event;


import io.bitsquare.trade.Offer;

import org.joda.time.DateTime;

/**
 * <p>Message to add or remove a new offer to buy or sell Bitcoins.</p>
 */
public class OfferRemoved {

    private final Offer offer;

    private final DateTime removedTimestamp;

    protected OfferRemoved(Offer offer, DateTime removedTimestamp) {
        this.offer = offer;
        this.removedTimestamp = removedTimestamp;
    }

    public OfferRemoved(Offer offer) {
        this.offer = offer;
        this.removedTimestamp = DateTime.now();
    }

    public Offer getOffer() {
        return offer;
    }

    public DateTime getRemovedTimestamp() {
        return removedTimestamp;
    }
}
