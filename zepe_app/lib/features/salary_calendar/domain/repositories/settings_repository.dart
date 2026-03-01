import '../entities/salary_settings.dart';

/// Abstract repository contract for salary settings
abstract class SettingsRepository {
  /// Get current salary settings
  Future<SalarySettings> getSettings();

  /// Save salary settings
  Future<void> saveSettings(SalarySettings settings);
}
