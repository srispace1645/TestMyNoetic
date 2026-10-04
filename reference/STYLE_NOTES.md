# Style notes for writing questions

These notes come from reading the reference PDFs in this folder. No contest
text is copied into the app; every question in the bank is original.

## Contest format
- 20 short-answer questions, 45 minutes, no calculators.
- 5 points per question, 100 points max, no partial credit.
- Topic areas: computation and number properties, pattern and algebra,
  geometry and measurement, probability and statistics (plus logic and counting).
- Fall 2026 contest window: November 12–25.

## What the real Fall 2024 Grade 3 paper looks like
- Question 1 is a giveaway (a plain addition).
- Difficulty climbs slowly. Questions 15–20 use contest tricks: bar models for
  sum/difference, worst-case picking, "15th from the front and the back",
  Venn overlaps, "hands raised" (heads-and-legs style), river crossings.
- Every question has exactly one answer, nearly always a whole number,
  written in a blank followed by a unit word ("Answer: ____ sticks").
  A few answers are names or days.
- Stories are short and friendly, with kids' names and seasonal themes.
- Pictures are either needed (sticks, cards, Venn circles, digit boxes)
  or just decorative.
- Official solutions are short steps for kids, with a table or bar diagram
  when it helps.

## The official Grade 4 samples step up to
- counting with a rule (2-digit even numbers),
- swapping prices (cupcakes and cookies),
- number pyramids,
- seat swaps solved by working backwards,
- chains of age relationships.

## Cautions about the AI-made practice papers
Some of their questions can't be solved, so don't use them as an answer source:
- `Noetic QuestionPape03262026.pdf` Q1: "3A + 5A + AA = 167" has no digit
  answer (80 + 13A = 167).
- `Noetic QuestionPaper03292026_1.pdf` Q15: "6A + 3A = 89" has no answer
  (90 + 2A = 89). Its answer key solves a different problem.
- Several of them ask two or three things in one question, which real papers never do.

## Rules this app's bank follows
- Each 20-question set has 5 questions from each topic, ordered easy to hard
  (difficulty 1 → 3), starting with a warm-up.
- Every answer is re-solved by an independent computer check in
  `core/src/test/kotlin/com/testmynoetic/core/verify/SetNN.kt`. The build
  fails if any answer disagrees.
- Every written solution must state its own answer.
