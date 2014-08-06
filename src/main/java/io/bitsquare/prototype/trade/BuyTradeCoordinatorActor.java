package io.bitsquare.prototype.trade;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.bitsquare.prototype.trade.completebuyoffer.BuyOfferFsm;
import io.bitsquare.prototype.trade.completebuyoffer.events.BuyOfferPublished;
import io.bitsquare.prototype.trade.validatebuyoffer.events.BuyOfferValidated;

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
          BuyOfferValidated.class,
          e -> {
            log.info("Message received {}", e);
            buyOfferFsm = context().actorOf(BuyOfferFsm.props(), "buyOfferFsm");
            buyOfferFsm.tell(e, self());
          })
        .match(
          BuyOfferPublished.class,
          e -> {
            //next...
            log.info("Received PlaceBuyOffer msg: {}", e);
          }
        )
        .matchAny(
          o -> log.info("received unknown message"))
        .build()
    );

  }

}
