import 'package:equatable/equatable.dart';

/// Month metadata with calculated payout information
class MonthMeta extends Equatable {
  final int monthNum;
  final int year;
  final double salary;

  // Derived fields
  final int totalDays;
  final int workdays;
  final int holidays;
  final double salaryPerDay;
  final int advanceWorkdays;
  final double advanceValue;
  final DateTime advanceDate;
  final double restValue;
  final DateTime restDate;

  const MonthMeta({
    required this.monthNum,
    required this.year,
    required this.salary,
    required this.totalDays,
    required this.workdays,
    required this.holidays,
    required this.salaryPerDay,
    required this.advanceWorkdays,
    required this.advanceValue,
    required this.advanceDate,
    required this.restValue,
    required this.restDate,
  });

  /// Create a copy with updated fields
  MonthMeta copyWith({
    int? monthNum,
    int? year,
    double? salary,
    int? totalDays,
    int? workdays,
    int? holidays,
    double? salaryPerDay,
    int? advanceWorkdays,
    double? advanceValue,
    DateTime? advanceDate,
    double? restValue,
    DateTime? restDate,
  }) {
    return MonthMeta(
      monthNum: monthNum ?? this.monthNum,
      year: year ?? this.year,
      salary: salary ?? this.salary,
      totalDays: totalDays ?? this.totalDays,
      workdays: workdays ?? this.workdays,
      holidays: holidays ?? this.holidays,
      salaryPerDay: salaryPerDay ?? this.salaryPerDay,
      advanceWorkdays: advanceWorkdays ?? this.advanceWorkdays,
      advanceValue: advanceValue ?? this.advanceValue,
      advanceDate: advanceDate ?? this.advanceDate,
      restValue: restValue ?? this.restValue,
      restDate: restDate ?? this.restDate,
    );
  }

  @override
  List<Object?> get props => [
        monthNum,
        year,
        salary,
        totalDays,
        workdays,
        holidays,
        salaryPerDay,
        advanceWorkdays,
        advanceValue,
        advanceDate,
        restValue,
        restDate,
      ];
}
