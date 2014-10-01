package io.bitsquare.btc.actor.command;


/**
 * <p>Message to initialize Bitcoin HD wallet.</p>
 */
public class InitializeWallet {

    private final String networkId;
    private final String walletPrefix;

    public InitializeWallet(String networkId, String walletPrefix) {

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
