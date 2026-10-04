package com.testmynoetic.prep

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import com.testmynoetic.core.DrillGenerator
import com.testmynoetic.core.DrillKind
import com.testmynoetic.core.Progress
import com.testmynoetic.core.Question
import com.testmynoetic.core.QuestionBank
import com.testmynoetic.core.QuestionSet
import com.testmynoetic.core.Topic
import com.testmynoetic.core.buildShuffleTest
import com.testmynoetic.core.gradeExam
import java.time.LocalDate
import kotlin.random.Random

sealed interface Screen {
    data object Home : Screen
    data object Sets : Screen
    data class Exam(val session: ExamSession) : Screen
    data class Results(val session: ExamSession) : Screen
    data object Topics : Screen
    data object Drills : Screen
    data class Practice(val session: PracticeSession) : Screen
    data object Stats : Screen
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val prefs = app.getSharedPreferences("prep", Context.MODE_PRIVATE)

    val bank: QuestionBank = QuestionBank.loadAll().first()

    var progress by mutableStateOf(Progress.fromJson(prefs.getString(KEY_PROGRESS, null)))
        private set

    /** Simple back stack: the last screen is the one showing. */
    val backStack = mutableStateListOf<Screen>(Screen.Home)

    fun open(screen: Screen) {
        backStack.add(screen)
    }

    fun back() {
        if (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    fun goHome() {
        while (backStack.size > 1) backStack.removeAt(backStack.lastIndex)
    }

    private fun replaceTop(screen: Screen) {
        backStack[backStack.lastIndex] = screen
    }

    private fun save(p: Progress) {
        progress = p
        prefs.edit().putString(KEY_PROGRESS, p.toJson()).apply()
    }

    fun resetProgress() = save(Progress())

    // ---- Tests ----

    fun startExam(set: QuestionSet) = open(Screen.Exam(ExamSession(set.title, set.questions)))

    fun startShuffleTest() =
        open(Screen.Exam(ExamSession("Shuffle Test", buildShuffleTest(bank.allQuestions, Random.nextLong()))))

    fun finishExam(session: ExamSession, timedOut: Boolean) {
        if (session.result != null) return
        val result = gradeExam(session.questions, session.answers.toMap())
        session.markFinished(result, timedOut)
        save(progress.recordMock(session.title, LocalDate.now().toString(), session.questions, result))
        replaceTop(Screen.Results(session))
    }

    fun bestScore(title: String): Int? =
        progress.mocks.filter { it.title == title }.maxOfOrNull { it.correct * 5 }

    // ---- Practice ----

    fun startTopic(topic: Topic) {
        val pool = bank.byTopic(topic)
        val queue = ArrayDeque<Question>()
        open(Screen.Practice(PracticeSession(topic.label, PracticeSession.Mode.TOPIC) {
            if (queue.isEmpty()) queue.addAll(pool.shuffled())
            queue.removeFirst()
        }))
    }

    fun startReview() {
        val queue = ArrayDeque<Question>()
        open(Screen.Practice(PracticeSession("Review Mistakes", PracticeSession.Mode.REVIEW) {
            if (queue.isEmpty()) queue.addAll(progress.mistakeIds().mapNotNull { bank.question(it) }.shuffled())
            queue.removeFirstOrNull()
        }))
    }

    fun startDrill(kinds: List<DrillKind>, title: String) {
        open(Screen.Practice(PracticeSession(title, PracticeSession.Mode.DRILL) {
            DrillGenerator.random(Random.nextLong(), kinds).question
        }))
    }

    fun check(session: PracticeSession) {
        val q = session.question ?: return
        val ok = session.check() ?: return
        var p = progress.record(q, ok, keepForReview = session.mode != PracticeSession.Mode.DRILL)
        if (session.mode == PracticeSession.Mode.DRILL && session.streak > p.bestDrillStreak) {
            p = p.copy(bestDrillStreak = session.streak)
        }
        save(p)
    }

    private companion object {
        const val KEY_PROGRESS = "progress"
    }
}
