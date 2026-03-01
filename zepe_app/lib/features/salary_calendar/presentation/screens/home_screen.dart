import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';
import 'package:zepe_app/features/salary_calendar/data/providers.dart';
import 'package:zepe_app/features/salary_calendar/presentation/state/home_state.dart';
import '../widgets/error_view.dart';
import '../widgets/loading_view.dart';
import '../widgets/month_card.dart';

/// Home screen showing salary calendar
class HomeScreen extends ConsumerStatefulWidget {
  const HomeScreen({super.key});

  @override
  ConsumerState<HomeScreen> createState() => _HomeScreenState();
}

class _HomeScreenState extends ConsumerState<HomeScreen> {
  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref
          .read(homeControllerProvider.notifier)
          .loadYearData(DateTime.now().year);
    });
  }

  @override
  Widget build(BuildContext context) {
    final state = ref.watch(homeControllerProvider);

    return Scaffold(
      appBar: AppBar(
        title: const Text('Зарплатный календарь'),
        actions: [
          IconButton(
            icon: const Icon(Icons.refresh),
            onPressed: () => _refresh(),
          ),
          IconButton(
            icon: const Icon(Icons.settings),
            onPressed: () => _goToSettings(),
          ),
        ],
      ),
      body: Column(
        children: [
          // Year selector
          _buildYearSelector(state.year),
          // Months list
          Expanded(
            child: _buildBody(state),
          ),
        ],
      ),
    );
  }

  Widget _buildYearSelector(int year) {
    return Padding(
      padding: const EdgeInsets.all(16.0),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          IconButton(
            icon: const Icon(Icons.chevron_left),
            onPressed: () => _changeYear(year, -1),
          ),
          Text(
            '$year',
            style: Theme.of(context).textTheme.headlineSmall,
          ),
          IconButton(
            icon: const Icon(Icons.chevron_right),
            onPressed: () => _changeYear(year, 1),
          ),
        ],
      ),
    );
  }

  Widget _buildBody(HomeState state) {
    switch (state.status) {
      case HomeStatus.initial:
      case HomeStatus.loading:
        return const LoadingView(message: 'Загружаем расчеты...');
      case HomeStatus.error:
        return ErrorView(
          message: state.errorMessage ?? 'Не удалось загрузить данные',
          onRetry: _refresh,
        );
      case HomeStatus.data:
        return _buildMonthsList(state);
    }
  }

  Widget _buildMonthsList(HomeState state) {
    if (state.months.isEmpty) {
      return const Center(
        child: Text('Нет данных для отображения'),
      );
    }

    return ListView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      itemCount: state.months.length,
      itemBuilder: (context, index) {
        return MonthCard(monthMeta: state.months[index]);
      },
    );
  }

  void _changeYear(int currentYear, int delta) {
    final newYear = currentYear + delta;
    ref.read(homeControllerProvider.notifier).changeYear(newYear);
  }

  void _refresh() {
    ref.read(homeControllerProvider.notifier).refresh();
  }

  Future<void> _goToSettings() async {
    await context.push('/settings');
    if (!mounted) return;
    await ref.read(homeControllerProvider.notifier).refresh();
  }
}
