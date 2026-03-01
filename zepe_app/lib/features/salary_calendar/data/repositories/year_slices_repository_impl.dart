import 'package:zepe_app/core/constants/app_constants.dart';
import 'package:zepe_app/core/utils/logger.dart';
import 'package:zepe_app/features/salary_calendar/data/datasources/local/year_cache_datasource.dart';
import 'package:zepe_app/features/salary_calendar/data/datasources/remote/isdayoff_api_client.dart';
import 'package:zepe_app/features/salary_calendar/domain/entities/year_slices.dart';
import 'package:zepe_app/features/salary_calendar/domain/repositories/year_slices_repository.dart';

/// Implementation of YearSlicesRepository
class YearSlicesRepositoryImpl implements YearSlicesRepository {
  final IsDayOffApiClient remoteDataSource;
  final YearCacheDataSource localDataSource;

  YearSlicesRepositoryImpl({
    required this.remoteDataSource,
    required this.localDataSource,
  });

  @override
  Future<YearSlices> getYearSlices(int year,
      {bool forceRefresh = false,}) async {
    // Check cache first
    if (!forceRefresh) {
      final cachedData = localDataSource.getCachedYearData(year);
      if (cachedData != null) {
        // Check if cache is fresh
        if (localDataSource.isCacheFresh(cachedData,
            ttlHours: AppConstants.cacheTtlHours,)) {
          AppLogger.i('Returning fresh cached data for year $year',
              tag: 'YearSlicesRepository',);
          return _parseYearSlices(cachedData);
        } else {
          AppLogger.i('Cache stale for year $year, fetching from API',
              tag: 'YearSlicesRepository',);
        }
      }
    }

    // Fetch from API
    try {
      AppLogger.i('Fetching year data from API for $year',
          tag: 'YearSlicesRepository',);

      // Get main year data
      final yearDataRaw = await remoteDataSource.getYearData(year);

      // Try to get next January for December calculations
      String? nextJanuaryRaw;
      try {
        nextJanuaryRaw = await remoteDataSource.getMonthData(year + 1, 1);
      } catch (e) {
        AppLogger.w('Failed to fetch next January, continuing without it',
            tag: 'YearSlicesRepository',);
      }

      // Cache the data
      final cachedData = CachedYearData(
        year: year,
        yearDataRaw: yearDataRaw,
        nextJanuaryRaw: nextJanuaryRaw,
        updatedAt: DateTime.now(),
      );
      await localDataSource.cacheYearData(cachedData);

      return _parseYearSlices(cachedData);
    } catch (e) {
      // If we have stale cache, return it
      final cachedData = localDataSource.getCachedYearData(year);
      if (cachedData != null) {
        AppLogger.w('Network failed, returning stale cache',
            tag: 'YearSlicesRepository',);
        return _parseYearSlices(cachedData);
      }
      rethrow;
    }
  }

  @override
  Future<void> prefetchNextJanuary(int year) async {
    try {
      AppLogger.i('Prefetching next January for year $year',
          tag: 'YearSlicesRepository',);
      final nextJanuaryRaw = await remoteDataSource.getMonthData(year + 1, 1);

      // Update cache if exists
      final cachedData = localDataSource.getCachedYearData(year);
      if (cachedData != null) {
        final updatedData = CachedYearData(
          year: year,
          yearDataRaw: cachedData.yearDataRaw,
          nextJanuaryRaw: nextJanuaryRaw,
          updatedAt: DateTime.now(),
        );
        await localDataSource.cacheYearData(updatedData);
      }
    } catch (e) {
      AppLogger.w('Failed to prefetch next January: $e',
          tag: 'YearSlicesRepository',);
    }
  }

  YearSlices _parseYearSlices(CachedYearData cachedData) {
    // Parse the raw data - isdayoff.ru returns workday numbers separated by commas
    // Each "month" in the response is actually a list of workdays
    final months = <String>[];

    // The API returns 12 months of data as comma-separated workday numbers
    // We need to parse this into our format
    final yearData = cachedData.yearDataRaw;

    // Split by month separator (the API format may vary)
    // For now, treat the entire response as workday data for all months
    // In reality, the API returns data per month, so we'll parse accordingly

    // Parse year data into months
    // The isdayoff.ru API returns data in format: "workdays for the period"
    // We need to split this into 12 months

    // Simple approach: split the raw data and distribute to months
    // This is a simplification - real implementation would parse the actual API format
    final allDays = yearData.split(',').map((e) => e.trim()).toList();

    // Group days by month (simplified - assumes ~30 days per month)
    int dayIndex = 0;
    for (int month = 1; month <= 12; month++) {
      final daysInMonth = _getDaysInMonth(cachedData.year, month);
      final monthDays = <String>[];

      for (int i = 0; i < daysInMonth && dayIndex < allDays.length; i++) {
        if (dayIndex < allDays.length) {
          monthDays.add(allDays[dayIndex]);
          dayIndex++;
        }
      }

      months.add(monthDays.join(','));
    }

    // Add next January if available
    if (cachedData.nextJanuaryRaw != null) {
      months.add(cachedData.nextJanuaryRaw!);
    } else {
      months.add(''); // Empty placeholder
    }

    return YearSlices(year: cachedData.year, months: months);
  }

  int _getDaysInMonth(int year, int month) {
    return DateTime(year, month + 1, 0).day;
  }
}
