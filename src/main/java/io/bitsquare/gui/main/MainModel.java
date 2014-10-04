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

package io.bitsquare.gui.main;

import com.google.inject.Inject;
import io.bitsquare.bank.BankAccount;
import io.bitsquare.btc.BTCService;
import io.bitsquare.btc.WalletFacade;
import io.bitsquare.btc.actor.event.DownloadDone;
import io.bitsquare.btc.actor.event.DownloadProgress;
import io.bitsquare.btc.actor.event.DownloadStarted;
import io.bitsquare.btc.actor.event.WalletInitialized;
import io.bitsquare.gui.UIModel;
import io.bitsquare.gui.util.Profiler;
import io.bitsquare.msg.DHTPeerService;
import io.bitsquare.msg.DHTSeedService;
import io.bitsquare.msg.MessageFacade;
import io.bitsquare.msg.actor.event.PeerInitialized;
import io.bitsquare.persistence.Persistence;
import io.bitsquare.trade.Trade;
import io.bitsquare.trade.TradeManager;
import io.bitsquare.user.User;
import javafx.beans.property.*;
import javafx.collections.MapChangeListener;
import javafx.collections.ObservableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class MainModel extends UIModel {
    private static final Logger log = LoggerFactory.getLogger(MainModel.class);

    private final User user;
    private final WalletFacade walletFacade;
    private final MessageFacade messageFacade;
    private final TradeManager tradeManager;
    private final Persistence persistence;

    private final BTCService btcService;
    private final DHTSeedService dhtSeedService;
    private final DHTPeerService dhtPeerService;

    private boolean messageFacadeInited;
    private boolean walletFacadeInited;

    final BooleanProperty backendInited = new SimpleBooleanProperty();
    final DoubleProperty networkSyncProgress = new SimpleDoubleProperty();
    final BooleanProperty networkSyncComplete = new SimpleBooleanProperty();
    //  final ObjectProperty<Coin> balance = new SimpleObjectProperty<>();
    final IntegerProperty numPendingTrades = new SimpleIntegerProperty(0);

    ///////////////////////////////////////////////////////////////////////////////////////////
    // Constructor
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Inject
    private MainModel(User user, BTCService btcService, DHTSeedService dhtSeedService, DHTPeerService dhtPeerService,
                      WalletFacade walletFacade,
                      MessageFacade messageFacade,
                      TradeManager tradeManager, Persistence persistence) {
        this.user = user;
        this.btcService = btcService;
        this.dhtSeedService = dhtSeedService;
        this.dhtPeerService = dhtPeerService;
        this.walletFacade = walletFacade;
        this.messageFacade = messageFacade;
        this.tradeManager = tradeManager;
        this.persistence = persistence;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Lifecycle
    ///////////////////////////////////////////////////////////////////////////////////////////

    @SuppressWarnings("EmptyMethod")
    @Override
    public void initialize() {
        super.initialize();
    }

    @SuppressWarnings("EmptyMethod")
    @Override
    public void terminate() {
        super.terminate();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Public
    ///////////////////////////////////////////////////////////////////////////////////////////

    void initBackend() {
        Profiler.printMsgWithTime("MainModel.initFacades");

        btcService.setHandler(m -> {
            if (m instanceof WalletInitialized) {
                log.debug("btc wallet initialized. ");
                walletFacadeInited = true;
                if (messageFacadeInited) onFacadesInitialised();
            } else if (m instanceof DownloadStarted) {
                log.debug("btc download started. ");

            } else if (m instanceof DownloadProgress) {
                log.debug("btc download progress: " + ((DownloadProgress) m).getPercent());
                networkSyncProgress.set(((DownloadProgress) m).getPercent());
            } else if (m instanceof DownloadDone) {
                log.debug("btc download Done. ");
                networkSyncComplete.set(true);
            } else {
                log.error("received unknown message from btcService.");
            }
        });

        btcService.initializeWallet();

        dhtPeerService.setHandler(m -> {
            if (m instanceof PeerInitialized) {
                log.debug("dht peer initialized. ");
                messageFacadeInited = true;
                if (walletFacadeInited) onFacadesInitialised();
            }
        });

        dhtSeedService.setHandler(m -> {
            if (m instanceof PeerInitialized) {
                log.debug("dht seed initialized. ");
                dhtPeerService.initializePeer();
            }
        });

        dhtSeedService.initializePeer();

    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Setters
    ///////////////////////////////////////////////////////////////////////////////////////////

    void setCurrentBankAccount(BankAccount bankAccount) {
        user.setCurrentBankAccount(bankAccount);
        persistence.write(user);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Getters
    ///////////////////////////////////////////////////////////////////////////////////////////

    ObservableList<BankAccount> getBankAccounts() {
        return user.getBankAccounts();
    }

    ObjectProperty<BankAccount> currentBankAccountProperty() {
        return user.currentBankAccountProperty();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Private
    ///////////////////////////////////////////////////////////////////////////////////////////

    private void onFacadesInitialised() {
        // TODO Consider to use version sync notification pane from Mike Hearn
        walletFacade.addDownloadListener(new WalletFacade.DownloadListener() {
            @Override
            public void progress(double percent) {
                networkSyncProgress.set(percent);
            }

            @Override
            public void downloadComplete() {
                networkSyncComplete.set(true);
            }
        });

        tradeManager.getPendingTrades().addListener((MapChangeListener<String,
                Trade>) change -> updateNumPendingTrades());
        updateNumPendingTrades();

        backendInited.set(true);
    }

    private void updateNumPendingTrades() {
        numPendingTrades.set(tradeManager.getPendingTrades().size());
    }

   /* private void updateBalance(Coin balance) {
        this.balance.set(balance);
    }*/
}
