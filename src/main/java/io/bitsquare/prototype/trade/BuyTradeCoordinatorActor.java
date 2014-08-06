package io.bitsquare.prototype.trade;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.bitsquare.prototype.trade.validatebuyoffer.events.BuyOfferValidated;

import java.util.HashMap;
import java.util.Map;

public class BuyTradeCoordinatorActor extends AbstractActor {

  public static Props props() {
    return Props.create(
      BuyTradeCoordinatorActor.class, () -> new BuyTradeCoordinatorActor());
  }

  private final LoggingAdapter log = Logging.getLogger(context().system(), this);

  private Map<String, ActorRef> buyTradeActors = new HashMap<>();

  public BuyTradeCoordinatorActor() {
    receive(
      ReceiveBuilder
        //entry event
        .match(
          BuyOfferValidated.class,
          e -> {
            log.info("Message received {}", e);
            ActorRef child = getContext().actorOf(BuyTradeActor.props(), e.id);
            buyTradeActors.put(e.id, child);
            child.tell(e, self());
          }
        )
        .matchAny(
          o -> log.info("received unknown message")
        )
        .build()
    );
  }
}
