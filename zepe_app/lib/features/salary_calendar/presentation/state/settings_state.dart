import 'package:freezed_annotation/freezed_annotation.dart';

part 'settings_state.freezed.dart';

@freezed
class SettingsState with _$SettingsState {
  const factory SettingsState({
    required SettingsStatus status,
    required String salaryInput,
    required double salaryMultiplier,
    required int advanceDays,
    required int advancePayDay,
    required int restPayDay,
    String? validationMessage,
    String? errorMessage,
  }) = _SettingsState;

  factory SettingsState.initial() => const SettingsState(
        status: SettingsStatus.idle,
        salaryInput: '100000',
        salaryMultiplier: 1.0,
        advanceDays: 15,
        advancePayDay: 25,
        restPayDay: 10,
      );
}

enum SettingsStatus { idle, saving, saved, validationError, error }
