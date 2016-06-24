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

package io.bitsquare.trade;

import org.bitcoinj.core.Coin;
import org.bitcoinj.utils.Fiat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class PriceTest {
    private static final Logger log = LoggerFactory.getLogger(AltcoinPrice.class);

    //  @Test
    public void testFiatPrice() {
        Coin amount = Coin.parseCoin("0.1");
        Fiat fiat = Fiat.parseFiat("EUR", "500");
        FiatPrice fiatPrice = new FiatPrice(fiat);
        assertEquals(Fiat.parseFiat("EUR", "50"), fiatPrice.getVolume(amount));
    }

    @Test
    public void testAltcoinPrice() {
        Coin btcAmount = Coin.parseCoin("0.2");
        Coin btcPerEth = Coin.parseCoin("0.02");
        AltcoinPrice altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        assertEquals(Altcoin.parseCoin("ETH", "10"), altcoinPrice.getVolume(btcAmount));

        btcAmount = Coin.parseCoin("0");
        btcPerEth = Coin.parseCoin("0.02");
        altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        assertEquals(Altcoin.parseCoin("ETH", "0"), altcoinPrice.getVolume(btcAmount));

        btcAmount = Coin.parseCoin("1");
        btcPerEth = Coin.parseCoin("0.02");
        altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        assertEquals(Altcoin.parseCoin("ETH", "50"), altcoinPrice.getVolume(btcAmount));

        btcAmount = Coin.parseCoin("0.2");
        btcPerEth = Coin.parseCoin("1");
        altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        assertEquals(Altcoin.parseCoin("ETH", "0.2"), altcoinPrice.getVolume(btcAmount));

        btcAmount = Coin.parseCoin("1");
        btcPerEth = Coin.parseCoin("0.00000001");
        altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        assertEquals(Altcoin.parseCoin("ETH", "100000000"), altcoinPrice.getVolume(btcAmount));

        btcAmount = Coin.parseCoin("1");
        btcPerEth = Coin.parseCoin("10000000");
        altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        assertEquals(Altcoin.parseCoin("ETH", "0.0000001"), altcoinPrice.getVolume(btcAmount));

    }
}
