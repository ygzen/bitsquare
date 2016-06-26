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

package io.bitsquare.gui.main.offer.createoffer;

import io.bitsquare.app.DevFlags;
import io.bitsquare.btc.pricefeed.PriceFeed;
import io.bitsquare.common.Timer;
import io.bitsquare.common.UserThread;
import io.bitsquare.gui.Navigation;
import io.bitsquare.gui.common.model.ActivatableWithDataModel;
import io.bitsquare.gui.common.model.ViewModel;
import io.bitsquare.gui.main.MainView;
import io.bitsquare.gui.main.funds.FundsView;
import io.bitsquare.gui.main.funds.deposit.DepositView;
import io.bitsquare.gui.main.overlays.popups.Popup;
import io.bitsquare.gui.main.settings.SettingsView;
import io.bitsquare.gui.main.settings.preferences.PreferencesView;
import io.bitsquare.gui.util.BSFormatter;
import io.bitsquare.gui.util.validation.*;
import io.bitsquare.locale.BSResources;
import io.bitsquare.locale.TradeCurrency;
import io.bitsquare.p2p.P2PService;
import io.bitsquare.payment.PaymentAccount;
import io.bitsquare.trade.Price;
import io.bitsquare.trade.PriceFactory;
import io.bitsquare.trade.exceptions.MarketPriceNoAvailableException;
import io.bitsquare.trade.offer.Offer;
import io.bitsquare.user.Preferences;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Monetary;

import javax.inject.Inject;

import static javafx.beans.binding.Bindings.createStringBinding;

class CreateOfferViewModel extends ActivatableWithDataModel<CreateOfferDataModel> implements ViewModel {
    private final BtcValidator btcValidator;
    private final P2PService p2PService;
    private PriceFeed priceFeed;
    private Preferences preferences;
    private Navigation navigation;
    final BSFormatter formatter;
    private final FiatValidator fiatValidator;
    private final AltcoinValidator altcoinValidator;
    private final PriceValidator priceValidator;

    private String amountDescription;
    private String directionLabel;
    private String addressAsString;
    private final String paymentLabel;
    private boolean createOfferRequested;

    final StringProperty amount = new SimpleStringProperty();
    final StringProperty minAmount = new SimpleStringProperty();

    // Price for fiat is: fiat/btc, for altcoin it is inverted (btc/altcoin)
    final StringProperty price = new SimpleStringProperty();

    // Positive % value means always a better price from the offerers perspective: 
    // Buyer (with fiat): wants a lower price as market -> spend less fiat
    // Buyer (with altcoin): wants a higher price as market -> spend less altcoin
    final StringProperty percentagePrice = new SimpleStringProperty();

    final StringProperty volume = new SimpleStringProperty();
    final StringProperty volumeDescriptionLabel = new SimpleStringProperty();
    final StringProperty volumePromptLabel = new SimpleStringProperty();
    final StringProperty tradeAmount = new SimpleStringProperty();
    final StringProperty totalToPay = new SimpleStringProperty();
    final StringProperty errorMessage = new SimpleStringProperty();
    final StringProperty btcCode = new SimpleStringProperty();
    final StringProperty tradeCurrencyCode = new SimpleStringProperty();
    final StringProperty spinnerInfoText = new SimpleStringProperty("");

    final BooleanProperty isPlaceOfferButtonDisabled = new SimpleBooleanProperty(true);
    final BooleanProperty cancelButtonDisabled = new SimpleBooleanProperty();
    final BooleanProperty isNextButtonDisabled = new SimpleBooleanProperty(true);
    final BooleanProperty showWarningAdjustedVolume = new SimpleBooleanProperty();
    final BooleanProperty showWarningInvalidDecimalPlacesPrice = new SimpleBooleanProperty();
    final BooleanProperty showWarningInvalidDecimalPlacesVolume = new SimpleBooleanProperty();
    final BooleanProperty showWarningInvalidDecimalPlacesAmount = new SimpleBooleanProperty();
    final BooleanProperty placeOfferCompleted = new SimpleBooleanProperty();
    final BooleanProperty showPayFundsScreenDisplayed = new SimpleBooleanProperty();
    final BooleanProperty showTransactionPublishedScreen = new SimpleBooleanProperty();
    final BooleanProperty isSpinnerVisible = new SimpleBooleanProperty();

    final ObjectProperty<InputValidator.ValidationResult> amountValidationResult = new SimpleObjectProperty<>();
    final ObjectProperty<InputValidator.ValidationResult> minAmountValidationResult = new
            SimpleObjectProperty<>();
    final ObjectProperty<InputValidator.ValidationResult> priceValidationResult = new SimpleObjectProperty<>();
    final ObjectProperty<InputValidator.ValidationResult> volumeValidationResult = new SimpleObjectProperty<>();

    // Those are needed for the addressTextField
    final ObjectProperty<Address> address = new SimpleObjectProperty<>();

    private ChangeListener<String> amountListener;
    private ChangeListener<String> minAmountListener;
    private ChangeListener<String> priceListener, marketPriceMarginListener;
    private ChangeListener<String> volumeListener;
    private ChangeListener<Coin> amountDataListener;
    private ChangeListener<Coin> minAmountDataListener;
    private ChangeListener<Price> priceDataListener;
    private ChangeListener<Monetary> volumeDataListener;
    private ChangeListener<Boolean> isWalletFundedListener;
    //private ChangeListener<Coin> feeFromFundingTxListener;
    private ChangeListener<String> errorMessageListener;
    private Offer offer;
    private Timer timeoutTimer;
    private PriceFeed.Type priceFeedType;
    private boolean inputIsMarketBasedPrice;
    private ChangeListener<Boolean> useMarketBasedPriceListener;


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor, lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    public CreateOfferViewModel(CreateOfferDataModel dataModel, FiatValidator fiatValidator,
                                AltcoinValidator altcoinValidator, PriceValidator priceValidator, BtcValidator btcValidator,
                                P2PService p2PService, PriceFeed priceFeed, Preferences preferences, Navigation navigation,
                                BSFormatter formatter) {
        super(dataModel);

        this.fiatValidator = fiatValidator;
        this.altcoinValidator = altcoinValidator;
        this.priceValidator = priceValidator;
        this.btcValidator = btcValidator;
        this.p2PService = p2PService;
        this.priceFeed = priceFeed;
        this.preferences = preferences;
        this.navigation = navigation;
        this.formatter = formatter;

        paymentLabel = BSResources.get("createOffer.fundsBox.paymentLabel", dataModel.shortOfferId);

        if (dataModel.getAddressEntry() != null) {
            addressAsString = dataModel.getAddressEntry().getAddressString();
            address.set(dataModel.getAddressEntry().getAddress());
        }
        createListeners();
    }

    @Override
    protected void activate() {
        if (DevFlags.DEV_MODE) {
            amount.set("0.0001");
            minAmount.set(amount.get());
            price.set("0.02");
            volume.set("0.04");

            setAmountToModel();
            setMinAmountToModel();
            setPriceToModel();
            calculateVolume();

            dataModel.calculateTotalToPay();
            updateButtonDisableState();
            updateSpinnerInfo();
        }

        addBindings();
        addListeners();

        updateButtonDisableState();

        if (dataModel.getDirection() == Offer.Direction.BUY) {
            directionLabel = BSResources.get("shared.buyBitcoin");
            amountDescription = BSResources.get("createOffer.amountPriceBox.amountDescription", BSResources.get("shared.buy"));
        } else {
            directionLabel = BSResources.get("shared.sellBitcoin");
            amountDescription = BSResources.get("createOffer.amountPriceBox.amountDescription", BSResources.get("shared.sell"));
        }
    }

    @Override
    protected void deactivate() {
        removeBindings();
        removeListeners();
        stopTimeoutTimer();
    }

    private void addBindings() {
        if (dataModel.getDirection() == Offer.Direction.BUY) {
            volumeDescriptionLabel.bind(createStringBinding(
                    () -> BSResources.get("createOffer.amountPriceBox.buy.volumeDescription", dataModel.tradeCurrencyCode.get()),
                    dataModel.tradeCurrencyCode));
        } else {
            volumeDescriptionLabel.bind(createStringBinding(
                    () -> BSResources.get("createOffer.amountPriceBox.sell.volumeDescription", dataModel.tradeCurrencyCode.get()),
                    dataModel.tradeCurrencyCode));
        }
        volumePromptLabel.bind(createStringBinding(
                () -> BSResources.get("createOffer.volume.prompt", dataModel.tradeCurrencyCode.get()),
                dataModel.tradeCurrencyCode));

        totalToPay.bind(createStringBinding(() -> formatter.formatBitcoinWithCode(dataModel.totalToPayAsCoin.get()),
                dataModel.totalToPayAsCoin));


        tradeAmount.bind(createStringBinding(() -> formatter.formatBitcoinWithCode(dataModel.amountAsCoin.get()),
                dataModel.amountAsCoin));


        btcCode.bind(dataModel.btcCode);
        tradeCurrencyCode.bind(dataModel.tradeCurrencyCode);
    }

    private void removeBindings() {
        totalToPay.unbind();
        tradeAmount.unbind();
        btcCode.unbind();
        tradeCurrencyCode.unbind();
        volumeDescriptionLabel.unbind();
        volumePromptLabel.unbind();
    }

    //TODO
    private void createListeners() {
        amountListener = (ov, oldValue, newValue) -> {
            if (newValue != null) {
                if (isBtcInputValid(newValue).isValid) {
                    setAmountToModel();
                    calculateVolume();
                    dataModel.calculateTotalToPay();
                }
                updateButtonDisableState();
            }
        };
        minAmountListener = (ov, oldValue, newValue) -> {
            if (newValue != null) {
                setMinAmountToModel();
                updateButtonDisableState();
            }
        };
        priceListener = (ov, oldValue, newValue) -> {
            if (newValue != null) {
                if (isPriceInputValid(newValue).isValid) {
                    setPriceToModel();
                    calculateVolume();
                    dataModel.calculateTotalToPay();
                    if (!inputIsMarketBasedPrice) {
                        try {
                            double percentagePriceAsDouble = Price.getPercentagePriceFromPrice(priceFeed,
                                    dataModel.tradeCurrencyCode.get(),
                                    dataModel.getDirection(),
                                    formatter.parseNumberStringToDouble(price.get()));
                            percentagePrice.set(formatter.formatToPercent(percentagePriceAsDouble));
                            dataModel.setPercentagePrice(percentagePriceAsDouble);
                        } catch (MarketPriceNoAvailableException e) {
                            UserThread.execute(() -> percentagePrice.set("-"));
                            log.warn("We don't have a market price. We use the static price instead.");
                        } catch (NumberFormatException t) {
                            resetPrice();
                            new Popup().warning("Your input is not a valid number.")
                                    .show();
                        }
                    }
                }
                updateButtonDisableState();
            }
        };
        marketPriceMarginListener = (ov, oldValue, newValue) -> {
            if (newValue != null && inputIsMarketBasedPrice)
                updateMarketBasedPrice(newValue);
        };
        useMarketBasedPriceListener = (observable, oldValue, newValue) -> {
            if (newValue)
                priceValidationResult.set(new InputValidator.ValidationResult(true));
        };

        volumeListener = (ov, oldValue, newValue) -> {
            if (newValue != null && isVolumeInputValid(newValue).isValid) {
                setVolumeToModel();
                setPriceToModel();
                dataModel.calculateAmount();
                dataModel.calculateTotalToPay();
            }
            updateButtonDisableState();
        };
        amountDataListener = (ov, oldValue, newValue) -> {
            if (newValue != null)
                amount.set(formatter.formatCoin(newValue));
        };
        minAmountDataListener = (ov, oldValue, newValue) -> {
            if (newValue != null)
                minAmount.set(formatter.formatCoin(newValue));
        };
        priceDataListener = (ov, oldValue, newValue) -> {
            if (newValue != null)
                price.set(formatter.formatPrice(newValue));
        };
        volumeDataListener = (ov, oldValue, newValue) -> {
            if (newValue != null)
                volume.set(formatter.formatVolume(newValue));
        };

        isWalletFundedListener = (ov, oldValue, newValue) -> {
            updateButtonDisableState();
        };
       /* feeFromFundingTxListener = (ov, oldValue, newValue) -> {
            updateButtonDisableState();
        };*/
    }

    private void updateMarketBasedPrice(String percentagePrice) {
        try {
            if (!percentagePrice.isEmpty() && !percentagePrice.equals("-")) {
                double percentagePriceAsDouble = formatter.parsePercentStringToDouble(percentagePrice);
               /* if (Math.abs(percentagePriceAsDouble) > preferences.getMaxPriceDistanceInPercent()) {
                    dataModel.setPercentagePrice(0);
                    resetPrice();
                    new Popup().warning("The percentage you have entered is outside the max. allowed deviation from the market price.\n" +
                            "The max. allowed deviation is " +
                            formatter.formatPercentagePrice(preferences.getMaxPriceDistanceInPercent()) +
                            " and can be adjusted in the preferences.")
                            .show();
                    resetPrice();
                } else {*/
                try {
                    double priceAsDouble = Price.getPriceFromPercentagePrice(priceFeed,
                            dataModel.tradeCurrencyCode.get(),
                            dataModel.getDirection(),
                            percentagePriceAsDouble);
                    price.set(formatter.formatDoubleToString(priceAsDouble, 8));

                    dataModel.setPercentagePrice(percentagePriceAsDouble);

                    setPriceToModel();
                    calculateVolume();
                    dataModel.calculateTotalToPay();
                    updateButtonDisableState();
                } catch (MarketPriceNoAvailableException e) {
                    new Popup().warning("There is no price feed available for that currency. You cannot use percent based price.")
                            .show();
                }
                //}
            } else {
                resetPrice();
                dataModel.setPercentagePrice(0);
            }
        } catch (Throwable t) {
            dataModel.setPercentagePrice(0);
            resetPrice();
            new Popup().warning("Your input is not a valid number. Please enter a percentage number like \"5.4\" for 5.4%")
                    .show();
        }
    }

    private void resetPrice() {
        UserThread.execute(() -> {
            percentagePrice.set("");
            price.set("");
            dataModel.setPrice(null);
            dataModel.setPercentagePrice(0);
            volume.set("");
            dataModel.volume.set(null);
        });
    }

    private void addListeners() {
        // Bidirectional bindings are used for all input fields: amount, price, volume and minAmount
        // We do volume/amount calculation during input, so user has immediate feedback
        amount.addListener(amountListener);
        minAmount.addListener(minAmountListener);
        price.addListener(priceListener);
        percentagePrice.addListener(marketPriceMarginListener);
        dataModel.usePercentagePrice.addListener(useMarketBasedPriceListener);
        volume.addListener(volumeListener);

        // Binding with Bindings.createObjectBinding does not work because of bi-directional binding
        dataModel.amountAsCoin.addListener(amountDataListener);
        dataModel.minAmountAsCoin.addListener(minAmountDataListener);
        dataModel.price.addListener(priceDataListener);
        dataModel.volume.addListener(volumeDataListener);

        // dataModel.feeFromFundingTxProperty.addListener(feeFromFundingTxListener);
        dataModel.isWalletFunded.addListener(isWalletFundedListener);
    }

    private void removeListeners() {
        amount.removeListener(amountListener);
        minAmount.removeListener(minAmountListener);
        price.removeListener(priceListener);
        percentagePrice.removeListener(marketPriceMarginListener);
        dataModel.usePercentagePrice.removeListener(useMarketBasedPriceListener);
        volume.removeListener(volumeListener);

        // Binding with Bindings.createObjectBinding does not work because of bi-directional binding
        dataModel.amountAsCoin.removeListener(amountDataListener);
        dataModel.minAmountAsCoin.removeListener(minAmountDataListener);
        dataModel.price.removeListener(priceDataListener);
        dataModel.volume.removeListener(volumeDataListener);

        //dataModel.feeFromFundingTxProperty.removeListener(feeFromFundingTxListener);
        dataModel.isWalletFunded.removeListener(isWalletFundedListener);

        if (offer != null && errorMessageListener != null)
            offer.errorMessageProperty().removeListener(errorMessageListener);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // API
    ///////////////////////////////////////////////////////////////////////////////////////////

    boolean initWithData(Offer.Direction direction, TradeCurrency tradeCurrency) {
        boolean result = dataModel.initWithData(direction, tradeCurrency);
        if (dataModel.paymentAccount != null)
            btcValidator.setMaxTradeLimitInBitcoin(dataModel.paymentAccount.getPaymentMethod().getMaxTradeLimit());

        priceFeedType = direction == Offer.Direction.BUY ? PriceFeed.Type.ASK : PriceFeed.Type.BID;

        return result;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // UI actions
    ///////////////////////////////////////////////////////////////////////////////////////////

    void onPlaceOffer(Offer offer, Runnable resultHandler) {
        errorMessage.set(null);
        createOfferRequested = true;

        if (timeoutTimer == null) {
            timeoutTimer = UserThread.runAfter(() -> {
                stopTimeoutTimer();
                createOfferRequested = false;
                errorMessage.set("A timeout occurred at publishing the offer.");

                updateButtonDisableState();
                updateSpinnerInfo();

                resultHandler.run();
            }, 30);
        }
        errorMessageListener = (observable, oldValue, newValue) -> {
            if (newValue != null) {
                stopTimeoutTimer();
                createOfferRequested = false;
                if (offer.getState() == Offer.State.OFFER_FEE_PAID)
                    errorMessage.set(newValue +
                            "\n\nThe offer fee is already paid. In the worst case you have lost that fee. " +
                            "We are sorry about that but keep in mind it is a very small amount.\n" +
                            "Please try to restart you application and check your network connection to see if you can resolve the issue.");
                else
                    errorMessage.set(newValue);

                updateButtonDisableState();
                updateSpinnerInfo();

                resultHandler.run();
            }
        };

        offer.errorMessageProperty().addListener(errorMessageListener);

        dataModel.onPlaceOffer(offer, transaction -> {
            stopTimeoutTimer();
            resultHandler.run();
            placeOfferCompleted.set(true);
            errorMessage.set(null);
        });

        updateButtonDisableState();
        updateSpinnerInfo();
    }

    public void onPaymentAccountSelected(PaymentAccount paymentAccount) {
        btcValidator.setMaxTradeLimitInBitcoin(paymentAccount.getPaymentMethod().getMaxTradeLimit());
        dataModel.onPaymentAccountSelected(paymentAccount);
        if (amount.get() != null)
            amountValidationResult.set(isBtcInputValid(amount.get()));
    }

    public void onCurrencySelected(TradeCurrency tradeCurrency) {
        volume.set("");
        price.set("");
        percentagePrice.set("");
        dataModel.onCurrencySelected(tradeCurrency);
    }

    void onShowPayFundsScreen() {
        showPayFundsScreenDisplayed.set(true);
        updateSpinnerInfo();
    }

    boolean fundFromSavingsWallet() {
        dataModel.fundFromSavingsWallet();
        if (dataModel.isWalletFunded.get()) {
            updateButtonDisableState();
            return true;
        } else {
            new Popup().warning("You don't have enough funds in your Bitsquare wallet.\n" +
                    "You need " + formatter.formatBitcoinWithCode(dataModel.totalToPayAsCoin.get()) + " but you have only " +
                    formatter.formatBitcoinWithCode(dataModel.totalAvailableBalance) + " in your Bitsquare wallet.\n\n" +
                    "Please fund that trade from an external Bitcoin wallet or fund your Bitsquare " +
                    "wallet at \"Funds/Depost funds\".")
                    .actionButtonText("Go to \"Funds/Depost funds\"")
                    .onAction(() -> navigation.navigateTo(MainView.class, FundsView.class, DepositView.class))
                    .show();
            return false;
        }

    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Handle focus
    ///////////////////////////////////////////////////////////////////////////////////////////

    // On focus out we do validation and apply the data to the model
    void onFocusOutAmountTextField(boolean oldValue, boolean newValue, String userInput) {
        if (oldValue && !newValue) {
            InputValidator.ValidationResult result = isBtcInputValid(amount.get());
            amountValidationResult.set(result);
            if (result.isValid) {
                showWarningInvalidDecimalPlacesAmount.set(!formatter.hasBitcoinValidDecimals(userInput));
                setAmountToModel();
                amount.set(formatter.formatCoin(dataModel.amountAsCoin.get()));

                calculateVolume();

                if (!dataModel.isMinAmountLessOrEqualAmount())
                    minAmount.set(amount.get());
                else
                    amountValidationResult.set(result);

                if (minAmount.get() != null)
                    minAmountValidationResult.set(isBtcInputValid(minAmount.get()));
            }
        }
    }

    void onFocusOutMinAmountTextField(boolean oldValue, boolean newValue, String userInput) {
        if (oldValue && !newValue) {
            InputValidator.ValidationResult result = isBtcInputValid(minAmount.get());
            minAmountValidationResult.set(result);
            if (result.isValid) {
                showWarningInvalidDecimalPlacesAmount.set(!formatter.hasBitcoinValidDecimals(userInput));
                setMinAmountToModel();
                minAmount.set(formatter.formatCoin(dataModel.minAmountAsCoin.get()));

                if (!dataModel.isMinAmountLessOrEqualAmount()) {
                    amount.set(minAmount.get());
                   /* minAmountValidationResult.set(new InputValidator.ValidationResult(false,
                            BSResources.get("createOffer.validation.minAmountLargerThanAmount")));*/
                } else {
                    minAmountValidationResult.set(result);
                    if (amount.get() != null)
                        amountValidationResult.set(isBtcInputValid(amount.get()));
                }
            }
        }
    }

    void onFocusOutPriceTextField(boolean oldValue, boolean newValue, String userInput) {
        if (oldValue && !newValue) {
            InputValidator.ValidationResult result = isPriceInputValid(price.get());
            boolean isValid = result.isValid;
            priceValidationResult.set(result);
            if (isValid) {
                showWarningInvalidDecimalPlacesPrice.set(!formatter.hasPriceValidDecimals(userInput, dataModel.tradeCurrencyCode.get()));
                setPriceToModel();
                price.set(formatter.formatPrice(dataModel.price.get()));

                calculateVolume();
            }
            isPriceInRange();
        }
    }

    void onFocusOutPriceAsPercentageTextField(boolean oldValue, boolean newValue, String userInput) {
        inputIsMarketBasedPrice = !oldValue && newValue;
        if (oldValue && !newValue) {
            percentagePrice.set(formatter.formatToPercent(dataModel.getPercentagePrice()));
            isPriceInRange();
        }
    }

    void onFocusOutVolumeTextField(boolean oldValue, boolean newValue, String userInput) {
        if (oldValue && !newValue) {
            InputValidator.ValidationResult result = isVolumeInputValid(volume.get());
            volumeValidationResult.set(result);
            if (result.isValid) {
                final String currencyCode = dataModel.tradeCurrencyCode.get();
                showWarningInvalidDecimalPlacesVolume.set(!formatter.hasVolumeValidDecimals(userInput, currencyCode));
                setVolumeToModel();
                volume.set(formatter.formatVolume(dataModel.volume.get()));

                calculateAmount();

                // must be placed after calculateAmount (btc value has been adjusted in case the calculation leads to
                // invalid decimal places for the amount value
                final String formattedWithDecimalsCheck = formatter.formatVolume(formatter.parseToVolumeWithDecimals(userInput, currencyCode));
                showWarningAdjustedVolume.set(!formattedWithDecimalsCheck.equals(volume.get()));
            }
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////////////////////

    public boolean isPriceInRange() {
        if (Math.abs(dataModel.percentagePrice) > preferences.getMaxPriceDistanceInPercent()) {
            new Popup().warning("The price you have entered is outside the max. allowed deviation from the market price.\n" +
                    "The max. allowed deviation is " +
                    formatter.formatPercentagePrice(preferences.getMaxPriceDistanceInPercent()) +
                    " and can be adjusted in the preferences.")
                    .actionButtonText("Change price")
                    .closeButtonText("Go to \"Preferences\"")
                    .onClose(() -> navigation.navigateTo(MainView.class, SettingsView.class, PreferencesView.class))
                    .show();
            resetPrice();
            return false;
        } else {
            return true;
        }
    }


    BSFormatter getFormatter() {
        return formatter;
    }

    boolean isSellOffer() {
        return dataModel.getDirection() == Offer.Direction.SELL;
    }

    public ObservableList<PaymentAccount> getPaymentAccounts() {
        return dataModel.paymentAccounts;
    }

    public TradeCurrency getTradeCurrency() {
        return dataModel.getTradeCurrency();
    }

    public String getOfferFee() {
        return formatter.formatBitcoinWithCode(dataModel.getOfferFeeAsCoin());
    }

    public String getNetworkFee() {
        return formatter.formatBitcoinWithCode(dataModel.getNetworkFeeAsCoin());
    }

    public String getSecurityDeposit() {
        return formatter.formatBitcoinWithCode(dataModel.getSecurityDepositAsCoin());
    }

    public PaymentAccount getPaymentAccount() {
        return dataModel.getPaymentAccount();
    }

    public String getAmountDescription() {
        return amountDescription;
    }

    public String getDirectionLabel() {
        return directionLabel;
    }

    public String getAddressAsString() {
        return addressAsString;
    }

    public String getPaymentLabel() {
        return paymentLabel;
    }

    public String formatCoin(Coin coin) {
        return formatter.formatCoin(coin);
    }

    public Offer createAndGetOffer() {
        offer = dataModel.createAndGetOffer();
        return offer;
    }

    boolean hasAcceptedArbitrators() {
        return dataModel.hasAcceptedArbitrators();
    }

    boolean isBootstrapped() {
        return p2PService.isBootstrapped();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void calculateVolume() {
        setAmountToModel();
        setPriceToModel();
        dataModel.calculateVolume();
    }

    private void calculateAmount() {
        setVolumeToModel();
        setPriceToModel();
        dataModel.calculateAmount();

        // Amount calculation could lead to amount/minAmount invalidation
        if (!dataModel.isMinAmountLessOrEqualAmount()) {
            amountValidationResult.set(new InputValidator.ValidationResult(false,
                    BSResources.get("createOffer.validation.amountSmallerThanMinAmount")));
        } else {
            if (amount.get() != null)
                amountValidationResult.set(isBtcInputValid(amount.get()));
            if (minAmount.get() != null)
                minAmountValidationResult.set(isBtcInputValid(minAmount.get()));
        }
    }

    private void setAmountToModel() {
        final Coin value = formatter.parseToBitcoinWith4Decimals(amount.get());
        dataModel.amountAsCoin.set(value);
        if (dataModel.minAmountAsCoin.get() == null || dataModel.minAmountAsCoin.get().equals(Coin.ZERO)) {
            minAmount.set(amount.get());
            setMinAmountToModel();
        }
    }

    private void setMinAmountToModel() {
        dataModel.minAmountAsCoin.set(formatter.parseToBitcoinWith4Decimals(minAmount.get()));
    }

    private void setPriceToModel() {
        final String currencyCode = dataModel.tradeCurrencyCode.get();
        if (price.get() != null && !price.get().isEmpty() && currencyCode != null && !currencyCode.isEmpty())
            dataModel.setPrice(PriceFactory.getPriceFromString(currencyCode, formatter.cleanPriceString(price.get(), currencyCode)));
    }

    private void setVolumeToModel() {
        final Monetary value = formatter.parseToVolumeWithDecimals(volume.get(), dataModel.tradeCurrencyCode.get());
        dataModel.volume.set(value);
    }

    private InputValidator.ValidationResult isBtcInputValid(String input) {
        return btcValidator.validate(input);
    }

    private InputValidator.ValidationResult isVolumeInputValid(String input) {
        if (dataModel.isAltcoin())
            return altcoinValidator.validate(input);
        else
            return fiatValidator.validate(input);
    }

    private InputValidator.ValidationResult isPriceInputValid(String input) {
        return priceValidator.validate(input);
    }

    private void updateSpinnerInfo() {
        if (!showPayFundsScreenDisplayed.get() ||
                errorMessage.get() != null ||
                showTransactionPublishedScreen.get()) {
            spinnerInfoText.set("");
        } else if (dataModel.isWalletFunded.get()) {
            spinnerInfoText.set("");
           /* if (dataModel.isFeeFromFundingTxSufficient.get()) {
                spinnerInfoText.set("");
            } else {
                spinnerInfoText.set("Check if funding tx miner fee is sufficient...");
            }*/
        } else {
            spinnerInfoText.set("Waiting for funds...");
        }

        isSpinnerVisible.set(!spinnerInfoText.get().isEmpty());
    }

    private void updateButtonDisableState() {
        log.debug("updateButtonDisableState");
        boolean inputDataValid = isBtcInputValid(amount.get()).isValid &&
                isBtcInputValid(minAmount.get()).isValid &&
                isPriceInputValid(price.get()).isValid &&
                dataModel.price.get() != null &&
                dataModel.price.get().getPriceAsDouble() != 0 &&
                isVolumeInputValid(volume.get()).isValid &&
                dataModel.isMinAmountLessOrEqualAmount();

        isNextButtonDisabled.set(!inputDataValid);
        // boolean notSufficientFees = dataModel.isWalletFunded.get() && dataModel.isMainNet.get() && !dataModel.isFeeFromFundingTxSufficient.get();
        //isPlaceOfferButtonDisabled.set(createOfferRequested || !inputDataValid || notSufficientFees);
        isPlaceOfferButtonDisabled.set(createOfferRequested || !inputDataValid || !dataModel.isWalletFunded.get());
    }


    private void stopTimeoutTimer() {
        if (timeoutTimer != null) {
            timeoutTimer.stop();
            timeoutTimer = null;
        }
    }
}
