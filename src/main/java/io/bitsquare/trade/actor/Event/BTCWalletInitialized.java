package io.bitsquare.trade.actor.Event;

import com.google.bitcoin.core.NetworkParameters;

import java.util.List;

/**
 * <p>Message to return mnemonic code for Bitcoin HD wallet seed.</p>
 */
public class BTCWalletInitialized {

    private final String networkId;
    private final List<String> mnemonicCode;

    public BTCWalletInitialized(String networkId, List<String> mnemonicCode) {

        this.networkId = networkId;
        this.mnemonicCode = mnemonicCode;
    }

    public String getNetworkId() {
        return networkId;
    }

    public List<String> getMnemonicCode() {
        return mnemonicCode;
    }
}
