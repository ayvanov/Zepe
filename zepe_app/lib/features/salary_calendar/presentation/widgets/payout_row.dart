import 'package:flutter/material.dart';
import '../../../../core/utils/money_utils.dart';
import '../../domain/entities/payout.dart';

/// Row widget displaying payout information
class PayoutRow extends StatelessWidget {
  final Payout payout;

  const PayoutRow({super.key, required this.payout});

  @override
  Widget build(BuildContext context) {
    final isAdvance = payout.type == 'advance';
    final icon = isAdvance ? Icons.arrow_circle_up : Icons.arrow_circle_down;
    final color = isAdvance ? Colors.green : Colors.blue;
    final label = isAdvance ? 'Аванс' : 'Остаток';

    return Padding(
      padding: const EdgeInsets.symmetric(vertical: 4),
      child: Row(
        children: [
          Icon(icon, color: color, size: 20),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  label,
                  style: Theme.of(context).textTheme.bodySmall?.copyWith(
                        color: Colors.grey[600],
                      ),
                ),
                Text(
                  '${payout.date.day}.${payout.date.month}.${payout.date.year}',
                  style: Theme.of(context).textTheme.bodySmall,
                ),
              ],
            ),
          ),
          if (payout.isPast) ...[
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
              decoration: BoxDecoration(
                color: Colors.grey[200],
                borderRadius: BorderRadius.circular(4),
              ),
              child: Text(
                'Получено',
                style: Theme.of(context).textTheme.labelSmall,
              ),
            ),
            const SizedBox(width: 8),
          ],
          Text(
            MoneyUtils.formatMoney(payout.value),
            style: Theme.of(context).textTheme.titleMedium?.copyWith(
                  color: color,
                  fontWeight: FontWeight.bold,
                ),
          ),
        ],
      ),
    );
  }
}
