@file:OptIn(ExperimentalMaterial3Api::class)

package com.testmynoetic.prep.ui

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.InputMode
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalInputModeManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testmynoetic.core.AnswerChecker
import com.testmynoetic.core.ChoiceMaker
import com.testmynoetic.core.Question

/** True on Fire TV and other TVs. */
@Composable
fun isTv(): Boolean =
    (LocalConfiguration.current.uiMode and Configuration.UI_MODE_TYPE_MASK) == Configuration.UI_MODE_TYPE_TELEVISION

/** True when there's room for two columns: TVs and landscape tablets. Set by [Page]. */
val LocalWide = compositionLocalOf { false }

/**
 * A thick orange outline (and a tiny zoom) around whatever the TV remote is
 * pointing at. Touch screens don't move focus, so phones never show it.
 */
fun Modifier.focusRing(shape: Shape = RoundedCornerShape(12.dp)): Modifier = composed {
    var focused by remember { mutableStateOf(false) }
    val ring = MaterialTheme.colorScheme.secondary
    this
        .graphicsLayer {
            val s = if (focused) 1.03f else 1f
            scaleX = s
            scaleY = s
        }
        .border(4.dp, if (focused) ring else Color.Transparent, shape)
        .onFocusChanged { focused = it.isFocused || it.hasFocus }
}

/** Attaches [requester] if there is one. */
fun Modifier.optionalFocus(requester: FocusRequester?): Modifier =
    if (requester == null) this else focusRequester(requester)

/** On TVs, moves the remote's focus to [requester] whenever [key] changes (and when the screen opens). */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun TvFocus(requester: FocusRequester, key: Any? = Unit) {
    if (!isTv()) return
    val inputMode = LocalInputModeManager.current
    LaunchedEffect(key) {
        // Buttons only take focus in remote/keyboard mode. A TV should start that way, but make sure.
        inputMode.requestInputMode(InputMode.Keyboard)
        withFrameNanos { }
        runCatching { requester.requestFocus() }
    }
}

/** Standard page frame: top bar with an optional back arrow, scrolling content, TV-safe margins. */
@Composable
fun Page(
    title: String,
    onBack: (() -> Unit)?,
    actions: @Composable () -> Unit = {},
    scroll: Boolean = true,
    content: @Composable () -> Unit,
) {
    // TVs can crop the outer edge of the picture, so keep everything a little further in.
    val tv = isTv()
    val edge = if (tv) 32.dp else 0.dp
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                modifier = Modifier.padding(start = edge, end = edge, top = if (tv) 16.dp else 0.dp),
                navigationIcon = {
                    if (onBack != null) {
                        TextButton(onClick = onBack, modifier = Modifier.focusRing()) { Text("←", fontSize = 22.sp) }
                    }
                },
                actions = { actions() },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
    ) { padding ->
        BoxWithConstraints(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = edge),
            contentAlignment = Alignment.TopCenter,
        ) {
            val wide = maxWidth >= 840.dp
            val inner = Modifier
                .widthIn(max = if (wide) 1100.dp else 640.dp)
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
            CompositionLocalProvider(LocalWide provides wide) {
                Column(
                    modifier = if (scroll) inner.verticalScroll(rememberScrollState()) else inner,
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Spacer(Modifier.height(4.dp))
                    content()
                    Spacer(Modifier.height(if (tv) 32.dp else 24.dp))
                }
            }
        }
    }
}

/** A big menu card that can be tapped or picked with the remote. */
@Composable
fun MenuCard(
    emoji: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onClick: () -> Unit,
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().optionalFocus(focusRequester).focusRing(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 30.sp)
            Column(Modifier.padding(start = 16.dp)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

data class MenuItem(val emoji: String, val title: String, val subtitle: String, val onClick: () -> Unit)

/** Menu cards one per row, or two per row on wide screens. The first card can take the TV focus. */
@Composable
fun MenuGrid(items: List<MenuItem>, firstFocus: FocusRequester? = null) {
    val columns = if (LocalWide.current) 2 else 1
    items.chunked(columns).forEachIndexed { r, row ->
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            row.forEachIndexed { c, item ->
                MenuCard(
                    item.emoji, item.title, item.subtitle,
                    modifier = Modifier.weight(1f),
                    focusRequester = if (r == 0 && c == 0) firstFocus else null,
                    onClick = item.onClick,
                )
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
fun QuestionCard(question: Question, number: Int?) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (number != null) {
                    Text("$number)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.padding(4.dp))
                }
                Text(
                    "${question.topic.label} · ${question.skill}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(question.prompt, style = MaterialTheme.typography.bodyLarge.copy(fontSize = 19.sp, lineHeight = 27.sp))
            question.figure?.let { FigureView(it) }
        }
    }
}

/**
 * Tap-and-pick answers: one big lettered button per option. Phones stack them;
 * wide screens (TV) show two per row. After checking ([reveal]), the right answer
 * turns green and a wrong pick turns red. [focusRequester] goes on choice A.
 */
@Composable
fun ChoiceButtons(
    question: Question,
    selected: String?,
    onSelect: (String) -> Unit,
    reveal: Boolean = false,
    focusRequester: FocusRequester? = null,
) {
    val options = remember(question.id) { ChoiceMaker.optionsFor(question) }
    val columns = if (LocalWide.current) 2 else 1
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Pick the answer:", style = MaterialTheme.typography.titleMedium)
        options.withIndex().chunked(columns).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                row.forEach { (i, option) ->
                    val right = AnswerChecker.isCorrect(question, option)
                    val picked = option == selected
                    val (container, content) = when {
                        reveal && right -> rightColor to Color.White
                        reveal && picked -> wrongColor to Color.White
                        picked -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.onPrimary
                        else -> MaterialTheme.colorScheme.surface to MaterialTheme.colorScheme.onSurface
                    }
                    val letter = choiceLetter(i)
                    Button(
                        onClick = { if (!reveal) onSelect(option) },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 60.dp)
                            .optionalFocus(if (i == 0) focusRequester else null)
                            .focusRing(RoundedCornerShape(14.dp))
                            .testTag("choice-$letter"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = container, contentColor = content),
                        border = BorderStroke(2.dp, if (picked || (reveal && right)) container else MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                    ) {
                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                            Text(letter, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.padding(horizontal = 8.dp))
                            Text(ChoiceMaker.label(question, option), fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

/** "A", "B", "C", "D". */
fun choiceLetter(index: Int): String = ('A' + index).toString()

/** "B) 30 feet": an answer written with its letter, for results and review. */
fun lettered(question: Question, option: String): String {
    val i = ChoiceMaker.optionsFor(question).indexOf(option)
    val label = ChoiceMaker.label(question, option)
    return if (i < 0) label else "${choiceLetter(i)}) $label"
}

/** Formats seconds as m:ss. */
fun clockText(seconds: Long): String = "%d:%02d".format(seconds / 60, seconds % 60)
