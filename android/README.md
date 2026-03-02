# Zepe Android MVP

Native Android MVP for Zepe using Kotlin + Jetpack Compose.

## Requirements

- JDK 17
- Android SDK (API 34)

## Build

```bash
cd android
./gradlew.bat :app:assembleDebug
```

## Tests

```bash
cd android
./gradlew.bat :app:testDebugUnitTest
./gradlew.bat :app:connectedDebugAndroidTest
```

## App Scope (MVP)

- Salary input
- Year input
- Calculate payout schedule for 12 months
- Show advance/rest payment values and dates
- Handle invalid input and API loading errors
