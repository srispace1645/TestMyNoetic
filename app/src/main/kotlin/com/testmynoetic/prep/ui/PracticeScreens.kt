package com.testmynoetic.prep.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.testmynoetic.core.DrillKind
import com.testmynoetic.core.Topic
import com.testmynoetic.prep.AppViewModel
import com.testmynoetic.prep.PracticeSession

@Composable
fun TopicsScreen(vm: AppViewModel) {
    val emoji = mapOf(Topic.NUMBER to "🔢", Topic.ALGEBRA to "🧩", Topic.GEOMETRY to "📐", Topic.LOGIC to "🎲")
    val first = remember { FocusRequester() }
    TvFocus(first)
    Page(title = "Practice by Topic", onBack = vm::back) {
        MenuGrid(
            Topic.entries.map { t ->
                val acc = vm.progress.accuracy(t)
                MenuItem(
                    emoji.getValue(t), t.label,
                    "${vm.bank.byTopic(t).size} questions · " + if (acc == null) "not started" else "$acc% right so far",
                ) { vm.startTopic(t) }
            },
            firstFocus = first,
        )
    }
}

@Composable
fun DrillsScreen(vm: AppViewModel) {
    val first = remember { FocusRequester() }
    TvFocus(first)
    Page(title = "Quick Drill", onBack = vm::back) {
        Text(
            "Short questions that never run out. Best streak: ${vm.progress.bestDrillStreak}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MenuGrid(
            listOf(MenuItem("🌀", "Mixed drill", "A little bit of everything") { vm.startDrill(DrillKind.entries, "Mixed drill") }),
            firstFocus = first,
        )
        Topic.entries.forEach { t ->
            Text(t.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
            MenuGrid(
                DrillKind.entries.filter { it.topic == t }.map { k ->
                    MenuItem("•", k.label, "Endless ${k.label.lowercase()} questions") { vm.startDrill(listOf(k), k.label) }
                },
            )
        }
    }
}

@Composable
fun PracticeScreen(vm: AppViewModel, session: PracticeSession) {
    val q = session.question
    val verdict = session.verdict
    val answerFocus = remember { FocusRequester() }
    val nextFocus = remember { FocusRequester() }
    // On a TV, focus the keypad for a new question and the Next button once it's checked.
    TvFocus(if (verdict == null) answerFocus else nextFocus, key = q?.id to verdict)
    Page(
        title = session.title,
        onBack = vm::back,
        actions = {
            val text = if (session.mode == PracticeSession.Mode.DRILL) "🔥 ${session.streak}" else "✓ ${session.right}/${session.answered}"
            Text(text, modifier = Modifier.padding(end = 16.dp), fontWeight = FontWeight.Bold)
        },
    ) {
        if (q == null) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            ) {
                Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", style = MaterialTheme.typography.displaySmall)
                    Text("All caught up!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("There are no mistakes left to review. Questions you miss later will show up here.")
                }
            }
            Button(onClick = vm::back, modifier = Modifier.fillMaxWidth().optionalFocus(nextFocus).focusRing()) { Text("Back") }
            return@Page
        }
        val answerSide = @Composable {
            ChoiceButtons(
                question = q,
                selected = session.selected,
                onSelect = { session.selected = it },
                reveal = verdict != null,
                focusRequester = answerFocus,
            )
            if (session.showHint && verdict == null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
                ) {
                    Text("💡 ${q.hint}", modifier = Modifier.padding(14.dp))
                }
            }
            if (verdict == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = { session.showHint = true }, modifier = Modifier.focusRing()) { Text("Hint") }
                    Button(onClick = { vm.check(session) }, modifier = Modifier.weight(1f).focusRing()) { Text("Check") }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (verdict) "✓ Correct!" else "✗ Not quite",
                            color = if (verdict) rightColor else wrongColor,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                        )
                        if (!verdict) Text("Answer: ${lettered(q, q.answer)}", fontWeight = FontWeight.SemiBold)
                        Text(q.solution, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                Button(onClick = session::next, modifier = Modifier.fillMaxWidth().optionalFocus(nextFocus).focusRing()) {
                    Text("Next question")
                }
            }
        }
        if (LocalWide.current) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Column(Modifier.weight(1.15f)) { QuestionCard(q, number = null) }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) { answerSide() }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuestionCard(q, number = null)
                answerSide()
            }
        }
    }
}
