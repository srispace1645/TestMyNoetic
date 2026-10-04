package com.testmynoetic.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString

@Serializable
data class QuestionStats(
    val right: Int = 0,
    val wrong: Int = 0,
    /** Right answers in a row since the last miss. */
    val streak: Int = 0,
)

@Serializable
data class MockRecord(
    val title: String,
    val dateIso: String,
    val correct: Int,
    val total: Int,
    val byTopic: Map<Topic, Int> = emptyMap(),
    val topicTotals: Map<Topic, Int> = emptyMap(),
)

/** Everything the app remembers. Stored on the phone only, as one JSON string. */
@Serializable
data class Progress(
    val stats: Map<String, QuestionStats> = emptyMap(),
    val topicRight: Map<Topic, Int> = emptyMap(),
    val topicTotal: Map<Topic, Int> = emptyMap(),
    val mocks: List<MockRecord> = emptyList(),
    val bestDrillStreak: Int = 0,
) {
    /** Records one answer. Drill questions count toward topic accuracy but aren't kept for review. */
    fun record(question: Question, correct: Boolean, keepForReview: Boolean = true): Progress {
        val newStats = if (keepForReview) {
            val old = stats[question.id] ?: QuestionStats()
            val updated = if (correct) old.copy(right = old.right + 1, streak = old.streak + 1)
            else old.copy(wrong = old.wrong + 1, streak = 0)
            stats + (question.id to updated)
        } else stats
        return copy(
            stats = newStats,
            topicRight = topicRight + (question.topic to (topicRight[question.topic] ?: 0) + if (correct) 1 else 0),
            topicTotal = topicTotal + (question.topic to (topicTotal[question.topic] ?: 0) + 1),
        )
    }

    fun recordMock(title: String, dateIso: String, questions: List<Question>, result: ExamResult): Progress {
        var p = this
        questions.forEachIndexed { i, q -> p = p.record(q, result.perQuestion[i]) }
        val mock = MockRecord(
            title = title,
            dateIso = dateIso,
            correct = result.correct,
            total = result.total,
            byTopic = result.byTopic.mapValues { it.value.correct },
            topicTotals = result.byTopic.mapValues { it.value.total },
        )
        return p.copy(mocks = p.mocks + mock)
    }

    /** Missed questions stay here until they're answered right twice in a row. */
    fun mistakeIds(): List<String> =
        stats.filter { (_, s) -> s.wrong > 0 && s.streak < MASTERED_STREAK }.keys.toList()

    fun accuracy(topic: Topic): Int? {
        val total = topicTotal[topic] ?: 0
        if (total == 0) return null
        return (topicRight[topic] ?: 0) * 100 / total
    }

    fun toJson(): String = QuestionBank.json.encodeToString(this)

    companion object {
        const val MASTERED_STREAK = 2

        fun fromJson(text: String?): Progress =
            if (text.isNullOrBlank()) Progress()
            else runCatching { QuestionBank.json.decodeFromString<Progress>(text) }.getOrDefault(Progress())
    }
}
