package io.bitsquare.trade.actor.command;

import com.google.bitcoin.core.Coin;
import com.google.bitcoin.core.Sha256Hash;

/**
 * <p>Message to create and broadcast a Bitcoin transaction.</p>
 */
public class SendCoins {

    public enum Purpose {
        OFFER_FEE,
        FUND_ESCROW,
        START_ARBITRATION
    }

    private final String address;
    private final Coin value;
    private final Purpose purpose;
    private final Sha256Hash offerId;

    public SendCoins(String address, Coin value, Purpose purpose, Sha256Hash offerId) {
        this.address = address;
        this.value = value;
        this.purpose = purpose;
        this.offerId = offerId;
    }

    public String getAddress() {
       return address;
    }

    public Coin getValue() {
        return value;
    }

    public Purpose getPurpose() {
       return purpose;
    }

    public Sha256Hash getOfferId() {
        return offerId;
    }
}
