package io.bitsquare.gui.main.funds;

import io.bitsquare.btc.actor.command.InitializeWallet;
import io.bitsquare.gui.util.ActorService;
import io.bitsquare.btc.actor.BTCManager;

import com.google.bitcoin.core.NetworkParameters;

import com.google.inject.Inject;

import akka.actor.ActorSystem;

public class BTCService extends ActorService {

    @Inject
    public BTCService(ActorSystem system) {
        super(system, "/user/"+ BTCManager.NAME);
    }

    public void initializeBTCWallet() {

        send(new InitializeWallet(NetworkParameters.ID_REGTEST, "regtest"));
    }
}
