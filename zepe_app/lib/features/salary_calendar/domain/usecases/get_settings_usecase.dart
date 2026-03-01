import 'package:dartz/dartz.dart';
import 'package:zepe_app/core/error/failure.dart';
import 'package:zepe_app/features/salary_calendar/domain/entities/salary_settings.dart';
import 'package:zepe_app/features/salary_calendar/domain/repositories/settings_repository.dart';

/// Use case to get salary settings
class GetSettingsUseCase {
  final SettingsRepository repository;

  GetSettingsUseCase(this.repository);

  Future<Either<Failure, SalarySettings>> call() async {
    try {
      final settings = await repository.getSettings();
      return Right(settings);
    } catch (e) {
      return Left(UnknownFailure(message: 'Failed to get settings: $e'));
    }
  }
}
