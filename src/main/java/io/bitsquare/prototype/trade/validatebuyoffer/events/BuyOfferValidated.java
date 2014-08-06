package io.bitsquare.prototype.trade.validatebuyoffer.events;

import java.math.BigInteger;
import java.time.ZonedDateTime;
import java.util.List;

public final class BuyOfferValidated {

  public final String id;

  public final ZonedDateTime creatingDateTime;

  public final String currencyCode;
  public final double price;
  public final BigInteger amount;
  public final BigInteger minAmount;
  public final float collateral;

  public final String bankAccountId;
  public final String bankAccountTypeCode;
  public final String bankAccountCountryCode;

  public final String messagePublicKey;

  public final BigInteger bidFee;
  public final String bidFeePaymentTransactionId;

  public final List<String> acceptedCountryCodes;
  public final List<String> acceptedLanguageCodes;
  public final List<String> acceptedArbitratorIds;

  public BuyOfferValidated(String id,
                           ZonedDateTime creatingDateTime,
                           String currencyCode,
                           double price,
                           BigInteger amount,
                           BigInteger minAmount,
                           float collateral,
                           String bankAccountId,
                           String bankAccountTypeCode,
                           String bankAccountCountryCode,
                           String messagePublicKey,
                           BigInteger bidFee,
                           String bidFeePaymentTransactionId,
                           List<String> acceptedCountryCodes,
                           List<String> acceptedLanguageCodes,
                           List<String> acceptedArbitratorIds) {
    this.id = id;
    this.creatingDateTime = creatingDateTime;
    this.currencyCode = currencyCode;
    this.price = price;
    this.amount = amount;
    this.minAmount = minAmount;
    this.collateral = collateral;
    this.bankAccountId = bankAccountId;
    this.bankAccountTypeCode = bankAccountTypeCode;
    this.bankAccountCountryCode = bankAccountCountryCode;
    this.messagePublicKey = messagePublicKey;
    this.bidFee = bidFee;
    this.bidFeePaymentTransactionId = bidFeePaymentTransactionId;
    this.acceptedCountryCodes = acceptedCountryCodes;
    this.acceptedLanguageCodes = acceptedLanguageCodes;
    this.acceptedArbitratorIds = acceptedArbitratorIds;
  }

}
