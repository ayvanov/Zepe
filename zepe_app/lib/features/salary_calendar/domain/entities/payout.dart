import 'package:equatable/equatable.dart';

/// Payout information for UI display
class Payout extends Equatable {
  final String type; // 'advance' or 'rest'
  final double value;
  final DateTime date;
  final bool isPast;

  const Payout({
    required this.type,
    required this.value,
    required this.date,
    required this.isPast,
  });

  @override
  List<Object?> get props => [type, value, date, isPast];

  @override
  String toString() =>
      'Payout(type: $type, value: $value, date: $date, isPast: $isPast)';
}
