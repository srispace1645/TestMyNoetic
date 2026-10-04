package com.testmynoetic.core

import kotlin.math.abs

/** An exact fraction, so "0.75", "3/4" and "6/8" all compare equal. */
class Rational private constructor(val num: Long, val den: Long) {
    companion object {
        fun of(num: Long, den: Long = 1): Rational {
            require(den != 0L) { "zero denominator" }
            val sign = if (den < 0) -1 else 1
            val g = gcd(abs(num), abs(den)).coerceAtLeast(1)
            return Rational(sign * num / g, sign * den / g)
        }

        /** Parses "12", "-3", "2.50", "3/4" or a mixed number like "1 1/2". Returns null if it can't. */
        fun parse(text: String): Rational? {
            val s = text.trim()
            if (s.isEmpty()) return null
            val mixed = Regex("""^(-?\d+)\s+(\d+)/(\d+)$""").matchEntire(s)
            if (mixed != null) {
                val (w, n, d) = mixed.destructured
                val whole = w.toLongOrNull() ?: return null
                val dd = d.toLongOrNull()?.takeIf { it != 0L } ?: return null
                val frac = of(n.toLongOrNull() ?: return null, dd)
                val size = of(kotlin.math.abs(whole)) + frac
                return if (w.startsWith("-")) -size else size
            }
            val fraction = Regex("""^(-?\d+)/(\d+)$""").matchEntire(s)
            if (fraction != null) {
                val (n, d) = fraction.destructured
                val dd = d.toLongOrNull()?.takeIf { it != 0L } ?: return null
                return of(n.toLongOrNull() ?: return null, dd)
            }
            val decimal = Regex("""^(-?)(\d*)\.?(\d*)$""").matchEntire(s) ?: return null
            val (minus, intPart, fracPart) = decimal.destructured
            if (intPart.isEmpty() && fracPart.isEmpty()) return null
            if (intPart.length + fracPart.length > 15) return null
            var den = 1L
            repeat(fracPart.length) { den *= 10 }
            val digits = (intPart + fracPart).ifEmpty { "0" }.toLong()
            return of(if (minus == "-") -digits else digits, den)
        }

        private tailrec fun gcd(a: Long, b: Long): Long = if (b == 0L) a else gcd(b, a % b)
    }

    operator fun plus(o: Rational) = of(num * o.den + o.num * den, den * o.den)

    operator fun unaryMinus() = of(-num, den)

    override fun equals(other: Any?) = other is Rational && other.num == num && other.den == den

    override fun hashCode() = 31 * num.hashCode() + den.hashCode()

    override fun toString() = if (den == 1L) "$num" else "$num/$den"
}
