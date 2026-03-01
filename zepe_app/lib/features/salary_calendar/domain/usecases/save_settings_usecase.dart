import 'package:dartz/dartz.dart';
import 'package:zepe_app/core/error/failure.dart';
import 'package:zepe_app/features/salary_calendar/domain/entities/salary_settings.dart';
import 'package:zepe_app/features/salary_calendar/domain/repositories/settings_repository.dart';

/// Use case to save salary settings
class SaveSettingsUseCase {
  final SettingsRepository repository;

  SaveSettingsUseCase(this.repository);

  Future<Either<Failure, void>> call(SalarySettings settings) async {
    // Validate settings first
    final validationError = settings.validate();
    if (validationError != null) {
      return Left(ValidationFailure(message: validationError));
    }

    try {
      await repository.saveSettings(settings);
      return const Right(null);
    } catch (e) {
      return Left(UnknownFailure(message: 'Failed to save settings: $e'));
    }
  }
}
