package io.bitsquare.trade;

import com.google.common.math.LongMath;
import org.bitcoinj.core.Monetary;
import org.bitcoinj.utils.MonetaryFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Represents an Altcoin value. This class is immutable.
 */
public class Altcoin1 implements Monetary, Comparable<Altcoin1>, Serializable {
    private static final Logger log = LoggerFactory.getLogger(Altcoin1.class);

    public final String currencyCode;

    /**
     * Number of decimals for one Bitcoin. This constant is useful for quick adapting to other coins because a lot of
     * constants derive from it.
     */
    public static final int SMALLEST_UNIT_EXPONENT = 8;
    public static final long COIN_VALUE = LongMath.pow(10, SMALLEST_UNIT_EXPONENT);
    public final long value;

    private Altcoin1(final String currencyCode, final long value) {
        this.value = value;
        this.currencyCode = currencyCode;
    }

    public static Altcoin1 valueOf(final String currencyCode, final long value) {
        return new Altcoin1(currencyCode, value);
    }

    @Override
    public int smallestUnitExponent() {
        return SMALLEST_UNIT_EXPONENT;
    }

    /**
     * Returns the number of value of this monetary value.
     */
    @Override
    public long getValue() {
        return value;
    }

    /**
     * Parses an amount expressed in the way humans are used to.<p>
     * <p>
     * This takes string in a format understood by {@link BigDecimal#BigDecimal(String)},
     * for example "0", "1", "0.10", "1.23E3", "1234.5E-5".
     *
     * @throws IllegalArgumentException if you try to specify fractional value, or a value out of range.
     */
    public static Altcoin1 parseCoin(final String currencyCode, final String str) {
        try {
            long value = new BigDecimal(str).movePointRight(SMALLEST_UNIT_EXPONENT).toBigIntegerExact().longValue();
            return Altcoin1.valueOf(currencyCode, value);
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(e); // Repackage exception to honor method contract
        }
    }

    public Altcoin1 add(final String currencyCode, final Altcoin1 value) {
        return new Altcoin1(currencyCode, LongMath.checkedAdd(this.value, value.value));
    }

    public Altcoin1 subtract(final String currencyCode, final Altcoin1 value) {
        return new Altcoin1(currencyCode, LongMath.checkedSubtract(this.value, value.value));
    }

    public Altcoin1 multiply(final String currencyCode, final long factor) {
        return new Altcoin1(currencyCode, LongMath.checkedMultiply(this.value, factor));
    }

    public Altcoin1 divide(final String currencyCode, final long divisor) {
        return new Altcoin1(currencyCode, this.value / divisor);
    }

    public Altcoin1[] divideAndRemainder(final String currencyCode, final long divisor) {
        return new Altcoin1[]{new Altcoin1(currencyCode, this.value / divisor), new Altcoin1(currencyCode, this.value % divisor)};
    }

    public long divide(final String currencyCode, final Altcoin1 divisor) {
        return this.value / divisor.value;
    }

    /**
     * Returns true if and only if this instance represents a monetary value greater than zero,
     * otherwise false.
     */
    public boolean isPositive() {
        return signum() == 1;
    }

    /**
     * Returns true if and only if this instance represents a monetary value less than zero,
     * otherwise false.
     */
    public boolean isNegative() {
        return signum() == -1;
    }

    /**
     * Returns true if and only if this instance represents zero monetary value,
     * otherwise false.
     */
    public boolean isZero() {
        return signum() == 0;
    }

    /**
     * Returns true if the monetary value represented by this instance is greater than that
     * of the given other Altcoin, otherwise false.
     */
    public boolean isGreaterThan(Altcoin1 other) {
        return compareTo(other) > 0;
    }

    /**
     * Returns true if the monetary value represented by this instance is less than that
     * of the given other Altcoin, otherwise false.
     */
    public boolean isLessThan(Altcoin1 other) {
        return compareTo(other) < 0;
    }

    public Altcoin1 shiftLeft(final String currencyCode, final int n) {
        return new Altcoin1(currencyCode, this.value << n);
    }

    public Altcoin1 shiftRight(final String currencyCode, final int n) {
        return new Altcoin1(currencyCode, this.value >> n);
    }

    @Override
    public int signum() {
        if (this.value == 0)
            return 0;
        return this.value < 0 ? -1 : 1;
    }

    public Altcoin1 negate() {
        return new Altcoin1(currencyCode, -this.value);
    }

    /**
     * Returns the number of value of this monetary value. It's deprecated in favour of accessing {@link #value}
     * directly.
     */
    public long longValue() {
        return this.value;
    }

    private static final MonetaryFormat FRIENDLY_FORMAT = MonetaryFormat.BTC.minDecimals(2).repeatOptionalDecimals(1, 6).postfixCode();

    /**
     * Returns the value as a 0.12 type string. More digits after the decimal place will be used
     * if necessary, but two will always be present.
     */
    public String toFriendlyString() {
        return FRIENDLY_FORMAT.format(this).toString();
    }

    private static final MonetaryFormat PLAIN_FORMAT = MonetaryFormat.BTC.minDecimals(0).repeatOptionalDecimals(1, 8).noCode();

    /**
     * <p>
     * Returns the value as a plain string denominated in BTC.
     * The result is unformatted with no trailing zeroes.
     * For instance, a value of 150000 value gives an output string of "0.0015" BTC
     * </p>
     */
    public String toPlainString() {
        return PLAIN_FORMAT.format(this).toString();
    }

    @Override
    public String toString() {
        return Long.toString(value);
    }

    @Override
    public boolean equals(final Object o) {
        if (o == this)
            return true;
        if (o == null || o.getClass() != getClass())
            return false;
        final Altcoin1 other = (Altcoin1) o;
        if (this.value != other.value)
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        return (int) this.value;
    }

    @Override
    public int compareTo(final Altcoin1 other) {
        if (this.value == other.value)
            return 0;
        return this.value > other.value ? 1 : -1;
    }
}
