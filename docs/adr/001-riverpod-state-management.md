# ADR 001: Выбор Riverpod для управления состоянием

**Дата:** 2025-02-28  
**Статус:** Принято

## Контекст

Приложению требуется управление состоянием с следующими требованиями:
- Реактивное обновление UI при изменении данных
- Внедрение зависимостей (DI) для слоёв data и domain
- Минимизация boilerplate-кода
- Поддержка тестирования
- Возможность инвалидации кэша и перезагрузки данных

## Решение

Выбран **Riverpod** (flutter_riverpod) по следующим причинам:

### Преимущества
1. **Встроенный DI** — провайдеры служат контейнером зависимостей
2. **Типобезопасность** — компилятор отлавливает ошибки доступа
3. **Автоматическое управление жизненным циклом** — dispose при отсутствии слушателей
4. **Инвалидация кэша** — `ref.invalidate()` для перезагрузки данных
5. **Минимум boilerplate** — не требуется создавать Cubits/Blocs
6. **Тестируемость** — легко мокать провайдеры в тестах
7. **Code generation** — riverpod_generator для автоматизации

### Пример использования
```dart
final yearSlicesRepositoryProvider = Provider<YearSlicesRepository>((ref) {
  final remoteDataSource = ref.watch(isDayOffApiClientProvider);
  final localDataSource = ref.watch(yearCacheDataSourceProvider);
  return YearSlicesRepositoryImpl(
    remoteDataSource: remoteDataSource,
    localDataSource: localDataSource,
  );
});
```

## Альтернативы

### BLoC
- **Плюсы:** Чёткая архитектура, отладка через DevTools
- **Минусы:** Много boilerplate, сложнее DI

### Provider
- **Плюсы:** Простой для маленьких приложений
- **Минусы:** Нет встроенной инвалидации, менее типобезопасен

### MobX
- **Плюсы:** Автоматическое отслеживание зависимостей
- **Минусы:** Требует codegen, менее популярен в Flutter-сообществе

## Последствия

### Положительные
- Упрощённая архитектура
- Лёгкое тестирование через переопределение провайдеров
- Автоматическая очистка ресурсов

### Отрицательные
- Меньше разработчиков знакомы с Riverpod vs BLoC
- Требуется изучение концепций (Provider, Ref, StateNotifier)

## Ссылки
- [Riverpod Documentation](https://riverpod.dev/)
- [flutter_riverpod pub.dev](https://pub.dev/packages/flutter_riverpod)
