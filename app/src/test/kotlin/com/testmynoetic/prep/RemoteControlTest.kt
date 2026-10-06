package com.testmynoetic.prep

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.isFocused
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import com.testmynoetic.core.ChoiceMaker
import com.testmynoetic.prep.ui.PrepApp
import com.testmynoetic.prep.ui.PrepTheme
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/** Drives the app the way a Fire TV remote does: arrow keys and the center (select) button only. */
@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = TV)
class RemoteControlTest {

    @get:Rule
    val compose = createComposeRule()

    private val vm by lazy { AppViewModel(RuntimeEnvironment.getApplication()) }

    private fun start() {
        compose.mainClock.autoAdvance = false
        compose.setContent { PrepTheme { PrepApp(vm) } }
        settle()
    }

    private fun settle() = compose.mainClock.advanceTimeBy(600)

    private fun press(key: Key) {
        compose.onNode(isFocused()).performKeyInput { pressKey(key) }
        settle()
    }

    private fun SemanticsNodeInteraction.focused() = fetchSemanticsNode().config.getOrElse(SemanticsProperties.Focused) { false }

    /** Presses [keys] in order until [target] has focus; fails if it never does. */
    private fun reach(target: SemanticsNodeInteraction, keys: List<Key>) {
        for (key in keys) {
            if (target.focused()) return
            press(key)
        }
        assertTrue(target.focused(), "Couldn't reach the target with the remote")
    }

    @Test
    fun homeMenuWorksWithArrowKeys() {
        start()
        compose.onNodeWithText("Practice Tests").assertIsFocused()
        press(Key.DirectionRight)
        compose.onNodeWithText("Shuffle Test").assertIsFocused()
        press(Key.DirectionDown)
        compose.onNodeWithText("Quick Drill").assertIsFocused()
        press(Key.DirectionCenter)
        compose.onNodeWithText("Mixed drill").assertIsFocused()
    }

    @Test
    fun testCanBeAnsweredWithTheRemote() {
        vm.startExam(vm.bank.sets.first())
        start()
        val exam = (vm.backStack.last() as Screen.Exam).session
        val options = ChoiceMaker.optionsFor(exam.questions[0])

        // Focus starts on choice A; move right to B and pick it.
        compose.onNodeWithTag("choice-A").assertIsFocused()
        press(Key.DirectionRight)
        compose.onNodeWithTag("choice-B").assertIsFocused()
        press(Key.DirectionCenter)
        assertEquals(options[1], exam.answers[0])

        // Then down to Next and on to question 2.
        reach(compose.onNodeWithTag("next"), listOf(Key.DirectionDown, Key.DirectionDown, Key.DirectionRight, Key.DirectionRight, Key.DirectionRight))
        press(Key.DirectionCenter)
        assertEquals(1, exam.index)
        compose.onNodeWithText("Question 2 of 20").assertExists()
    }

    @Test
    fun practiceCanBeCheckedWithTheRemote() {
        vm.startTopic(com.testmynoetic.core.Topic.NUMBER)
        start()
        val practice = (vm.backStack.last() as Screen.Practice).session
        compose.onNodeWithTag("choice-A").assertIsFocused()
        press(Key.DirectionCenter)
        assertEquals(ChoiceMaker.optionsFor(practice.question!!)[0], practice.selected)
        reach(compose.onNodeWithText("Check"), listOf(Key.DirectionDown, Key.DirectionDown, Key.DirectionDown, Key.DirectionRight))
        press(Key.DirectionCenter)
        assertTrue(practice.verdict != null)
        // After checking, focus jumps to Next question.
        compose.onNodeWithText("Next question").assertIsFocused()
    }
}
