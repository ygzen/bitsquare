package io.bitsquare.prototype.trade.actors;

import akka.actor.AbstractActor;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

public class TradeManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    public TradeManager() {
        receive(ReceiveBuilder.
                        match(String.class, s -> {
                            log.info("Received String message: {}", s);
                        }).
                        matchAny(o -> log.info("received unknown message")).build()
        );
    }
}
