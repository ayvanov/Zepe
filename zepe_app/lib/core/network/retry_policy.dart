import 'package:dio/dio.dart';

/// Retry policy for network requests
class RetryPolicy {
  final int maxRetries;
  final Duration initialDelay;
  final Duration maxDelay;
  final double delayMultiplier;

  const RetryPolicy({
    this.maxRetries = 3,
    this.initialDelay = const Duration(seconds: 1),
    this.maxDelay = const Duration(seconds: 30),
    this.delayMultiplier = 2.0,
  });

  /// Execute a function with retry logic
  Future<T> execute<T>(Future<T> Function() operation) async {
    int attempt = 0;
    Duration delay = initialDelay;

    while (true) {
      try {
        return await operation();
      } on DioException catch (e) {
        attempt++;
        if (attempt > maxRetries || !_shouldRetry(e)) {
          rethrow;
        }
        await Future.delayed(delay);
        final nextDelay = Duration(
            milliseconds: (delay.inMilliseconds * delayMultiplier).toInt(),);
        delay = nextDelay > maxDelay ? maxDelay : nextDelay;
      }
    }
  }

  bool _shouldRetry(DioException error) {
    switch (error.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
      case DioExceptionType.connectionError:
        return true;
      case DioExceptionType.badResponse:
        // Retry on 5xx server errors
        final status = error.response?.statusCode ?? 0;
        return status >= 500;
      case DioExceptionType.cancel:
      case DioExceptionType.badCertificate:
      case DioExceptionType.unknown:
        return false;
    }
  }
}
