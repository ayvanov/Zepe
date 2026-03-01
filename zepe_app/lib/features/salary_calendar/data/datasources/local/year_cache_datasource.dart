import 'package:hive/hive.dart';
import 'package:zepe_app/core/utils/logger.dart';

/// Local cache data source for year slices
class YearCacheDataSource {
  final Box _box;

  YearCacheDataSource(this._box);

  /// Get cached year data
  CachedYearData? getCachedYearData(int year) {
    try {
      final key = _getYearKey(year);
      final data = _box.get(key);

      if (data == null) return null;

      return CachedYearData(
        year: data['year'] as int,
        yearDataRaw: data['yearDataRaw'] as String,
        nextJanuaryRaw: data['nextJanuaryRaw'] as String?,
        updatedAt: DateTime.parse(data['updatedAt'] as String),
      );
    } catch (e) {
      AppLogger.e('Failed to get cached year data',
          tag: 'YearCacheDataSource', error: e,);
      return null;
    }
  }

  /// Cache year data
  Future<void> cacheYearData(CachedYearData data) async {
    try {
      final key = _getYearKey(data.year);
      await _box.put(key, {
        'year': data.year,
        'yearDataRaw': data.yearDataRaw,
        'nextJanuaryRaw': data.nextJanuaryRaw,
        'updatedAt': data.updatedAt.toIso8601String(),
      });
      AppLogger.i('Cached year data for ${data.year}',
          tag: 'YearCacheDataSource',);
    } catch (e) {
      AppLogger.e('Failed to cache year data',
          tag: 'YearCacheDataSource', error: e,);
      rethrow;
    }
  }

  /// Check if cache is fresh
  bool isCacheFresh(CachedYearData data, {int ttlHours = 24}) {
    final now = DateTime.now();
    final age = now.difference(data.updatedAt);
    return age.inHours < ttlHours;
  }

  /// Clear all cache
  Future<void> clearCache() async {
    await _box.clear();
  }

  String _getYearKey(int year) => 'year:$year';
}

/// Cached year data model
class CachedYearData {
  final int year;
  final String yearDataRaw;
  final String? nextJanuaryRaw;
  final DateTime updatedAt;

  CachedYearData({
    required this.year,
    required this.yearDataRaw,
    this.nextJanuaryRaw,
    required this.updatedAt,
  });
}
