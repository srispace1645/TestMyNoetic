package com.testmynoetic.prep.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.testmynoetic.core.Topic
import com.testmynoetic.prep.AppViewModel

@Composable
fun StatsScreen(vm: AppViewModel) {
    val p = vm.progress
    var confirmReset by remember { mutableStateOf(false) }
    Page(title = "My Progress", onBack = vm::back) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("${p.topicTotal.values.sum()} questions answered", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("${p.mocks.size} test${if (p.mocks.size == 1) "" else "s"} taken · best drill streak ${p.bestDrillStreak}")
                Text("${p.mistakeIds().size} questions waiting in Review Mistakes")
            }
        }
        Text("Topics", style = MaterialTheme.typography.titleMedium)
        Topic.entries.forEach { t ->
            val total = p.topicTotal[t] ?: 0
            if (total == 0) {
                Text("${t.label}: not started yet", style = MaterialTheme.typography.bodyMedium)
            } else {
                TopicBar(t.label, p.topicRight[t] ?: 0, total)
            }
        }
        val weakest = Topic.entries.filter { (p.topicTotal[it] ?: 0) >= 5 }.minByOrNull { p.accuracy(it) ?: 100 }
        if (weakest != null) {
            Text("Tip: spend some extra time on ${weakest.label}.", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
        Text("Test history", style = MaterialTheme.typography.titleMedium)
        if (p.mocks.isEmpty()) Text("No tests yet. Try Practice Test 1!", style = MaterialTheme.typography.bodyMedium)
        p.mocks.asReversed().forEach { m ->
            Row(Modifier.fillMaxWidth()) {
                Text("${m.dateIso}  ${m.title}", modifier = Modifier.weight(1f))
                Text("${m.correct * 5} / ${m.total * 5}", fontWeight = FontWeight.SemiBold)
            }
        }
        OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.padding(top = 16.dp).focusRing()) { Text("Reset all progress") }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Reset all progress?") },
            text = { Text("This erases test scores, topic stats and the review list on this phone.") },
            confirmButton = { TextButton(onClick = { confirmReset = false; vm.resetProgress() }, modifier = Modifier.focusRing()) { Text("Reset") } },
            dismissButton = { Button(onClick = { confirmReset = false }, modifier = Modifier.focusRing()) { Text("Cancel") } },
        )
    }
}
