# ADR 004: Политика кэширования и fallback

**Дата:** 2025-02-28  
**Статус:** Принято

## Контекст

Приложение работает с API isdayoff.ru, которое может быть недоступно.
Требуется обеспечить:
- Быструю загрузку данных при повторном открытии
- Работу офлайн
- Актуальность данных
- Graceful degradation при проблемах с сетью

## Решение

### TTL-based кэширование с stale data fallback

**Параметры:**
- **TTL:** 24 часа (конфигурируемо в `AppConstants.cacheTtlHours`)
- **Ключ кэша:** `year:{yyyy}`
- **Метаданные:** `updatedAt` timestamp

**Алгоритм загрузки:**

```
1. Проверить кэш
   ├─ Нет кэша → Запрос к API → Кэш → Возврат
   └─ Кэш есть → Проверка свежести
       ├─ Свежий (< TTL) → Возврат кэша
       └─ Несвежий (≥ TTL) → Запрос к API
           ├─ Успех → Обновление кэша → Возврат
           └─ Ошибка сети → Возврат stale кэша + флаг isStaleData
```

**Состояния UI:**
- `loading` — первоначальная загрузка
- `data` — данные загружены
- `error` — ошибка без кэша
- `data + isStaleData=true` — показываем старые данные с индикатором

### Реализация

```dart
class YearCacheDataSource {
  bool isCacheFresh(CachedYearData data, {int ttlHours = 24}) {
    final now = DateTime.now();
    final age = now.difference(data.updatedAt);
    return age.inHours < ttlHours;
  }
}

// В репозитории:
final cachedData = localDataSource.getCachedYearData(year);
if (cachedData != null && localDataSource.isCacheFresh(cachedData)) {
  return _parseYearSlices(cachedData); // Свежий кэш
}

// Пытаемся обновить
try {
  final freshData = await remoteDataSource.getYearData(year);
  await localDataSource.cacheYearData(...);
  return freshData;
} catch (e) {
  if (cachedData != null) {
    // Fallback на stale данные
    return _parseYearSlices(cachedData);
  }
  rethrow;
}
```

### Индикатор stale данных

В UI показывать:
```dart
if (state.isStaleData) {
  SnackBar(content: Text('Показаны сохранённые данные'));
}
```

## Альтернативы

### Cache-aside (lazy loading)
- **Плюсы:** Простая реализация
- **Минусы:** Нет контроля актуальности

### Write-through
- **Плюсы:** Всегда актуальные данные в кэше
- **Минусы:** Медленная запись, нет офлайн-поддержки

### Refresh-ahead (pre-fetch)
- **Плюсы:** Данные всегда свежие
- **Минусы:** Сложная реализация, лишние запросы

### Без кэширования
- **Плюсы:** Всегда актуальные данные
- **Минусы:** Не работает офлайн, медленно

## Последствия

### Положительные
- Быстрая загрузка при повторном открытии
- Работа без интернета
- Пользователь видит данные даже при проблемах с API

### Отрицательные
- Усложнённая логика загрузки
- Нужно обрабатывать stale state в UI
- Возможны неактуальные данные (24 часа)

### Будущие улучшения
- Push-обновления при изменении производственного календаря
- Ручная кнопка "Обновить"
- Настройка TTL пользователем

## Ссылки
- [Cache-aside pattern](https://learn.microsoft.com/en-us/azure/architecture/patterns/cache-aside)
