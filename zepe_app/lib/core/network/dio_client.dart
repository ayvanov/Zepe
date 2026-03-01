import 'package:dio/dio.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Dio client wrapper with common configuration
class DioClient {
  final Dio _dio;

  DioClient(
      {required String baseUrl,
      Duration? connectTimeout,
      Duration? readTimeout,})
      : _dio = Dio(BaseOptions(
          baseUrl: baseUrl,
          connectTimeout: connectTimeout ?? const Duration(seconds: 10),
          receiveTimeout: readTimeout ?? const Duration(seconds: 10),
          headers: {
            'Content-Type': 'application/json',
            'Accept': 'application/json',
          },
        ),) {
    _dio.interceptors.add(LogInterceptor(
      requestBody: true,
      responseBody: true,
      error: true,
    ),);
  }

  Dio get dio => _dio;

  /// Handle Dio exceptions and convert to AppError
  Exception handleException(DioException error) {
    switch (error.type) {
      case DioExceptionType.connectionTimeout:
      case DioExceptionType.sendTimeout:
      case DioExceptionType.receiveTimeout:
        return Exception('Connection timeout: ${error.message}');
      case DioExceptionType.connectionError:
        return Exception('No internet connection');
      case DioExceptionType.badResponse:
        return Exception('Server error: ${error.response?.statusCode}');
      case DioExceptionType.cancel:
        return Exception('Request cancelled');
      case DioExceptionType.badCertificate:
        return Exception('Invalid certificate');
      case DioExceptionType.unknown:
        return Exception('Unknown error: ${error.message}');
    }
  }
}

final dioProvider = Provider<DioClient>((ref) {
  return DioClient(baseUrl: 'https://isdayoff.ru');
});
