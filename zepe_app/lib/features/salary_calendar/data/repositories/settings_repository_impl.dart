import 'package:zepe_app/features/salary_calendar/data/datasources/local/settings_local_datasource.dart';
import 'package:zepe_app/features/salary_calendar/domain/entities/salary_settings.dart';
import 'package:zepe_app/features/salary_calendar/domain/repositories/settings_repository.dart';

/// Implementation of SettingsRepository
class SettingsRepositoryImpl implements SettingsRepository {
  final SettingsLocalDataSource localDataSource;

  SettingsRepositoryImpl(this.localDataSource);

  @override
  Future<SalarySettings> getSettings() async {
    return localDataSource.getSettings();
  }

  @override
  Future<void> saveSettings(SalarySettings settings) async {
    await localDataSource.saveSettings(settings);
  }
}
