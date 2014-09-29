package io.bitsquare.trade.actor.command;


import com.google.bitcoin.core.NetworkParameters;

/**
 * <p>Message to get initialize Bitcoin HD wallet, if not already initialized return seed mnemonic code.</p>
 */
public class InitializeBTCWallet {

    private final String networkId;
    private final String walletPrefix;

    public InitializeBTCWallet(String networkId, String walletPrefix) {

        this.networkId = networkId;
        this.walletPrefix = walletPrefix;
    }

    public String getNetworkId() {
        return networkId;
    }

    public String getWalletPrefix() {
        return walletPrefix;
    }
}
