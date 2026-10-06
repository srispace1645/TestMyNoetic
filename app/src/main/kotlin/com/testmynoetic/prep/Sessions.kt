package com.testmynoetic.prep

import android.os.SystemClock
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.testmynoetic.core.AnswerChecker
import com.testmynoetic.core.CONTEST_SECONDS
import com.testmynoetic.core.ExamResult
import com.testmynoetic.core.Question

/** A 20-question test taken under contest rules: timer running, no feedback until the end. */
class ExamSession(val title: String, val questions: List<Question>) {
    private val startedAt = SystemClock.elapsedRealtime()

    val answers = mutableStateMapOf<Int, String>()
    val flagged = mutableStateMapOf<Int, Boolean>()
    var index by mutableIntStateOf(0)
    var result by mutableStateOf<ExamResult?>(null)
        private set
    var secondsUsed by mutableLongStateOf(0L)
        private set
    var timedOut by mutableStateOf(false)
        private set

    fun secondsLeft(now: Long = SystemClock.elapsedRealtime()): Long =
        (CONTEST_SECONDS - (now - startedAt) / 1000).coerceAtLeast(0)

    fun blanks(): Int = questions.indices.count { answers[it].isNullOrBlank() }

    fun markFinished(result: ExamResult, timedOut: Boolean) {
        this.secondsUsed = (CONTEST_SECONDS - secondsLeft()).coerceAtMost(CONTEST_SECONDS.toLong())
        this.timedOut = timedOut
        this.result = result
    }
}

/** One question at a time: pick a choice, check it, then see the worked solution. */
class PracticeSession(
    val title: String,
    val mode: Mode,
    private val supplier: () -> Question?,
) {
    enum class Mode { TOPIC, REVIEW, DRILL }

    var question by mutableStateOf(supplier())
        private set
    /** The choice the kid has picked but not checked yet. */
    var selected by mutableStateOf<String?>(null)
    var verdict by mutableStateOf<Boolean?>(null)
        private set
    var showHint by mutableStateOf(false)
    var streak by mutableIntStateOf(0)
        private set
    var answered by mutableIntStateOf(0)
        private set
    var right by mutableIntStateOf(0)
        private set

    /** Grades the picked choice. Returns null if nothing is picked yet. */
    fun check(): Boolean? {
        val q = question ?: return null
        val pick = selected
        if (verdict != null || pick == null) return null
        val ok = AnswerChecker.isCorrect(q, pick)
        verdict = ok
        answered++
        if (ok) {
            right++
            streak++
        } else {
            streak = 0
        }
        return ok
    }

    fun next() {
        question = supplier()
        selected = null
        verdict = null
        showHint = false
    }
}
