import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_riverpod/legacy.dart';
import 'package:hive/hive.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:zepe_app/core/constants/app_constants.dart';
import 'package:zepe_app/core/network/dio_client.dart';
import 'package:zepe_app/features/salary_calendar/data/datasources/local/settings_local_datasource.dart';
import 'package:zepe_app/features/salary_calendar/data/datasources/local/year_cache_datasource.dart';
import 'package:zepe_app/features/salary_calendar/data/datasources/remote/isdayoff_api_client.dart';
import 'package:zepe_app/features/salary_calendar/data/repositories/settings_repository_impl.dart';
import 'package:zepe_app/features/salary_calendar/data/repositories/year_slices_repository_impl.dart';
import 'package:zepe_app/features/salary_calendar/domain/repositories/settings_repository.dart';
import 'package:zepe_app/features/salary_calendar/domain/repositories/year_slices_repository.dart';
import 'package:zepe_app/features/salary_calendar/domain/services/payout_calculator.dart';
import 'package:zepe_app/features/salary_calendar/domain/usecases/get_settings_usecase.dart';
import 'package:zepe_app/features/salary_calendar/domain/usecases/get_year_payouts_usecase.dart';
import 'package:zepe_app/features/salary_calendar/domain/usecases/save_settings_usecase.dart';
import 'package:zepe_app/features/salary_calendar/presentation/controllers/home_controller.dart';
import 'package:zepe_app/features/salary_calendar/presentation/controllers/settings_controller.dart';
import 'package:zepe_app/features/salary_calendar/presentation/state/home_state.dart';
import 'package:zepe_app/features/salary_calendar/presentation/state/settings_state.dart';

// ============================================================================
// Data Sources
// ============================================================================

/// Hive box provider for year cache
final yearCacheBoxProvider = Provider<Box>((ref) {
  return Hive.box(AppConstants.yearCacheBoxName);
});

/// SharedPreferences provider
final sharedPreferencesProvider = Provider<SharedPreferences>((ref) {
  throw UnimplementedError(
    'SharedPreferences must be initialized in main.dart',
  );
});

/// IsDayOff API client provider
final isDayOffApiClientProvider = Provider<IsDayOffApiClient>((ref) {
  final dioClient = ref.read(dioProvider);
  return IsDayOffApiClient(dioClient);
});

/// Year cache data source provider
final yearCacheDataSourceProvider = Provider<YearCacheDataSource>((ref) {
  final box = ref.watch(yearCacheBoxProvider);
  return YearCacheDataSource(box);
});

/// Settings local data source provider
final settingsLocalDataSourceProvider =
    Provider<SettingsLocalDataSource>((ref) {
  final prefs = ref.watch(sharedPreferencesProvider);
  return SettingsLocalDataSource(prefs);
});

// ============================================================================
// Repositories
// ============================================================================

/// Year slices repository provider
final yearSlicesRepositoryProvider = Provider<YearSlicesRepository>((ref) {
  final remoteDataSource = ref.watch(isDayOffApiClientProvider);
  final localDataSource = ref.watch(yearCacheDataSourceProvider);
  return YearSlicesRepositoryImpl(
    remoteDataSource: remoteDataSource,
    localDataSource: localDataSource,
  );
});

/// Settings repository provider
final settingsRepositoryProvider = Provider<SettingsRepository>((ref) {
  final localDataSource = ref.watch(settingsLocalDataSourceProvider);
  return SettingsRepositoryImpl(localDataSource);
});

// ============================================================================
// Services
// ============================================================================

/// Payout calculator provider
final payoutCalculatorProvider = Provider<PayoutCalculator>((ref) {
  return PayoutCalculator();
});

// ============================================================================
// Use Cases
// ============================================================================

/// Get settings use case provider
final getSettingsUseCaseProvider = Provider<GetSettingsUseCase>((ref) {
  final repository = ref.watch(settingsRepositoryProvider);
  return GetSettingsUseCase(repository);
});

/// Save settings use case provider
final saveSettingsUseCaseProvider = Provider<SaveSettingsUseCase>((ref) {
  final repository = ref.watch(settingsRepositoryProvider);
  return SaveSettingsUseCase(repository);
});

/// Get year payouts use case provider
final getYearPayoutsUseCaseProvider = Provider<GetYearPayoutsUseCase>((ref) {
  final settingsRepository = ref.watch(settingsRepositoryProvider);
  final yearSlicesRepository = ref.watch(yearSlicesRepositoryProvider);
  final calculator = ref.watch(payoutCalculatorProvider);
  return GetYearPayoutsUseCase(
    settingsRepository: settingsRepository,
    yearSlicesRepository: yearSlicesRepository,
    calculator: calculator,
  );
});

// ============================================================================
// Controllers
// ============================================================================

/// Home controller provider
final homeControllerProvider =
    StateNotifierProvider<HomeController, HomeState>((ref) {
  final getYearPayouts = ref.watch(getYearPayoutsUseCaseProvider);
  final getSettings = ref.watch(getSettingsUseCaseProvider);
  return HomeController(
    getYearPayouts: getYearPayouts,
    getSettings: getSettings,
  );
});

/// Settings controller provider
final settingsControllerProvider =
    StateNotifierProvider<SettingsController, SettingsState>((ref) {
  final saveSettings = ref.watch(saveSettingsUseCaseProvider);
  final getSettings = ref.watch(getSettingsUseCaseProvider);
  return SettingsController(
    saveSettings: saveSettings,
    getSettings: getSettings,
  );
});
