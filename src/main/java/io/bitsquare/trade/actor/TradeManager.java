package io.bitsquare.trade.actor;

import io.bitsquare.btc.Restrictions;
import io.bitsquare.trade.Offer;
import io.bitsquare.trade.actor.event.OfferValidationFailed;
import io.bitsquare.trade.actor.command.PlaceOffer;

import java.util.HashSet;
import java.util.Set;

import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.actor.ActorSelection;
import akka.actor.Props;
import akka.event.Logging;
import akka.event.LoggingAdapter;
import akka.japi.pf.ReceiveBuilder;

import static com.google.common.base.Preconditions.*;

public class TradeManager extends AbstractActor {
    private final LoggingAdapter log = Logging.getLogger(context().system(), this);

    public final static String NAME = "tradeManager";

    public final static Props getProps() {
        return Props.create(TradeManager.class);
    }

    private final ActorSelection btcManager;
    private final ActorSelection dhtManager;

    private final Set<ActorRef> listeners = new HashSet<ActorRef>();

    public TradeManager() {

        btcManager = context().actorSelection("/user/btcManager");
        dhtManager = context().actorSelection("/user/dhtManager");

        receive(ReceiveBuilder.
                        match(PlaceOffer.class, po -> {
                            log.debug("Received PlaceOffer message: {}", po);
                            listeners.add(sender());
                            if (isValidOffer(po.getOffer())) {

                                //dhtManager.tell(po, context().self());
                            }
                        }).
//                        match(RemoveOffer.class, ro -> {
//                            log.debug("Received message: {}", ro);
//                            // TODO may remove sender to list of offer listeners
//                            listeners.remove(sender());
//                            dhtManager.tell(ro, self());
//                        }).
//                        match(GetOffers.class, go -> {
//                            log.debug("Received message: {}", go);
//                            dhtManager.tell(go, self());
//                        }).
//                        match(OffersFound.class, of -> {
//                            log.debug("Received message: {}", of);
//                            listeners.forEach(ar -> {
//                                ar.tell(of, self());
//                            });
//                        }).
        matchAny(o -> log.info("received unknown message")).build()
        );
    }

    private boolean isValidOffer(Offer offer) {
        Boolean isValid = false;
        try {
            checkNotNull(offer.getAcceptedCountries(), "AcceptedCountries is null");
            checkNotNull(offer.getAcceptedLanguageLocales(), "AcceptedLanguageLocales is null");
            checkNotNull(offer.getAmount(), "Amount is null");
            checkNotNull(offer.getArbitrators(), "Arbitrator is null");
            checkNotNull(offer.getBankAccountCountry(), "BankAccountCountry is null");
            checkNotNull(offer.getBankAccountId(), "BankAccountId is null");
            checkNotNull(offer.getCollateral(), "Collateral is null");
            checkNotNull(offer.getCreationDate(), "CreationDate is null");
            checkNotNull(offer.getCurrency(), "Currency is null");
            checkNotNull(offer.getDirection(), "Direction is null");
            checkNotNull(offer.getId(), "Id is null");
            checkNotNull(offer.getMessagePublicKey(), "MessagePublicKey is null");
            checkNotNull(offer.getMinAmount(), "MinAmount is null");
            checkNotNull(offer.getPrice(), "Price is null");

            checkArgument(!offer.getAcceptedCountries().isEmpty(), "AcceptedCountries is empty");
            checkArgument(!offer.getAcceptedLanguageLocales().isEmpty(), "AcceptedLanguageLocales is empty");
            checkArgument(offer.getMinAmount().compareTo(Restrictions.MIN_TRADE_AMOUNT) >= 0,
                    "MinAmount is less then " + Restrictions.MIN_TRADE_AMOUNT);
            checkArgument(offer.getAmount().compareTo(Restrictions.MIN_TRADE_AMOUNT) >= 0,
                    "Amount is less then " + Restrictions.MIN_TRADE_AMOUNT);
            checkArgument(offer.getAmount().compareTo(offer.getMinAmount()) >= 0, "MinAmount is larger then Amount");
            checkArgument(offer.getCollateral() > 0, "Collateral is 0");
            checkArgument(offer.getPrice().isPositive(), "Price is 0 or negative");

            // TODO check balance
            // Coin collateralAsCoin = offer.getAmount().divide((long) (1d / offer.getCollateral()));
            // Coin totalsToFund = collateralAsCoin.add(FeePolicy.CREATE_OFFER_FEE.add(FeePolicy.TX_FEE));
            // getAddressInfoByTradeID(offerId)
            // TODO when offer is flattened continue here...
            isValid = true;

        } catch (RuntimeException t) {
            sender().tell(new OfferValidationFailed(offer.getId(), t.getMessage()), self());
        }
        return isValid;
    }

}
