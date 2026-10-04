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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.testmynoetic.core.DrillKind
import com.testmynoetic.core.Topic
import com.testmynoetic.prep.AppViewModel
import com.testmynoetic.prep.PracticeSession

@Composable
fun TopicsScreen(vm: AppViewModel) {
    val emoji = mapOf(Topic.NUMBER to "🔢", Topic.ALGEBRA to "🧩", Topic.GEOMETRY to "📐", Topic.LOGIC to "🎲")
    Page(title = "Practice by Topic", onBack = vm::back) {
        Topic.entries.forEach { t ->
            val acc = vm.progress.accuracy(t)
            MenuCard(
                emoji.getValue(t), t.label,
                "${vm.bank.byTopic(t).size} questions · " + if (acc == null) "not started" else "$acc% right so far",
            ) { vm.startTopic(t) }
        }
    }
}

@Composable
fun DrillsScreen(vm: AppViewModel) {
    Page(title = "Quick Drill", onBack = vm::back) {
        Text(
            "Short questions that never run out. Best streak: ${vm.progress.bestDrillStreak}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        MenuCard("🌀", "Mixed drill", "A little bit of everything") { vm.startDrill(DrillKind.entries, "Mixed drill") }
        Topic.entries.forEach { t ->
            Text(t.label, style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(top = 8.dp))
            DrillKind.entries.filter { it.topic == t }.forEach { k ->
                MenuCard("•", k.label, "Endless ${k.label.lowercase()} questions") { vm.startDrill(listOf(k), k.label) }
            }
        }
    }
}

@Composable
fun PracticeScreen(vm: AppViewModel, session: PracticeSession) {
    val q = session.question
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
                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🎉", style = MaterialTheme.typography.displaySmall)
                    Text("All caught up!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("There are no mistakes left to review. Questions you miss later will show up here.")
                }
            }
            Button(onClick = vm::back, modifier = Modifier.fillMaxWidth()) { Text("Back") }
            return@Page
        }
        QuestionCard(q, number = null)
        val verdict = session.verdict
        AnswerInput(
            question = q,
            value = session.input,
            onValueChange = { session.input = it },
            enabled = verdict == null,
            onDone = { vm.check(session) },
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
                OutlinedButton(onClick = { session.showHint = true }, enabled = !session.showHint) { Text("Hint") }
                Button(onClick = { vm.check(session) }, enabled = session.input.isNotBlank(), modifier = Modifier.weight(1f)) {
                    Text("Check")
                }
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
                    if (!verdict) Text("Answer: ${q.unitBefore}${q.answer} ${q.unit}".trim(), fontWeight = FontWeight.SemiBold)
                    Text(q.solution, style = MaterialTheme.typography.bodyMedium)
                }
            }
            Button(onClick = session::next, modifier = Modifier.fillMaxWidth()) { Text("Next question") }
        }
    }
}
