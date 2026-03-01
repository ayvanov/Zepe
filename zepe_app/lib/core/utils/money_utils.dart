import 'package:intl/intl.dart';

/// Money calculation utilities
class MoneyUtils {
  /// Calculate salary per day
  static double calculateSalaryPerDay({
    required double salary,
    required int workdays,
  }) {
    if (workdays <= 0) return 0;
    return salary / workdays;
  }

  /// Calculate advance payment
  static double calculateAdvance({
    required double salaryPerDay,
    required int advanceWorkdays,
  }) {
    return salaryPerDay * advanceWorkdays;
  }

  /// Calculate rest payment
  static double calculateRestPayment({
    required double salary,
    required double advanceValue,
  }) {
    return salary - advanceValue;
  }

  /// Round money to 2 decimal places
  static double round(double amount) {
    return (amount * 100).round() / 100;
  }

  /// Format money amount with ruble symbol
  static String formatMoney(double amount) {
    return NumberFormat.currency(locale: 'ru_RU', symbol: '₽', decimalDigits: 0)
        .format(amount);
  }

  /// Validate salary amount
  static bool isValidSalary(double salary) {
    return salary >= 0 && salary.isFinite;
  }

  /// Validate multiplier
  static bool isValidMultiplier(double multiplier) {
    return multiplier > 0 && multiplier.isFinite;
  }
}
