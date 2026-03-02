# Android MVP Design

**Date:** 2026-03-02
**Project:** Zepe Android MVP

## Scope
- Native Android app in `android/` alongside existing web project.
- Stack: Kotlin + Jetpack Compose + Material 3.
- Logic: 1:1 перенос текущей расчетной логики Zepe (`MonthMeta`/`ZepeCalc`).

## Architecture
- `data`: API client for `https://isdayoff.ru/api/getdata` and DTO mapping.
- `domain`: salary calendar calculation models and use-cases.
- `ui`: Compose screen, ViewModel, state/effects.

## MVP UX
- Single `CalculatorScreen`.
- Inputs: salary, year (default current year).
- Action: "Рассчитать".
- Output: 12 months with advance/rest payment values and dates.

## Data Flow
1. User enters salary/year and taps calculate.
2. ViewModel validates input and triggers use-case.
3. Repository fetches year data + next January.
4. Domain builds month metadata list.
5. UI renders month cards.

## Error Handling
- Invalid salary: inline validation message.
- Network/API errors: error state + snackbar.

## Out of Scope (MVP)
- Offline cache.
- Settings screen.
- Notifications/widgets.
- Multi-language support.

## Testing
- Unit tests for domain calculations (workdays, advance/rest values, payout dates).
- One Compose UI smoke test for successful rendering of month list.
