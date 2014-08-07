package io.bitsquare.prototype.trade.createbuyoffer;

import akka.actor.AbstractActor;
import akka.actor.AbstractLoggingFSM;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.bitsquare.prototype.trade.createbuyoffer.commands.PayBuyOfferFee;
import io.bitsquare.prototype.trade.createbuyoffer.events.BuyOfferCreated;
import io.bitsquare.prototype.trade.createbuyoffer.events.BuyOfferFeePaid;
import io.bitsquare.prototype.trade.createbuyoffer.events.BuyOfferPublished;
import io.bitsquare.prototype.trade.placebuyoffer.events.BuyOfferPlaced;

import java.util.function.Supplier;


public class CreateBuyOfferActor extends AbstractActor {

  public static Supplier<Props> props() {
    return () -> Props.create(
      CreateBuyOfferActor.class, () -> new CreateBuyOfferActor());
  }

  private final LoggingAdapter log = Logging.getLogger(context().system(), this);

  private String offerFeePaymentTransactionId;

  public CreateBuyOfferActor() {
    receive(
      ReceiveBuilder
        .match(
          BuyOfferPlaced.class,
          e -> {
            PayBuyOfferFee c = new PayBuyOfferFee(e.id);
            log.info("Message received {}", e);
          }
        )
        .match(
          BuyOfferFeePaid.class,
          e -> {
            offerFeePaymentTransactionId = e.offerFeePaymentTransactionId;
            log.info("Message received {}", e);
          }
        )
        .match(
          BuyOfferPublished.class,
          e -> {
            log.info("Message received {}", e);
            BuyOfferCreated created = new BuyOfferCreated(e.id, offerFeePaymentTransactionId);
          }
        )
        .matchAny(
          o -> log.info("received unknown message"))
        .build()
    );

  }
}
