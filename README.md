# Math Contest Prep: Grade 4 Fall

An Android app that helps kids practice for the Noetic Learning Math Contest
(Grade 4, Fall 2026, contest window Nov 12–25). It works fully offline, has
no ads or accounts, and keeps progress on the phone only.

## What's inside
- **10 practice tests** (200 original questions). Each has 20 questions, a
  45-minute timer and no feedback until the end, like the real contest. You
  get a score out of 100, a topic breakdown and a solution for every question.
- **Shuffle Test**: a new 20-question mix from all the tests.
- **Practice by Topic**: one question at a time, with a hint and an instant solution.
- **Quick Drill**: endless auto-made warm-up questions with a streak counter.
- **Review Mistakes**: missed questions come back until they're answered right twice in a row.
- **My Progress**: test history and how you're doing in each topic.

## Getting the app onto an Android phone
1. On GitHub, open this repo's **Actions** tab and click the latest green **Build app** run.
2. Scroll down to **Artifacts** and download **math-contest-prep-apk**. It's a zip file.
3. Unzip it to get `app-debug.apk`, then copy it to the phone (or download it on the phone and open it with the Files app).
4. Tap the APK. Android will ask you to allow installs from that app (Files or Chrome). Allow it, then tap **Install**.

The app needs Android 8.0 or newer.

## For developers
- `core/` is plain Kotlin: question model, answer checker, test grading,
  drills, progress. Run `./gradlew :core:test` with any JDK 17+ (no Android SDK needed).
- `core/src/main/resources/banks/` holds the question bank as JSON. Add a new
  grade or season by adding a folder and listing it in `manifest.json`.
- Every bank answer is re-solved independently in
  `core/src/test/kotlin/com/testmynoetic/core/verify/`.
- `app/` is the Jetpack Compose UI. It's only included in the build when an
  Android SDK is installed (`ANDROID_HOME` or `local.properties`).
- `reference/` holds the source PDFs and `STYLE_NOTES.md`. Keep this repo
  private while those PDFs are in it.
