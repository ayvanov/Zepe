import 'package:zepe_app/core/utils/date_utils.dart';
import 'package:zepe_app/core/utils/money_utils.dart';
import 'package:zepe_app/features/salary_calendar/domain/entities/month_meta.dart';
import 'package:zepe_app/features/salary_calendar/domain/entities/salary_settings.dart';
import 'package:zepe_app/features/salary_calendar/domain/entities/year_slices.dart';

/// Service for calculating month payout metadata
class PayoutCalculator {
  /// Calculate MonthMeta for a specific month
  MonthMeta calculateMonthMeta({
    required int year,
    required int monthNum,
    required SalarySettings settings,
    required YearSlices yearSlices,
  }) {
    final monthSlice = yearSlices.getMonthSlice(monthNum);
    final effectiveSalary = settings.salary * settings.salaryMultiplier;

    // Parse workdays from slice (format: "5,6,7..." or similar)
    final workdays = _parseWorkdays(monthSlice);
    final totalDays = DateUtils.getDaysInMonth(year, monthNum);
    final holidays = totalDays - workdays.length;

    // Calculate salary per day
    final salaryPerDay = MoneyUtils.calculateSalaryPerDay(
      salary: effectiveSalary,
      workdays: workdays.length,
    );

    // Calculate advance workdays (from 1st to advanceDays)
    final advanceWorkdays =
        workdays.where((d) => d <= settings.advanceDays).length;
    final advanceValue = MoneyUtils.calculateAdvance(
      salaryPerDay: salaryPerDay,
      advanceWorkdays: advanceWorkdays,
    );

    // Calculate rest payment
    final restValue = MoneyUtils.calculateRestPayment(
      salary: effectiveSalary,
      advanceValue: advanceValue,
    );

    // Calculate advance date
    final advanceDate =
        _calculatePayDate(year, monthNum, settings.advancePayDay);

    // Calculate rest date (next month's pay day)
    DateTime restDate;
    if (monthNum == 12) {
      // For December, use next January's data if available
      final nextJanuary = yearSlices.getNextJanuary();
      if (nextJanuary != null) {
        restDate = _calculatePayDate(year + 1, 1, settings.restPayDay);
      } else {
        restDate = _calculatePayDate(year + 1, 1, settings.restPayDay);
      }
    } else {
      restDate = _calculatePayDate(year, monthNum + 1, settings.restPayDay);
    }

    return MonthMeta(
      monthNum: monthNum,
      year: year,
      salary: effectiveSalary,
      totalDays: totalDays,
      workdays: workdays.length,
      holidays: holidays,
      salaryPerDay: MoneyUtils.round(salaryPerDay),
      advanceWorkdays: advanceWorkdays,
      advanceValue: MoneyUtils.round(advanceValue),
      advanceDate: advanceDate,
      restValue: MoneyUtils.round(restValue),
      restDate: restDate,
    );
  }

  /// Parse workdays from isdayoff.ru format
  /// Format: comma-separated day numbers, with "!" prefix for holidays
  List<int> _parseWorkdays(String monthSlice) {
    if (monthSlice.isEmpty) return [];

    final days = <int>[];
    final parts = monthSlice.split(',');

    for (final part in parts) {
      final trimmed = part.trim();
      if (trimmed.startsWith('!')) {
        // Holiday (skip)
        continue;
      }
      final day = int.tryParse(trimmed);
      if (day != null && day >= 1 && day <= 31) {
        days.add(day);
      }
    }

    return days..sort();
  }

  /// Calculate payment date, adjusting for weekends
  DateTime _calculatePayDate(int year, int month, int targetDay) {
    final daysInMonth = DateUtils.getDaysInMonth(year, month);
    int day = targetDay.clamp(1, daysInMonth);

    DateTime date = DateTime(year, month, day);

    // If pay day falls on weekend, move to previous Friday
    while (
        date.weekday == DateTime.saturday || date.weekday == DateTime.sunday) {
      date = date.subtract(const Duration(days: 1));
    }

    return date;
  }
}
