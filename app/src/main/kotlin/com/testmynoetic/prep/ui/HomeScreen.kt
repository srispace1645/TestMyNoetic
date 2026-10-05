package com.testmynoetic.prep.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.testmynoetic.prep.AppViewModel
import com.testmynoetic.prep.Screen
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(vm: AppViewModel) {
    val info = vm.bank.info
    val days = remember { vm.bank.daysUntilContest(LocalDate.now()) }
    val mistakes = vm.progress.mistakeIds().size
    val first = remember { FocusRequester() }
    TvFocus(first)
    Page(title = "Mathlete Prep", onBack = null) {
        Text(
            "${info.title}: practice for the math contest. 20 short-answer questions, 45 minutes, no calculator.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        CountdownCard(days, info.contestStart, info.contestEnd)
        MenuGrid(
            listOf(
                MenuItem("📝", "Practice Tests", "${vm.bank.sets.size} full tests under contest rules") { vm.open(Screen.Sets) },
                MenuItem("🔀", "Shuffle Test", "A fresh mix of 20 questions from all the tests") { vm.startShuffleTest() },
                MenuItem("🎯", "Practice by Topic", "One question at a time, with hints and solutions") { vm.open(Screen.Topics) },
                MenuItem("⚡", "Quick Drill", "Endless warm-up questions. How long can your streak go?") { vm.open(Screen.Drills) },
                MenuItem(
                    "🔁", "Review Mistakes",
                    if (mistakes == 0) "Nothing to review yet" else "$mistakes question${if (mistakes == 1) "" else "s"} to fix",
                ) { vm.startReview() },
                MenuItem("📈", "My Progress", "Test scores and strong and weak topics") { vm.open(Screen.Stats) },
            ),
            firstFocus = first,
        )
    }
}

@Composable
private fun CountdownCard(days: Long, startIso: String, endIso: String) {
    val fmt = DateTimeFormatter.ofPattern("MMM d", Locale.US)
    val start = LocalDate.parse(startIso)
    val end = LocalDate.parse(endIso)
    val window = "${start.format(fmt)} – ${end.format(fmt)}, ${end.year}"
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Column(Modifier.padding(16.dp)) {
            when {
                days > 0 -> {
                    Text("$days day${if (days == 1L) "" else "s"} to go", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("Contest window: $window", style = MaterialTheme.typography.bodyMedium)
                }
                days == 0L -> {
                    Text("Contest time!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("The contest window is open ($window). Good luck!", style = MaterialTheme.typography.bodyMedium)
                }
                else -> {
                    Text("Contest finished", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text("The $window window is over. Keep practicing for spring!", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
