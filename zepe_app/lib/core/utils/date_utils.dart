import 'package:intl/intl.dart';

/// Date formatting utilities for ru-RU locale
class DateUtils {
  static const List<String> _monthNames = [
    'Январь',
    'Февраль',
    'Март',
    'Апрель',
    'Май',
    'Июнь',
    'Июль',
    'Август',
    'Сентябрь',
    'Октябрь',
    'Ноябрь',
    'Декабрь',
  ];

  static const List<String> _monthNamesGenitive = [
    'января',
    'февраля',
    'марта',
    'апреля',
    'мая',
    'июня',
    'июля',
    'августа',
    'сентября',
    'октября',
    'ноября',
    'декабря',
  ];

  /// Get month name in nominative case
  static String getMonthName(int monthNum) {
    if (monthNum < 1 || monthNum > 12) {
      throw ArgumentError('Month must be between 1 and 12');
    }
    return _monthNames[monthNum - 1];
  }

  /// Get month name in genitive case (for dates)
  static String getMonthNameGenitive(int monthNum) {
    if (monthNum < 1 || monthNum > 12) {
      throw ArgumentError('Month must be between 1 and 12');
    }
    return _monthNamesGenitive[monthNum - 1];
  }

  /// Format date as "DD month YYYY"
  static String formatDateFull(DateTime date) {
    return '${date.day} ${getMonthNameGenitive(date.month)} ${date.year}';
  }

  /// Format date as "DD.MM.YYYY"
  static String formatDateShort(DateTime date) {
    return DateFormat('dd.MM.yyyy').format(date);
  }

  /// Format money amount with ruble symbol
  static String formatMoney(double amount) {
    return NumberFormat.currency(locale: 'ru_RU', symbol: '₽', decimalDigits: 0)
        .format(amount);
  }

  /// Check if year is leap
  static bool isLeapYear(int year) {
    return DateTime(year, 2, 29).month == 2;
  }

  /// Get days in month
  static int getDaysInMonth(int year, int month) {
    return DateTime(year, month + 1, 0).day;
  }

  /// Create DateTime for first day of month
  static DateTime firstDayOfMonth(int year, int month) {
    return DateTime(year, month, 1);
  }

  /// Create DateTime for last day of month
  static DateTime lastDayOfMonth(int year, int month) {
    return DateTime(year, month + 1, 0);
  }
}
