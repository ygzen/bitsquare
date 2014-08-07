package io.bitsquare.prototype.trade;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.bitsquare.prototype.trade.createbuyoffer.CreateBuyOfferActor;
import io.bitsquare.prototype.trade.createbuyoffer.events.BuyOfferCreated;
import io.bitsquare.prototype.trade.placebuyoffer.events.BuyOfferPlaced;

import java.util.function.Function;
import java.util.function.Supplier;

public class BuyTradeCoordinatorActor extends AbstractActor {

  public static Function<Supplier<Props>, Supplier<Props>> props() {
    return p -> () -> Props.create(
      BuyTradeCoordinatorActor.class, () -> new BuyTradeCoordinatorActor(p));
  }

  private final LoggingAdapter log = Logging.getLogger(context().system(), this);

  private ActorRef buyOfferFsm;

  public BuyTradeCoordinatorActor(Supplier<Props> createBuyOfferProps) {
    receive(
      ReceiveBuilder
        .match(
          BuyOfferPlaced.class,
          e -> {
            log.info("Message received {}", e);
            buyOfferFsm = context().actorOf(
              createBuyOfferProps.get(),
              CreateBuyOfferActor.class.getSimpleName());
            buyOfferFsm.tell(e, self());
          })
        .match(
          BuyOfferCreated.class,
          e -> {
            //next...
            log.info("Message received {}", e);
          }
        )
        .matchAny(
          o -> log.info("received unknown message"))
        .build()
    );

  }

}
