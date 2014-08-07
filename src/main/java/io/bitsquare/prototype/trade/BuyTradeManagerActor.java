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
import java.util.function.Function;
import java.util.function.Supplier;

public class BuyTradeManagerActor extends AbstractActor {

  public static Function<Supplier<Props>, Supplier<Props>> props() {
    return p -> () -> Props.create(
      BuyTradeManagerActor.class, () -> new BuyTradeManagerActor(p));
  }

  private final LoggingAdapter log = Logging.getLogger(context().system(), this);

  private Map<String, ActorRef> buyTradeActors = new HashMap<>();

  public BuyTradeManagerActor(Supplier<Props> buyTradeCoordinatorProps) {
    receive(
      ReceiveBuilder
        //entry event
        .match(
          BuyOfferPlaced.class,
          e -> {
            log.info("Message received {}", e);
            ActorRef child = getContext().actorOf(
              buyTradeCoordinatorProps.get(),
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
