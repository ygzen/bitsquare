package io.bitsquare.trade.actor.command;

import com.google.bitcoin.core.Coin;
import com.google.bitcoin.core.Sha256Hash;

/**
 * <p>Message to create and broadcast a Bitcoin transaction.</p>
 */
public class PayOfferFee {

    private final String offerId;
    private final Long btcAmount;

    public PayOfferFee(String offerId, Long btcAmount) {

        this.offerId = offerId;
        this.btcAmount = btcAmount;
    }

    public String getOfferId() {
        return offerId;
    }
}
