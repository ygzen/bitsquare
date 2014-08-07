package io.bitsquare;

import akka.actor.ActorSystem;
import io.bitsquare.prototype.MessageActorBus;
import io.bitsquare.prototype.trade.Bootstrap;
import io.bitsquare.prototype.trade.placebuyoffer.commands.PlaceBuyOffer;

import java.math.BigInteger;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

public class ActorTestRunner {

  private static String APP_NAME = "bitsquare";

  public static void main(String[] args) throws Exception {
    new ActorTestRunner();
  }

  private ActorTestRunner() {
    ActorSystem system = ActorSystem.create(APP_NAME);
    MessageActorBus bus = Bootstrap.start(system);

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

    bus.publish(pbo);
  }
}
