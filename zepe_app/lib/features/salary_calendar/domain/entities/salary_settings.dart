import 'package:equatable/equatable.dart';

/// User salary settings entity
class SalarySettings extends Equatable {
  final double salary;
  final double salaryMultiplier;
  final int advanceDays;
  final int advancePayDay;
  final int restPayDay;

  const SalarySettings({
    this.salary = 100000.0,
    this.salaryMultiplier = 1.0,
    this.advanceDays = 15,
    this.advancePayDay = 25,
    this.restPayDay = 10,
  });

  /// Create a copy with updated fields
  SalarySettings copyWith({
    double? salary,
    double? salaryMultiplier,
    int? advanceDays,
    int? advancePayDay,
    int? restPayDay,
  }) {
    return SalarySettings(
      salary: salary ?? this.salary,
      salaryMultiplier: salaryMultiplier ?? this.salaryMultiplier,
      advanceDays: advanceDays ?? this.advanceDays,
      advancePayDay: advancePayDay ?? this.advancePayDay,
      restPayDay: restPayDay ?? this.restPayDay,
    );
  }

  /// Validate settings
  String? validate() {
    if (salary < 0) return 'Salary cannot be negative';
    if (salaryMultiplier <= 0) return 'Multiplier must be positive';
    if (advanceDays < 1 || advanceDays > 31) {
      return 'Advance days must be between 1 and 31';
    }
    if (advancePayDay < 1 || advancePayDay > 31) {
      return 'Advance pay day must be between 1 and 31';
    }
    if (restPayDay < 1 || restPayDay > 31) {
      return 'Rest pay day must be between 1 and 31';
    }
    return null;
  }

  @override
  List<Object?> get props =>
      [salary, salaryMultiplier, advanceDays, advancePayDay, restPayDay];
}
