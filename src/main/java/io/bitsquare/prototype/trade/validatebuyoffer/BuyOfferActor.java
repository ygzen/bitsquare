package io.bitsquare.prototype.trade.validatebuyoffer;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.bitsquare.prototype.DomainEventActorBus;
import io.bitsquare.prototype.trade.completebuyoffer.commands.PlaceBuyOffer;
import io.bitsquare.prototype.trade.validatebuyoffer.events.BuyOfferValidated;

import java.util.Optional;

public class BuyOfferActor extends AbstractActor {

  public static Props props(DomainEventActorBus eventBus) {
    return Props.create(BuyOfferActor.class, () -> new BuyOfferActor(eventBus));
  }

  private final DomainEventActorBus eventBus;

  private final LoggingAdapter log = Logging.getLogger(context().system(), this);

  public BuyOfferActor(DomainEventActorBus eventBus) {
    this.eventBus = eventBus;
    receive(
      ReceiveBuilder
        .match(
          PlaceBuyOffer.class,
          c -> {
            log.info("Received PlaceBuyOffer msg: {}", c);
            Optional<BuyOfferValidated> bov = handle(c);
            if(bov.isPresent()) {
              eventBus.publish(bov.get());
            } else {
              log.info("not present");
            }
          })
        .matchAny(
          o -> log.info("received unknown message"))
        .build()
    );

  }

  public Optional<BuyOfferValidated> handle(PlaceBuyOffer command) {
    if (command.minAmount.compareTo(command.amount) > 0) {
      return Optional.empty();
    }

    return Optional.of(new BuyOfferValidated(
      command.id,
      command.creatingDateTime,
      command.currencyCode,
      command.price,
      command.amount,
      command.minAmount,
      command.collateral,
      command.bankAccountId,
      command.bankAccountTypeCode,
      command.bankAccountCountryCode,
      command.messagePublicKey,
      command.bidFee,
      command.bidFeePaymentTransactionId,
      command.acceptedCountryCodes,
      command.acceptedLanguageCodes,
      command.acceptedArbitratorIds));
  }


}
