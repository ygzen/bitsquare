package io.bitsquare.trade.actor.event;

import io.bitsquare.trade.Offer;

import java.util.Collections;
import java.util.List;

/**
 * <p>Message to add or remove a new offer to buy or sell Bitcoins.</p>
 */
public class OffersFound {

    private final List<Offer> offers;

    public OffersFound(List<Offer> offers) {
        this.offers = Collections.unmodifiableList(offers);
    }

    public List<Offer> getOffers() {
        return offers;
    }
}
