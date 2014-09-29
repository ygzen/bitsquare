package io.bitsquare.gui.main.trade;

import io.bitsquare.gui.util.ActorService;
import io.bitsquare.trade.actor.BTCManager;
import io.bitsquare.trade.actor.command.InitializeBTCWallet;

import com.google.bitcoin.core.NetworkParameters;

import com.google.inject.Inject;

import akka.actor.ActorSystem;

public class BTCService extends ActorService {

    @Inject
    public BTCService(ActorSystem system) {
        super(system, "/user/"+ BTCManager.NAME);
    }

    public void initializeBTCWallet() {

        send(new InitializeBTCWallet(NetworkParameters.ID_REGTEST, "regtest"));
    }
}
