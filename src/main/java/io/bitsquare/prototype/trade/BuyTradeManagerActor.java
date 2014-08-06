package io.bitsquare.prototype.trade;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.bitsquare.prototype.trade.placebuyoffer.events.BuyOfferPlaced;

import java.util.HashMap;
import java.util.Map;

public class BuyTradeManagerActor extends AbstractActor {

  public static Props props() {
    return Props.create(
      BuyTradeManagerActor.class, () -> new BuyTradeManagerActor());
  }

  private final LoggingAdapter log = Logging.getLogger(context().system(), this);

  private Map<String, ActorRef> buyTradeActors = new HashMap<>();

  public BuyTradeManagerActor() {
    receive(
      ReceiveBuilder
        //entry event
        .match(
          BuyOfferPlaced.class,
          e -> {
            log.info("Message received {}", e);
            ActorRef child = getContext().actorOf(
              BuyTradeCoordinatorActor.props(),
              BuyTradeCoordinatorActor.class.getSimpleName() + '-' + e.id);
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
