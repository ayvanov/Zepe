import 'package:flutter_test/flutter_test.dart';
import 'package:zepe_app/features/salary_calendar/domain/entities/salary_settings.dart';

void main() {
  test('salary settings validation accepts valid values', () {
    const settings = SalarySettings(
      salary: 120000,
      salaryMultiplier: 0.87,
      advanceDays: 15,
      advancePayDay: 25,
      restPayDay: 10,
    );

    expect(settings.validate(), isNull);
  });

  test('salary settings validation rejects invalid salary', () {
    const settings = SalarySettings(salary: -1);

    expect(settings.validate(), isNotNull);
  });
}
