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

import io.bitsquare.btc.multisigwallet.MultiSigWallet;
import io.bitsquare.gui.common.model.ActivatableWithDataModel;
import io.bitsquare.gui.common.model.ViewModel;
import io.bitsquare.gui.util.BSFormatter;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

class MultiSigWalletViewModel extends ActivatableWithDataModel<MultiSigWalletDataModel> implements ViewModel {
    private static final Logger log = LoggerFactory.getLogger(MultiSigWalletViewModel.class);

    private final BSFormatter formatter;

    final BooleanProperty setupKeyHoldersScreenButtonDisable = new SimpleBooleanProperty(true);
    final BooleanProperty sendInvitationsButtonDisable = new SimpleBooleanProperty(true);


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public MultiSigWalletViewModel(MultiSigWalletDataModel dataModel, BSFormatter formatter) {
        super(dataModel);
        this.formatter = formatter;
    }

    @Override
    protected void activate() {
    }

    @Override
    protected void deactivate() {
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////////////////
    // UI actions
    ///////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////////////////////

    String getType(MultiSigWallet item) {
        return item.mumOfRequiredSigners + " of " + item.mumOfKeyHolders;
    }

    String getBalance(MultiSigWallet item) {
        return formatter.formatCoin(dataModel.getBalance(item.addressEntry));
    }

    void setNumOfRequiredSigners(int value) {
        dataModel.setNumOfRequiredSigners(value);
        updateSetupKeyHoldersScreenButtonState();
    }

    void setNumOfKeyHolders(int value) {
        dataModel.setNumOfKeyHolders(value);
        updateSetupKeyHoldersScreenButtonState();
    }

    void setTitle(String value) {
        dataModel.setTitle(value);
        updateSetupKeyHoldersScreenButtonState();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void updateSetupKeyHoldersScreenButtonState() {
        boolean valid = dataModel.getNumOfKeyHolders() > 0 &&
                dataModel.getNumOfRequiredSigners() > 0 &&
                dataModel.getTitle() != null &&
                !dataModel.getTitle().isEmpty();
        setupKeyHoldersScreenButtonDisable.set(!valid);
    }

    private void updateSendInvitationsButtonState() {
        boolean valid = dataModel.getNumOfKeyHolders() > 0 &&
                dataModel.getNumOfRequiredSigners() > 0 &&
                dataModel.getTitle() != null &&
                !dataModel.getTitle().isEmpty();
        sendInvitationsButtonDisable.set(!valid);
    }

    public void setKeyHolderName(String value, int index) {
        dataModel.setKeyHolderName(value, index);
        updateSendInvitationsButtonState();
    }

    public void setKeyHolderNetworkAddress(String value, int index) {
        dataModel.setKeyHolderNetworkAddress(value, index);
        updateSendInvitationsButtonState();
    }
}
