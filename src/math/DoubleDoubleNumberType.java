package math;

import java.util.Arrays;
import java.util.TreeSet;

/**
 * double: 53 bits DoubleDoubleNumberType: >106 bits
 *
 * @author Zom-B
 * @author David, added functions, removed not needed for proj to reduce LOC
 * @since 1.0
 * @see http://crd.lbl.gov/~dhbailey/mpdist/index.html
 * @date 2006/10/22
 */
public strictfp class DoubleDoubleNumberType implements NumberType {

    public static final char[] BASE_36_TABLE = { //
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', //
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', //
        'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', //
        'U', 'V', 'W', 'X', 'Y', 'Z'};
    public static final char[] ZEROES = { //
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', //
        '0', '0', '0', '0', '0'};

    // public static final double MUL_SPLIT = 0x08000001;
    public static final double POSITIVE_INFINITY = Double.MAX_VALUE / 0x08000001;
    public static final double NEGATIVE_INFINITY = -DoubleDoubleNumberType.POSITIVE_INFINITY;
    public static final double HALF_EPSILON = 1.1102230246251565E-16;
    public static final double EPSILON = 1.232595164407831E-32;

    public static final DoubleDoubleNumberType ZERO = new DoubleDoubleNumberType(0);
    public static final DoubleDoubleNumberType ONE = new DoubleDoubleNumberType(1);
    public static final DoubleDoubleNumberType TEN = new DoubleDoubleNumberType(10);
    public static final int MAX_ZOOM = 29;
    public double hi;
    public double lo;

    // ***********************************************************************//
    // ************************ Creation functions ***************************//
    // ***********************************************************************//
    public DoubleDoubleNumberType() {
        this.hi = 0;
        this.lo = 0;
    }

    public DoubleDoubleNumberType(double d) {
        this.hi = d;
        this.lo = 0;
    }

    public DoubleDoubleNumberType(double hi, double lo) {
        this.hi = hi;
        this.lo = lo;
    }

    public DoubleDoubleNumberType(DoubleDoubleNumberType dd) {
        this.hi = dd.hi;
        this.lo = dd.lo;
    }

    public void set(double hi) {
        this.hi = hi;
        this.lo = 0;
    }

    public void set(double hi, double lo) {
        this.hi = hi;
        this.lo = lo;
    }

    public void set(DoubleDoubleNumberType dd) {
        this.hi = dd.hi;
        this.lo = dd.lo;
    }

    // ***********************************************************************//
    // ************************** Other functions ****************************//
    // ***********************************************************************//
    @Override
    public String toString() {
        if (this.hi != this.hi) {
            return "NaN";
        }
        if (this.hi >= DoubleDoubleNumberType.POSITIVE_INFINITY) {
            return "Infinity";
        }
        if (this.hi <= DoubleDoubleNumberType.NEGATIVE_INFINITY) {
            return "-Infinity";
        }
        return "dd" + DoubleDoubleNumberType.sciString(this, 10);
    }

    public static String sciString(DoubleDoubleNumberType dd, int base) {
        double digitsPerBit = StrictMath.log(2) / StrictMath.log(base);
        int minPrecision = (int) StrictMath.floor(105.0 * digitsPerBit + 2);

        // Get the precision. (The minimum number of significant digits required
        // for an accurate representation of this number)
        int expHi = (int) ((Double.doubleToRawLongBits(dd.hi) & 0x7FF0000000000000L) >> 52);
        int expLo = dd.lo == 0 ? expHi - 53 : (int) ((Double.doubleToRawLongBits(dd.lo) & 0x7FF0000000000000L) >> 52);
        int precision = (int) StrictMath.ceil((expHi - expLo + 53) * digitsPerBit);
        precision = StrictMath.max(minPrecision, precision);
        char[] chars = new char[precision + 1];
        int exp = DoubleDoubleNumberType.to_digits(dd, chars, precision, base) + 1;
        StringBuffer out = new StringBuffer(precision + 3 + (exp > 0 ? 1 : 2));

        out.append(chars, 0, 1);
        out.append('.');
        out.append(chars, 1, precision - 1);
        out.append('e');
        out.append(exp - 1);
        return out.toString();
    }

    /**
     * Format a string in an easily readable format. The number is represented
     * as scientific form on the following conditions: <br>
     * <ol>
     * <li>(for big numbers) When the first digit right of the decimal point
     * would not be within the first minPrecision positions of the string, <br>
     * <li>(for small numbers) When the most significant digit would not be
     * within the first minPrecision positions of the string
     * </ol>
     * <br>
     * Where: <code>minPrecision = floor(105 / log2(base) + 1)</code>
     */
    public static String toString(DoubleDoubleNumberType dd, int base) {
        double digitsPerBit = StrictMath.log(2) / StrictMath.log(base);
        int minPrecision = (int) StrictMath.floor(105.0 * digitsPerBit + 2);

        // Get the precision. (The minimum number of significant digits required
        // for an accurate representation of this number)
        int expHi = (int) ((Double.doubleToRawLongBits(dd.hi) & 0x7FF0000000000000L) >> 52);
        int expLo = dd.lo == 0 ? expHi - 53 : (int) ((Double.doubleToRawLongBits(dd.lo) & 0x7FF0000000000000L) >> 52);
        int precision = (int) StrictMath.ceil((expHi - expLo + 53) * digitsPerBit);
        precision = StrictMath.max(minPrecision, precision);

        // Get the raw digit representation.
        char[] chars = new char[precision + 1];
        int exp = DoubleDoubleNumberType.to_digits(dd, chars, precision, base) + 1;

        // Get some properties.
        int left = StrictMath.max(0, -exp);
        int right = StrictMath.max(0, exp);
        if (chars[precision - 1] == 0) {
            precision--;
        }
        boolean sci = -exp >= minPrecision || exp >= minPrecision;

        // Allocate exactly the right size string.
        StringBuffer out = new StringBuffer(precision + (sci ? 3 : left) + (exp > 0 ? 1 : 2));

        // Build the string.
        if (dd.hi < 0) {
            out.append('-');
        }
        if (sci) {
            out.append(chars, 0, 1);
            out.append('.');
            out.append(chars, 1, precision - 1);
            out.append('e');
            out.append(exp - 1);
        } else {
            if (exp <= 0) {
                out.append('0');
            }
            if (right > 0) {
                out.append(chars, 0, right);
            }
            out.append('.');
            if (left > 0) {
                if (DoubleDoubleNumberType.ZEROES.length < left) {
                    System.err.println(left);
                } else {
                    out.append(DoubleDoubleNumberType.ZEROES, 0, left);
                }
            }
            out.append(chars, right, precision - right);
        }
        return out.toString();
    }

    private static int to_digits(DoubleDoubleNumberType dd, char[] s, int precision, int base) {
        int halfBase = (base + 1) >> 1;

        if (dd.hi == 0.0) {
            // Assume dd.lo == 0.
            Arrays.fill(s, 0, precision, '0');
            return 0;
        }

        // First determine the (approximate) exponent.
        DoubleDoubleNumberType temp = dd.abs();
        int exp = (int) StrictMath.floor(StrictMath.log(temp.hi) / StrictMath.log(base));

        DoubleDoubleNumberType p = new DoubleDoubleNumberType(base);
        if (exp < -300) {
            temp.mulSelf(p.pow(150));
            p.powSelf(-exp - 150);
            temp.mulSelf(p);
        } else {
            p.powSelf(-exp);
            temp.mulSelf(p);
        }

        // Fix roundoff errors. (eg. floor(log10(1e9))=floor(8.9999~)=8)
        if (temp.hi >= base) {
            exp++;
            temp.hi /= base;
            temp.lo /= base;
        } else if (temp.hi < 1) {
            exp--;
            temp.hi *= base;
            temp.lo *= base;
        }

        if (temp.hi >= base || temp.hi < 1) {
            throw new RuntimeException("Can't compute exponent.");
        }

        // Handle one digit more. Used afterwards for rounding.
        int numDigits = precision + 1;
        // Extract the digits.
        for (int i = 0; i < numDigits; i++) {
            int val = (int) temp.hi;
            temp = temp.sub(val);
            temp = temp.mul(base);

            s[i] = (char) val;
        }

        if (s[0] <= 0) {
            throw new RuntimeException("Negative leading digit.");
        }

        // Fix negative digits due to roundoff error in exponent.
        for (int i = numDigits - 1; i > 0; i--) {
            if (s[i] >= 32768) {
                s[i - 1]--;
                s[i] += base;
            }
        }

        // Round, handle carry.
        if (s[precision] >= halfBase) {
            s[precision - 1]++;

            int i = precision - 1;
            while (i > 0 && s[i] >= base) {
                s[i] -= base;
                s[--i]++;
            }
        }
        s[precision] = 0;

        // If first digit became too high, shift right.
        if (s[0] >= base) {
            exp++;
            for (int i = precision; i >= 1;) {
                s[i] = s[--i];
            }
        }

        // Convert to ASCII.
        for (int i = 0; i < precision; i++) {
            s[i] = DoubleDoubleNumberType.BASE_36_TABLE[s[i]];
        }

        // If first digit became zero, and exp > 0, shift left.
        if (s[0] == '0' && exp < 32768) {
            exp--;
            for (int i = 0; i < precision;) {
                s[i] = s[++i];
            }
        }

        return exp;
    }

    // ***********************************************************************//
    // ************************ Temporary functions **************************//
    // ***********************************************************************//
    @Override
    public DoubleDoubleNumberType clone() {
        return new DoubleDoubleNumberType(this.hi, this.lo);
    }

    public DoubleDoubleNumberType normalize() {
        double s = this.hi + this.lo;
        return new DoubleDoubleNumberType(s, this.lo + (this.hi - s));
    }

    public void normalizeSelf() {
        double a = this.hi;
        this.hi = a + this.lo;
        this.lo = this.lo + (a - this.hi);
    }

    public int intValue() {
        int rhi = (int) StrictMath.round(this.hi);

        if (this.hi == rhi) {
            return rhi + (int) StrictMath.round(this.lo);
        }
        if (StrictMath.abs(rhi - this.hi) == 0.5 && this.lo < 0.0) {
            return rhi - 1;
        }
        return rhi;
    }

    public long longValue() {
        long rhi = StrictMath.round(this.hi);

        if (this.hi == rhi) {
            return rhi + StrictMath.round(this.lo);
        }
        if (StrictMath.abs(rhi - this.hi) == 0.5 && this.lo < 0.0) {
            return rhi - 1;
        }
        return rhi;
    }

    public static DoubleDoubleNumberType min(DoubleDoubleNumberType x, DoubleDoubleNumberType y) {
        if (x.hi < y.hi || (x.hi == y.hi && x.lo < y.lo)) {
            return x;
        }
        return y;
    }

    public static DoubleDoubleNumberType max(DoubleDoubleNumberType x, DoubleDoubleNumberType y) {
        if (x.hi > y.hi || (x.hi == y.hi && x.lo > y.lo)) {
            return x;
        }
        return y;
    }

    public static int sgn(double x) {
        if (x > 0) {
            return 1;
        }
        if (x < 0) {
            return -1;
        }
        return 0;
    }

    // ***********************************************************************//
    // ************************* Simple functions ****************************//
    // ***********************************************************************//
    public DoubleDoubleNumberType round() {
        DoubleDoubleNumberType out = new DoubleDoubleNumberType();

        double rhi = StrictMath.round(this.hi);

        if (this.hi == rhi) {
            double rlo = StrictMath.round(this.lo);
            out.hi = rhi + rlo;
            out.lo = rlo + (rhi - out.hi);
        } else {
            if (StrictMath.abs(rhi - this.hi) == 0.5 && this.lo < 0.0) {
                rhi--;
            }
            out.hi = rhi;
        }
        return out;
    }

    public void roundSelf() {
        double rhi = StrictMath.round(this.hi);

        if (this.hi == rhi) {
            double rlo = StrictMath.round(this.lo);
            this.hi = rhi + rlo;
            this.lo = rlo + (rhi - this.hi);
        } else {
            if (StrictMath.abs(rhi - this.hi) == 0.5 && this.lo < 0.0) {
                rhi--;
            }
            this.hi = rhi;
            this.lo = 0;
        }
    }

    public DoubleDoubleNumberType floor() {
        DoubleDoubleNumberType out = new DoubleDoubleNumberType();

        double rhi = StrictMath.floor(this.hi);

        if (this.hi == rhi) {
            double rlo = StrictMath.floor(this.lo);
            out.hi = rhi + rlo;
            out.lo = rlo + (rhi - out.hi);
        } else {
            out.hi = rhi;
        }
        return out;
    }

    public void floorSelf() {
        double rhi = StrictMath.floor(this.hi);

        if (this.hi == rhi) {
            double rlo = StrictMath.floor(this.lo);
            this.hi = rhi + rlo;
            this.lo = rlo + (rhi - this.hi);
        } else {
            this.hi = rhi;
            this.lo = 0;
        }
    }

    public DoubleDoubleNumberType ceil() {
        DoubleDoubleNumberType out = new DoubleDoubleNumberType();

        double rhi = StrictMath.ceil(this.hi);

        if (this.hi == rhi) {
            double rlo = StrictMath.ceil(this.lo);
            out.hi = rhi + rlo;
            out.lo = rlo + (rhi - out.hi);
        } else {
            out.hi = rhi;
        }
        return out;
    }

    public void ceilSelf() {
        double rhi = StrictMath.ceil(this.hi);

        if (this.hi == rhi) {
            double rlo = StrictMath.ceil(this.lo);
            this.hi = rhi + rlo;
            this.lo = rlo + (rhi - this.hi);
        } else {
            this.hi = rhi;
            this.lo = 0;
        }
    }

    public DoubleDoubleNumberType trunc() {
        DoubleDoubleNumberType out = new DoubleDoubleNumberType();

        double rhi = (long) (this.hi);

        if (this.hi == rhi) {
            double rlo = (long) (this.lo);
            out.hi = rhi + rlo;
            out.lo = rlo + (rhi - out.hi);
        } else {
            out.hi = rhi;
        }
        return out;
    }

    public void truncSelf() {
        double rhi = (long) (this.hi);

        if (this.hi == rhi) {
            double rlo = (long) (this.lo);
            this.hi = rhi + rlo;
            this.lo = rlo + (rhi - this.hi);
        } else {
            this.hi = rhi;
            this.lo = 0;
        }
    }

    // ***********************************************************************//
    // *********************** Calculation functions *************************//
    // ***********************************************************************//
    public DoubleDoubleNumberType neg() {
        return new DoubleDoubleNumberType(-this.hi, -this.lo);
    }

    public void negSelf() {
        this.hi = -this.hi;
        this.lo = -this.lo;
    }

    public DoubleDoubleNumberType abs() {
        if (this.hi < 0) {
            return new DoubleDoubleNumberType(-this.hi, -this.lo);
        }
        return new DoubleDoubleNumberType(this.hi, this.lo);
    }

    public void absSelf() {
        if (this.hi < 0) {
            this.hi = -this.hi;
            this.lo = -this.lo;
        }
    }

    public DoubleDoubleNumberType add(double y) {
        double a, b, c;
        b = this.hi + y;
        a = this.hi - b;
        c = ((this.hi - (b + a)) + (y + a)) + this.lo;
        a = b + c;
        return new DoubleDoubleNumberType(a, c + (b - a));
    }

    public void addSelf(double y) {
        double a, b;
        b = this.hi + y;
        a = this.hi - b;
        this.lo = ((this.hi - (b + a)) + (y + a)) + this.lo;
        this.hi = b + this.lo;
        this.lo += b - this.hi;
    }

    public DoubleDoubleNumberType add(DoubleDoubleNumberType y) {
        double a, b, c, d, e, f;
        e = this.hi + y.hi;
        d = this.hi - e;
        a = this.lo + y.lo;
        f = this.lo - a;
        d = ((this.hi - (d + e)) + (d + y.hi)) + a;
        b = e + d;
        c = ((this.lo - (f + a)) + (f + y.lo)) + (d + (e - b));
        a = b + c;
        return new DoubleDoubleNumberType(a, c + (b - a));
    }

    public void addSelf(DoubleDoubleNumberType y) {
        double a, b, c, d, e;
        a = this.hi + y.hi;
        b = this.hi - a;
        c = this.lo + y.lo;
        d = this.lo - c;
        b = ((this.hi - (b + a)) + (b + y.hi)) + c;
        e = a + b;
        this.lo = ((this.lo - (d + c)) + (d + y.lo)) + (b + (a - e));
        this.hi = e + this.lo;
        this.lo += e - this.hi;
    }

    public DoubleDoubleNumberType addFast(DoubleDoubleNumberType y) {
        double a, b, c;
        b = this.hi + y.hi;
        a = this.hi - b;
        c = ((this.hi - (a + b)) + (a + y.hi)) + (this.lo + y.lo);
        a = b + c;
        return new DoubleDoubleNumberType(a, c + (b - a));
    }

    public void addSelfFast(DoubleDoubleNumberType y) {
        double a, b;
        b = this.hi + y.hi;
        a = this.hi - b;
        this.lo = ((this.hi - (a + b)) + (a + y.hi)) + (this.lo + y.lo);
        this.hi = b + this.lo;
        this.lo += b - this.hi;
    }

    public DoubleDoubleNumberType sub(double y) {
        double a, b, c;
        b = this.hi - y;
        a = this.hi - b;
        c = ((this.hi - (a + b)) + (a - y)) + this.lo;
        a = b + c;
        return new DoubleDoubleNumberType(a, c + (b - a));
    }

    public DoubleDoubleNumberType subR(double x) {
        double a, b, c;
        b = x - this.hi;
        a = x - b;
        c = ((x - (a + b)) + (a - this.hi)) - this.lo;
        a = b + c;
        return new DoubleDoubleNumberType(a, c + (b - a));
    }

    public void subSelf(double y) {
        double a, b;
        b = this.hi - y;
        a = this.hi - b;
        this.lo = ((this.hi - (a + b)) + (a - y)) + this.lo;
        this.hi = b + this.lo;
        this.lo += b - this.hi;
    }

    public void subRSelf(double x) {
        double a, b;
        b = x - this.hi;
        a = x - b;
        this.lo = ((x - (a + b)) + (a - this.hi)) - this.lo;
        this.hi = b + this.lo;
        this.lo += b - this.hi;
    }

    public DoubleDoubleNumberType sub(DoubleDoubleNumberType y) {
        double a, b, c, d, e, f, g;
        g = this.lo - y.lo;
        f = this.lo - g;
        e = this.hi - y.hi;
        d = this.hi - e;
        d = ((this.hi - (d + e)) + (d - y.hi)) + g;
        b = e + d;
        c = (d + (e - b)) + ((this.lo - (f + g)) + (f - y.lo));
        a = b + c;
        return new DoubleDoubleNumberType(a, c + (b - a));
    }

    public void subSelf(DoubleDoubleNumberType y) {
        double a, b, c, d, e;
        c = this.lo - y.lo;
        a = this.lo - c;
        e = this.hi - y.hi;
        d = this.hi - e;
        d = ((this.hi - (d + e)) + (d - y.hi)) + c;
        b = e + d;
        this.lo = (d + (e - b)) + ((this.lo - (a + c)) + (a - y.lo));
        this.hi = b + this.lo;
        this.lo += b - this.hi;
    }

    public DoubleDoubleNumberType subR(DoubleDoubleNumberType y) {
        double a, b, c, d, e, f, g;
        g = y.lo - this.lo;
        f = y.lo - g;
        e = y.hi - this.hi;
        d = y.hi - e;
        d = ((y.hi - (d + e)) + (d - this.hi)) + g;
        b = e + d;
        c = (d + (e - b)) + ((y.lo - (f + g)) + (f - this.lo));
        a = b + c;
        return new DoubleDoubleNumberType(a, c + (b - a));
    }

    public void subRSelf(DoubleDoubleNumberType y) {
        double b, d, e, f, g;
        g = y.lo - this.lo;
        f = y.lo - g;
        e = y.hi - this.hi;
        d = y.hi - e;
        d = ((y.hi - (d + e)) + (d - this.hi)) + g;
        b = e + d;
        this.lo = (d + (e - b)) + ((y.lo - (f + g)) + (f - this.lo));
        this.hi = b + this.lo;
        this.lo = this.lo + (b - this.hi);
    }

    public DoubleDoubleNumberType subFast(DoubleDoubleNumberType y) {
        double a, b, c;
        b = this.hi - y.hi;
        a = this.hi - b;
        c = (((this.hi - (a + b)) + (a - y.hi)) + this.lo) - y.lo;
        a = b + c;
        return new DoubleDoubleNumberType(a, c + (b - a));
    }

    public void subSelfFast(DoubleDoubleNumberType y) {
        double a, b;
        b = this.hi - y.hi;
        a = this.hi - b;
        this.lo = (((this.hi - (a + b)) + (a - y.hi)) + this.lo) - y.lo;
        this.hi = b + this.lo;
        this.lo += b - this.hi;
    }

    public DoubleDoubleNumberType mulPwrOf2(double y) {
        return new DoubleDoubleNumberType(this.hi * y, this.lo * y);
    }

    public void mulSelfPwrOf2(double y) {
        this.hi *= y;
        this.lo *= y;
    }

    public DoubleDoubleNumberType mul(double y) {
        double a, b, c, d, e;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = this.hi - a;
        c = 0x08000001 * y;
        c += y - c;
        d = y - c;
        e = this.hi * y;
        c = (((a * c - e) + (a * d + b * c)) + b * d) + this.lo * y;
        a = e + c;
        return new DoubleDoubleNumberType(a, c + (e - a));
    }

    public void mulSelf(double y) {
        double a, b, c, d, e;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = this.hi - a;
        c = 0x08000001 * y;
        c += y - c;
        d = y - c;
        e = this.hi * y;
        this.lo = (((a * c - e) + (a * d + b * c)) + b * d) + this.lo * y;
        this.hi = e + this.lo;
        this.lo += e - this.hi;
    }

    public DoubleDoubleNumberType mul(DoubleDoubleNumberType y) {
        double a, b, c, d, e;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = this.hi - a;
        c = 0x08000001 * y.hi;
        c += y.hi - c;
        d = y.hi - c;
        e = this.hi * y.hi;
        c = (((a * c - e) + (a * d + b * c)) + b * d) + (this.lo * y.hi + this.hi * y.lo);
        a = e + c;
        return new DoubleDoubleNumberType(a, c + (e - a));
    }

    public void mulSelf(DoubleDoubleNumberType y) {
        double a, b, c, d, e;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = this.hi - a;
        c = 0x08000001 * y.hi;
        c += y.hi - c;
        d = y.hi - c;
        e = this.hi * y.hi;
        this.lo = (((a * c - e) + (a * d + b * c)) + b * d) + (this.lo * y.hi + this.hi * y.lo);
        this.hi = e + this.lo;
        this.lo += e - this.hi;
    }

    public DoubleDoubleNumberType divPwrOf2(double y) {
        return new DoubleDoubleNumberType(this.hi / y, this.lo / y);
    }

    public void divSelfPwrOf2(double y) {
        this.hi /= y;
        this.lo /= y;
    }

    public DoubleDoubleNumberType div(double y) {
        double a, b, c, d, e, f, g, h;
        f = this.hi / y;
        a = 0x08000001 * f;
        a += f - a;
        b = f - a;
        c = 0x08000001 * y;
        c += y - c;
        d = y - c;
        e = f * y;
        g = this.hi - e;
        h = this.hi - g;
        b = (g + ((((this.hi - (h + g)) + (h - e)) + this.lo) - (((a * c - e) + (a * d + b * c)) + b * d))) / y;
        a = f + b;
        return new DoubleDoubleNumberType(a, b + (f - a));
    }

    public void divSelf(double y) {
        double a, b, c, d, e, f, g, h;
        f = this.hi / y;
        a = 0x08000001 * f;
        a += f - a;
        b = f - a;
        c = 0x08000001 * y;
        c += y - c;
        d = y - c;
        e = f * y;
        g = this.hi - e;
        h = this.hi - g;
        this.lo = (g + ((((this.hi - (h + g)) + (h - e)) + this.lo) - (((a * c - e) + (a * d + b * c)) + b * d))) / y;
        this.hi = f + this.lo;
        this.lo += f - this.hi;
    }

    public DoubleDoubleNumberType divr(double y) {
        double a, b, c, d, e, f;
        f = y / this.hi;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = this.hi - a;
        c = 0x08000001 * f;
        c += f - c;
        d = f - c;
        e = this.hi * f;
        b = ((y - e) - ((((a * c - e) + (a * d + b * c)) + b * d) + this.lo * f)) / this.hi;
        a = f + b;
        return new DoubleDoubleNumberType(a, b + (f - a));
    }

    public void divrSelf(double y) {
        double a, b, c, d, e, f;
        f = y / this.hi;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = this.hi - a;
        c = 0x08000001 * f;
        c += f - c;
        d = f - c;
        e = this.hi * f;
        this.lo = ((y - e) - ((((a * c - e) + (a * d + b * c)) + b * d) + this.lo * f)) / this.hi;
        this.hi = f + this.lo;
        this.lo += f - this.hi;
    }

    public DoubleDoubleNumberType div(DoubleDoubleNumberType y) {
        double a, b, c, d, e, f, g;
        f = this.hi / y.hi;
        a = 0x08000001 * y.hi;
        a += y.hi - a;
        b = y.hi - a;
        c = 0x08000001 * f;
        c += f - c;
        d = f - c;
        e = y.hi * f;
        c = (((a * c - e) + (a * d + b * c)) + b * d) + y.lo * f;
        b = this.lo - c;
        d = this.lo - b;
        a = this.hi - e;
        e = (this.hi - ((this.hi - a) + a)) + b;
        g = a + e;
        e += (a - g) + ((this.lo - (d + b)) + (d - c));
        a = g + e;
        b = a / y.hi;
        f += (e + (g - a)) / y.hi;
        a = f + b;
        return new DoubleDoubleNumberType(a, b + (f - a));
    }

    public void divSelf(DoubleDoubleNumberType y) {
        double a, b, c, d, e, f, g;
        f = this.hi / y.hi;
        a = 0x08000001 * y.hi;
        a += y.hi - a;
        b = y.hi - a;
        c = 0x08000001 * f;
        c += f - c;
        d = f - c;
        e = y.hi * f;
        c = (((a * c - e) + (a * d + b * c)) + b * d) + y.lo * f;
        b = this.lo - c;
        d = this.lo - b;
        a = this.hi - e;
        e = (this.hi - ((this.hi - a) + a)) + b;
        g = a + e;
        e += (a - g) + ((this.lo - (d + b)) + (d - c));
        a = g + e;
        this.lo = a / y.hi;
        f += (e + (g - a)) / y.hi;
        this.hi = f + this.lo;
        this.lo += f - this.hi;
    }

    public DoubleDoubleNumberType divFast(DoubleDoubleNumberType y) {
        double a, b, c, d, e, f, g;
        f = this.hi / y.hi;
        a = 0x08000001 * y.hi;
        a += y.hi - a;
        b = y.hi - a;
        c = 0x08000001 * f;
        c += f - c;
        d = f - c;
        e = y.hi * f;
        b = (((a * c - e) + (a * d + b * c)) + b * d) + y.lo * f;
        a = e + b;
        c = this.hi - a;
        g = (c + ((((this.hi - c) - a) - ((e - a) + b)) + this.lo)) / y.hi;
        a = f + g;
        return new DoubleDoubleNumberType(a, g + (f - a));
    }

    public void divSelfFast(DoubleDoubleNumberType y) {
        double a, b, c, d, e, f;
        f = this.hi / y.hi;
        a = 0x08000001 * y.hi;
        a += y.hi - a;
        b = y.hi - a;
        c = 0x08000001 * f;
        c += f - c;
        d = f - c;
        e = y.hi * f;
        b = (((a * c - e) + (a * d + b * c)) + b * d) + y.lo * f;
        a = e + b;
        c = this.hi - a;
        this.lo = (c + ((((this.hi - c) - a) - ((e - a) + b)) + this.lo)) / y.hi;
        this.hi = f + this.lo;
        this.lo += f - this.hi;
    }

    public DoubleDoubleNumberType recip() {
        double a, b, c, d, e, f;
        f = 1 / this.hi;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = this.hi - a;
        c = 0x08000001 * f;
        c += f - c;
        d = f - c;
        e = this.hi * f;
        b = ((1 - e) - ((((a * c - e) + (a * d + b * c)) + b * d) + this.lo * f)) / this.hi;
        a = f + b;
        return new DoubleDoubleNumberType(a, b + (f - a));
    }

    public void recipSelf() {
        double a, b, c, d, e, f;
        f = 1 / this.hi;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = this.hi - a;
        c = 0x08000001 * f;
        c += f - c;
        d = f - c;
        e = this.hi * f;
        this.lo = ((1 - e) - ((((a * c - e) + (a * d + b * c)) + b * d) + this.lo * f)) / this.hi;
        this.hi = f + this.lo;
        this.lo += f - this.hi;
    }

    public DoubleDoubleNumberType sqr() {
        double a, b, c;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = this.hi - a;
        c = this.hi * this.hi;
        b = ((((a * a - c) + a * b * 2) + b * b) + this.hi * this.lo * 2) + this.lo * this.lo;
        a = b + c;
        return new DoubleDoubleNumberType(a, b + (c - a));
    }

    public void sqrSelf() {
        double a, b, c;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = this.hi - a;
        c = this.hi * this.hi;
        this.lo = ((((a * a - c) + a * b * 2) + b * b) + this.hi * this.lo * 2) + this.lo * this.lo;
        this.hi = c + this.lo;
        this.lo += c - this.hi;
    }

    public DoubleDoubleNumberType sqrt() {
        if (this.hi == 0 && this.lo == 0) {
            return new DoubleDoubleNumberType();
        }

        double a, b, c, d, e, f, g, h;
        g = 1 / StrictMath.sqrt(this.hi);
        h = this.hi * g;
        g *= 0.5;
        a = 0x08000001 * h;
        a += h - a;
        b = h - a;
        c = h * h;
        b = ((a * a - c) + a * b * 2) + b * b;
        a = this.lo - b;
        f = this.lo - a;
        e = this.hi - c;
        d = this.hi - e;
        d = ((this.hi - (d + e)) + (d - c)) + a;
        c = e + d;
        b = (d + (e - c)) + ((this.lo - (f + a)) + (f - b));
        a = c + b;
        b += (c - a);
        c = 0x08000001 * a;
        c += a - c;
        d = a - c;
        e = 0x08000001 * g;
        e += g - e;
        f = g - e;
        a = a * g;
        e = ((c * e - a) + (c * f + d * e)) + d * f;
        e += b * g;
        b = a + e;
        e += a - b;
        f = b + h;
        c = b - f;
        return new DoubleDoubleNumberType(f, e + ((b - (f + c)) + (h + c)));
    }

    public void sqrtSelf() {
        if (this.hi == 0 && this.lo == 0) {
            return;
        }

        double a, b, c, d, e, f, g, h;
        g = 1 / StrictMath.sqrt(this.hi);
        h = this.hi * g;
        g *= 0.5;
        a = 0x08000001 * h;
        a += h - a;
        b = h - a;
        c = h * h;
        b = ((a * a - c) + a * b * 2) + b * b;
        a = this.lo - b;
        f = this.lo - a;
        e = this.hi - c;
        d = this.hi - e;
        d = ((this.hi - (d + e)) + (d - c)) + a;
        c = e + d;
        b = (d + (e - c)) + ((this.lo - (f + a)) + (f - b));
        a = c + b;
        b += (c - a);
        c = 0x08000001 * a;
        c += a - c;
        d = a - c;
        e = 0x08000001 * g;
        e += g - e;
        f = g - e;
        a = a * g;
        e = ((c * e - a) + (c * f + d * e)) + d * f;
        e += b * g;
        b = a + e;
        e += a - b;
        this.hi = b + h;
        c = b - this.hi;
        this.lo = e + ((b - (this.hi + c)) + (h + c));
    }

    public DoubleDoubleNumberType sqrtFast() {
        if (this.hi == 0 && this.lo == 0) {
            return new DoubleDoubleNumberType();
        }

        double a, b, c, d, e;
        d = 1 / StrictMath.sqrt(this.hi);
        e = this.hi * d;
        a = 0x08000001 * e;
        a += e - a;
        b = e - a;
        c = e * e;
        b = ((a * a - c) + a * b * 2) + b * b;
        a = this.hi - c;
        c = this.hi - a;
        c = (a + ((((this.hi - (c + a)) + (c - c)) + this.lo) - b)) * d * 0.5;
        a = e + c;
        b = e - a;
        return new DoubleDoubleNumberType(a, (e - (b + a)) + (b + c));
    }

    public void sqrtSelfFast() {
        if (this.hi == 0 && this.lo == 0) {
            return;
        }

        double a, b, c, d, e;
        d = 1 / StrictMath.sqrt(this.hi);
        e = this.hi * d;
        a = 0x08000001 * e;
        a += e - a;
        b = e - a;
        c = e * e;
        b = ((a * a - c) + a * b * 2) + b * b;
        a = this.hi - c;
        c = this.hi - a;
        c = (a + ((((this.hi - (c + a)) + (c - c)) + this.lo) - b)) * d * 0.5;
        this.hi = e + c;
        b = e - this.hi;
        this.lo = (e - (b + this.hi)) + (b + c);
    }

    // Devil's values:
    // 0.693147180559945309417232121458174
    // 1.03972077083991796412584818218727
    // 1.03972077083991796312584818218727
    public DoubleDoubleNumberType exp() {
        if (this.hi > 691.067739) {
            return new DoubleDoubleNumberType(Double.POSITIVE_INFINITY);
        }

        double a, b, c, d, e, f, g = 0.5, h = 0, i, j, k, l, m, n, o, p, q = 2, r = 1;
        int s;

        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = a - this.hi;
        c = this.hi * 1.4426950408889634;
        b = (((a * 1.4426950514316559 - c) - (b * 1.4426950514316559 + a * 1.0542692496784412E-8)) + b * 1.0542692496784412E-8)
                + (this.lo * 1.4426950408889634 + this.hi * 2.0355273740931033E-17);
        s = (int) StrictMath.round(c);
        if (c == s) {
            s += (int) StrictMath.round(b);
        } else if (StrictMath.abs(s - c) == 0.5 && b < 0.0) {
            s--;
        }
        e = 0.6931471805599453 * s;
        c = ((s * 0.6931471824645996 - e) - (s * 1.904654323148236E-9)) + 2.3190468138462996E-17 * s;
        b = this.lo - c;
        d = this.lo - b;
        e = this.hi - e;
        a = e + b;
        b = ((this.lo - (d + b)) + (d - c)) + (b + (e - a));
        e = a + 1;
        c = a - e;
        d = ((a - (e + c)) + (1 + c)) + b;
        c = e + d;
        d += e - c;
        e = 0x08000001 * a;
        e += a - e;
        f = a - e;
        i = a * a;
        f = ((e * e - i) + e * f * 2) + f * f;
        f += a * b * 2;
        f += b * b;
        e = f + i;
        f += i - e;
        i = e * g;
        j = f * g;
        do {
            k = d + j;
            l = d - k;
            m = c + i;
            n = c - m;
            n = ((c - (n + m)) + (n + i)) + k;
            o = m + n;
            d = (n + (m - o)) + ((d - (l + k)) + (l + j));
            c = o + d;
            d += o - c;
            k = 0x08000001 * e;
            k += e - k;
            l = e - k;
            m = 0x08000001 * a;
            m += a - m;
            n = a - m;
            o = e * a;
            f = (((k * m - o) + (k * n + l * m)) + l * n) + (f * a + e * b);
            e = o + f;
            f += o - e;
            n = g / ++q;
            k = 0x08000001 * n;
            k += n - k;
            l = n - k;
            m = n * q;
            o = g - m;
            p = g - o;
            h = (o + ((((g - (p + o)) + (p - m)) + h) - (((k * q - m) + l * q)))) / q;
            g = n;
            i = 0x08000001 * e;
            i += e - i;
            k = e - i;
            j = 0x08000001 * g;
            j += g - j;
            l = g - j;
            m = e * g;
            j = (((i * j - m) + (i * l + k * j)) + k * l) + (f * g + e * h);
            i = m + j;
            j += m - i;
        } while (i > 1e-40 || i < -1e-40);
        if (s < 0) {
            s = -s;
            a = 0.5;
        } else {
            a = 2;
        }
        while (s > 0) {
            if ((s & 1) > 0) {
                r *= a;
            }
            a *= a;
            s >>= 1;
        }
        a = d + j;
        b = d - a;
        e = c + i;
        f = c - e;
        f = ((c - (f + e)) + (f + i)) + a;
        c = e + f;
        d = (f + (e - c)) + ((d - (b + a)) + (b + j));
        return new DoubleDoubleNumberType(c * r, d * r);
    }

    public void expSelf() {
        if (this.hi > 691.067739) {
            this.hi = Double.POSITIVE_INFINITY;
            return;
        }

        double a, b, c, d, e, f, g = 0.5, h = 0, i, j, k, l, m, n, o, p, q = 2, r = 1;
        int s;

        a = 0x08000001 * this.hi;
        a += this.hi - a;
        b = a - this.hi;
        c = this.hi * 1.4426950408889634;
        b = (((a * 1.4426950514316559 - c) - (b * 1.4426950514316559 + a * 1.0542692496784412E-8)) + b * 1.0542692496784412E-8)
                + (this.lo * 1.4426950408889634 + this.hi * 2.0355273740931033E-17);
        s = (int) StrictMath.round(c);
        if (c == s) {
            s += (int) StrictMath.round(b);
        } else if (StrictMath.abs(s - c) == 0.5 && b < 0.0) {
            s--;
        }
        e = 0.6931471805599453 * s;
        c = ((s * 0.6931471824645996 - e) - (s * 1.904654323148236E-9)) + 2.3190468138462996E-17 * s;
        b = this.lo - c;
        d = this.lo - b;
        e = this.hi - e;
        a = e + b;
        b = ((b + (e - a)) + ((this.lo - (d + b)) + (d - c)));
        e = a + 1;
        c = a - e;
        d = ((a - (e + c)) + (1 + c)) + b;
        c = e + d;
        d += e - c;
        e = 0x08000001 * a;
        e += a - e;
        f = a - e;
        i = a * a;
        f = ((e * e - i) + e * f * 2) + f * f;
        f += a * b * 2;
        f += b * b;
        e = f + i;
        f += i - e;
        i = e * g;
        j = f * g;
        do {
            k = d + j;
            l = d - k;
            m = c + i;
            n = c - m;
            n = ((c - (n + m)) + (n + i)) + k;
            o = m + n;
            d = (n + (m - o)) + ((d - (l + k)) + (l + j));
            c = o + d;
            d += o - c;
            k = 0x08000001 * e;
            k += e - k;
            l = e - k;
            m = 0x08000001 * a;
            m += a - m;
            n = a - m;
            o = e * a;
            f = (((k * m - o) + (k * n + l * m)) + l * n) + (f * a + e * b);
            e = o + f;
            f += o - e;
            n = g / ++q;
            k = 0x08000001 * n;
            k += n - k;
            l = n - k;
            m = n * q;
            o = g - m;
            p = g - o;
            h = (o + ((((g - (p + o)) + (p - m)) + h) - (((k * q - m) + l * q)))) / q;
            g = n;
            i = 0x08000001 * e;
            i += e - i;
            k = e - i;
            j = 0x08000001 * g;
            j += g - j;
            l = g - j;
            m = e * g;
            j = (((i * j - m) + (i * l + k * j)) + k * l) + (f * g + e * h);
            i = m + j;
            j += m - i;
        } while (i > 1e-40 || i < -1e-40);
        if (s < 0) {
            s = -s;
            a = 0.5;
        } else {
            a = 2;
        }
        while (s > 0) {
            if ((s & 1) > 0) {
                r *= a;
            }
            a *= a;
            s >>= 1;
        }
        a = d + j;
        b = d - a;
        e = c + i;
        f = c - e;
        f = ((c - (f + e)) + (f + i)) + a;
        this.hi = e + f;
        this.lo = ((f + (e - this.hi)) + ((d - (b + a)) + (b + j))) * r;
        this.hi *= r;
    }

    public DoubleDoubleNumberType log() {
        if (this.hi <= 0.0) {
            return new DoubleDoubleNumberType(Double.NaN);
        }

        double a, b, c, d, e, f, g = 0.5, h = 0, i, j, k, l, m, n, o, p, q = 2, r = 1, s;
        int t;

        s = StrictMath.log(this.hi);

        a = 0x08000001 * s;
        a += s + a;
        b = s - a;
        c = s * -1.4426950408889634;
        b = (((a * -1.4426950514316559 - c) + (a * 1.0542692496784412E-8 - b * 1.4426950514316559)) + b * 1.0542692496784412E-8) - (s * 2.0355273740931033E-17);
        t = (int) StrictMath.round(c);
        if (a == t) {
            t += (int) StrictMath.round(b);
        } else if (StrictMath.abs(t - a) == 0.5 && b < 0.0) {
            t--;
        }
        e = 0.6931471805599453 * t;
        c = ((t * 0.6931471824645996 - e) - (t * 1.904654323148236E-9)) + 2.3190468138462996E-17 * t;
        e += s;
        a = e + c;
        b = (a - e) - c;
        e = 1 - a;
        d = ((1 - e) - a) + b;
        c = e + d;
        d += e - c;
        e = 0x08000001 * -a;
        e -= a + e;
        f = a + e;
        i = a * a;
        f = ((e * e - i) - e * f * 2) + f * f;
        f += -a * b * 2;
        a = -a;
        f += b * b;
        e = f + i;
        f += i - e;
        l = 0x08000001 * e;
        l += e - l;
        k = e - l;
        i = e * g;
        j = f * g;
        do {
            k = d + j;
            l = d - k;
            m = c + i;
            n = c - m;
            n = ((c - (n + m)) + (n + i)) + k;
            o = m + n;
            d = (n + (m - o)) + ((d - (l + k)) + (l + j));
            c = o + d;
            d += o - c;
            k = 0x08000001 * e;
            k += e - k;
            l = e - k;
            m = 0x08000001 * a;
            m += a - m;
            n = a - m;
            o = e * a;
            f = (((k * m - o) + (k * n + l * m)) + l * n) + (f * a + e * b);
            e = o + f;
            f += o - e;
            n = g / ++q;
            k = 0x08000001 * n;
            k += n - k;
            l = n - k;
            m = n * q;
            o = g - m;
            p = g - o;
            h = (o + ((((g - (p + o)) + (p - m)) + h) - (((k * q - m) + l * q)))) / q;
            g = n;
            i = 0x08000001 * e;
            i += e - i;
            k = e - i;
            j = 0x08000001 * g;
            j += g - j;
            l = g - j;
            m = e * g;
            j = (((i * j - m) + (i * l + k * j)) + k * l) + (f * g + e * h);
            i = m + j;
            j += m - i;
        } while (i > 1e-40 || i < -1e-40);
        if (t < 0) {
            t = -t;
            k = 0.5;
        } else {
            k = 2;
        }
        while (t > 0) {
            if ((t & 1) > 0) {
                r *= k;
            }
            k *= k;
            t >>= 1;
        }
        a = d + j;
        b = d - a;
        e = c + i;
        f = c - e;
        f = ((c - (f + e)) + (f + i)) + a;
        g = e + f;
        h = ((f + (e - g)) + ((d - (b + a)) + (b + j))) * r;
        g *= r;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        c = this.hi - a;
        b = 0x08000001 * g;
        b += g - b;
        d = g - b;
        e = this.hi * g;
        b = (((a * b - e) + (a * d + c * b)) + c * d) + (this.lo * g + this.hi * h);
        a = --e + b;
        b += e - a;
        c = a + s;
        d = a - c;
        b += ((a - (c + d)) + (s + d));
        a = c + b;
        return new DoubleDoubleNumberType(a, b + (c - a));
    }

    public void logSelf() {
        if (this.hi <= 0.0) {
            this.hi = Double.NaN;
            return;
        }

        double a, b, c, d, e, f, g = 0.5, h = 0, i, j, k, l, m, n, o, p, q = 2, r = 1, s;
        int t;

        s = StrictMath.log(this.hi);

        a = 0x08000001 * s;
        a += s + a;
        b = s - a;
        c = s * -1.4426950408889634;
        b = (((a * -1.4426950514316559 - c) + (a * 1.0542692496784412E-8 - b * 1.4426950514316559)) + b * 1.0542692496784412E-8) - (s * 2.0355273740931033E-17);
        t = (int) StrictMath.round(c);
        if (c == t) {
            t += (int) StrictMath.round(b);
        } else if (StrictMath.abs(t + c) == 0.5 && b < 0.0) {
            t--;
        }
        e = 0.6931471805599453 * t;
        c = ((t * 0.6931471824645996 - e) - (t * 1.904654323148236E-9)) + 2.3190468138462996E-17 * t;
        e += s;
        a = e + c;
        b = (a - e) - c;
        e = 1 - a;
        d = ((1 - e) - a) + b;
        c = e + d;
        d += e - c;
        e = 0x08000001 * -a;
        e -= a + e;
        f = a + e;
        i = a * a;
        f = ((e * e - i) - e * f * 2) + f * f;
        f += -a * b * 2;
        a = -a;
        f += b * b;
        e = f + i;
        f += i - e;
        l = 0x08000001 * e;
        l += e - l;
        k = e - l;
        i = e * g;
        j = f * g;
        do {
            k = d + j;
            l = d - k;
            m = c + i;
            n = c - m;
            n = ((c - (n + m)) + (n + i)) + k;
            o = m + n;
            d = (n + (m - o)) + ((d - (l + k)) + (l + j));
            c = o + d;
            d += o - c;
            k = 0x08000001 * e;
            k += e - k;
            l = e - k;
            m = 0x08000001 * a;
            m += a - m;
            n = a - m;
            o = e * a;
            f = (((k * m - o) + (k * n + l * m)) + l * n) + (f * a + e * b);
            e = o + f;
            f += o - e;
            n = g / ++q;
            k = 0x08000001 * n;
            k += n - k;
            l = n - k;
            m = n * q;
            o = g - m;
            p = g - o;
            h = (o + ((((g - (p + o)) + (p - m)) + h) - (((k * q - m) + l * q)))) / q;
            g = n;
            i = 0x08000001 * e;
            i += e - i;
            k = e - i;
            j = 0x08000001 * g;
            j += g - j;
            l = g - j;
            m = e * g;
            j = (((i * j - m) + (i * l + k * j)) + k * l) + (f * g + e * h);
            i = m + j;
            j += m - i;
        } while (i > 1e-40 || i < -1e-40);
        if (t < 0) {
            t = -t;
            k = 0.5;
        } else {
            k = 2;
        }
        while (t > 0) {
            if ((t & 1) > 0) {
                r *= k;
            }
            k *= k;
            t >>= 1;
        }
        a = d + j;
        b = d - a;
        e = c + i;
        f = c - e;
        f = ((c - (f + e)) + (f + i)) + a;
        g = e + f;
        h = ((f + (e - g)) + ((d - (b + a)) + (b + j))) * r;
        g *= r;
        a = 0x08000001 * this.hi;
        a += this.hi - a;
        c = this.hi - a;
        b = 0x08000001 * g;
        b += g - b;
        d = g - b;
        e = this.hi * g;
        this.lo = (((a * b - e) + (a * d + c * b)) + c * d) + (this.lo * g + this.hi * h);
        a = --e + this.lo;
        this.lo += e - a;
        c = a + s;
        d = a - c;
        this.lo += ((a - (c + d)) + (s + d));
        this.hi = c + this.lo;
        this.lo += c - this.hi;
    }

    public static double powOf2(int y) {
        return ((long) y + 0xFF) << 52;
    }

    public DoubleDoubleNumberType pow(int y) {
        DoubleDoubleNumberType temp;
        int e = y;
        if (e < 0) {
            e = -y;
        }
        temp = new DoubleDoubleNumberType(this.hi, this.lo);
        DoubleDoubleNumberType prod = new DoubleDoubleNumberType(1);
        while (e > 0) {
            if ((e & 1) > 0) {
                prod.mulSelf(temp);
            }
            temp.sqrSelf();
            e >>= 1;
        }
        if (y < 0) {
            return prod.recip();
        }
        return prod;
    }

    public void powSelf(int y) {
        DoubleDoubleNumberType temp;
        int e = y;
        if (e < 0) {
            e = -y;
        }
        temp = new DoubleDoubleNumberType(this.hi, this.lo);
        this.hi = 1;
        this.lo = 0;
        while (e > 0) {
            if ((e & 1) > 0) {
                this.mulSelf(temp);
            }
            temp.sqrSelf();
            e >>= 1;
        }
        if (y < 0) {
            this.recipSelf();
        }
    }

    public DoubleDoubleNumberType pow(double y) {
        return this.log().mul(y).exp();
    }

    public void powSelf(double y) {
        this.logSelf();
        this.mulSelf(y);
        this.expSelf();
    }

    public DoubleDoubleNumberType pow(DoubleDoubleNumberType y) {
        return this.log().mul(y).exp();
    }

    public void powSelf(DoubleDoubleNumberType y) {
        this.logSelf();
        this.mulSelf(y);
        this.expSelf();
    }

    public DoubleDoubleNumberType root(int y) {
        if (this.hi == 0 && this.lo == 0) {
            return new DoubleDoubleNumberType();
        }
        if (this.hi < 0.0 && ((y & 1) == 0)) {
            return new DoubleDoubleNumberType(Double.NaN);
        }

        if (y == 1) {
            return this;
        }
        if (y == 2) {
            double a, b, c, d, e, f, g, h;
            g = 1 / StrictMath.sqrt(this.hi);
            h = this.hi * g;
            g *= 0.5;
            a = 0x08000001 * h;
            a += h - a;
            b = h - a;
            c = h * h;
            b = ((a * a - c) + a * b * 2) + b * b;
            a = this.lo - b;
            f = this.lo - a;
            e = this.hi - c;
            d = this.hi - e;
            d = ((this.hi - (d + e)) + (d - c)) + a;
            c = e + d;
            b = (d + (e - c)) + ((this.lo - (f + a)) + (f - b));
            a = c + b;
            b += (c - a);
            c = 0x08000001 * a;
            c += a - c;
            d = a - c;
            e = 0x08000001 * g;
            e += g - e;
            f = g - e;
            a = a * g;
            e = ((c * e - a) + (c * f + d * e)) + d * f;
            e += b * g;
            b = a + e;
            e += a - b;
            f = b + h;
            c = b - f;
            return new DoubleDoubleNumberType(f, e + ((b - (f + c)) + (h + c)));
        }

        double a, b, c, d, e, f, g, h, i, j, k, l, m;
        int z;

        if (this.hi < 0) {
            b = -this.hi;
            c = -this.lo;
        } else {
            b = this.hi;
            c = this.lo;
        }

        a = StrictMath.exp(StrictMath.log(b) / (-y));

        z = y;
        k = a;
        l = 0;
        g = 1;
        h = 0;
        while (z > 0) {
            if ((z & 1) > 0) {
                d = 0x08000001 * g;
                d += g - d;
                e = g - d;
                f = 0x08000001 * k;
                f += k - f;
                i = k - f;
                j = g * k;
                h = (((d * f - j) + (d * i + e * f)) + e * i) + (h * k + g * l);
                g = j + h;
                h += j - g;
            }
            f = 0x08000001 * k;
            f = f + (k - f);
            i = k - f;
            j = k * k;
            i = ((f * f - j) + f * i * 2) + i * i;
            i += k * l * 2;
            i += l * l;
            k = i + j;
            l = i + (j - k);
            z >>= 1;
        }

        l = 0x08000001 * b;
        l += b - l;
        m = b - l;
        d = 0x08000001 * g;
        d += g - d;
        e = g - d;
        f = b * g;
        d = (((l * d - f) + (l * e + m * d)) + m * e) + (c * g + b * h);
        e = 1 - f;
        l = e - d;
        m = (e - l) - d;
        d = 0x08000001 * l;
        d += l - d;
        e = l - d;
        f = 0x08000001 * a;
        f += a - f;
        g = a - f;
        l *= a;
        m *= a;
        m += (((d * f - l) + (d * g + e * f)) + e * g);
        d = l / y;
        e = 0x08000001 * d;
        e += d - e;
        f = d - e;
        g = 0x08000001 * y;
        g += y - g;
        h = y - g;
        i = d * y;
        j = l - i;
        k = l - j;
        m = (j + ((((l - (k + j)) + (k - i)) + m) - (((e * g - i) + (e * h + f * g)) + f * h))) / y;
        e = d + a;
        l = d - e;
        m += (d - (e + l)) + (a + l);
        if (this.hi < 0.0) {
            e = -e;
            m = -m;
        }
        i = 1 / e;
        l = 0x08000001 * e;
        l += e - l;
        d = e - l;
        f = 0x08000001 * i;
        f += i - f;
        g = i - f;
        h = e * i;
        m = ((1 - h) - ((((l * f - h) + (l * g + d * f)) + d * g) + m * i)) / e;
        l = i + m;
        return new DoubleDoubleNumberType(l, m + (i - l));
    }

    public void rootSelf(int y) {
        if (this.hi == 0 && this.lo == 0) {
            return;
        }
        if (this.hi < 0.0 && ((y & 1) == 0)) {
            this.hi = Double.NaN;
            return;
        }

        if (y == 1) {
            return;
        }
        if (y == 2) {
            double a, b, c, d, e, f, g, h;
            g = 1 / StrictMath.sqrt(this.hi);
            h = this.hi * g;
            g *= 0.5;
            a = 0x08000001 * h;
            a += h - a;
            b = h - a;
            c = h * h;
            b = ((a * a - c) + a * b * 2) + b * b;
            a = this.lo - b;
            f = this.lo - a;
            e = this.hi - c;
            d = this.hi - e;
            d = ((this.hi - (d + e)) + (d - c)) + a;
            c = e + d;
            b = (d + (e - c)) + ((this.lo - (f + a)) + (f - b));
            a = c + b;
            b += (c - a);
            c = 0x08000001 * a;
            c += a - c;
            d = a - c;
            e = 0x08000001 * g;
            e += g - e;
            f = g - e;
            a = a * g;
            e = ((c * e - a) + (c * f + d * e)) + d * f;
            e += b * g;
            b = a + e;
            e += a - b;
            this.hi = b + h;
            c = b - this.hi;
            this.lo = e + ((b - (this.hi + c)) + (h + c));
            return;
        }

        double a, b, c, d, e, f, g, h, i, j, k, l, m;
        int z;

        if (this.hi < 0) {
            b = -this.hi;
            c = -this.lo;
        } else {
            b = this.hi;
            c = this.lo;
        }

        a = StrictMath.exp(StrictMath.log(b) / (-y));

        z = y;
        k = a;
        l = 0;
        g = 1;
        h = 0;
        while (z > 0) {
            if ((z & 1) > 0) {
                d = 0x08000001 * g;
                d += g - d;
                e = g - d;
                f = 0x08000001 * k;
                f += k - f;
                i = k - f;
                j = g * k;
                h = (((d * f - j) + (d * i + e * f)) + e * i) + (h * k + g * l);
                g = j + h;
                h += j - g;
            }
            f = 0x08000001 * k;
            f = f + (k - f);
            i = k - f;
            j = k * k;
            i = ((f * f - j) + f * i * 2) + i * i;
            i += k * l * 2;
            i += l * l;
            k = i + j;
            l = i + (j - k);
            z >>= 1;
        }

        l = 0x08000001 * b;
        l += b - l;
        m = b - l;
        d = 0x08000001 * g;
        d += g - d;
        e = g - d;
        f = b * g;
        d = (((l * d - f) + (l * e + m * d)) + m * e) + (c * g + b * h);
        e = 1 - f;
        l = e - d;
        m = (e - l) - d;
        d = 0x08000001 * l;
        d += l - d;
        e = l - d;
        f = 0x08000001 * a;
        f += a - f;
        g = a - f;
        l *= a;
        m *= a;
        m += (((d * f - l) + (d * g + e * f)) + e * g);
        d = l / y;
        e = 0x08000001 * d;
        e += d - e;
        f = d - e;
        g = 0x08000001 * y;
        g += y - g;
        h = y - g;
        i = d * y;
        j = l - i;
        k = l - j;
        m = (j + ((((l - (k + j)) + (k - i)) + m) - (((e * g - i) + (e * h + f * g)) + f * h))) / y;
        e = d + a;
        l = d - e;
        m += (d - (e + l)) + (a + l);
        if (this.hi < 0.0) {
            e = -e;
            m = -m;
        }
        i = 1 / e;
        l = 0x08000001 * e;
        l += e - l;
        d = e - l;
        f = 0x08000001 * i;
        f += i - f;
        g = i - f;
        h = e * i;
        m = ((1 - h) - ((((l * f - h) + (l * g + d * f)) + d * g) + m * i)) / e;
        l = i + m;
        this.hi = l;
        this.lo = m + (i - l);
    }

    public DoubleDoubleNumberType root(double y) {
        return this.log().div(y).exp();
    }

    public void rootSelf(double y) {
        this.logSelf();
        this.divSelf(y);
        this.expSelf();
    }

    public DoubleDoubleNumberType rootr(double y) {
        return this.divr(StrictMath.log(y)).exp();
    }

    public void rootrSelf(double y) {
        this.divrSelf(StrictMath.log(y));
        this.expSelf();
    }

    public DoubleDoubleNumberType root(DoubleDoubleNumberType y) {
        return this.log().div(y).exp();
    }

    public void rootSelf(DoubleDoubleNumberType y) {
        this.logSelf();
        this.divSelf(y);
        this.expSelf();
    }

    @Override
    public int escape(NumberType x, NumberType y, TreeSet<Integer> hashes, int MAX_ITERATIONS) {
        DoubleDoubleNumberType xn, yn, x0, y0, xsq, ysq;
        xn = x0 = (DoubleDoubleNumberType) x;
        yn = y0 = (DoubleDoubleNumberType) y;
        y0 = y0.mul(1);
        x0 = x0.mul(1);

        hashes.clear();
        int z = 0;
        while (z < MAX_ITERATIONS - 1) {
            xsq = xn.multiply(xn);
            ysq = yn.multiply(yn);
            if (xsq.add(ysq).longValue() > 4) {
                //System.out.println("problem");
                return z;
            }
            if (!hashes.add(quadHash(xn, yn))) {
                //System.out.println("saved " + (MAX_ITERATIONS - z));
                return MAX_ITERATIONS;
            }

            yn = xn.multiply(yn).mult2().add(y0);
            xn = xsq.subtract(ysq).add(x0);
            z++;
        }
        return MAX_ITERATIONS;
    }

    @Override
    public DoubleDoubleNumberType add(NumberType addend) {
        return this.addFast((DoubleDoubleNumberType) addend);
    }

    @Override
    public DoubleDoubleNumberType subtract(NumberType subtrahend) {
        return this.subFast((DoubleDoubleNumberType) subtrahend);
    }

    @Override
    public DoubleDoubleNumberType multiply(NumberType multiplicand) {
        return this.mul((DoubleDoubleNumberType) multiplicand);
    }

    @Override
    public DoubleDoubleNumberType divide(NumberType dividend) {
        return this.div((DoubleDoubleNumberType) dividend);
    }

    @Override
    public DoubleDoubleNumberType subtract(double subtrahend) {
        return this.sub(subtrahend);
    }

    @Override
    public DoubleDoubleNumberType multiply(double multiplicand) {
        return this.mul(multiplicand);
    }

    @Override
    public DoubleDoubleNumberType divide(double dividend) {
        return this.div(dividend);
    }

    @Override
    public DoubleDoubleNumberType square() {

        return this.pow(2);
    }

    @Override
    public DoubleDoubleNumberType mult2() {
        return this.mulPwrOf2(2);
    }

    @Override
    public int compareTo(int i) {
        return hi > i ? 1 : 0;
    }

    private int quadHash(NumberType xn, NumberType yn) {
        DoubleDoubleNumberType x = (DoubleDoubleNumberType) xn;
        DoubleDoubleNumberType y = (DoubleDoubleNumberType) yn;
        return 37 * x.hashCode() + y.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.hi) ^ (Double.doubleToLongBits(this.hi) >>> 32));
        hash = 79 * hash + (int) (Double.doubleToLongBits(this.lo) ^ (Double.doubleToLongBits(this.lo) >>> 32));
        return hash;
    }

    @Override
    public NumberType toNextSystem() {
        return new QuadDoubleNumberType(hi, lo, 0, 0);
    }

    @Override
    public NumberType toPreviousSystem() {
        return new DoubleNumberType(hi);
    }

}
