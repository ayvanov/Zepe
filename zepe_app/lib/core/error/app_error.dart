import 'package:equatable/equatable.dart';

/// Base error class for all application errors
sealed class AppError extends Equatable {
  final String message;
  final String? details;

  const AppError({required this.message, this.details});

  @override
  List<Object?> get props => [message, details];
}

/// Network-related errors (no connection, timeout)
class NetworkError extends AppError {
  const NetworkError({required super.message, super.details});
}

/// Server errors (5xx, 4xx responses)
class ServerError extends AppError {
  final int? statusCode;

  const ServerError({required super.message, super.details, this.statusCode});

  @override
  List<Object?> get props => [message, details, statusCode];
}

/// Cache-related errors
class CacheError extends AppError {
  const CacheError({required super.message, super.details});
}

/// Validation errors (user input)
class ValidationError extends AppError {
  const ValidationError({required super.message, super.details});
}

/// Unknown/unexpected errors
class UnknownError extends AppError {
  final Object? originalError;

  const UnknownError({
    required super.message,
    super.details,
    this.originalError,
  });

  @override
  List<Object?> get props => [message, details, originalError];
}
