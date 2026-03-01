import 'package:flutter_riverpod/legacy.dart';
import 'package:zepe_app/core/utils/logger.dart';
import '../../domain/usecases/get_year_payouts_usecase.dart';
import '../../domain/usecases/get_settings_usecase.dart';
import '../state/home_state.dart';

/// Controller for home screen
class HomeController extends StateNotifier<HomeState> {
  final GetYearPayoutsUseCase getYearPayouts;
  final GetSettingsUseCase getSettings;

  HomeController({
    required this.getYearPayouts,
    required this.getSettings,
  }) : super(HomeState.initial());

  /// Load year payouts data
  Future<void> loadYearData(int year) async {
    state = state.copyWith(status: HomeStatus.loading, year: year);

    // Get settings first
    final settingsResult = await getSettings();
    settingsResult.fold(
      (failure) {
        AppLogger.w('Failed to load settings: ${failure.message}');
        // Continue with default settings
      },
      (settings) {
        state = state.copyWith(settings: settings);
      },
    );

    // Get year payouts
    final result = await getYearPayouts(year);
    result.fold(
      (failure) {
        state = state.copyWith(
          status: HomeStatus.error,
          errorMessage: failure.message,
        );
        AppLogger.e(
          'Failed to load year data',
          tag: 'HomeController',
          error: failure.message,
        );
      },
      (months) {
        state = state.copyWith(
          status: HomeStatus.data,
          months: months,
          isStaleData: false,
        );
        AppLogger.i('Loaded ${months.length} months', tag: 'HomeController');
      },
    );
  }

  /// Change selected year
  void changeYear(int year) {
    loadYearData(year);
  }

  /// Refresh data
  Future<void> refresh() async {
    await loadYearData(state.year);
  }

  /// Navigate to settings
  void goToSettings() {
    // Navigation is handled by the router
    AppLogger.d('Navigate to settings', tag: 'HomeController');
  }
}
