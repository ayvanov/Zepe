import 'package:equatable/equatable.dart';

/// Base failure class for functional error handling
sealed class Failure extends Equatable {
  final String message;
  final String? stackTrace;

  const Failure({required this.message, this.stackTrace});

  @override
  List<Object?> get props => [message, stackTrace];
}

class NetworkFailure extends Failure {
  const NetworkFailure({required super.message, super.stackTrace});
}

class ServerFailure extends Failure {
  final int? statusCode;

  const ServerFailure({
    required super.message,
    super.stackTrace,
    this.statusCode,
  });

  @override
  List<Object?> get props => [message, stackTrace, statusCode];
}

class CacheFailure extends Failure {
  const CacheFailure({required super.message, super.stackTrace});
}

class ValidationFailure extends Failure {
  const ValidationFailure({required super.message, super.stackTrace});
}

class UnknownFailure extends Failure {
  const UnknownFailure({required super.message, super.stackTrace});
}
