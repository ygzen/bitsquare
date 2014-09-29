package io.bitsquare.trade.actor.command;

/**
 * <p>Message to create a Bitcoin address to fund a local trade wallet.</p>
 */
public class CreateBTCAddress {

    private final String offerId;

    public CreateBTCAddress(String offerId) {

        this.offerId = offerId;
    }

    public String getOfferId() {
        return offerId;
    }
}
