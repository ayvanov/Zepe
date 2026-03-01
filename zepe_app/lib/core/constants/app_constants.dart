/// Application-wide constants
class AppConstants {
  /// Cache TTL in hours
  static const int cacheTtlHours = 24;

  /// Default salary
  static const double defaultSalary = 100000.0;

  /// Default salary multiplier
  static const double defaultSalaryMultiplier = 1.0;

  /// Default advance days (from start of month)
  static const int defaultAdvanceDays = 15;

  /// Default advance pay day
  static const int defaultAdvancePayDay = 25;

  /// Default rest pay day
  static const int defaultRestPayDay = 10;

  /// API base URL
  static const String apiBaseUrl = 'https://isdayoff.ru';

  /// Hive box names
  static const String yearCacheBoxName = 'year_cache';

  /// SharedPreferences keys
  static const String settingsSalaryKey = 'settings_salary';
  static const String settingsMultiplierKey = 'settings_multiplier';
  static const String settingsAdvanceDaysKey = 'settings_advance_days';
  static const String settingsAdvancePayDayKey = 'settings_advance_payday';
  static const String settingsRestPayDayKey = 'settings_rest_payday';

  /// Route paths
  static const String routeHome = '/';
  static const String routeSettings = '/settings';
  static const String routeSummary = '/summary';
}
