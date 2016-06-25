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

import io.bitsquare.app.DevFlags;
import io.bitsquare.gui.main.overlays.popups.Popup;
import io.bitsquare.trade.Price;
import io.bitsquare.trade.Trade;
import io.bitsquare.trade.offer.Offer;
import io.bitsquare.user.Preferences;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.ScrollBar;
import org.bitcoinj.core.Monetary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GUIUtil {
    private static final Logger log = LoggerFactory.getLogger(GUIUtil.class);

    public static double getScrollbarWidth(Node scrollablePane) {
        Node node = scrollablePane.lookup(".scroll-bar");
        if (node instanceof ScrollBar) {
            final ScrollBar bar = (ScrollBar) node;
            if (bar.getOrientation().equals(Orientation.VERTICAL))
                return bar.getWidth();
        }
        return 0;
    }

    public static void showFeeInfoBeforeExecute(Runnable runnable) {
        String key = "miningFeeInfo";
        if (!DevFlags.DEV_MODE && Preferences.INSTANCE.showAgain(key)) {
            new Popup<>().information("Please be sure that the mining fee used at your external wallet is " +
                    "sufficiently high so that the funding transaction will be accepted by the miners.\n" +
                    "Otherwise the trade transactions cannot be confirmed and a trade would end up in a dispute.\n\n" +
                    "The recommended fee is about 0.0001 - 0.0002 BTC.\n\n" +
                    "You can view typically used fees at: https://tradeblock.com/blockchain")
                    .dontShowAgainId(key, Preferences.INSTANCE)
                    .onClose(runnable::run)
                    .closeButtonText("I understand")
                    .show();
        } else {
            runnable.run();
        }
    }

    public static int compareOfferVolumes(Offer offer1, Offer offer2) {
        return compareVolumes(offer1.getOfferVolume(), offer2.getOfferVolume());
    }

    public static int compareVolumes(Monetary offerVolume1, Monetary offerVolume2) {
        if (offerVolume1 instanceof Comparable && offerVolume2 instanceof Comparable) {
            Comparable volume1 = (Comparable) offerVolume1;
            Comparable volume2 = (Comparable) offerVolume2;
            return volume1.compareTo(volume2);
        } else {
            return 0;
        }
    }

    public static int compareTradePrices(Trade trade1, Trade trade2) {
        Price price1 = trade1.getTradePrice();
        Price price2 = trade2.getTradePrice();
        return price1 != null && price2 != null ? price1.compareTo(price2) : 0;
    }

    public static int compareOfferPrices(Offer offer1, Offer offer2) {
        Price price1 = offer1.getPrice();
        Price price2 = offer2.getPrice();
        return price1 != null && price2 != null ? price1.compareTo(price2) : 0;
    }
}
