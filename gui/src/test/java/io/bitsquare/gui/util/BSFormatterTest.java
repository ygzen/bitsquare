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

import org.bitcoinj.core.Coin;
import org.junit.Test;

import java.util.Locale;

import static org.junit.Assert.*;

public class BSFormatterTest {

    @Test
    public void testParseToBtc() {
        BSFormatter formatter = new BSFormatter();
        formatter.useMilliBitFormat(false);
        assertEquals(Coin.ZERO, formatter.parseToBitcoin("0"));
        assertEquals(Coin.COIN, formatter.parseToBitcoin("1"));
        assertEquals(Coin.SATOSHI, formatter.parseToBitcoin("0,00000001"));

        assertEquals(Coin.parseCoin("-1"), formatter.parseToBitcoin("-1"));
        assertEquals(Coin.parseCoin("1.1"), formatter.parseToBitcoin("1,1"));
        assertEquals(Coin.parseCoin("1.1"), formatter.parseToBitcoin("1.1"));
        assertEquals(Coin.parseCoin("0"), formatter.parseToBitcoin("1.123,45"));
        assertEquals(Coin.parseCoin("0"), formatter.parseToBitcoin("1,123.45"));

        assertEquals(Coin.parseCoin("1.1234"), formatter.parseToBitcoinWith4Decimals("1,12342"));
        assertEquals(Coin.parseCoin("1.1235"), formatter.parseToBitcoinWith4Decimals("1,12345"));
        assertEquals(Coin.parseCoin("1.1230"), formatter.parseToBitcoinWith4Decimals("1,123"));

        // change to mBTC
        formatter.useMilliBitFormat(true);
        assertEquals(Coin.parseCoin("1"), formatter.parseToBitcoin("1000"));
        assertEquals(Coin.parseCoin("0.123"), formatter.parseToBitcoin("123"));
        assertEquals(Coin.parseCoin("0.1234"), formatter.parseToBitcoin("123.4"));
        assertEquals(Coin.parseCoin("0.12345"), formatter.parseToBitcoin("123.45"));
        assertEquals(Coin.parseCoin("0.123456"), formatter.parseToBitcoin("123.456"));
        assertEquals(Coin.parseCoin("0"), formatter.parseToBitcoin("123,456.7"));

        assertEquals(Coin.parseCoin("0.001123"), formatter.parseToBitcoinWith4Decimals("1.123"));
        assertEquals(Coin.parseCoin("0.0011234"), formatter.parseToBitcoinWith4Decimals("1.1234"));
        assertEquals(Coin.parseCoin("0.0011234"), formatter.parseToBitcoinWith4Decimals("1.12342"));
        assertEquals(Coin.parseCoin("0.0011235"), formatter.parseToBitcoinWith4Decimals("1.12345"));
    }

    @Test
    public void testFormatCoin() {
        BSFormatter formatter = new BSFormatter();
        formatter.useMilliBitFormat(false);
        assertEquals("1.00", formatter.formatCoin(Coin.COIN));
        assertEquals("1.0120", formatter.formatCoin(Coin.parseCoin("1.012")));
        assertEquals("1012.30", formatter.formatCoin(Coin.parseCoin("1012.3")));
        assertEquals("1.0120", formatter.formatCoin(Coin.parseCoin("1.01200")));
        assertEquals("1.000123", formatter.formatCoin(Coin.parseCoin("1.0001234")));
        assertEquals("0.10", formatter.formatCoin(Coin.parseCoin("0.1")));
        assertEquals("0.01", formatter.formatCoin(Coin.parseCoin("0.01")));
        assertEquals("0.0010", formatter.formatCoin(Coin.parseCoin("0.001")));
        assertEquals("0.0001", formatter.formatCoin(Coin.parseCoin("0.0001")));

        assertEquals("0.000010", formatter.formatCoin(Coin.parseCoin("0.00001")));
        assertEquals("0.000001", formatter.formatCoin(Coin.parseCoin("0.000001")));
        assertEquals("0.00", formatter.formatCoin(Coin.parseCoin("0.0000001")));

        assertEquals("1.2345", formatter.formatCoin(Coin.parseCoin("1.2345")));
        assertEquals("1.002346", formatter.formatCoin(Coin.parseCoin("1.0023456")));
        assertEquals("1.002346", formatter.formatCoin(Coin.parseCoin("1.00234567")));
        assertEquals("1.002345", formatter.formatCoin(Coin.parseCoin("1.0023448")));

        assertEquals("1.00", formatter.formatCoin(Coin.COIN));
        assertEquals("1012.30", formatter.formatCoin(Coin.parseCoin("1012.3")));

        // change to mBTC
        formatter.useMilliBitFormat(true);
        assertEquals("1000.00", formatter.formatCoin(Coin.COIN));
        assertEquals("1.00", formatter.formatCoin(Coin.MILLICOIN));
        assertEquals("0.0010", formatter.formatCoin(Coin.MICROCOIN));
    }

    @Test
    public void testFormatCoinWithCode() {
        BSFormatter formatter = new BSFormatter();
        formatter.useMilliBitFormat(false);
        assertEquals("1.00 BTC", formatter.formatBitcoinWithCode(Coin.COIN));
        assertEquals("1.01 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.01")));
        assertEquals("1.0120 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.012")));
        assertEquals("1012.30 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1012.3")));
        assertEquals("1.0120 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.01200")));
        assertEquals("1.012340 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.01234")));
        assertEquals("1.012345 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.012345")));
        assertEquals("1.012345 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.0123454")));
        assertEquals("1.012346 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.0123455")));

        assertEquals("0.10 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("0.1")));
        assertEquals("0.01 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("0.01")));
        assertEquals("0.0010 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("0.001")));
        assertEquals("0.0001 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("0.0001")));

        assertEquals("1.2345 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.2345")));
        assertEquals("1.002346 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.0023456")));
        assertEquals("1.002346 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.00234567")));
        assertEquals("1.002345 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1.0023448")));

        assertEquals("1.00 BTC", formatter.formatBitcoinWithCode(Coin.COIN));
        assertEquals("1012.30 BTC", formatter.formatBitcoinWithCode(Coin.parseCoin("1012.3")));

        // change to mBTC
        formatter.useMilliBitFormat(true);
        assertEquals("1000.00 mBTC", formatter.formatBitcoinWithCode(Coin.COIN));
        assertEquals("1.00 mBTC", formatter.formatBitcoinWithCode(Coin.MILLICOIN));
        assertEquals("0.0010 mBTC", formatter.formatBitcoinWithCode(Coin.MICROCOIN));
    }


    @Test
    public void testParseToBtcWith4Decimals() {
        BSFormatter formatter = new BSFormatter();
        formatter.useMilliBitFormat(false);
        assertEquals(Coin.parseCoin("0"), formatter.parseToBitcoinWith4Decimals("0"));
        assertEquals(Coin.parseCoin("0"), formatter.parseToBitcoinWith4Decimals(null));
        assertEquals(Coin.parseCoin("0"), formatter.parseToBitcoinWith4Decimals("s"));
        assertEquals(Coin.parseCoin("0.0012"), formatter.parseToBitcoinWith4Decimals("0,00123"));
        assertEquals(Coin.parseCoin("0.0013"), formatter.parseToBitcoinWith4Decimals("0,00125"));
    }

    @Test
    public void testHasBtcValidDecimals() {
        BSFormatter formatter = new BSFormatter();
        formatter.useMilliBitFormat(false);
        formatter.setLocale(Locale.GERMAN);
        assertTrue(formatter.hasBitcoinValidDecimals(null));
        assertTrue(formatter.hasBitcoinValidDecimals("0"));
        assertTrue(formatter.hasBitcoinValidDecimals("0,0001"));
        assertTrue(formatter.hasBitcoinValidDecimals("0.0001"));
        assertTrue(formatter.hasBitcoinValidDecimals("0.0009"));
        assertTrue(formatter.hasBitcoinValidDecimals("20000000.0001"));
        assertFalse(formatter.hasBitcoinValidDecimals("20000000.000123"));
        assertFalse(formatter.hasBitcoinValidDecimals("0.00012"));
        assertFalse(formatter.hasBitcoinValidDecimals("0.0001222312312312313"));
    }

    @Test
    public void testParseToFiatWith2Decimals() {
        BSFormatter formatter = new BSFormatter();
        formatter.useMilliBitFormat(false);
        formatter.setLocale(Locale.GERMAN);
        assertEquals("0", formatter.parseToFiatWithDecimals("0", "EUR", 2).toPlainString());
        assertEquals("0", formatter.parseToFiatWithDecimals(null, "EUR", 2).toPlainString());
        assertEquals("0", formatter.parseToFiatWithDecimals("s", "EUR", 2).toPlainString());
        assertEquals("0.12", formatter.parseToFiatWithDecimals("0.123", "EUR", 2).toPlainString());
        assertEquals("0.13", formatter.parseToFiatWithDecimals("0.125", "EUR", 2).toPlainString());
        assertEquals("0.13", formatter.parseToFiatWithDecimals("0,125", "EUR", 2).toPlainString());
    }

    @Test
    public void testHasFiatValidDecimals() {
        BSFormatter formatter = new BSFormatter();
        formatter.useMilliBitFormat(false);
        formatter.setLocale(Locale.GERMAN);
        assertTrue(formatter.hasFiatValidDecimals(null, "EUR"));
        assertTrue(formatter.hasFiatValidDecimals("0", "EUR"));
        assertTrue(formatter.hasFiatValidDecimals("0,01", "EUR"));
        assertTrue(formatter.hasFiatValidDecimals("0.01", "EUR"));
        assertTrue(formatter.hasFiatValidDecimals("0.09", "EUR"));
        assertTrue(formatter.hasFiatValidDecimals("20000000.01", "EUR"));
        assertFalse(formatter.hasFiatValidDecimals("20000000.0123", "EUR"));
        assertFalse(formatter.hasFiatValidDecimals("0.012", "EUR"));
        assertFalse(formatter.hasFiatValidDecimals("0.01222312312312313", "EUR"));
    }

}
