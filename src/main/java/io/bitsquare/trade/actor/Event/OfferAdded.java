package io.bitsquare.trade.actor.event;

import io.bitsquare.trade.Offer;

import org.joda.time.DateTime;

/**
 * <p>Message to add or remove a new offer to buy or sell Bitcoins.</p>
 */
public class OfferAdded {

    private final Offer offer;

    private final DateTime addedTimestamp;

    protected OfferAdded(Offer offer, DateTime addedTimestamp) {
        this.offer = offer;
        this.addedTimestamp = addedTimestamp;
    }

    public OfferAdded(Offer offer) {
        this.offer = offer;
        this.addedTimestamp = DateTime.now();
    }

    public Offer getOffer() {
        return offer;
    }

    public DateTime getAddedTimestamp() {
        return addedTimestamp;
    }
}
