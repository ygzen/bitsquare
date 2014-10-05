package io.bitsquare.msg;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import io.bitsquare.util.ActorService;
import io.bitsquare.msg.actor.DHTManager;
import io.bitsquare.msg.actor.command.InitializePeer;
import net.tomp2p.peers.Number160;

public class DHTSeedService extends ActorService {

    @Inject
    public DHTSeedService(ActorSystem system) {
        super(system, "/user/" + DHTManager.SEED_NAME);
    }

    public void initializePeer() {

        // TODO hard coded seed peer config for now, should read from config properties file
        send(new InitializePeer(new Number160(4000), 4000, null));
    }
}
