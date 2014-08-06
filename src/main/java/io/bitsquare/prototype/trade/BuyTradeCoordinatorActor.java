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

public class BuyTradeCoordinatorActor extends AbstractActor {

  public static Props props() {
    return Props.create(BuyTradeCoordinatorActor.class, () -> new BuyTradeCoordinatorActor());
  }

  private final LoggingAdapter log = Logging.getLogger(context().system(), this);

  private ActorRef buyOfferFsm;

  public BuyTradeCoordinatorActor() {
    receive(
      ReceiveBuilder
        .match(
          BuyOfferPlaced.class,
          e -> {
            log.info("Message received {}", e);
            buyOfferFsm = context().actorOf(
              CreateBuyOfferActor.props(),
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
