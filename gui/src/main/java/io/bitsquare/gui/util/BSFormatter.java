/*
 * This file is part of Bitsquare.
 *
 * Bitsquare is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bitsquare is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bitsquare. If not, see <http://www.gnu.org/licenses/>.
 */

package io.bitsquare.gui.util;

import com.google.common.annotations.VisibleForTesting;
import io.bitsquare.btc.BitcoinNetwork;
import io.bitsquare.common.util.MathUtils;
import io.bitsquare.locale.CurrencyUtil;
import io.bitsquare.locale.LanguageUtil;
import io.bitsquare.p2p.NodeAddress;
import io.bitsquare.trade.Altcoin;
import io.bitsquare.trade.Price;
import io.bitsquare.trade.offer.Offer;
import io.bitsquare.user.Preferences;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Monetary;
import org.bitcoinj.utils.Fiat;
import org.bitcoinj.utils.MonetaryFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class BSFormatter {
    private static final Logger log = LoggerFactory.getLogger(BSFormatter.class);

    private Locale locale = Preferences.getDefaultLocale();
    private boolean useMilliBit;
    private int scale = 3;

    // Format use 2 min decimal places and 2 more optional: 1.00 or 1.0010
    // There are not more then 4 decimals allowed.
    // We don't support localized formatting. Format is always using "." as decimal mark and no grouping separator.
    // Input of "," as decimal mark (like in german locale) will be replaced with ".".
    // Input of a group separator (1,123,45) lead to an validation error.
    // Note: BtcFormat was intended to be used, but it lead to many problems (automatic format to mBit,
    // no way to remove grouping separator). It seems to be not optimal for user input formatting.
    private MonetaryFormat coinFormat = MonetaryFormat.BTC.minDecimals(2).repeatOptionalDecimals(1, 6);

    // format is like: 1,00  never more then 2 decimals
    private final MonetaryFormat fiatFormat = MonetaryFormat.FIAT.repeatOptionalDecimals(0, 0);

    public static BSFormatter INSTANCE;

    @Inject
    public BSFormatter() {
        INSTANCE = this;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Config
    ///////////////////////////////////////////////////////////////////////////////////////////


    public void useMilliBitFormat(boolean useMilliBit) {
        this.useMilliBit = useMilliBit;
        coinFormat = getMonetaryFormat();
        scale = useMilliBit ? 0 : 3;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    private MonetaryFormat getMonetaryFormat() {
        if (useMilliBit)
            return MonetaryFormat.MBTC;
        else
            return MonetaryFormat.BTC.minDecimals(2).repeatOptionalDecimals(1, 6);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Bitcoin
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String formatCoin(Coin coin) {
        if (coin != null) {
            try {
                return coinFormat.noCode().format(coin).toString();
            } catch (Throwable t) {
                log.warn("Exception at formatBtc: " + t.toString());
                return "";
            }
        } else {
            return "";
        }
    }

    public String formatBitcoinWithCode(Coin coin) {
        if (coin != null) {
            try {
                // we don't use the code feature from coinFormat as it does automatic switching between mBTC and BTC and
                // pre and post fixing
                return coinFormat.postfixCode().format(coin).toString();
            } catch (Throwable t) {
                log.warn("Exception at formatBtcWithCode: " + t.toString());
                return "";
            }
        } else {
            return "";
        }
    }

    public Coin parseToBitcoin(String input) {
        if (input != null && !input.isEmpty()) {
            try {
                return coinFormat.parse(cleanInput(input));
            } catch (Throwable t) {
                log.warn("Exception at parseToBtc: " + t.toString());
                return Coin.ZERO;
            }
        } else {
            return Coin.ZERO;
        }
    }

    public Coin parseToBitcoinWith4Decimals(String input) {
        try {
            return Coin.valueOf(BigDecimal.valueOf(parseToBitcoin(cleanInput(input)).value).
                    setScale(-scale - 1, BigDecimal.ROUND_HALF_UP).
                    setScale(scale + 1, BigDecimal.ROUND_HALF_UP).
                    toBigInteger().
                    longValue());
        } catch (Throwable t) {
            if (input != null && !input.isEmpty())
                log.warn("Exception at parseToCoinWith4Decimals: " + t.toString());
            return Coin.ZERO;
        }
    }

    public Coin getRoundedCoinTo4Digits(Coin input) {
        try {
            return Coin.valueOf(BigDecimal.valueOf(input.value).
                    setScale(-scale - 1, BigDecimal.ROUND_HALF_UP).
                    setScale(scale + 1, BigDecimal.ROUND_HALF_UP).
                    toBigInteger().
                    longValue());
        } catch (Throwable t) {
            log.warn("Exception at parseToCoinWith4Decimals: " + t.toString());
            return Coin.ZERO;
        }
    }

    public boolean hasBitcoinValidDecimals(String input) {
        return parseToBitcoin(input).equals(parseToBitcoinWith4Decimals(input));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Amount
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String formatAmountWithMinAmount(Offer offer) {
        return formatCoin(offer.getAmount()) + " (" + formatCoin(offer.getMinAmount()) + ")";
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Price
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String formatPriceWithCode(Price price) {
        if (price != null) {
            return formatPrice(price) + " " + price.getCurrencyCodePair();
        } else {
            return "N/A";
        }
    }

    public String formatPriceWithCodeAndPercent(Offer offer) {
        Price price = offer.getPrice();
        if (price != null) {
            String postFix = "";
            if (offer.getUseMarketBasedPrice())
                postFix = " (" + formatPercentagePrice(offer.getMarketPriceMargin()) + ")";
            return formatPriceWithCode(price) + postFix;
        } else {
            return "N/A";
        }
    }

    public String formatPrice(Price price) {
        if (price != null) {
            try {
                return price.getPriceAsString();
            } catch (Throwable t) {
                log.warn("Exception at formatFiat: " + t.toString());
                return "N/A " + price.getCurrencyCodePair();
            }
        } else {
            return "N/A";
        }
    }

    public boolean hasPriceValidDecimals(String input, String currencyCode) {
        return getLimitedDecimals(input, 20).equals(cleanPriceString(input, currencyCode));
    }

    public String cleanPriceString(String input, String currencyCode) {
        if (CurrencyUtil.isCryptoCurrency(currencyCode))
            return getLimitedDecimals(input, 8);
        else
            return getLimitedDecimals(input, 4);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // MarketPrice
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String formatMarketPrice(double price, String currencyCode) {
        return formatDoubleToString(price, CurrencyUtil.isCryptoCurrency(currencyCode) ? 8 : 4);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Volume
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String formatVolume(Monetary monetary) {
        if (monetary instanceof Fiat)
            return formatFiat((Fiat) monetary);
        else
            return formatAltcoin((Altcoin) monetary);
    }

    public Monetary parseToVolumeWithDecimals(String input, String currencyCode) {
        if (CurrencyUtil.isCryptoCurrency(currencyCode))
            return parseToAltcoinWithDecimals(input, currencyCode, 4);
        else
            return parseToFiatWithDecimals(input, currencyCode, 2);
    }

    public boolean hasVolumeValidDecimals(String input, String currencyCode) {
        if (CurrencyUtil.isCryptoCurrency(currencyCode))
            return hasAltcoinValidDecimals(input, currencyCode);
        else
            return hasFiatValidDecimals(input, currencyCode);
    }


    public String formatVolumeWithMinVolume(Offer offer, boolean includeCode) {
        if (offer != null) {
            final Monetary minOfferVolume = offer.getMinOfferVolume();
            final Monetary offerVolume = offer.getOfferVolume();
            if (offerVolume != null && minOfferVolume != null) {
                if (includeCode)
                    return formatVolumeWithCode(offerVolume) + " (" + formatVolumeWithCode(minOfferVolume) + ")";
                else
                    return formatVolume(offerVolume) + " (" + formatVolume(minOfferVolume) + ")";
            } else {
                return "N/A";
            }
        } else {
            return "N/A";
        }
    }

    public String formatVolumeWithCode(Monetary monetary) {
        return formatVolume(monetary) + getCurrencyPair(monetary, " ");
    }

    private String cleanVolumeString(String input, String currencyCode) {
        if (CurrencyUtil.isCryptoCurrency(currencyCode))
            return getLimitedDecimals(input, 4);
        else
            return getLimitedDecimals(input, 2);
    }

    public Monetary getRoundedVolumeWithLimitedDigits(Monetary volume, String currencyCode) {
        return parseToVolumeWithDecimals(cleanVolumeString(formatVolume(volume), currencyCode), currencyCode);
    }

    public String formatVolumeWithCodeAndLimitedDigits(Monetary volume, String currencyCode) {
        return formatVolumeWithCode(getRoundedVolumeWithLimitedDigits(volume, currencyCode));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Fiat
    ///////////////////////////////////////////////////////////////////////////////////////////

    private String formatFiat(Fiat fiat) {
        if (fiat != null) {
            try {
                return fiatFormat.noCode().format(fiat).toString();
            } catch (Throwable t) {
                log.warn("Exception at formatFiat: " + t.toString());
                return "N/A " + fiat.getCurrencyCode();
            }
        } else {
            return "N/A";
        }
    }

    private Fiat parseToFiat(String input, String currencyCode) {
        if (input != null && !input.isEmpty()) {
            try {
                return Fiat.parseFiat(currencyCode, cleanInput(input));
            } catch (Exception e) {
                log.warn("Exception at parseToFiat: " + e.toString());
                return Fiat.valueOf(currencyCode, 0);
            }

        } else {
            return Fiat.valueOf(currencyCode, 0);
        }
    }

    @VisibleForTesting
    Fiat parseToFiatWithDecimals(String input, String currencyCode, int digits) {
        return parseToFiat(getLimitedDecimals(input, digits), currencyCode);
    }

    @VisibleForTesting
    boolean hasFiatValidDecimals(String input, String currencyCode) {
        return parseToFiat(input, currencyCode).equals(parseToFiatWithDecimals(input, currencyCode, 2));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Altcoin
    ///////////////////////////////////////////////////////////////////////////////////////////

    private String formatAltcoin(Altcoin altcoin) {
        if (altcoin != null) {
            try {
                return altcoin.toPlainString();
            } catch (Throwable t) {
                log.warn("Exception at formatFiat: " + t.toString());
                return "N/A " + altcoin.currencyCode;
            }
        } else {
            return "N/A";
        }
    }

    private Altcoin parseToAltcoin(String input, String currencyCode) {
        if (input != null && !input.isEmpty()) {
            try {
                return Altcoin.parseCoin(currencyCode, cleanInput(input));
            } catch (Exception e) {
                log.warn("Exception at parseToFiat: " + e.toString());
                return Altcoin.valueOf(currencyCode, 0);
            }

        } else {
            return Altcoin.valueOf(currencyCode, 0);
        }
    }

    private Altcoin parseToAltcoinWithDecimals(String input, String currencyCode, int digits) {
        return parseToAltcoin(getLimitedDecimals(input, digits), currencyCode);
    }

    private boolean hasAltcoinValidDecimals(String input, String currencyCode) {
        return parseToAltcoin(input, currencyCode).equals(parseToAltcoinWithDecimals(input, currencyCode, 4));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PercentagePrice
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String formatPercentagePrice(double value) {
        return formatToPercentWithSymbol(value);
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Percentage
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String formatToPercent(double value) {
        return formatDoubleToString(MathUtils.exactMultiply(value, 100), 2);
    }

    public String formatToPercentWithSymbol(double value) {
        return formatToPercent(value) + " %";
    }

    public double parsePercentStringToDouble(String percentString) throws NumberFormatException {
        try {
            String input = percentString.replace("%", "");
            input = input.replace(" ", "");
            input = input.replace(",", ".");
            if (input.equals("-"))
                input = "-0";
            if (input.equals("."))
                input = "0.";
            if (input.equals("-."))
                input = "-0.";
            double value = Double.parseDouble(input);
            return value / 100;
        } catch (NumberFormatException e) {
            throw e;
        }
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Utils
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String getCurrencyPair(Monetary monetary) {
        return getCurrencyPair(monetary, "");
    }

    public String getCurrencyPair(Monetary monetary, String prefix) {
        String code;
        if (monetary instanceof Fiat)
            code = ((Fiat) monetary).getCurrencyCode();
        else
            code = ((Altcoin) monetary).getCurrencyCode();

        return prefix + getCurrencyPair(code);
    }

    public String getCurrencyPair(String currencyCode) {
        if (CurrencyUtil.isCryptoCurrency(currencyCode))
            return "BTC/" + currencyCode;
        else
            return currencyCode + "/BTC";
    }

    private String getLimitedDecimals(String input, int digits) {
        if (input != null && !input.isEmpty()) {
            try {
                return formatDoubleToString(new BigDecimal(cleanInput(cleanInput(input))).setScale(digits, BigDecimal.ROUND_HALF_UP).doubleValue(), digits);
            } catch (Throwable t) {
                log.warn("Exception at parseToAltcoinWithDecimals: " + t.toString());
                return "";
            }
        }
        return "";
    }

    public String formatDoubleToString(double value, int digits) {
        DecimalFormat decimalFormat = new DecimalFormat("0.#");
        decimalFormat.setMaximumFractionDigits(digits);
        decimalFormat.setGroupingUsed(false);
        return decimalFormat.format(value).replace(",", ".");
    }

    public double parseNumberStringToDouble(String input) throws NumberFormatException {
        try {
            return Double.parseDouble(cleanInput(input));
        } catch (NumberFormatException e) {
            throw e;
        }
    }

    private String cleanInput(String input) {
        input = input.replace(",", ".");
        input = input.replace(" ", "");
        // don't use String.valueOf(Double.parseDouble(input)) as return value as it gives scientific
        // notation (1.0E-6) which screw up coinFormat.parse
        //noinspection ResultOfMethodCallIgnored
        Double.parseDouble(input);
        return input;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Direction
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String getDirection(Offer.Direction direction) {
        return getDirection(direction, false) + " bitcoin";
    }

    private String getDirection(Offer.Direction direction, boolean allUpperCase) {
        String result = (direction == Offer.Direction.BUY) ? "Buy" : "Sell";
        if (allUpperCase) {
            result = result.toUpperCase();
        }
        return result;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Date
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String formatDateTime(Date date) {
        if (date != null) {
            DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
            DateFormat timeFormatter = DateFormat.getTimeInstance(DateFormat.DEFAULT, locale);
            return dateFormatter.format(date) + " " + timeFormatter.format(date);
        } else {
            return "";
        }
    }

    public String formatDate(Date date) {
        if (date != null) {
            DateFormat dateFormatter = DateFormat.getDateInstance(DateFormat.DEFAULT, locale);
            return dateFormatter.format(date);
        } else {
            return "";
        }
    }

    public String formatDurationAsWords(long durationMillis) {
        return formatDurationAsWords(durationMillis, false);
    }

    public static String formatDurationAsWords(long durationMillis, boolean showSeconds) {
        String format;
        if (showSeconds)
            format = "d\' days, \'H\' hours, \'m\' minutes, \'s\' seconds\'";
        else
            format = "d\' days, \'H\' hours, \'m\' minutes\'";
        String duration = DurationFormatUtils.formatDuration(durationMillis, format);
        String tmp;
        duration = " " + duration;
        tmp = StringUtils.replaceOnce(duration, " 0 days", "");
        if (tmp.length() != duration.length()) {
            duration = tmp;
            tmp = StringUtils.replaceOnce(tmp, " 0 hours", "");
            if (tmp.length() != duration.length()) {
                tmp = StringUtils.replaceOnce(tmp, " 0 minutes", "");
                duration = tmp;
                if (tmp.length() != tmp.length()) {
                    duration = StringUtils.replaceOnce(tmp, " 0 seconds", "");
                }
            }
        }

        if (duration.length() != 0) {
            duration = duration.substring(1);
        }

        tmp = StringUtils.replaceOnce(duration, " 0 seconds", "");

        if (tmp.length() != duration.length()) {
            duration = tmp;
            tmp = StringUtils.replaceOnce(tmp, " 0 minutes", "");
            if (tmp.length() != duration.length()) {
                duration = tmp;
                tmp = StringUtils.replaceOnce(tmp, " 0 hours", "");
                if (tmp.length() != duration.length()) {
                    duration = StringUtils.replaceOnce(tmp, " 0 days", "");
                }
            }
        }

        duration = " " + duration;
        duration = StringUtils.replaceOnce(duration, " 1 seconds", " 1 second");
        duration = StringUtils.replaceOnce(duration, " 1 minutes", " 1 minute");
        duration = StringUtils.replaceOnce(duration, " 1 hours", " 1 hour");
        duration = StringUtils.replaceOnce(duration, " 1 days", " 1 day");
        if (duration.equals(" ,"))
            duration = "Trade period is over";
        return duration.trim();
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Lists
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String arbitratorAddressesToString(List<NodeAddress> nodeAddresses) {
        return nodeAddresses.stream().map(e -> e.getFullAddress()).collect(Collectors.joining(", "));
    }

    public String languageCodesToString(List<String> languageLocales) {
        return languageLocales.stream().map(LanguageUtil::getDisplayName).collect(Collectors.joining(", "));
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // Misc
    ///////////////////////////////////////////////////////////////////////////////////////////

    public String formatBytes(long bytes) {
        double kb = 1024;
        double mb = kb * kb;
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        if (bytes < kb)
            return bytes + " bytes";
        else if (bytes < mb)
            return decimalFormat.format(bytes / kb) + " KB";
        else
            return decimalFormat.format(bytes / mb) + " MB";
    }


    public String booleanToYesNo(boolean value) {
        return value ? "Yes" : "No";
    }

    public String formatBitcoinNetwork(BitcoinNetwork bitcoinNetwork) {
        switch (bitcoinNetwork) {
            case MAINNET:
                return "Mainnet";
            case TESTNET:
                return "Testnet";
            case REGTEST:
                return "Regtest";
            default:
                return "";
        }
    }

    public String getDirectionBothSides(Offer.Direction direction) {
        return direction == Offer.Direction.BUY ? "Offerer as bitcoin buyer / Taker as bitcoin seller" :
                "Offerer as bitcoin seller / Taker as bitcoin buyer";
    }

    public String getDirectionForBuyer(boolean isMyOffer) {
        return isMyOffer ? "You are buying bitcoin as offerer / Taker is selling bitcoin" :
                "You are buying bitcoin as taker / Offerer is selling bitcoin";
    }

    public String getDirectionForSeller(boolean isMyOffer) {
        return isMyOffer ? "You are selling bitcoin as offerer / Taker is buying bitcoin" :
                "You are selling bitcoin as taker / Offerer is buying bitcoin";
    }

    public String getDirectionForTakeOffer(Offer.Direction direction) {
        return direction == Offer.Direction.BUY ? "You are selling bitcoin (by taking an offer from someone who wants to buy bitcoin)" :
                "You are buying bitcoin (by taking an offer from someone who wants to sell bitcoin)";
    }

    public String getOfferDirectionForCreateOffer(Offer.Direction direction) {
        return direction == Offer.Direction.BUY ? "You are creating an offer for buying bitcoin" :
                "You are creating an offer for selling bitcoin";
    }

    public String getRole(boolean isBuyerOffererAndSellerTaker, boolean isOfferer) {
        if (isBuyerOffererAndSellerTaker)
            return isOfferer ? "Buyer (offerer)" : "Seller (taker)";
        else
            return isOfferer ? "Seller (offerer)" : "Buyer (taker)";
    }


}
