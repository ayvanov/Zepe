import 'package:logger/logger.dart';

/// Application logger wrapper
class AppLogger {
  static final Logger _logger = Logger(
    printer: PrettyPrinter(
      methodCount: 2,
      errorMethodCount: 8,
      lineLength: 120,
      colors: true,
      printEmojis: true,
      dateTimeFormat: DateTimeFormat.none,
    ),
  );

  static void d(String message, {String? tag}) {
    _logger.d(_formatMessage(message, tag));
  }

  static void i(String message, {String? tag}) {
    _logger.i(_formatMessage(message, tag));
  }

  static void w(String message, {String? tag}) {
    _logger.w(_formatMessage(message, tag));
  }

  static void e(String message,
      {String? tag, dynamic error, StackTrace? stackTrace,}) {
    _logger.e(_formatMessage(message, tag),
        error: error, stackTrace: stackTrace,);
  }

  static String _formatMessage(String message, String? tag) {
    return tag != null ? '[$tag] $message' : message;
  }
}
