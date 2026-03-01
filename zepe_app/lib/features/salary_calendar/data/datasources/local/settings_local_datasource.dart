import 'package:shared_preferences/shared_preferences.dart';
import 'package:zepe_app/core/constants/app_constants.dart';
import 'package:zepe_app/core/utils/logger.dart';
import 'package:zepe_app/features/salary_calendar/domain/entities/salary_settings.dart';

/// Local data source for settings storage
class SettingsLocalDataSource {
  final SharedPreferences _prefs;

  SettingsLocalDataSource(this._prefs);

  /// Get stored settings
  SalarySettings getSettings() {
    try {
      return SalarySettings(
        salary: _prefs.getDouble(AppConstants.settingsSalaryKey) ??
            AppConstants.defaultSalary,
        salaryMultiplier:
            _prefs.getDouble(AppConstants.settingsMultiplierKey) ??
                AppConstants.defaultSalaryMultiplier,
        advanceDays: _prefs.getInt(AppConstants.settingsAdvanceDaysKey) ??
            AppConstants.defaultAdvanceDays,
        advancePayDay: _prefs.getInt(AppConstants.settingsAdvancePayDayKey) ??
            AppConstants.defaultAdvancePayDay,
        restPayDay: _prefs.getInt(AppConstants.settingsRestPayDayKey) ??
            AppConstants.defaultRestPayDay,
      );
    } catch (e) {
      AppLogger.e('Failed to get settings',
          tag: 'SettingsLocalDataSource', error: e,);
      return const SalarySettings();
    }
  }

  /// Save settings
  Future<void> saveSettings(SalarySettings settings) async {
    try {
      await _prefs.setDouble(AppConstants.settingsSalaryKey, settings.salary);
      await _prefs.setDouble(
          AppConstants.settingsMultiplierKey, settings.salaryMultiplier,);
      await _prefs.setInt(
          AppConstants.settingsAdvanceDaysKey, settings.advanceDays,);
      await _prefs.setInt(
          AppConstants.settingsAdvancePayDayKey, settings.advancePayDay,);
      await _prefs.setInt(
          AppConstants.settingsRestPayDayKey, settings.restPayDay,);
      AppLogger.i('Settings saved successfully',
          tag: 'SettingsLocalDataSource',);
    } catch (e) {
      AppLogger.e('Failed to save settings',
          tag: 'SettingsLocalDataSource', error: e,);
      rethrow;
    }
  }
}
