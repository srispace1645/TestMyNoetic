package com.testmynoetic.core

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlinx.serialization.json.Json

/** One grade + season worth of questions, e.g. Grade 4 Fall. */
class QuestionBank(val info: BankInfo, val sets: List<QuestionSet>) {

    val allQuestions: List<Question> = sets.flatMap { it.questions }

    private val byId = allQuestions.associateBy { it.id }

    fun question(id: String): Question? = byId[id]

    fun byTopic(topic: Topic): List<Question> = allQuestions.filter { it.topic == topic }

    /** Days until the contest window opens; 0 while it's open, negative once it's over. */
    fun daysUntilContest(today: LocalDate): Long {
        val start = LocalDate.parse(info.contestStart)
        val end = LocalDate.parse(info.contestEnd)
        return when {
            today.isBefore(start) -> ChronoUnit.DAYS.between(today, start)
            today.isAfter(end) -> -ChronoUnit.DAYS.between(end, today)
            else -> 0
        }
    }

    companion object {
        val json = Json { ignoreUnknownKeys = true }

        /** Reads every bank listed in banks/manifest.json from the classpath. */
        fun loadAll(): List<QuestionBank> {
            val manifest = json.decodeFromString<Manifest>(readResource("banks/manifest.json"))
            return manifest.banks.map { info ->
                QuestionBank(info, info.sets.map { json.decodeFromString<QuestionSet>(readResource("banks/$it")) })
            }
        }

        private fun readResource(path: String): String {
            val stream = QuestionBank::class.java.classLoader?.getResourceAsStream(path)
                ?: error("Missing resource $path")
            return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
        }
    }
}
