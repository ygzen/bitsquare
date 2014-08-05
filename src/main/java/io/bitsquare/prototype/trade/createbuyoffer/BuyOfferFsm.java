package io.bitsquare.prototype.trade.createbuyoffer;

import akka.actor.AbstractLoggingFSM;
import akka.actor.Props;
import io.bitsquare.prototype.trade.createbuyoffer.events.BuyOfferFeePaid;
import io.bitsquare.prototype.trade.createbuyoffer.events.BuyOfferValidated;


public class BuyOfferFsm extends AbstractLoggingFSM<BuyOfferFsm.State, BuyOfferFsm.Data> {

  public static Props props() {
    return Props.create(BuyOfferFsm.class, () -> new BuyOfferFsm());
  }

  enum State {
    IDLE,
    PAY_FEE,
    PUBLISH,
    COMPLETE
  }

  class Data {
    private final String id;

    Data(String id) {
      this.id = id;
    }
  }

  {

    startWith(State.IDLE, new Data(""));

    when(State.IDLE,
      matchEvent(BuyOfferValidated.class, Data.class,
        (e, d) ->
          goTo(State.PAY_FEE).using(new Data(e.id))
      )
    );

    when(State.PAY_FEE,
      matchEvent(BuyOfferFeePaid.class, Data.class,
        (e, d) ->
          stay().using(new Data(d.id))
      )
    );

    onTransition(
      matchState(State.IDLE, State.PAY_FEE, () ->
        //create and send PayBuyOfferFee
        log().info("state transition from idle to pay fee")
      )
    );


    initialize();
  }

}
