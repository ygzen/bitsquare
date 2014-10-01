package io.bitsquare.btc.actor.command;

/**
 * <p>Message to create a Bitcoin address to fund a trade wallet.</p>
 */
public class CreateAddress {

    private final String offerId;

    public CreateAddress(String offerId) {

        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }
}
