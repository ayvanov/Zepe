# ADR 002: Выбор Dio для HTTP-запросов

**Дата:** 2025-02-28  
**Статус:** Принято

## Контекст

Приложению требуется HTTP-клиент для взаимодействия с API isdayoff.ru:
- GET /api/getdata?year={year}
- GET /api/getdata?year={year}&month={month}

Требования:
- Настройка таймаутов
- Обработка ошибок
- Retry logic
- Интерцепторы для логирования
- Поддержка отмены запросов

## Решение

Выбран **Dio** (dio) — мощный HTTP-клиент для Flutter.

### Преимущества
1. **Интерцепторы** — логирование, добавление заголовков, обработка ошибок
2. **Гибкая настройка таймаутов** — отдельно на connection/read/write
3. **Типизированные ошибки** — DioException с информацией о статусе
4. **Поддержка retry** — возможность повторных запросов
5. **Отмена запросов** — CancelToken
6. **Загрузка/выгрузка файлов** — на будущее
7. **Поддержка FormData** — для будущих API

### Пример использования
```dart
class DioClient {
  final Dio _dio;

  DioClient({required String baseUrl, Duration? connectTimeout, Duration? readTimeout})
      : _dio = Dio(BaseOptions(
          baseUrl: baseUrl,
          connectTimeout: connectTimeout ?? const Duration(seconds: 10),
          receiveTimeout: readTimeout ?? const Duration(seconds: 10),
        ));
}
```

## Альтернативы

### http (package:http)
- **Плюсы:** Встроен в Flutter, простой API
- **Минусы:** Нет интерцепторов, сложнее настройка таймаутов, менее удобный API

### Retrofit
- **Плюсы:** Type-safe, code generation
- **Минусы:** Требует dio как зависимость, избыточен для простого API

### Chopper
- **Плюсы:** REST-ориентированный, codegen
- **Минусы:** Меньше сообщество, требует обучения

## Последствия

### Положительные
- Единый стандарт для всех HTTP-запросов
- Лёгкое добавление авторизации в будущем
- Централизованная обработка ошибок

### Отрицательные
- Дополнительная зависимость
- Требуется настройка интерцепторов для production

## Ссылки
- [Dio Documentation](https://pub.dev/packages/dio)
- [Dio GitHub](https://github.com/cfug/dio)
