package io.bitsquare.btc.actor.event;

import com.google.bitcoin.core.Coin;
import com.google.bitcoin.core.Sha256Hash;
import io.bitsquare.btc.actor.command.SendCoins;

/**
 * <p>Message to send or confirm sending Bitcoin transaction.</p>
 */
public class CoinsSent {

    private final SendCoins sendCoins;
    private final Sha256Hash txId;

    public CoinsSent(SendCoins sendCoins, Sha256Hash txId) {
        this.sendCoins = sendCoins;
        this.txId = txId;
    }

    public String getAddress() { return sendCoins.getAddress();}

    public Coin getValue() {
        return sendCoins.getValue();
    }

    public SendCoins.Purpose getPurpose() {
       return sendCoins.getPurpose();
    }

    public Sha256Hash getOfferId() {
        return sendCoins.getOfferId();
    }

    public Sha256Hash getTxId() {
        return txId;
    }
}
