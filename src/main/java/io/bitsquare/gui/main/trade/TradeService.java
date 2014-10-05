package io.bitsquare.gui.main.trade;

import io.bitsquare.util.ActorService;
import io.bitsquare.persistence.Persistence;
import io.bitsquare.settings.Settings;
import io.bitsquare.trade.Direction;
import io.bitsquare.trade.Offer;
import io.bitsquare.trade.actor.TradeManager;
import io.bitsquare.trade.actor.command.PlaceOffer;
import io.bitsquare.user.User;

import com.google.bitcoin.core.Coin;
import com.google.bitcoin.utils.Fiat;

import com.google.inject.Inject;

import akka.actor.ActorSystem;

public class TradeService extends ActorService {

    private final User user;
    private final Settings settings;
    private final Persistence persistence;

    @Inject
    public TradeService(ActorSystem system, User user, Settings settings, Persistence persistence) {
        super(system, "/user/"+ TradeManager.NAME);

        this.user = user;
        this.settings = settings;
        this.persistence = persistence;
    }

    public void placeOffer(String id,
                           Direction direction,
                           Fiat price,
                           Coin amount,
                           Coin minAmount) {

        Offer offer = new Offer(id,
                user.getMessagePublicKey(),
                direction,
                price.getValue(),
                amount,
                minAmount,
                user.getCurrentBankAccount().getBankAccountType(),
                user.getCurrentBankAccount().getCurrency(),
                user.getCurrentBankAccount().getCountry(),
                user.getCurrentBankAccount().getUid(),
                settings.getAcceptedArbitrators(),
                settings.getCollateral(),
                settings.getAcceptedCountries(),
                settings.getAcceptedLanguageLocales());

        send(new PlaceOffer(offer));
    }
}
