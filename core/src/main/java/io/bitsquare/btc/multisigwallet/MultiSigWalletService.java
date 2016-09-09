/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.btc.multisigwallet;

import io.bitsquare.btc.AddressEntry;
import io.bitsquare.btc.WalletService;
import io.bitsquare.p2p.NodeAddress;
import io.bitsquare.p2p.P2PService;
import io.bitsquare.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.File;
import java.util.Optional;

import static com.google.common.base.Preconditions.checkArgument;

public class MultiSigWalletService {
    private static final Logger log = LoggerFactory.getLogger(MultiSigWalletService.class);

    private final P2PService p2PService;
    private WalletService walletService;
    private final Storage<MultiSigWalletList<MultiSigWallet>> multiSigWalletListStorage;


    private final MultiSigWalletList<MultiSigWallet> multiSigWallets;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public MultiSigWalletService(P2PService p2PService, WalletService walletService,
                                 @Named(Storage.DIR_KEY) File storageDir) {
        this.p2PService = p2PService;
        this.walletService = walletService;

        multiSigWalletListStorage = new Storage<>(storageDir);
        multiSigWallets = new MultiSigWalletList<>(multiSigWalletListStorage, "MultiSigWallets");
    }

    public void fill() {
        MultiSigWallet multiSigWallet1 = addMultiSigWallet("test title 1", 2, 3);

        MultiSigKeyHolder holder1 = new MultiSigKeyHolder("holder1", new NodeAddress("ewrwerwerwer.onion"), true, "rtzrtztrztrz", null);
        addMultiSigKeyHolderToWallet(holder1, multiSigWallet1.uid);
        MultiSigKeyHolder holder2 = new MultiSigKeyHolder("holder2", new NodeAddress("dsafadfsafsd.onion"), false, "asfdsdafasdfdas", null);
        addMultiSigKeyHolderToWallet(holder2, multiSigWallet1.uid);

        MultiSigWallet multiSigWallet2 = addMultiSigWallet("test title 2", 4, 6);
    }

    public MultiSigWalletList<MultiSigWallet> getMultiSigWallets() {
        return multiSigWallets;
    }

    public MultiSigWallet addMultiSigWallet(String title, int mumOfRequiredSigners, int mumOfKeyHolders) {
        checkArgument(mumOfRequiredSigners <= mumOfKeyHolders, "mumOfRequiredSigners must be smaller than mumOfKeyHolders.");
        checkArgument(mumOfKeyHolders <= 6, "mumOfKeyHolders cannot be larger than 6.");
        if (mumOfKeyHolders > 4)
            checkArgument(mumOfRequiredSigners <= 4, "Only 4of5 or 4of6 MultiSig are supported.");

        AddressEntry addressEntry = walletService.getOrCreateAddressEntry(AddressEntry.Context.NON_TRADE_MULTI_SIG);
        MultiSigWallet multiSigWallet = new MultiSigWallet(title, mumOfRequiredSigners, mumOfKeyHolders, addressEntry);
        multiSigWallets.add(multiSigWallet);
        return multiSigWallet;
    }

    public void addMultiSigKeyHolderToWallet(MultiSigKeyHolder multiSigKeyHolder, String walletId) {
        getMultiSigWalletById(walletId).ifPresent(e -> e.addMultiSigKeyHolder(multiSigKeyHolder));
    }

    public Optional<MultiSigWallet> getMultiSigWalletById(String id) {
        return multiSigWallets.stream().filter(e -> e.uid.equals(id)).findAny();
    }

}
