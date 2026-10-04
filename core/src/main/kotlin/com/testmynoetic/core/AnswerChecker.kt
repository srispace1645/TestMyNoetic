package com.testmynoetic.core

/**
 * Decides whether a kid's typed answer matches the key.
 *
 * Real Noetic answers must match the key exactly, but kids type things like
 * "$12", "1,200", "11 cats" or "saturday", so the app is forgiving about
 * format while staying strict about the value.
 */
object AnswerChecker {

    fun isCorrect(question: Question, input: String): Boolean {
        val keys = listOf(question.answer) + question.accept
        return when (question.kind) {
            AnswerKind.NUMBER -> {
                val given = parseNumber(input)
                if (given != null) keys.any { parseNumber(it) == given } else matchesWord(keys, input)
            }
            AnswerKind.WORD -> matchesWord(keys, input)
        }
    }

    /** Pulls the number out of things like "$1,250", "45¢", "11 cats" or "3/4 cup". */
    fun parseNumber(input: String): Rational? {
        var s = input.trim().lowercase()
            .replace(",", "")
            .replace("$", "")
            .replace("¢", "")
            .replace("°", "")
            .replace("−", "-")
        // Drop a leading word like "day" in "Day 11", and a trailing unit word such as "cats", "sq cm" or "minutes".
        s = s.replace(Regex("""^[a-z]+\s+(?=[-.\d])"""), "")
        s = s.replace(Regex("""\s*[a-z][a-z .²]*$"""), "")
        s = s.trim().removeSuffix(".").trim()
        return Rational.parse(s)
    }

    fun normalizeWord(input: String): String =
        input.lowercase().replace(Regex("""[\s.,'!]"""), "")

    private fun matchesWord(keys: List<String>, input: String): Boolean {
        val given = normalizeWord(input)
        if (given.isEmpty()) return false
        return keys.any { normalizeWord(it) == given }
    }
}
