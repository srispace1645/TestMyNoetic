package com.testmynoetic.prep

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.testmynoetic.prep.ui.ExamScreen
import com.testmynoetic.prep.ui.HomeScreen
import com.testmynoetic.prep.ui.PracticeScreen
import com.testmynoetic.prep.ui.PrepTheme
import com.testmynoetic.prep.ui.ResultsScreen
import com.testmynoetic.prep.ui.SetsScreen
import com.testmynoetic.prep.ui.StatsScreen
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

const val TV = "w960dp-h540dp-land-television-xhdpi"
const val PHONE = "w411dp-h891dp-port-xxhdpi"

/**
 * Renders the main screens at Fire TV size (and a few at phone size) and saves
 * PNGs to app/build/outputs/roborazzi. CI publishes them to the ci-screenshots branch.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [34], qualifiers = TV)
class ScreenshotTest {

    @get:Rule
    val compose = createComposeRule()

    private val vm by lazy { AppViewModel(RuntimeEnvironment.getApplication()) }
    private val set1 get() = vm.bank.sets.first()

    private fun shot(name: String, content: @Composable () -> Unit) {
        // Manual clock: lets focus and scroll effects run without fast-forwarding the 45-minute timer.
        compose.mainClock.autoAdvance = false
        compose.setContent { PrepTheme { content() } }
        compose.mainClock.advanceTimeBy(600)
        compose.onRoot().captureRoboImage("build/outputs/roborazzi/$name.png")
    }

    private fun examAt(index: Int) = ExamSession(set1.title, set1.questions).apply {
        answers[0] = "1000"
        answers[1] = "52"
        this.index = index
    }

    @Test fun tvHome() = shot("tv_1_home") { HomeScreen(vm) }

    @Test fun tvTests() = shot("tv_2_practice_tests") { SetsScreen(vm) }

    @Test fun tvExamShape() = shot("tv_3_exam_shape") { ExamScreen(vm, examAt(15)) }

    @Test fun tvExamPyramid() = shot("tv_4_exam_pyramid") { ExamScreen(vm, examAt(11)) }

    @Test fun tvExamWordAnswer() = shot("tv_5_exam_word_answer") { ExamScreen(vm, examAt(4)) }

    @Test fun tvPracticeSolution() {
        val q = set1.questions[15]
        val session = PracticeSession("Geometry & Measurement", PracticeSession.Mode.TOPIC) { q }
        session.input = "42"
        session.check()
        shot("tv_6_practice_solution") { PracticeScreen(vm, session) }
    }

    @Test fun tvResults() {
        val session = examAt(0)
        set1.questions.take(14).forEachIndexed { i, q -> session.answers[i] = q.answer }
        vm.finishExam(session, timedOut = false)
        shot("tv_7_results") { ResultsScreen(vm, session) }
    }

    @Test fun tvStats() {
        val session = examAt(0)
        set1.questions.take(12).forEachIndexed { i, q -> session.answers[i] = q.answer }
        vm.finishExam(session, timedOut = false)
        shot("tv_8_progress") { StatsScreen(vm) }
    }

    @Config(qualifiers = PHONE)
    @Test fun phoneHome() = shot("phone_1_home") { HomeScreen(vm) }

    @Config(qualifiers = PHONE)
    @Test fun phoneExam() = shot("phone_2_exam_shape") { ExamScreen(vm, examAt(15)) }
}
