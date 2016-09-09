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

package io.bitsquare.gui.main.funds.multisigwallet;

import com.google.inject.Inject;
import io.bitsquare.btc.AddressEntry;
import io.bitsquare.btc.WalletService;
import io.bitsquare.btc.multisigwallet.MultiSigWallet;
import io.bitsquare.btc.multisigwallet.MultiSigWalletService;
import io.bitsquare.gui.common.model.ActivatableDataModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.bitcoinj.core.Coin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * Domain for that UI element.
 * Note that the create offer domain has a deeper scope in the application domain (TradeManager).
 * That model is just responsible for the domain specific parts displayed needed in that UI element.
 */
class MultiSigWalletDataModel extends ActivatableDataModel {
    private static final Logger log = LoggerFactory.getLogger(MultiSigWalletDataModel.class);

    private MultiSigWalletService multiSigWalletService;
    private WalletService walletService;
    ObservableList<MultiSigWallet> multiSigWallets = FXCollections.observableArrayList();
    private int numOfRequiredSigners;
    private int numOfKeyHolders;
    private String title;
    private ObservableList<Integer> numOfKeyHoldersList = FXCollections.observableArrayList(Arrays.asList(2, 3, 4, 5, 6));
    private ObservableList<Integer> numOfRequiredSignersList = FXCollections.observableArrayList(Arrays.asList(1, 2, 3, 4));


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    MultiSigWalletDataModel(MultiSigWalletService multiSigWalletService, WalletService walletService) {
        this.multiSigWalletService = multiSigWalletService;
        this.walletService = walletService;
    }

    @Override
    protected void activate() {
        //multiSigWallets = multiSigWalletService.getMultiSigWallets().getObservableList();
    }

    @Override
    protected void deactivate() {
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // UI actions
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void sendInvitations() {

    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Setters
    ///////////////////////////////////////////////////////////////////////////////////////////

    public void setTitle(String title) {
        this.title = title;
    }

    public Coin getBalance(AddressEntry addressEntry) {
        return walletService.getBalanceForAddress(addressEntry.getAddress());
    }

    public void setNumOfRequiredSigners(int numOfRequiredSigners) {
        this.numOfRequiredSigners = numOfRequiredSigners;
        numOfKeyHolders = Math.max(numOfRequiredSigners, numOfKeyHolders);
    }

    public void setNumOfKeyHolders(int numOfKeyHolders) {
        this.numOfKeyHolders = numOfKeyHolders;
        numOfRequiredSigners = Math.min(numOfRequiredSigners, numOfKeyHolders);
        numOfRequiredSignersList.clear();
        for (int i = 1; i <= numOfKeyHolders && i < 5; i++) {
            numOfRequiredSignersList.add(i);
        }
    }

    public void setKeyHolderName(String value, int index) {

    }

    public void setKeyHolderNetworkAddress(String value, int index) {

    }



    ///////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////////////////////

    public int getNumOfKeyHolders() {
        return numOfKeyHolders;
    }

    public ObservableList<Integer> getNumOfKeyHoldersList() {
        return numOfKeyHoldersList;
    }

    public ObservableList<Integer> getNumOfRequiredSignersList() {
        return numOfRequiredSignersList;
    }

    public String getTitle() {
        return title;
    }

    public int getNumOfRequiredSigners() {
        return numOfRequiredSigners;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////




    ///////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////////////////////////////////

}
