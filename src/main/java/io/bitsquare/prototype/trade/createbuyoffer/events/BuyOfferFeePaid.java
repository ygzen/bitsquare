package io.bitsquare.prototype.trade.createbuyoffer.events;

public final class BuyOfferFeePaid {

  public final String id;
  public final String offerFeePaymentTransactionId;

  public BuyOfferFeePaid(String id, String offerFeePaymentTransactionId) {
    this.id = id;
    this.offerFeePaymentTransactionId = offerFeePaymentTransactionId;
  }
}
