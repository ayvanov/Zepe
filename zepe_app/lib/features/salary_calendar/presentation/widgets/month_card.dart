import 'package:flutter/material.dart';
import 'package:zepe_app/core/utils/date_utils.dart' as app_date;
import '../../../../core/utils/money_utils.dart';
import '../../domain/entities/month_meta.dart';

/// Card widget displaying month salary information
class MonthCard extends StatelessWidget {
  final MonthMeta monthMeta;

  const MonthCard({super.key, required this.monthMeta});

  @override
  Widget build(BuildContext context) {
    return Card(
      margin: const EdgeInsets.only(bottom: 12),
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Month header
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  app_date.DateUtils.getMonthName(monthMeta.monthNum),
                  style: Theme.of(context).textTheme.titleLarge,
                ),
                Text(
                  '${monthMeta.workdays} раб. дн.',
                  style: Theme.of(context).textTheme.bodyMedium?.copyWith(
                        color: Colors.grey,
                      ),
                ),
              ],
            ),
            const SizedBox(height: 12),
            // Salary per day
            Row(
              children: [
                Icon(Icons.calculate, size: 16, color: Colors.grey[600]),
                const SizedBox(width: 4),
                Text(
                  'За день: ${MoneyUtils.formatMoney(monthMeta.salaryPerDay)}',
                  style: Theme.of(context).textTheme.bodyMedium,
                ),
              ],
            ),
            const Divider(height: 24),
            // Advance payment
            _buildPayoutRow(
              context,
              type: 'Аванс',
              value: monthMeta.advanceValue,
              date: monthMeta.advanceDate,
              icon: Icons.arrow_circle_up,
              color: Colors.green,
            ),
            const SizedBox(height: 8),
            // Rest payment
            _buildPayoutRow(
              context,
              type: 'Остаток',
              value: monthMeta.restValue,
              date: monthMeta.restDate,
              icon: Icons.arrow_circle_down,
              color: Colors.blue,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildPayoutRow(
    BuildContext context, {
    required String type,
    required double value,
    required DateTime date,
    required IconData icon,
    required Color color,
  }) {
    return Row(
      children: [
        Icon(icon, color: color, size: 20),
        const SizedBox(width: 8),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                type,
                style: Theme.of(context).textTheme.bodySmall?.copyWith(
                      color: Colors.grey[600],
                    ),
              ),
              Text(
                app_date.DateUtils.formatDateFull(date),
                style: Theme.of(context).textTheme.bodySmall,
              ),
            ],
          ),
        ),
        Text(
          MoneyUtils.formatMoney(value),
          style: Theme.of(context).textTheme.titleMedium?.copyWith(
                color: color,
                fontWeight: FontWeight.bold,
              ),
        ),
      ],
    );
  }
}
