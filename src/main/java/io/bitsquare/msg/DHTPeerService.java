package io.bitsquare.msg;

import akka.actor.ActorSystem;
import com.google.inject.Inject;
import io.bitsquare.util.ActorService;
import io.bitsquare.msg.actor.DHTManager;
import io.bitsquare.msg.actor.command.InitializePeer;
import net.tomp2p.peers.Number160;
import net.tomp2p.peers.PeerAddress;

import java.net.InetAddress;
import java.util.Arrays;

public class DHTPeerService extends ActorService {

    @Inject
    public DHTPeerService(ActorSystem system) {
        super(system, "/user/" + DHTManager.PEER_NAME);
    }

    public void initializePeer() {

        // TODO hard coded to local bootstrap seed peer for now, should read from config properties file
        send(new InitializePeer(new Number160(4001), 4001,
                Arrays.asList(new PeerAddress(new Number160(4000), InetAddress.getLoopbackAddress(), 4000, 4000))));
    }
}
