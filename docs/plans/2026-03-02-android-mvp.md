# Android MVP Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Build a native Android MVP in `android/` that reproduces Zepe salary payout calculations and displays 12 monthly payout cards.

**Architecture:** Create a standalone Android module with clear `data/domain/ui` separation. Keep logic deterministic and testable in domain layer, with ViewModel orchestrating state updates and API calls.

**Tech Stack:** Kotlin, Jetpack Compose, Material 3, AndroidX Lifecycle ViewModel, Kotlin Coroutines, JUnit, Compose UI Test.

---

### Task 1: Bootstrap Android Project

**Files:**
- Create: `android/settings.gradle.kts`
- Create: `android/build.gradle.kts`
- Create: `android/gradle.properties`
- Create: `android/app/build.gradle.kts`
- Create: `android/app/src/main/AndroidManifest.xml`
- Create: `android/app/src/main/java/com/zepe/android/MainActivity.kt`
- Create: `android/app/src/main/res/values/strings.xml`
- Create: `android/app/src/main/res/values/themes.xml`

**Step 1: Write the failing test**
- Create a placeholder instrumentation launch test that references `MainActivity`.

**Step 2: Run test to verify it fails**
- Run: `./gradlew :app:connectedDebugAndroidTest`
- Expected: FAIL because baseline app files do not exist yet.

**Step 3: Write minimal implementation**
- Add Android project scaffolding with Compose-enabled app and launchable `MainActivity`.

**Step 4: Run test to verify it passes**
- Run: `./gradlew :app:assembleDebug`
- Expected: BUILD SUCCESSFUL.

**Step 5: Commit**
- Run:
```bash
git add android
git commit -m "feat(android): bootstrap native compose app"
```

### Task 2: Port Domain Calculation Logic 1:1

**Files:**
- Create: `android/app/src/main/java/com/zepe/android/domain/model/MonthMeta.kt`
- Create: `android/app/src/main/java/com/zepe/android/domain/ZepeCalculator.kt`
- Create: `android/app/src/test/java/com/zepe/android/domain/ZepeCalculatorTest.kt`

**Step 1: Write the failing test**
- Add unit tests for:
  - workday counting by month slice,
  - salary per day calculation,
  - advance value,
  - rest value,
  - payout date derivation.

**Step 2: Run test to verify it fails**
- Run: `./gradlew :app:testDebugUnitTest --tests "*ZepeCalculatorTest"`
- Expected: FAIL because classes are not implemented.

**Step 3: Write minimal implementation**
- Implement `MonthMeta` and calculator logic equivalent to current TypeScript behavior.

**Step 4: Run test to verify it passes**
- Run: `./gradlew :app:testDebugUnitTest --tests "*ZepeCalculatorTest"`
- Expected: PASS.

**Step 5: Commit**
- Run:
```bash
git add android/app/src/main/java/com/zepe/android/domain android/app/src/test/java/com/zepe/android/domain
git commit -m "feat(android): port zepe salary calculation domain"
```

### Task 3: Implement Data Layer and API Integration

**Files:**
- Create: `android/app/src/main/java/com/zepe/android/data/api/IsDayOffApi.kt`
- Create: `android/app/src/main/java/com/zepe/android/data/repo/CalendarRepository.kt`
- Create: `android/app/src/test/java/com/zepe/android/data/repo/CalendarRepositoryTest.kt`

**Step 1: Write the failing test**
- Add tests for year slicing behavior and next January prefetch handling.

**Step 2: Run test to verify it fails**
- Run: `./gradlew :app:testDebugUnitTest --tests "*CalendarRepositoryTest"`
- Expected: FAIL because repository is missing.

**Step 3: Write minimal implementation**
- Implement API client and repository methods to fetch year and next January slices.

**Step 4: Run test to verify it passes**
- Run: `./gradlew :app:testDebugUnitTest --tests "*CalendarRepositoryTest"`
- Expected: PASS.

**Step 5: Commit**
- Run:
```bash
git add android/app/src/main/java/com/zepe/android/data android/app/src/test/java/com/zepe/android/data
git commit -m "feat(android): add calendar api repository"
```

### Task 4: Build UI and ViewModel for MVP Screen

**Files:**
- Create: `android/app/src/main/java/com/zepe/android/ui/CalculatorViewModel.kt`
- Create: `android/app/src/main/java/com/zepe/android/ui/CalculatorUiState.kt`
- Create: `android/app/src/main/java/com/zepe/android/ui/CalculatorScreen.kt`
- Modify: `android/app/src/main/java/com/zepe/android/MainActivity.kt`

**Step 1: Write the failing test**
- Add a Compose UI smoke test asserting that, with prepared state, month cards are displayed.

**Step 2: Run test to verify it fails**
- Run: `./gradlew :app:connectedDebugAndroidTest`
- Expected: FAIL because UI components are not implemented.

**Step 3: Write minimal implementation**
- Implement salary/year inputs, calculate action, loading/error/content states, and month list rendering.

**Step 4: Run test to verify it passes**
- Run: `./gradlew :app:connectedDebugAndroidTest`
- Expected: PASS for smoke test.

**Step 5: Commit**
- Run:
```bash
git add android/app/src/main/java/com/zepe/android/ui android/app/src/main/java/com/zepe/android/MainActivity.kt
git commit -m "feat(android): implement calculator screen and viewmodel"
```

### Task 5: Final Verification and Documentation

**Files:**
- Modify: `docs/tasks.md`
- Create: `android/README.md`

**Step 1: Write the failing test**
- Add missing assertions for error states if not covered.

**Step 2: Run test to verify it fails**
- Run: `./gradlew :app:testDebugUnitTest`
- Expected: FAIL until assertions/handling complete.

**Step 3: Write minimal implementation**
- Finalize error handling and document setup/build/run in `android/README.md`.

**Step 4: Run test to verify it passes**
- Run:
  - `./gradlew :app:testDebugUnitTest`
  - `./gradlew :app:assembleDebug`
- Expected: PASS and BUILD SUCCESSFUL.

**Step 5: Commit**
- Run:
```bash
git add android docs/tasks.md
git commit -m "docs(android): add mvp setup docs and finalize verification"
```
