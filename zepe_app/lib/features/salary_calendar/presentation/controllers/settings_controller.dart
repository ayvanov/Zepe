import 'package:flutter_riverpod/legacy.dart';
import 'package:zepe_app/core/utils/logger.dart';
import '../../domain/entities/salary_settings.dart';
import '../../domain/usecases/save_settings_usecase.dart';
import '../../domain/usecases/get_settings_usecase.dart';
import '../state/settings_state.dart';

/// Controller for settings screen
class SettingsController extends StateNotifier<SettingsState> {
  final SaveSettingsUseCase saveSettings;
  final GetSettingsUseCase getSettings;

  SettingsController({
    required this.saveSettings,
    required this.getSettings,
  }) : super(SettingsState.initial());

  /// Load current settings
  Future<void> loadSettings() async {
    final result = await getSettings();
    result.fold(
      (failure) {
        state = state.copyWith(
          status: SettingsStatus.error,
          errorMessage: failure.message,
        );
        AppLogger.w(
          'Failed to load settings: ${failure.message}',
          tag: 'SettingsController',
        );
      },
      (settings) {
        state = SettingsState.initial().copyWith(
          salaryInput: settings.salary.toString(),
          salaryMultiplier: settings.salaryMultiplier,
          advanceDays: settings.advanceDays,
          advancePayDay: settings.advancePayDay,
          restPayDay: settings.restPayDay,
        );
        AppLogger.i('Settings loaded', tag: 'SettingsController');
      },
    );
  }

  /// Update salary input
  void updateSalaryInput(String value) {
    state = state.copyWith(salaryInput: value);
  }

  /// Update salary multiplier
  void updateSalaryMultiplier(double value) {
    state = state.copyWith(salaryMultiplier: value);
  }

  /// Update advance days
  void updateAdvanceDays(int value) {
    state = state.copyWith(advanceDays: value);
  }

  /// Update advance pay day
  void updateAdvancePayDay(int value) {
    state = state.copyWith(advancePayDay: value);
  }

  /// Update rest pay day
  void updateRestPayDay(int value) {
    state = state.copyWith(restPayDay: value);
  }

  /// Save settings
  Future<bool> save() async {
    state = state.copyWith(status: SettingsStatus.saving);

    // Parse salary
    final salary = double.tryParse(state.salaryInput) ?? 0;

    final settings = SalarySettings(
      salary: salary,
      salaryMultiplier: state.salaryMultiplier,
      advanceDays: state.advanceDays,
      advancePayDay: state.advancePayDay,
      restPayDay: state.restPayDay,
    );

    // Validate
    final validationError = settings.validate();
    if (validationError != null) {
      state = state.copyWith(
        status: SettingsStatus.validationError,
        validationMessage: validationError,
      );
      return false;
    }

    // Save
    final result = await saveSettings(settings);
    result.fold(
      (failure) {
        state = state.copyWith(
          status: SettingsStatus.error,
          errorMessage: failure.message,
        );
        AppLogger.e(
          'Failed to save settings',
          tag: 'SettingsController',
          error: failure.message,
        );
      },
      (_) {
        state = state.copyWith(status: SettingsStatus.saved);
        AppLogger.i('Settings saved', tag: 'SettingsController');
      },
    );

    return state.status == SettingsStatus.saved;
  }

  /// Reset to initial state
  void reset() {
    state = SettingsState.initial();
  }
}
