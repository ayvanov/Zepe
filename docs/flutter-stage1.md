# Zepe Flutter — Этап 1: Детальная целевая архитектура

## 1) Цели этапа

На этом этапе фиксируем **архитектуру Flutter-приложения** без реализации всех фич:
- разделение на слои (`presentation` / `domain` / `data`);
- модель данных и контракты репозиториев;
- стратегия управления состоянием;
- навигация;
- политика ошибок, кэша и офлайн-поведения;
- шаблон структуры проекта для дальнейшей реализации этапов 2–7.

---

## 2) Архитектурный стиль

Рекомендуемый стиль: **Clean-ish architecture + feature-first в presentation + layer-first в domain/data**.

### Почему так
- Логику расчётов (`MonthMeta`) нужно легко тестировать и переносить 1:1.
- UI должен быть независим от API и локального хранения.
- Изменения API (`isdayoff.ru`) не должны ломать экраны напрямую.
- Упрощается покрытие unit/widget/integration тестами.

---

## 3) Выбранный стек

## Обязательные пакеты
- `flutter_riverpod` — state management и DI.
- `dio` — HTTP-клиент (таймауты, интерцепторы, ретраи).
- `intl` — форматирование дат/денег (`ru-RU`).
- `shared_preferences` — быстрые пользовательские настройки.
- `hive` (+ `hive_flutter`) — кэш year-slices с TTL.

## Опционально
- `go_router` — декларативная навигация.
- `freezed` + `json_serializable` — immutable DTO/state-модели.
- `sentry_flutter` / `firebase_crashlytics` — ошибки в проде.

---

## 4) Схема слоёв

```/dev/null/architecture.txt#L1-30
Presentation (Flutter UI)
  ├─ Screens/Widgets
  ├─ ViewModels / Notifiers (Riverpod)
  └─ UI State (loading/data/error)

Domain
  ├─ Entities (MonthMeta, SalarySettings, YearSlices)
  ├─ UseCases (GetYearPayouts, UpdateSalarySettings, etc.)
  ├─ Repositories (abstract contracts)
  └─ Pure business rules/calculations

Data
  ├─ Remote (IsDayOff API client)
  ├─ Local (Hive cache, SharedPreferences settings)
  ├─ DTO + mappers (DTO <-> Domain)
  └─ Repository implementations
```

Правило зависимостей:
- `presentation -> domain`
- `data -> domain`
- `domain` ни от кого не зависит.

---

## 5) Структура директорий (целевая)

```/dev/null/tree.txt#L1-80
lib/
  app/
    app.dart
    router.dart
    theme/
      app_theme.dart
      app_colors.dart
    localization/
      l10n.dart

  core/
    error/
      app_error.dart
      failure.dart
    network/
      dio_client.dart
      retry_policy.dart
      network_info.dart
    utils/
      date_utils.dart
      money_utils.dart
      logger.dart
    constants/
      app_constants.dart

  features/
    salary_calendar/
      presentation/
        screens/
          home_screen.dart
          settings_screen.dart
          year_summary_screen.dart   // optional
        widgets/
          month_card.dart
          payout_row.dart
          loading_view.dart
          error_view.dart
        state/
          home_state.dart
          settings_state.dart
        controllers/
          home_controller.dart
          settings_controller.dart

      domain/
        entities/
          month_meta.dart
          payout.dart
          salary_settings.dart
          year_slices.dart
        repositories/
          year_slices_repository.dart
          settings_repository.dart
        usecases/
          get_year_payouts_usecase.dart
          get_settings_usecase.dart
          save_settings_usecase.dart
        services/
          payout_calculator.dart

      data/
        datasources/
          remote/
            isdayoff_api_client.dart
          local/
            year_cache_datasource.dart
            settings_local_datasource.dart
        dto/
          year_slices_dto.dart
        mappers/
          year_slices_mapper.dart
        repositories/
          year_slices_repository_impl.dart
          settings_repository_impl.dart

  main.dart
```

---

## 6) Domain-модель (контракты)

## 6.1 Сущности

- `SalarySettings`
  - `salary` (double/int, >= 0)
  - `salaryMultiplier` (double, default `1.0`)
  - `advanceDays` (int, default `15`)
  - `advancePayDay` (int, default `25`)
  - `restPayDay` (int, default `10`)

- `YearSlices`
  - `year` (int)
  - `months` (`List<String>` длиной 13: 12 месяцев + январь следующего года)

- `MonthMeta`
  - `monthNum` (1..12)
  - `year`
  - `salary`
  - производные поля:
    - `totalDays`
    - `workdays`
    - `holidays`
    - `salaryPerDay`
    - `advanceWorkdays`
    - `advanceValue`
    - `advanceDate`
    - `restValue`
    - `restDate`

- `Payout` (для UI)
  - `type` (`advance` | `rest`)
  - `value`
  - `date`
  - `isPast`

## 6.2 Репозитории (интерфейсы)

- `YearSlicesRepository`
  - `Future<YearSlices> getYearSlices(int year, {bool forceRefresh = false})`
  - `Future<void> prefetchNextJanuary(int year)`

- `SettingsRepository`
  - `Future<SalarySettings> getSettings()`
  - `Future<void> saveSettings(SalarySettings settings)`

---

## 7) Use Cases

- `GetSettingsUseCase`  
  Загружает настройки (или значения по умолчанию).

- `SaveSettingsUseCase`  
  Валидирует и сохраняет настройки.

- `GetYearPayoutsUseCase`
  1. Получает `SalarySettings`.
  2. Получает `YearSlices` из репозитория.
  3. Через `PayoutCalculator` строит `List<MonthMeta>`.

- `PrefetchNextYearJanuaryUseCase` (опционально отдельно)
  Поддерживает быстрый расчёт `restDate` на декабрь.

---

## 8) Data слой

## 8.1 Remote

`IsDayOffApiClient`:
- endpoint: `GET https://isdayoff.ru/api/getdata?year={year}`
- endpoint: `GET https://isdayoff.ru/api/getdata?year={year+1}&month=1`
- настройки:
  - connect/read timeout;
  - retry на сетевые ошибки (ограниченный);
  - валидация ответа (не пустой, ожидаемая длина).

## 8.2 Local cache

`YearCacheDataSource` (Hive):
- ключ: `year:{yyyy}`
- значение:
  - `yearDataRaw`
  - `nextJanuaryRaw`
  - `updatedAt`
- TTL: 24 часа (конфигурируемо в `core/constants`).

Политика:
- если кэш свежий — отдать кэш;
- если кэш протух и сеть ок — обновить;
- если сеть недоступна — вернуть последний кэш + флаг stale (через state).

## 8.3 Settings storage

`SettingsLocalDataSource` (SharedPreferences):
- `salary`
- `salaryMultiplier`
- `advanceDays`
- `advancePayDay`
- `restPayDay`
- (опц.) `themeMode`, `locale`.

---

## 9) Состояние приложения (Riverpod)

## 9.1 HomeState

```/dev/null/home_state.txt#L1-20
HomeState:
  status: initial | loading | data | error
  year: int
  months: List<MonthMeta>
  settings: SalarySettings
  isStaleData: bool
  errorMessage: String?
```

## 9.2 SettingsState

```/dev/null/settings_state.txt#L1-20
SettingsState:
  status: idle | saving | saved | validationError | error
  salaryInput: String
  salaryMultiplier: double
  advanceDays: int
  advancePayDay: int
  restPayDay: int
  validationMessage: String?
```

## 9.3 Провайдеры (минимальный набор)

- `dioProvider`
- `isDayOffApiProvider`
- `yearCacheDataSourceProvider`
- `settingsLocalDataSourceProvider`
- `yearSlicesRepositoryProvider`
- `settingsRepositoryProvider`
- `payoutCalculatorProvider`
- `homeControllerProvider`
- `settingsControllerProvider`

---

## 10) Навигация

Минимальные маршруты:
- `/` → `HomeScreen`
- `/settings` → `SettingsScreen`
- `/summary` → `YearSummaryScreen` (опционально)

Правила:
- `HomeScreen` — источник истины по выбранному году.
- После сохранения в `SettingsScreen`:
  - возвращаемся назад;
  - `HomeController` перезагружает расчёт (invalidate provider).

---

## 11) Форматирование и локаль

- Локаль по умолчанию: `ru`.
- Деньги: `NumberFormat.currency(locale: 'ru_RU', symbol: '₽')`.
- Месяцы/даты: `DateFormat` с русскими шаблонами.
- Все форматтеры вынести в `core/utils` или `core/formatters`.

---

## 12) Ошибки и обработка исключений

Единый тип ошибок:
- `NetworkFailure`
- `ServerFailure`
- `CacheFailure`
- `ValidationFailure`
- `UnknownFailure`

UI-реакция:
- friendly message + retry.
- Не показывать stacktrace пользователю.
- Логировать технические детали в `logger`.

---

## 13) Нефункциональные требования

- Производительность:
  - пересчёт `MonthMeta` не должен блокировать UI (при необходимости `compute`).
- Тестируемость:
  - `PayoutCalculator` — чистая функция/сервис без внешних зависимостей.
- Расширяемость:
  - возможность добавить альтернативный API-клиент без правки `presentation`.

---

## 14) ADR (архитектурные решения, зафиксировать)

Создать `docs/adr/` и минимум 4 ADR:
1. Выбор `Riverpod`.
2. Выбор `Dio`.
3. Выбор `Hive + SharedPreferences`.
4. Политика кэша и fallback (TTL + stale data).

Формат ADR:
- Контекст
- Решение
- Альтернативы
- Последствия

---

## 15) Готовность этапа 1 (Definition of Done)

Этап 1 считается завершённым, если:
- утверждена структура директорий;
- описаны domain-сущности и репозитории;
- выбраны и зафиксированы ключевые пакеты;
- описаны состояния экранов и провайдеры;
- описаны маршруты и переходы;
- задокументирована стратегия кэширования и обработки ошибок;
- добавлены ADR с архитектурными решениями.

---
