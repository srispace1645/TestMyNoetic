package com.testmynoetic.core.verify

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

/** Brute-force helpers for re-solving bank questions a second, independent way. */
object V {

    fun <T> permutations(items: List<T>): List<List<T>> =
        if (items.size <= 1) listOf(items)
        else items.indices.flatMap { i ->
            permutations(items.take(i) + items.drop(i + 1)).map { listOf(items[i]) + it }
        }

    fun <T> subsets(items: List<T>): List<List<T>> =
        (0 until (1 shl items.size)).map { mask -> items.filterIndexed { i, _ -> mask and (1 shl i) != 0 } }

    fun <T> choose(items: List<T>, k: Int): List<List<T>> = subsets(items).filter { it.size == k }

    fun weekday(start: String, daysLater: Int): String {
        val day = DayOfWeek.valueOf(start.uppercase()).plus(daysLater.toLong())
        return day.getDisplayName(TextStyle.FULL, Locale.US)
    }

    private val clockFormat = DateTimeFormatter.ofPattern("h:mm a", Locale.US)

    /** Time [minutes] after h24:m, e.g. after(13, 50, 105) = "3:35 PM". */
    fun after(h24: Int, m: Int, minutes: Int): String = LocalTime.of(h24, m).plusMinutes(minutes.toLong()).format(clockFormat)

    fun before(h24: Int, m: Int, minutes: Int): String = LocalTime.of(h24, m).minusMinutes(minutes.toLong()).format(clockFormat)

    fun squaresInGrid(rows: Int, cols: Int): Int {
        var count = 0
        for (size in 1..minOf(rows, cols)) for (r in 0..rows - size) for (c in 0..cols - size) count++
        return count
    }

    fun rectanglesInGrid(rows: Int, cols: Int): Int {
        var count = 0
        for (r1 in 0 until rows) for (r2 in r1 until rows) for (c1 in 0 until cols) for (c2 in c1 until cols) count++
        return count
    }

    /** Area of a grid-aligned polygon, found by testing the center of every unit square. */
    fun cellArea(points: List<Pair<Int, Int>>): Int {
        val maxX = points.maxOf { it.first }
        val maxY = points.maxOf { it.second }
        var count = 0
        for (x in 0 until maxX) for (y in 0 until maxY) if (inside(points, x + 0.5, y + 0.5)) count++
        return count
    }

    fun perimeter(points: List<Pair<Int, Int>>): Int =
        points.indices.sumOf { i ->
            val (x1, y1) = points[i]
            val (x2, y2) = points[(i + 1) % points.size]
            require(x1 == x2 || y1 == y2) { "not grid aligned" }
            Math.abs(x1 - x2) + Math.abs(y1 - y2)
        }

    private fun inside(poly: List<Pair<Int, Int>>, px: Double, py: Double): Boolean {
        var result = false
        var j = poly.size - 1
        for (i in poly.indices) {
            val (xi, yi) = poly[i].let { it.first.toDouble() to it.second.toDouble() }
            val (xj, yj) = poly[j].let { it.first.toDouble() to it.second.toDouble() }
            if ((yi > py) != (yj > py) && px < (xj - xi) * (py - yi) / (yj - yi) + xi) result = !result
            j = i
        }
        return result
    }

    /** Top of a number pyramid where each box is the sum of the two below it. */
    fun pyramidTop(bottom: List<Int>): Int {
        var row = bottom
        while (row.size > 1) row = row.zipWithNext { a, b -> a + b }
        return row.single()
    }

    fun digits(n: Int): List<Int> = n.toString().map { it - '0' }

    fun factors(n: Int): List<Int> = (1..n).filter { n % it == 0 }

    fun isPrime(n: Int): Boolean = n >= 2 && (2 until n).none { n % it == 0 }
}
