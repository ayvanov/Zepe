import 'package:freezed_annotation/freezed_annotation.dart';

part 'year_slices_dto.g.dart';

/// DTO for year slices data from API
@JsonSerializable()
class YearSlicesDto {
  final int year;
  final List<String> months;

  YearSlicesDto({
    required this.year,
    required this.months,
  });

  factory YearSlicesDto.fromJson(Map<String, dynamic> json) =>
      _$YearSlicesDtoFromJson(json);

  Map<String, dynamic> toJson() => _$YearSlicesDtoToJson(this);

  /// Parse from isdayoff.ru API response format
  /// Format: "5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31"
  /// or with holidays: "5,6,7,!8,9,10..."
  factory YearSlicesDto.fromApiData(int year, String rawData) {
    // The API returns comma-separated values directly
    final months = rawData.split(',').map((e) => e.trim()).toList();
    return YearSlicesDto(year: year, months: months);
  }
}
