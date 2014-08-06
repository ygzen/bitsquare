package io.bitsquare.prototype.trade.placebuyoffer;

import akka.actor.AbstractActor;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;
import io.bitsquare.prototype.DomainEventActorBus;
import io.bitsquare.prototype.trade.placebuyoffer.commands.PlaceBuyOffer;
import io.bitsquare.prototype.trade.placebuyoffer.events.BuyOfferPlaced;

import java.util.Optional;

public class PlaceBuyOfferActor extends AbstractActor {

  public static Props props(DomainEventActorBus eventBus) {
    return Props.create(PlaceBuyOfferActor.class, () -> new PlaceBuyOfferActor(eventBus));
  }

  private final DomainEventActorBus eventBus;

  private final LoggingAdapter log = Logging.getLogger(context().system(), this);

  public PlaceBuyOfferActor(DomainEventActorBus eventBus) {
    this.eventBus = eventBus;
    receive(
      ReceiveBuilder
        .match(
          PlaceBuyOffer.class,
          c -> {
            log.info("Received PlaceBuyOffer msg: {}", c);
            Optional<BuyOfferPlaced> bov = handle(c);
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

  public Optional<BuyOfferPlaced> handle(PlaceBuyOffer command) {
    if (command.minAmount.compareTo(command.amount) > 0) {
      return Optional.empty();
    }

    return Optional.of(new BuyOfferPlaced(
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
