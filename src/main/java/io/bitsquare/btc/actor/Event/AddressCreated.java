package io.bitsquare.btc.actor.event;

import com.google.bitcoin.core.Address;

/**
 * <p>Message to return a Bitcoin address to fund a local trade wallet.</p>
 */
public class AddressCreated {

    private final String offerId;
    private final Address address;

    public AddressCreated(String offerId, Address address) {
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
