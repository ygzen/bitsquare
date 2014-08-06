package io.bitsquare.prototype.trade.createbuyoffer.events;

public class BuyOfferCreated {

  public final String id;
  public final String offerFeePaymentTransactionId;

  public BuyOfferCreated(String id, String offerFeePaymentTransactionId) {
    this.id = id;
    this.offerFeePaymentTransactionId = offerFeePaymentTransactionId;
  }
}
