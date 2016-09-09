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
import io.bitsquare.common.util.Tuple4;
import io.bitsquare.gui.common.view.ActivatableViewAndModel;
import io.bitsquare.gui.common.view.FxmlView;
import io.bitsquare.gui.components.InputTextField;
import io.bitsquare.gui.components.TitledGroupBg;
import io.bitsquare.gui.util.Layout;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static io.bitsquare.gui.util.FormBuilder.*;

@FxmlView
public class MultiSigWalletView extends ActivatableViewAndModel<VBox, MultiSigWalletViewModel> {
    private static final Logger log = LoggerFactory.getLogger(MultiSigWalletView.class);

    @FXML
    GridPane gridPane;
    private int gridRow = 0;
    private TableView<MultiSigWallet> tableView;
    private Button createMultiSigWalletButton;
    private ComboBox<Integer> mumOfRequiredSignersComboBox, mumOfKeyHoldersComboBox;
    private ChangeListener<String> titleTextListener;
    private InputTextField titleInputTextField;

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    private MultiSigWalletView(MultiSigWalletViewModel model) {
        super(model);
    }

    @Override
    protected void initialize() {
        VBox.setVgrow(gridPane, Priority.ALWAYS);
        createAndAddTable();
        createAndAddButton();
    }

    @Override
    protected void activate() {
        tableView.setItems(model.dataModel.multiSigWallets);

        createMultiSigWalletButton.setOnAction(e -> displayCreateMultiSigWalletScreen());
    }

    @Override
    protected void deactivate() {
        createMultiSigWalletButton.setOnAction(null);
        if (titleInputTextField != null && titleTextListener != null)
            titleInputTextField.textProperty().removeListener(titleTextListener);
        if (mumOfRequiredSignersComboBox != null)
            mumOfRequiredSignersComboBox.setOnAction(null);
        if (mumOfKeyHoldersComboBox != null)
            mumOfKeyHoldersComboBox.setOnAction(null);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////////////////////
    // UI actions
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void cleanGridPane() {
        gridPane.getChildren().removeAll(gridPane.getChildren());
        gridRow = 0;
    }

    private void displayCreateMultiSigWalletScreen() {
        cleanGridPane();
        addTitledGroupBg(gridPane, ++gridRow, 3, "Create new Multisig Wallet");
        titleInputTextField = addLabelInputTextField(gridPane, gridRow, "Title", Layout.FIRST_ROW_DISTANCE).second;
        titleTextListener = (observable, oldValue, newValue) -> model.setTitle(newValue);
        titleInputTextField.textProperty().addListener(titleTextListener);
        mumOfRequiredSignersComboBox = addLabelComboBox(gridPane, ++gridRow, "Required number of signatures:").second;
        mumOfKeyHoldersComboBox = addLabelComboBox(gridPane, ++gridRow, "Total number of key holders:").second;
        Button nextButton = addButtonAfterGroup(gridPane, ++gridRow, "Next");
        nextButton.disableProperty().bind(model.setupKeyHoldersScreenButtonDisable);
        nextButton.setOnAction(e -> displayInviteKeyHoldersScreen());


        mumOfKeyHoldersComboBox.setPromptText("Select");
        mumOfKeyHoldersComboBox.setItems(model.dataModel.getNumOfKeyHoldersList());
        mumOfKeyHoldersComboBox.setOnAction(e -> {
            SingleSelectionModel<Integer> selectionModel = mumOfKeyHoldersComboBox.getSelectionModel();
            if (selectionModel != null) {
                Integer selectedItem = selectionModel.getSelectedItem();
                if (selectedItem != null) {
                    model.setNumOfKeyHolders(selectedItem);
                    mumOfRequiredSignersComboBox.getSelectionModel().select(model.dataModel.getNumOfRequiredSigners() - 1);
                }
            }
        });

        mumOfRequiredSignersComboBox.setPromptText("Select");
        mumOfRequiredSignersComboBox.setItems(model.dataModel.getNumOfRequiredSignersList());
        mumOfRequiredSignersComboBox.setOnAction(e -> {
            SingleSelectionModel<Integer> selectionModel = mumOfRequiredSignersComboBox.getSelectionModel();
            if (selectionModel != null) {
                Integer selectedItem = selectionModel.getSelectedItem();
                if (selectedItem != null) {
                    model.setNumOfRequiredSigners(selectedItem);
                    mumOfKeyHoldersComboBox.getSelectionModel().select(model.dataModel.getNumOfKeyHolders() - 2);
                }
            }
        });

    }

    private void displayInviteKeyHoldersScreen() {
        cleanGridPane();
        int numOfKeyHolders = model.dataModel.getNumOfKeyHolders();
        TitledGroupBg titledGroupBg = addTitledGroupBg(gridPane, gridRow, numOfKeyHolders, "Invite key holders");
        GridPane.setColumnSpan(titledGroupBg, 4);
        for (int i = 0; i < numOfKeyHolders; i++) {
            double distance = (i == 0) ? Layout.FIRST_ROW_DISTANCE : 0;
            Tuple4<Label, InputTextField, Label, InputTextField> tuple = addLabelInputTextFieldLabelInputTextField(gridPane,
                    gridRow++, "Name of key holder " + (i + 1) + ":", "Network address:", distance);
            final int index = i;
            tuple.second.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    model.setKeyHolderName(newValue, index);
                }
            });
            tuple.forth.textProperty().addListener(new ChangeListener<String>() {
                @Override
                public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                    model.setKeyHolderNetworkAddress(newValue, index);
                }
            });
        }
        Button nextButton = addButtonAfterGroup(gridPane, gridRow, "Send invitations");
        nextButton.disableProperty().bind(model.sendInvitationsButtonDisable);
        nextButton.setOnAction(e -> model.dataModel.sendInvitations());
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Build UI
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void createAndAddTable() {
        tableView = new TableView<>();

        // title
        TableColumn<MultiSigWallet, MultiSigWallet> titleColumn = new TableColumn<MultiSigWallet, MultiSigWallet>("Wallet title") {
            {
                setMinWidth(150);
            }
        };
        titleColumn.setCellValueFactory((MultiSigWallet) -> new ReadOnlyObjectWrapper<>(MultiSigWallet.getValue()));
        titleColumn.setCellFactory(
                new Callback<TableColumn<MultiSigWallet, MultiSigWallet>, TableCell<MultiSigWallet,
                        MultiSigWallet>>() {
                    @Override
                    public TableCell<MultiSigWallet, MultiSigWallet> call(
                            TableColumn<MultiSigWallet, MultiSigWallet> column) {
                        return new TableCell<MultiSigWallet, MultiSigWallet>() {
                            @Override
                            public void updateItem(final MultiSigWallet item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(item.title);
                                else
                                    setText("");
                            }
                        };
                    }
                });
        titleColumn.setComparator((o1, o2) -> o1.title.compareTo(o2.title));
        tableView.getColumns().add(titleColumn);

        // balance
        TableColumn<MultiSigWallet, MultiSigWallet> balanceColumn = new TableColumn<MultiSigWallet, MultiSigWallet>("Balance") {
            {
                setMinWidth(150);
            }
        };
        balanceColumn.setCellValueFactory((MultiSigWallet) -> new ReadOnlyObjectWrapper<>(MultiSigWallet.getValue()));
        balanceColumn.setCellFactory(
                new Callback<TableColumn<MultiSigWallet, MultiSigWallet>, TableCell<MultiSigWallet,
                        MultiSigWallet>>() {
                    @Override
                    public TableCell<MultiSigWallet, MultiSigWallet> call(
                            TableColumn<MultiSigWallet, MultiSigWallet> column) {
                        return new TableCell<MultiSigWallet, MultiSigWallet>() {
                            @Override
                            public void updateItem(final MultiSigWallet item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(model.getBalance(item));
                                else
                                    setText("");
                            }
                        };
                    }
                });
        balanceColumn.setComparator((o1, o2) -> o1.title.compareTo(o2.title));
        tableView.getColumns().add(balanceColumn);

        // type
        TableColumn<MultiSigWallet, MultiSigWallet> typeColumn = new TableColumn<MultiSigWallet, MultiSigWallet>("Type") {
            {
                setMinWidth(120);
            }
        };
        typeColumn.setCellValueFactory((MultiSigWallet) -> new ReadOnlyObjectWrapper<>(MultiSigWallet.getValue()));
        typeColumn.setCellFactory(
                new Callback<TableColumn<MultiSigWallet, MultiSigWallet>, TableCell<MultiSigWallet,
                        MultiSigWallet>>() {
                    @Override
                    public TableCell<MultiSigWallet, MultiSigWallet> call(
                            TableColumn<MultiSigWallet, MultiSigWallet> column) {
                        return new TableCell<MultiSigWallet, MultiSigWallet>() {
                            @Override
                            public void updateItem(final MultiSigWallet item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null)
                                    setText(model.getType(item));
                                else
                                    setText("");
                            }
                        };
                    }
                });
        typeColumn.setComparator((o1, o2) -> model.getType(o1).compareTo(model.getType(o2)));
        tableView.getColumns().add(typeColumn);

        // details
        TableColumn<MultiSigWallet, MultiSigWallet> detailsColumn = new TableColumn<MultiSigWallet, MultiSigWallet>("Details") {
            {
                setMinWidth(80);
                setSortable(false);
            }
        };
        detailsColumn.setCellValueFactory((MultiSigWallet) -> new ReadOnlyObjectWrapper<>(MultiSigWallet.getValue()));
        detailsColumn.setCellFactory(
                new Callback<TableColumn<MultiSigWallet, MultiSigWallet>, TableCell<MultiSigWallet,
                        MultiSigWallet>>() {
                    @Override
                    public TableCell<MultiSigWallet, MultiSigWallet> call(
                            TableColumn<MultiSigWallet, MultiSigWallet> column) {
                        return new TableCell<MultiSigWallet, MultiSigWallet>() {

                            Button button;

                            @Override
                            public void updateItem(final MultiSigWallet item, boolean empty) {
                                super.updateItem(item, empty);
                                if (item != null && !empty) {
                                    if (button == null) {
                                        button = new Button("Details");
                                        button.setOnAction(e -> tableView.getSelectionModel().select(item));
                                        setGraphic(button);
                                    }
                                } else {
                                    setGraphic(null);
                                    if (button != null) {
                                        button.setOnAction(null);
                                        button = null;
                                    }
                                }
                            }
                        };
                    }
                });
        tableView.getColumns().add(detailsColumn);

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        Label placeholder = new Label("There are no Multisig Wallets available");
        placeholder.setWrapText(true);
        tableView.setPlaceholder(placeholder);

       /* GridPane.setRowIndex(tableView, gridRow);
        GridPane.setColumnIndex(tableView, 0);
        GridPane.setColumnSpan(tableView, 2);
        //GridPane.setMargin(tableView, new Insets(10, -10, -10, -10));
        GridPane.setVgrow(tableView, Priority.ALWAYS);*/
        VBox.setVgrow(tableView, Priority.SOMETIMES);
        root.getChildren().add(0, tableView);
    }

    private void createAndAddButton() {
        createMultiSigWalletButton = new Button("Create new Multisig Wallet");
        createMultiSigWalletButton.setDefaultButton(true);
        root.setAlignment(Pos.TOP_RIGHT);
        root.getChildren().add(1, createMultiSigWalletButton);
    }
}

