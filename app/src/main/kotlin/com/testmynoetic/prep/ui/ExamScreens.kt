package com.testmynoetic.prep.ui

import android.os.SystemClock
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.testmynoetic.core.QuestionSet
import com.testmynoetic.core.Topic
import com.testmynoetic.prep.AppViewModel
import com.testmynoetic.prep.ExamSession
import kotlinx.coroutines.delay

@Composable
fun SetsScreen(vm: AppViewModel) {
    var pending by remember { mutableStateOf<QuestionSet?>(null) }
    Page(title = "Practice Tests", onBack = vm::back) {
        Text(
            "Each test has 20 questions that get harder as you go, just like the real contest.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        vm.bank.sets.forEachIndexed { i, set ->
            val best = vm.bestScore(set.title)
            MenuCard(
                emoji = "${i + 1}",
                title = set.title,
                subtitle = if (best == null) "Not tried yet" else "Best score: $best / 100",
            ) { pending = set }
        }
    }
    pending?.let { set ->
        AlertDialog(
            onDismissRequest = { pending = null },
            title = { Text("Ready for ${set.title}?") },
            text = {
                Text(
                    "• 20 questions, 45 minutes\n• No calculator. Use scratch paper!\n• You can skip and come back\n• Answers are checked when you finish",
                )
            },
            confirmButton = { Button(onClick = { pending = null; vm.startExam(set) }) { Text("Start") } },
            dismissButton = { TextButton(onClick = { pending = null }) { Text("Not now") } },
        )
    }
}

@Composable
fun ExamScreen(vm: AppViewModel, session: ExamSession) {
    var now by remember { mutableLongStateOf(SystemClock.elapsedRealtime()) }
    var confirmFinish by remember { mutableStateOf(false) }
    var confirmQuit by remember { mutableStateOf(false) }
    val left = session.secondsLeft(now)

    LaunchedEffect(session) {
        while (session.result == null) {
            delay(1000)
            now = SystemClock.elapsedRealtime()
            if (session.secondsLeft(now) == 0L) vm.finishExam(session, timedOut = true)
        }
    }
    BackHandler { confirmQuit = true }

    val i = session.index
    val q = session.questions[i]
    Page(
        title = "Question ${i + 1} of ${session.questions.size}",
        onBack = { confirmQuit = true },
        actions = {
            Text(
                "⏱ ${clockText(left)}",
                modifier = Modifier.padding(end = 16.dp),
                fontWeight = FontWeight.Bold,
                color = if (left < 300) wrongColor else MaterialTheme.colorScheme.onBackground,
            )
        },
    ) {
        QuestionStrip(session)
        QuestionCard(q, number = i + 1)
        AnswerInput(
            question = q,
            value = session.answers[i].orEmpty(),
            onValueChange = { session.answers[i] = it },
            onDone = { if (i < session.questions.lastIndex) session.index = i + 1 },
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            val flagged = session.flagged[i] == true
            OutlinedButton(onClick = { session.flagged[i] = !flagged }) { Text(if (flagged) "🚩 Flagged" else "Flag") }
            OutlinedButton(onClick = { session.index = i - 1 }, enabled = i > 0) { Text("Back") }
            if (i < session.questions.lastIndex) {
                Button(onClick = { session.index = i + 1 }, modifier = Modifier.weight(1f)) { Text("Next") }
            } else {
                Button(onClick = { confirmFinish = true }, modifier = Modifier.weight(1f)) { Text("Finish") }
            }
        }
        if (i < session.questions.lastIndex) {
            TextButton(onClick = { confirmFinish = true }) { Text("Finish test now") }
        }
    }

    if (confirmFinish) {
        val blanks = session.blanks()
        AlertDialog(
            onDismissRequest = { confirmFinish = false },
            title = { Text("Finish the test?") },
            text = { Text(if (blanks == 0) "You answered every question." else "You left $blanks question${if (blanks == 1) "" else "s"} blank. Blank answers count as wrong.") },
            confirmButton = { Button(onClick = { confirmFinish = false; vm.finishExam(session, timedOut = false) }) { Text("Finish") } },
            dismissButton = { TextButton(onClick = { confirmFinish = false }) { Text("Keep working") } },
        )
    }
    if (confirmQuit) {
        AlertDialog(
            onDismissRequest = { confirmQuit = false },
            title = { Text("Leave this test?") },
            text = { Text("Your answers for this test won't be saved.") },
            confirmButton = { TextButton(onClick = { confirmQuit = false; vm.back() }) { Text("Leave") } },
            dismissButton = { Button(onClick = { confirmQuit = false }) { Text("Stay") } },
        )
    }
}

/** Numbered bubbles for jumping between questions: filled = answered, orange ring = flagged. */
@Composable
private fun QuestionStrip(session: ExamSession) {
    val listState = rememberLazyListState()
    LaunchedEffect(session.index) { listState.animateScrollToItem((session.index - 3).coerceAtLeast(0)) }
    LazyRow(state = listState, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        itemsIndexed(session.questions) { idx, _ ->
            val answered = !session.answers[idx].isNullOrBlank()
            val current = idx == session.index
            val flagged = session.flagged[idx] == true
            Box(
                Modifier
                    .size(36.dp)
                    .background(if (answered) MaterialTheme.colorScheme.primary else Color.Transparent, CircleShape)
                    .border(
                        BorderStroke(
                            if (current || flagged) 3.dp else 1.dp,
                            when {
                                flagged -> MaterialTheme.colorScheme.secondary
                                current -> MaterialTheme.colorScheme.onBackground
                                else -> MaterialTheme.colorScheme.outline
                            },
                        ),
                        CircleShape,
                    )
                    .clickable { session.index = idx },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    "${idx + 1}",
                    fontSize = 14.sp,
                    color = if (answered) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground,
                )
            }
        }
    }
}

@Composable
fun ResultsScreen(vm: AppViewModel, session: ExamSession) {
    val result = session.result ?: return
    val open = remember { mutableStateMapOf<Int, Boolean>() }
    Page(title = "${session.title} · Results", onBack = vm::back) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                if (session.timedOut) Text("⏰ Time's up!", style = MaterialTheme.typography.titleMedium)
                Text("${result.score} / ${result.maxScore}", style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Text("${result.correct} of ${result.total} correct · time used ${clockText(session.secondsUsed)}")
                Text(cheer(result.correct, result.total), style = MaterialTheme.typography.bodyLarge, modifier = Modifier.padding(top = 8.dp))
            }
        }
        Text("By topic", style = MaterialTheme.typography.titleMedium)
        Topic.entries.forEach { t ->
            val s = result.byTopic[t] ?: return@forEach
            TopicBar(t.label, s.correct, s.total)
        }
        Text("Questions (tap to see the solution)", style = MaterialTheme.typography.titleMedium)
        session.questions.forEachIndexed { idx, q ->
            val right = result.perQuestion[idx]
            val expanded = open[idx] == true
            Card(
                onClick = { open[idx] = !expanded },
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(if (right) "✓" else "✗", color = if (right) rightColor else wrongColor, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                        Text("  ${idx + 1}. ${q.skill}", style = MaterialTheme.typography.titleSmall)
                    }
                    if (expanded) {
                        Text(q.prompt, style = MaterialTheme.typography.bodyLarge)
                        q.figure?.let { FigureView(it) }
                        val given = session.answers[idx].orEmpty().ifBlank { "(blank)" }
                        Text("Your answer: $given", color = if (right) rightColor else wrongColor)
                        Text("Correct answer: ${q.unitBefore}${q.answer} ${q.unit}".trim(), fontWeight = FontWeight.SemiBold)
                        Text(q.solution, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = vm::back, modifier = Modifier.weight(1f)) { Text("Back") }
            Button(onClick = vm::goHome, modifier = Modifier.weight(1f)) { Text("Home") }
        }
    }
}

@Composable
fun TopicBar(label: String, correct: Int, total: Int) {
    Column(Modifier.fillMaxWidth()) {
        Row(Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text("$correct of $total", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        LinearProgressIndicator(
            progress = { if (total == 0) 0f else correct.toFloat() / total },
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        )
    }
}

private fun cheer(correct: Int, total: Int): String {
    val pct = if (total == 0) 0 else correct * 100 / total
    return when {
        pct == 100 -> "Perfect score! Amazing work! 🌟"
        pct >= 80 -> "Excellent! Check the ones you missed. 🎉"
        pct >= 60 -> "Good job! Review the solutions and try again. 👍"
        pct >= 40 -> "Nice effort! Practice by topic to get stronger. 💪"
        else -> "Every mistake is a lesson. Read the solutions and keep going! 🌱"
    }
}
