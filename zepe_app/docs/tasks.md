# Zepe Flutter — Статус реализации (Этап 1)

## Реализованные компоненты

### ✅ Core Layer

**Error Handling:**
- `app_error.dart` — базовые классы ошибок (NetworkError, ServerError, CacheError, ValidationError, UnknownError)
- `failure.dart` — классы Failures для функционального подхода

**Network:**
- `dio_client.dart` — HTTP клиент с настройками таймаутов
- `retry_policy.dart` — политика повторных запросов
- `network_info.dart` — интерфейс проверки подключения

**Utils:**
- `date_utils.dart` — форматирование дат (ru-RU)
- `money_utils.dart` — расчёты зарплаты
- `logger.dart` — логирование

**Constants:**
- `app_constants.dart` — константы приложения

### ✅ Domain Layer

**Entities:**
- `salary_settings.dart` — настройки зарплаты
- `year_slices.dart` — данные о рабочих днях
- `month_meta.dart` — расчётные данные месяца
- `payout.dart` — выплаты для UI

**Repositories (interfaces):**
- `year_slices_repository.dart`
- `settings_repository.dart`

**Use Cases:**
- `get_settings_usecase.dart`
- `save_settings_usecase.dart`
- `get_year_payouts_usecase.dart`

**Services:**
- `payout_calculator.dart` — расчёт MonthMeta

### ✅ Data Layer

**Remote:**
- `isdayoff_api_client.dart` — API клиент isdayoff.ru

**Local:**
- `year_cache_datasource.dart` — Hive кэш
- `settings_local_datasource.dart` — SharedPreferences

**DTO & Mappers:**
- `year_slices_dto.dart`
- `year_slices_mapper.dart`

**Repositories (implementations):**
- `year_slices_repository_impl.dart`
- `settings_repository_impl.dart`

**Providers:**
- `providers.dart` — все Riverpod провайдеры

### ✅ Presentation Layer

**State:**
- `home_state.dart` — состояние главного экрана
- `settings_state.dart` — состояние настроек

**Controllers:**
- `home_controller.dart` — управление главным экраном
- `settings_controller.dart` — управление настройками

**Screens:**
- `home_screen.dart` — главный экран
- `settings_screen.dart` — экран настроек

**Widgets:**
- `month_card.dart` — карточка месяца
- `payout_row.dart` — строка выплаты
- `loading_view.dart` — индикатор загрузки
- `error_view.dart` — отображение ошибки

### ✅ App Layer

- `app.dart` — корневой виджет
- `router.dart` — GoRouter конфигурация
- `theme/app_theme.dart` — темы Material 3
- `theme/app_colors.dart` — цветовая палитра

### ✅ ADR Documents

- `001-riverpod-state-management.md`
- `002-dio-http-client.md`
- `003-hive-shared-preferences-storage.md`
- `004-cache-ttl-fallback-policy.md`

## Требуется для запуска

```bash
cd zepe_app

# Установить зависимости
flutter pub get

# Запустить code generation (для Freezed)
flutter pub run build_runner build --delete-conflicting-outputs

# Запустить приложение
flutter run
```

## Следующие этапы

### Этап 2: Интеграция с API
- [ ] Реальная парсинг ответа isdayoff.ru
- [ ] Обработка формата API (разделение на месяцы)
- [ ] Тесты на API client

### Этап 3: UI Polishing
- [ ] Анимации переключения месяцев
- [ ] Pull-to-refresh
- [ ] Индикатор stale данных
- [ ] Тёмная тема

### Этап 4: Тестирование
- [ ] Unit тесты для PayoutCalculator
- [ ] Unit тесты для UseCases
- [ ] Widget тесты для MonthCard
- [ ] Integration тесты

### Этап 5: Дополнительные фичи
- [ ] Экран годовой сводки (YearSummaryScreen)
- [ ] Export в CSV/Excel
- [ ] Виджеты на home screen
- [ ] Уведомления о днях выплат

### Этап 6: Production готовность
- [ ] Sentry/Firebase Crashlytics
- [ ] Analytics
- [ ] Локализация (i18n)
- [ ] Accessibility

### Этап 7: Публикация
- [ ] Иконки
- [ ] Скриншоты
- [ ] Описание в stores
- [ ] CI/CD

## Структура проекта

```
zepe_app/
├── lib/
│   ├── app/
│   │   ├── app.dart
│   │   ├── router.dart
│   │   └── theme/
│   ├── core/
│   │   ├── error/
│   │   ├── network/
│   │   ├── utils/
│   │   └── constants/
│   ├── features/
│   │   └── salary_calendar/
│   │       ├── presentation/
│   │       ├── domain/
│   │       └── data/
│   └── main.dart
├── docs/
│   └── adr/
├── pubspec.yaml
└── README.md
```
