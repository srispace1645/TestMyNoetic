package com.testmynoetic.core

import java.math.BigDecimal
import kotlin.random.Random

/**
 * Turns a question into tap-to-pick options: the right answer plus up to three
 * wrong ones. Hand-written wrong answers ([Question.choices]) come first; number
 * answers are topped up with near-misses in the same format as the answer.
 *
 * The order is shuffled with a seed from the question id, so a question always
 * shows the same options in the same order.
 */
object ChoiceMaker {

    private const val WRONG_PER_QUESTION = 3
    private val TIME = Regex("""^(\d{1,2}):(\d{2})\s*([AaPp][Mm])$""")

    fun optionsFor(question: Question): List<String> {
        val rnd = Random(question.id.hashCode().toLong())
        val wrong = mutableListOf<String>()
        fun offer(option: String) {
            if (wrong.size < WRONG_PER_QUESTION && isNewWrongAnswer(question, wrong, option)) wrong += option
        }
        question.choices.forEach(::offer)
        val generated = when (question.kind) {
            AnswerKind.NUMBER -> numberNearMisses(question.answer, rnd)
            AnswerKind.WORD -> wordNearMisses(question.answer, rnd)
        }
        generated.forEach(::offer)
        return (wrong + question.answer).shuffled(rnd)
    }

    /** What a choice button shows, e.g. "$9" or "30 feet". */
    fun label(question: Question, option: String): String =
        (question.unitBefore + option + if (question.unit.isEmpty()) "" else " ${question.unit}").trim()

    private fun isNewWrongAnswer(question: Question, wrong: List<String>, option: String): Boolean {
        if (option.isBlank() || AnswerChecker.isCorrect(question, option)) return false
        val key = sameValueKey(question, option)
        return wrong.none { sameValueKey(question, it) == key }
    }

    private fun sameValueKey(question: Question, option: String): Any =
        if (question.kind == AnswerKind.NUMBER) AnswerChecker.parseNumber(option) ?: option
        else AnswerChecker.normalizeWord(option)

    // ---- Numbers ----

    private fun numberNearMisses(answer: String, rnd: Random): List<String> = when {
        '/' in answer -> fractionNearMisses(answer, rnd)
        '.' in answer -> decimalNearMisses(answer, rnd)
        else -> answer.toLongOrNull()?.let { integerNearMisses(it, rnd) }.orEmpty()
    }

    /**
     * A mix of small slips (off by 1 or 2) and bigger mistakes (off by 5, 10 or 20,
     * doubled, halved, digits swapped). Round answers like 3,700 get round
     * near-misses (3,600, 3,800, 370, 37,000) instead. A random number of them sit
     * above the answer, so the right answer isn't always the middle one.
     */
    private fun integerNearMisses(n: Long, rnd: Random): List<String> {
        val round = n >= 100 && n % 100 == 0L
        val step = if (round) 100L else 1L
        val small = listOf(n + step, n - step, n + 2 * step, n - 2 * step).shuffled(rnd).take(2)
        val big = buildList {
            if (round) {
                addAll(listOf(n * 10, n / 10, n + 5 * step, n - 5 * step))
            } else {
                addAll(listOf(n + 10, n - 10, n + 5, n - 5, n + 20, n - 20))
                if (n >= 10) n.toString().reversed().toLongOrNull()?.let { add(it) }
            }
            add(n * 2)
            if (n % 2 == 0L) add(n / 2)
        }.shuffled(rnd)
        // Whole-number answers here are counts and amounts, so wrong options stay positive.
        val pool = (small + big).filter { it > 0 && it != n }.distinct()
        val above = pool.filter { it > n }
        val below = pool.filter { it < n }
        val wantAbove = rnd.nextInt(0, WRONG_PER_QUESTION + 1)
        val picked = (above.take(wantAbove) + below.take(WRONG_PER_QUESTION - wantAbove)).toMutableList()
        pool.forEach { if (picked.size < WRONG_PER_QUESTION && it !in picked) picked += it }
        return picked.map { it.toString() }
    }

    private fun fractionNearMisses(answer: String, rnd: Random): List<String> {
        val (a, b) = answer.split('/').map { it.trim().toLongOrNull() ?: return emptyList() }
        val target = Rational.of(a, b)
        return listOf(a + 1 to b, a - 1 to b, b - a to b, a to b + 1, a to b - 1, 1L to b, a + 1 to b + 1)
            .filter { (x, y) -> x > 0 && y > 1 }
            .map { (x, y) -> Rational.of(x, y) }
            .filter { it != target && it.den > 1 }
            .distinct()
            .shuffled(rnd)
            .map { it.toString() }
    }

    private fun decimalNearMisses(answer: String, rnd: Random): List<String> {
        val value = answer.toBigDecimalOrNull() ?: return emptyList()
        val places = value.scale().coerceAtLeast(1)
        val steps = listOf("0.1", "-0.1", "0.05", "-0.05", "1", "-0.01").map { value + BigDecimal(it) } + value * BigDecimal.TEN
        return steps
            .filter { it.signum() > 0 && it.compareTo(value) != 0 }
            .map { it.setScale(places, java.math.RoundingMode.HALF_UP).toPlainString() }
            .distinct()
            .shuffled(rnd)
    }

    // ---- Words: days of the week and clock times ----

    private fun wordNearMisses(answer: String, rnd: Random): List<String> {
        val day = DrillGenerator.DAYS.indexOfFirst { it.equals(answer.trim(), ignoreCase = true) }
        if (day >= 0) {
            return listOf(1, -1, 2, -2, 3).shuffled(rnd).map { DrillGenerator.DAYS[(day + it + 7) % 7] }
        }
        val time = TIME.matchEntire(answer.trim()) ?: return emptyList()
        val (h, m, half) = time.destructured
        val minutes = (h.toInt() % 12) * 60 + m.toInt() + if (half.uppercase() == "PM") 12 * 60 else 0
        return listOf(10, -10, 5, -5, 60, -60, 15).shuffled(rnd)
            .map { DrillGenerator.clock(((minutes + it) % (24 * 60) + 24 * 60) % (24 * 60)) }
    }
}
