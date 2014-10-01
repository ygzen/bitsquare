package io.bitsquare.btc.actor.event;

import java.util.List;

/**
 * <p>Message to return mnemonic code for Bitcoin HD wallet seed.</p>
 */
public class WalletInitialized {

    private final String networkId;
    private final List<String> mnemonicCode;

    public WalletInitialized(String networkId, List<String> mnemonicCode) {

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
