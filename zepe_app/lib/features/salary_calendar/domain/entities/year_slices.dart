import 'package:equatable/equatable.dart';

/// Year slices entity containing workday data for 13 months (12 + next January)
class YearSlices extends Equatable {
  final int year;
  final List<String> months; // 13 elements: 12 months + next January

  const YearSlices({
    required this.year,
    required this.months,
  });

  /// Get month slice by month number (1-12)
  String getMonthSlice(int monthNum) {
    if (monthNum < 1 || monthNum > 12) {
      throw ArgumentError('Month must be between 1 and 12');
    }
    return months[monthNum - 1];
  }

  /// Get next January slice
  String? getNextJanuary() {
    if (months.length > 12) {
      return months[12];
    }
    return null;
  }

  /// Check if data is valid
  bool get isValid => months.length >= 12;

  @override
  List<Object?> get props => [year, months];
}
