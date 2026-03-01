import 'package:dartz/dartz.dart';
import 'package:zepe_app/core/error/failure.dart';
import 'package:zepe_app/features/salary_calendar/domain/entities/month_meta.dart';
import 'package:zepe_app/features/salary_calendar/domain/repositories/settings_repository.dart';
import 'package:zepe_app/features/salary_calendar/domain/repositories/year_slices_repository.dart';
import 'package:zepe_app/features/salary_calendar/domain/services/payout_calculator.dart';

/// Use case to get year payouts calculation
class GetYearPayoutsUseCase {
  final SettingsRepository settingsRepository;
  final YearSlicesRepository yearSlicesRepository;
  final PayoutCalculator calculator;

  GetYearPayoutsUseCase({
    required this.settingsRepository,
    required this.yearSlicesRepository,
    required this.calculator,
  });

  Future<Either<Failure, List<MonthMeta>>> call(int year) async {
    try {
      // Get settings
      final settingsResult = await settingsRepository.getSettings();

      // Get year slices
      final yearSlices = await yearSlicesRepository.getYearSlices(year);

      // Calculate payouts for all months
      final months = <MonthMeta>[];
      for (int month = 1; month <= 12; month++) {
        final monthMeta = calculator.calculateMonthMeta(
          year: year,
          monthNum: month,
          settings: settingsResult,
          yearSlices: yearSlices,
        );
        months.add(monthMeta);
      }

      return Right(months);
    } catch (e) {
      return Left(
          UnknownFailure(message: 'Failed to calculate year payouts: $e'),);
    }
  }
}
