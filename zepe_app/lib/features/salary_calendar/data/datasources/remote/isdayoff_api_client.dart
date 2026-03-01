import 'package:dio/dio.dart';
import 'package:zepe_app/core/network/dio_client.dart';
import 'package:zepe_app/core/utils/logger.dart';

/// Remote data source for isdayoff.ru API
class IsDayOffApiClient {
  final DioClient dioClient;

  const IsDayOffApiClient(this.dioClient);

  /// Get year data
  Future<String> getYearData(int year) async {
    try {
      final response = await dioClient.dio.get(
        '/api/getdata',
        queryParameters: {'year': year},
      );

      if (response.statusCode != 200) {
        throw Exception('API returned status ${response.statusCode}');
      }

      final data = response.data;
      if (data == null || data.toString().isEmpty) {
        throw Exception('API returned empty data');
      }

      return data.toString();
    } on DioException catch (e) {
      AppLogger.e('API error', tag: 'IsDayOffApiClient', error: e);
      throw Exception('Network error: ${e.message}');
    } catch (e) {
      AppLogger.e('Unknown error', tag: 'IsDayOffApiClient', error: e);
      rethrow;
    }
  }

  /// Get specific month data
  Future<String> getMonthData(int year, int month) async {
    try {
      final response = await dioClient.dio.get(
        '/api/getdata',
        queryParameters: {'year': year, 'month': month},
      );

      if (response.statusCode != 200) {
        throw Exception('API returned status ${response.statusCode}');
      }

      final data = response.data;
      if (data == null || data.toString().isEmpty) {
        throw Exception('API returned empty data');
      }

      return data.toString();
    } on DioException catch (e) {
      AppLogger.e('API error', tag: 'IsDayOffApiClient', error: e);
      throw Exception('Network error: ${e.message}');
    } catch (e) {
      AppLogger.e('Unknown error', tag: 'IsDayOffApiClient', error: e);
      rethrow;
    }
  }
}
