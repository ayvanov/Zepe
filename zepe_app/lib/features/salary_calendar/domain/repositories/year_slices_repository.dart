import '../entities/year_slices.dart';

/// Abstract repository contract for year slices data
abstract class YearSlicesRepository {
  /// Get year slices data, optionally forcing refresh from remote
  Future<YearSlices> getYearSlices(int year, {bool forceRefresh = false});

  /// Prefetch next January data for year-end calculations
  Future<void> prefetchNextJanuary(int year);
}
