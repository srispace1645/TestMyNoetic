package com.testmynoetic.core

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/** The four Noetic topic areas, worded for kids and parents. */
@Serializable
enum class Topic(val label: String) {
    NUMBER("Numbers & Computation"),
    ALGEBRA("Patterns & Algebra"),
    GEOMETRY("Geometry & Measurement"),
    LOGIC("Counting, Probability & Logic"),
}

/** NUMBER answers are compared by value ("0.5" = "1/2"); WORD answers by text (names, days, times). */
@Serializable
enum class AnswerKind { NUMBER, WORD }

@Serializable
data class Question(
    val id: String,
    val topic: Topic,
    val skill: String,
    /** 1 = warm-up, 2 = one or two steps, 3 = contest challenge. */
    val difficulty: Int,
    val prompt: String,
    val answer: String,
    val kind: AnswerKind = AnswerKind.NUMBER,
    /** Other answers that also count as correct, e.g. "Sat" for "Saturday". */
    val accept: List<String> = emptyList(),
    /** Shown before the answer blank, e.g. "$". */
    val unitBefore: String = "",
    /** Shown after the answer blank, e.g. "sticks". */
    val unit: String = "",
    val hint: String,
    val solution: String,
    val figure: Figure? = null,
    /**
     * Hand-written wrong answers for the tap-and-pick options, usually common
     * mistakes. [ChoiceMaker] fills in the rest for number answers.
     */
    val choices: List<String> = emptyList(),
)

@Serializable
data class QuestionSet(
    val id: String,
    val title: String,
    val questions: List<Question>,
)

@Serializable
data class BankInfo(
    val id: String,
    val grade: Int,
    val season: String,
    val title: String,
    /** First and last day of the contest window, ISO dates like 2026-11-12. */
    val contestStart: String,
    val contestEnd: String,
    val sets: List<String>,
)

@Serializable
data class Manifest(val banks: List<BankInfo>)

/** Pictures are described as data and drawn by the app, so no image files are needed. */
@Serializable
sealed class Figure {
    /** Lines of fixed-width text, e.g. a vertical addition problem. "?" and "□" are drawn as boxes. */
    @Serializable
    @SerialName("text")
    data class TextArt(val lines: List<String>) : Figure()

    /** A number pyramid, top row first. "" is an empty box and "?" is the box to find. */
    @Serializable
    @SerialName("pyramid")
    data class Pyramid(val rows: List<List<String>>) : Figure()

    /** Two overlapping circles. Region texts can be numbers, "?" or "". */
    @Serializable
    @SerialName("venn")
    data class Venn(
        val leftLabel: String,
        val rightLabel: String,
        val left: String = "",
        val both: String = "",
        val right: String = "",
    ) : Figure()

    /** A grid of squares. Shaded cells are numbered row by row from 0. */
    @Serializable
    @SerialName("grid")
    data class Grid(val rows: Int, val cols: Int, val shaded: List<Int> = emptyList()) : Figure()

    @Serializable
    @SerialName("clock")
    data class Clock(val hour: Int, val minute: Int) : Figure()

    /** A balanced scale with the items on each side. */
    @Serializable
    @SerialName("balance")
    data class Balance(val left: List<String>, val right: List<String>) : Figure()

    @Serializable
    @SerialName("bars")
    data class BarChart(
        val title: String,
        val labels: List<String>,
        val values: List<Int>,
        val step: Int = 1,
    ) : Figure()

    @Serializable
    @SerialName("table")
    data class Table(val header: List<String>, val rows: List<List<String>>) : Figure()

    /**
     * A shape drawn from corner points (in grid units, y pointing down).
     * Side i joins point i to point i + 1; labels are keyed by side index.
     */
    @Serializable
    @SerialName("shape")
    data class Shape(
        val points: List<Pt>,
        val sideLabels: Map<Int, String> = emptyMap(),
        val showGrid: Boolean = false,
    ) : Figure()
}

@Serializable
data class Pt(val x: Double, val y: Double)
