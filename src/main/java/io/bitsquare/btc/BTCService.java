package io.bitsquare.btc;

import akka.actor.ActorSystem;
import com.google.bitcoin.core.NetworkParameters;
import com.google.inject.Inject;
import io.bitsquare.btc.actor.BTCManager;
import io.bitsquare.btc.actor.command.InitializeWallet;
import io.bitsquare.gui.util.ActorService;

public class BTCService extends ActorService {

    @Inject
    public BTCService(ActorSystem system) {
        super(system, "/user/" + BTCManager.NAME);
    }

    public void initializeWallet() {

        send(new InitializeWallet(NetworkParameters.ID_REGTEST, "regtest"));
    }
}
