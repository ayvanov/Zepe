# Zepe Flutter App

Зарплатный календарь — приложение для расчёта аванса и остатка зарплаты по производственному календарю.

## Архитектура

Приложение следует **Clean Architecture** с разделением на слои:

```
lib/
├── app/              # App-level code (router, theme)
├── core/             # Shared utilities, error handling, network
└── features/         # Feature modules
    └── salary_calendar/
        ├── presentation/  # UI, controllers, state
        ├── domain/        # Entities, use cases, repositories
        └── data/          # API, cache, repository implementations
```

## Стек технологий

- **State Management:** Riverpod
- **HTTP Client:** Dio
- **Local Storage:** Hive (кэш), SharedPreferences (настройки)
- **Navigation:** GoRouter
- **Code Generation:** Freezed, json_serializable

## Запуск

```bash
# Установить зависимости
flutter pub get

# Запустить code generation
flutter pub run build_runner build --delete-conflicting-outputs

# Запустить приложение
flutter run
```

## Структура проекта

### Domain Layer
- `entities/` — бизнес-объекты (SalarySettings, YearSlices, MonthMeta)
- `repositories/` — абстрактные контракты
- `usecases/` — бизнес-правила
- `services/` — вспомогательная логика (PayoutCalculator)

### Data Layer
- `datasources/` — remote (API) и local (кэш) источники
- `dto/` — модели для сериализации
- `mappers/` — преобразование DTO ↔ Domain
- `repositories/` — реализации репозиториев

### Presentation Layer
- `screens/` — экраны приложения
- `widgets/` — переиспользуемые виджеты
- `controllers/` — StateNotifier контроллеры
- `state/` — модели состояния

## Настройки

По умолчанию:
- Оклад: 100 000 ₽
- Коэффициент: 1.0
- Дней аванса: 15
- День выдачи аванса: 25
- День выдачи остатка: 10

## Кэширование

- Данные кэшируются на 24 часа
- При отсутствии сети используются устаревшие данные
- Индикатор "сохранённых данных" показывается в UI

## API

Используется [isdayoff.ru API](https://isdayoff.ru/):
- `GET /api/getdata?year={year}` — данные за год
- `GET /api/getdata?year={year}&month={month}` — данные за месяц

## ADR (Архитектурные решения)

См. [`docs/adr/`](docs/adr/):
1. [Выбор Riverpod](docs/adr/001-riverpod-state-management.md)
2. [Выбор Dio](docs/adr/002-dio-http-client.md)
3. [Hive + SharedPreferences](docs/adr/003-hive-shared-preferences-storage.md)
4. [Политика кэширования](docs/adr/004-cache-ttl-fallback-policy.md)

## Лицензия

MIT
