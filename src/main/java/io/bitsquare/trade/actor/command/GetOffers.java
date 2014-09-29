package io.bitsquare.trade.actor.command;


import io.bitsquare.bank.BankAccount;
import io.bitsquare.locale.Country;

/**
 * <p>Message to create a new offer to buy or sell Bitcoins.</p>
 */
public class GetOffers {

    private final BankAccount account;

    public GetOffers(BankAccount account) {
        this.account = account;
    }

    public BankAccount getAccount() {
        return account;
    }
}
