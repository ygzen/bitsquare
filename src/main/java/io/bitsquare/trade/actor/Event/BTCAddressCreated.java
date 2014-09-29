package io.bitsquare.trade.actor.Event;

import com.google.bitcoin.core.Address;

/**
 * <p>Message to return a Bitcoin address to fund a local trade wallet.</p>
 */
public class BTCAddressCreated {

    private final String offerId;
    private final Address address;

    public BTCAddressCreated(String offerId, Address address) {
        this.offerId = offerId;
        this.address = address;
    }

    public String getOfferId() {
        return offerId;
    }

    public Address getAddress() {
        return address;
    }
}
