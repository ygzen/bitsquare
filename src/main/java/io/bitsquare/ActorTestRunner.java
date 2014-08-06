package io.bitsquare;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import io.bitsquare.prototype.DomainEventActorBus;
import io.bitsquare.prototype.trade.BuyTradeManagerActor;
import io.bitsquare.prototype.trade.validatebuyoffer.BuyOfferActor;
import io.bitsquare.prototype.trade.completebuyoffer.commands.PlaceBuyOffer;
import io.bitsquare.prototype.trade.validatebuyoffer.events.BuyOfferValidated;

import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;

public class ActorTestRunner {

  private static String APP_NAME = "bitsquare";
  private final ActorSystem system = ActorSystem.create(APP_NAME);
  private final DomainEventActorBus eventBus = new DomainEventActorBus();

  public static void main(String[] args) throws Exception {
    new ActorTestRunner();
  }

  private ActorTestRunner() {
    final ActorRef buyOfferActor =
      system.actorOf(BuyOfferActor.props(eventBus), "buyOfferActor");
    final ActorRef buyTradeManager =
      system.actorOf(BuyTradeManagerActor.props(), "buyTradeManager");

    eventBus.subscribe(buyTradeManager, BuyOfferValidated.class.getCanonicalName());

    PlaceBuyOffer pbo = new PlaceBuyOffer(
      UUID.randomUUID().toString(),
      ZonedDateTime.now(ZoneId.of("UTC")),
      "GBP",
      500d,
      new BigInteger("100000000"),
      new BigInteger("10000000"),
      0.1f,
      UUID.randomUUID().toString(),
      "SEPA",
      "DE",
      "fjkajfkajfiejfkadsjfkajdsfie",
      new BigInteger("100000"),
      UUID.randomUUID().toString(),
      new ArrayList<>(),
      new ArrayList<>(),
      new ArrayList<>());

    buyOfferActor.tell(pbo, null);
  }
}
