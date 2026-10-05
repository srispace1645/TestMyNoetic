# Mathlete Prep: Grade 4 Fall math contest practice

An Android app that helps kids practice for the Noetic Learning Math Contest
(Grade 4, Fall 2026, contest window Nov 12–25). It runs on Android phones,
tablets and Fire TV. It works fully offline, has no ads or accounts, and keeps
progress on the device only.

## What's inside
- **10 practice tests** (200 original questions). Each has 20 questions, a
  45-minute timer and no feedback until the end, like the real contest. You
  get a score out of 100, a topic breakdown and a solution for every question.
- **Shuffle Test**: a new 20-question mix from all the tests.
- **Practice by Topic**: one question at a time, with a hint and an instant solution.
- **Quick Drill**: endless auto-made warm-up questions with a streak counter.
- **Review Mistakes**: missed questions come back until they're answered right twice in a row.
- **My Progress**: test history and how you're doing in each topic.

Everything works with a TV remote: arrow keys to move, the center button to
select, Back to go back. Remotes with number buttons can type answers directly.

## Get the app file (APK)
1. On GitHub, open this repo's **Actions** tab and click the latest green **Build app** run.
2. Scroll down to **Artifacts** and download **mathlete-prep-apk**. It's a zip file.
3. Unzip it to get `app-debug.apk`.

The app needs Android 5.1 or newer.

## Install on an Android phone or tablet
Copy `app-debug.apk` to the phone and tap it. Android will ask you to allow
installs from that app (Files or Chrome). Allow it, then tap **Install**.

## Install on a Fire TV from a Windows laptop
This works on Android-based Fire TVs (Fire TV Stick, Lite, 4K, 4K Max, Cube and
Fire TV smart TVs). The 2025–26 Fire TV Stick 4K Select and Stick HD run Vega OS
and can't install outside apps.

The laptop and the Fire TV must be on the same Wi-Fi.

**On the Fire TV (one time only)**
1. Go to **Settings → My Fire TV → About**. Select the device name (for example "Fire TV Stick Lite") **7 times** until it says you're a developer.
2. Press Back. Open **Developer Options** and turn on **ADB debugging**. If you see **Apps from Unknown Sources**, turn that on too.
3. Go to **Settings → My Fire TV → About → Network** and write down the **IP address** (it looks like `192.168.1.23`).

**On the laptop**
1. Download **SDK Platform-Tools for Windows** from Google:
   https://developer.android.com/tools/releases/platform-tools
2. Unzip it. You'll get a folder named `platform-tools`. Move it to `C:\platform-tools`.
3. Copy `app-debug.apk` into `C:\platform-tools`.
4. Open the Start menu, type `cmd` and open **Command Prompt**. Type these, pressing Enter after each one (use your TV's IP address):
   ```
   cd C:\platform-tools
   adb connect 192.168.1.23:5555
   ```
5. The TV will ask **"Allow USB debugging?"**. Tick **Always allow from this computer** and choose **OK**.
   (If the laptop says "failed to authenticate", run the `adb connect` line again.)
6. Install the app:
   ```
   adb install -r app-debug.apk
   ```
   Wait for the word `Success`.
7. On the TV, open **Apps** (the three-squares icon) and choose **Mathlete Prep**. You can move it to the front row with the remote's Menu button.

To update later, download the newest APK and repeat steps 3, 4 and 6.

## For developers
- `core/` is plain Kotlin: question model, answer checker, test grading,
  drills, progress. Run `./gradlew :core:test` with any JDK 17+ (no Android SDK needed).
- `core/src/main/resources/banks/` holds the question bank as JSON. Add a new
  grade or season by adding a folder and listing it in `manifest.json`.
- Every bank answer is re-solved independently in
  `core/src/test/kotlin/com/testmynoetic/core/verify/`.
- `app/` is the Jetpack Compose UI. It's only included in the build when an
  Android SDK is installed (`ANDROID_HOME` or `local.properties`).
- `app/src/test/` has Robolectric tests that drive the app with remote-control
  keys and take TV and phone screenshots. CI publishes the screenshots to the
  `ci-screenshots` branch.
- `tools/make_art.py` redraws the launcher icons and the Fire TV banner.
- `reference/` holds the source PDFs and `STYLE_NOTES.md`. Keep this repo
  private while those PDFs are in it.
