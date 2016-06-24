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

    @Test
    public void testPriceFactory() {
        Fiat fiat = Fiat.parseFiat("EUR", "500");
        FiatPrice fiatPrice = new FiatPrice(fiat);
        Price price = PriceFactory.getPriceFromLong("EUR", fiat.value);
        assertEquals(price, fiatPrice);

        Coin btcPerEth = Coin.parseCoin("0.12345678");
        AltcoinPrice altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        price = PriceFactory.getPriceFromLong("ETH", btcPerEth.value);
        assertEquals(price, altcoinPrice);

        fiat = Fiat.parseFiat("EUR", "500");
        fiatPrice = new FiatPrice(fiat);
        price = PriceFactory.getPriceFromString("EUR", "500");
        assertEquals(price, fiatPrice);

        btcPerEth = Coin.parseCoin("0.12345678");
        altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        price = PriceFactory.getPriceFromString("ETH", "0.12345678");
        assertEquals(price, altcoinPrice);
    }

    @Test
    public void testFiatPriceGetVolume() {
        Coin amount = Coin.parseCoin("0.1");
        Fiat fiat = Fiat.parseFiat("EUR", "500");
        FiatPrice fiatPrice = new FiatPrice(fiat);
        assertEquals(Fiat.parseFiat("EUR", "50"), fiatPrice.getVolume(amount));
    }

    @Test
    public void testFiatPriceGetPriceAsString() {
        Fiat fiat = Fiat.parseFiat("EUR", "500.1234");
        FiatPrice fiatPrice = new FiatPrice(fiat);
        assertEquals("500.1234", fiatPrice.getPriceAsString());
    }

    @Test
    public void testAltcoinPriceGetPriceAsString() {
        Coin btcPerEth = Coin.parseCoin("0.12345678");
        AltcoinPrice altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        assertEquals("0.12345678", altcoinPrice.getPriceAsString());
    }

    @Test
    public void testFiatPriceGetPriceAsLong() {
        Fiat fiat = Fiat.parseFiat("EUR", "500.1234");
        FiatPrice fiatPrice = new FiatPrice(fiat);
        assertEquals(5001234, fiatPrice.getPriceAsLong());
    }

    @Test
    public void testAltcoinPriceGetPriceAsLong() {
        Coin btcPerEth = Coin.parseCoin("10.12345678");
        AltcoinPrice altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        assertEquals(1012345678, altcoinPrice.getPriceAsLong());

        btcPerEth = Coin.parseCoin("0.02");
        altcoinPrice = new AltcoinPrice("ETH", btcPerEth);
        log.error(btcPerEth.toPlainString());
        log.error(btcPerEth.toFriendlyString());
        assertEquals(50, altcoinPrice.getInvertedPriceAsLong());
    }

    @Test
    public void testAltcoinPriceGetVolume() {
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
