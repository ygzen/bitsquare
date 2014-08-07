package io.bitsquare.prototype.trade;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import io.bitsquare.prototype.MessageActorBus;
import io.bitsquare.prototype.trade.createbuyoffer.CreateBuyOfferActor;
import io.bitsquare.prototype.trade.placebuyoffer.PlaceBuyOfferActor;
import io.bitsquare.prototype.trade.placebuyoffer.commands.PlaceBuyOffer;
import io.bitsquare.prototype.trade.placebuyoffer.events.BuyOfferPlaced;

import java.util.function.Supplier;

public class Bootstrap {

  public static MessageActorBus start(ActorSystem system) {
    MessageActorBus bus = new MessageActorBus();

    final ActorRef buyOfferActor =
      system.actorOf(
        PlaceBuyOfferActor.props().apply(bus).get(),
        PlaceBuyOfferActor.class.getSimpleName());

    bus.subscribe(buyOfferActor, PlaceBuyOffer.class.getCanonicalName());

    Supplier<Props> createBuyOfferProps = CreateBuyOfferActor.props();

    Supplier<Props> buyTraderCoordinatorProps =
      BuyTradeCoordinatorActor.props().apply(createBuyOfferProps);

    final ActorRef buyTradeManager =
      system.actorOf(
        BuyTradeManagerActor.props().apply(buyTraderCoordinatorProps).get(),
        BuyTradeManagerActor.class.getSimpleName());

    bus.subscribe(buyTradeManager, BuyOfferPlaced.class.getCanonicalName());
    
    return bus;

  }
}
