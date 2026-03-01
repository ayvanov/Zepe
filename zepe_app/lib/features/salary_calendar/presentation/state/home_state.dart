import 'package:freezed_annotation/freezed_annotation.dart';
import '../../domain/entities/month_meta.dart';
import '../../domain/entities/salary_settings.dart';

part 'home_state.freezed.dart';

@freezed
class HomeState with _$HomeState {
  const factory HomeState({
    required HomeStatus status,
    required int year,
    required List<MonthMeta> months,
    required SalarySettings settings,
    required bool isStaleData,
    String? errorMessage,
  }) = _HomeState;

  factory HomeState.initial() => const HomeState(
        status: HomeStatus.initial,
        year: 2025,
        months: [],
        settings: SalarySettings(),
        isStaleData: false,
      );
}

enum HomeStatus { initial, loading, data, error }
