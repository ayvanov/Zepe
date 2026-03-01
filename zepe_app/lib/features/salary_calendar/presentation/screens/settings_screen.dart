import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:zepe_app/features/salary_calendar/data/providers.dart';
import 'package:zepe_app/features/salary_calendar/presentation/state/settings_state.dart';

/// Settings screen for configuring salary parameters
class SettingsScreen extends ConsumerStatefulWidget {
  const SettingsScreen({super.key});

  @override
  ConsumerState<SettingsScreen> createState() => _SettingsScreenState();
}

class _SettingsScreenState extends ConsumerState<SettingsScreen> {
  final _formKey = GlobalKey<FormState>();
  ProviderSubscription<SettingsState>? _settingsSub;
  late final TextEditingController _salaryController;
  late final TextEditingController _multiplierController;
  late final TextEditingController _advanceDaysController;
  late final TextEditingController _advancePayDayController;
  late final TextEditingController _restPayDayController;

  @override
  void initState() {
    super.initState();
    final initialState = SettingsState.initial();
    _salaryController = TextEditingController(text: initialState.salaryInput);
    _multiplierController = TextEditingController(
      text: initialState.salaryMultiplier.toString(),
    );
    _advanceDaysController = TextEditingController(
      text: initialState.advanceDays.toString(),
    );
    _advancePayDayController = TextEditingController(
      text: initialState.advancePayDay.toString(),
    );
    _restPayDayController = TextEditingController(
      text: initialState.restPayDay.toString(),
    );

    _settingsSub = ref.listenManual<SettingsState>(
      settingsControllerProvider,
      (previous, next) {
        _syncController(_salaryController, next.salaryInput);
        _syncController(
          _multiplierController,
          next.salaryMultiplier.toString(),
        );
        _syncController(_advanceDaysController, next.advanceDays.toString());
        _syncController(
            _advancePayDayController, next.advancePayDay.toString(),);
        _syncController(_restPayDayController, next.restPayDay.toString());
      },
    );

    // Load settings
    WidgetsBinding.instance.addPostFrameCallback((_) {
      ref.read(settingsControllerProvider.notifier).loadSettings();
    });
  }

  @override
  void dispose() {
    _settingsSub?.close();
    _salaryController.dispose();
    _multiplierController.dispose();
    _advanceDaysController.dispose();
    _advancePayDayController.dispose();
    _restPayDayController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Настройки'),
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            _buildSalaryField(),
            const SizedBox(height: 16),
            _buildMultiplierField(),
            const SizedBox(height: 16),
            _buildAdvanceDaysField(),
            const SizedBox(height: 16),
            _buildAdvancePayDayField(),
            const SizedBox(height: 16),
            _buildRestPayDayField(),
            const SizedBox(height: 32),
            _buildSaveButton(),
          ],
        ),
      ),
    );
  }

  Widget _buildSalaryField() {
    return TextFormField(
      controller: _salaryController,
      decoration: const InputDecoration(
        labelText: 'Оклад (₽)',
        prefixIcon: Icon(Icons.attach_money),
        border: OutlineInputBorder(),
      ),
      keyboardType: TextInputType.number,
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Введите оклад';
        }
        final salary = double.tryParse(value);
        if (salary == null || salary < 0) {
          return 'Некорректное значение';
        }
        return null;
      },
      onChanged: (value) {
        ref.read(settingsControllerProvider.notifier).updateSalaryInput(value);
      },
    );
  }

  Widget _buildMultiplierField() {
    return TextFormField(
      controller: _multiplierController,
      decoration: const InputDecoration(
        labelText: 'Коэффициент',
        prefixIcon: Icon(Icons.percent),
        border: OutlineInputBorder(),
        helperText: 'Например, 0.87 для НДФЛ',
      ),
      keyboardType: const TextInputType.numberWithOptions(decimal: true),
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Введите коэффициент';
        }
        final multiplier = double.tryParse(value);
        if (multiplier == null || multiplier <= 0) {
          return 'Некорректное значение';
        }
        return null;
      },
      onChanged: (value) {
        final multiplier = double.tryParse(value) ?? 1.0;
        ref
            .read(settingsControllerProvider.notifier)
            .updateSalaryMultiplier(multiplier);
      },
    );
  }

  Widget _buildAdvanceDaysField() {
    return TextFormField(
      controller: _advanceDaysController,
      decoration: const InputDecoration(
        labelText: 'Дней аванса',
        prefixIcon: Icon(Icons.calendar_today),
        helperText: 'Сколько дней с начала месяца учитывать в авансе',
        border: OutlineInputBorder(),
      ),
      keyboardType: TextInputType.number,
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Введите количество дней';
        }
        final days = int.tryParse(value);
        if (days == null || days < 1 || days > 31) {
          return 'От 1 до 31';
        }
        return null;
      },
      onChanged: (value) {
        final days = int.tryParse(value) ?? 15;
        ref.read(settingsControllerProvider.notifier).updateAdvanceDays(days);
      },
    );
  }

  Widget _buildAdvancePayDayField() {
    return TextFormField(
      controller: _advancePayDayController,
      decoration: const InputDecoration(
        labelText: 'День выдачи аванса',
        prefixIcon: Icon(Icons.event),
        helperText: 'Число месяца',
        border: OutlineInputBorder(),
      ),
      keyboardType: TextInputType.number,
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Введите день';
        }
        final day = int.tryParse(value);
        if (day == null || day < 1 || day > 31) {
          return 'От 1 до 31';
        }
        return null;
      },
      onChanged: (value) {
        final day = int.tryParse(value) ?? 25;
        ref.read(settingsControllerProvider.notifier).updateAdvancePayDay(day);
      },
    );
  }

  Widget _buildRestPayDayField() {
    return TextFormField(
      controller: _restPayDayController,
      decoration: const InputDecoration(
        labelText: 'День выдачи остатка',
        prefixIcon: Icon(Icons.event),
        helperText: 'Число следующего месяца',
        border: OutlineInputBorder(),
      ),
      keyboardType: TextInputType.number,
      validator: (value) {
        if (value == null || value.isEmpty) {
          return 'Введите день';
        }
        final day = int.tryParse(value);
        if (day == null || day < 1 || day > 31) {
          return 'От 1 до 31';
        }
        return null;
      },
      onChanged: (value) {
        final day = int.tryParse(value) ?? 10;
        ref.read(settingsControllerProvider.notifier).updateRestPayDay(day);
      },
    );
  }

  Widget _buildSaveButton() {
    return ElevatedButton(
      onPressed: _saveSettings,
      style: ElevatedButton.styleFrom(
        padding: const EdgeInsets.symmetric(vertical: 16),
      ),
      child: const Text(
        'Сохранить настройки',
        style: TextStyle(fontSize: 16),
      ),
    );
  }

  Future<void> _saveSettings() async {
    if (_formKey.currentState?.validate() ?? false) {
      final controller = ref.read(settingsControllerProvider.notifier);
      final success = await controller.save();

      if (success && mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Настройки сохранены')),
        );
        // Navigate back after delay
        Future.delayed(const Duration(milliseconds: 1000), () {
          if (mounted) Navigator.pop(context);
        });
      } else if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Ошибка сохранения')),
        );
      }
    }
  }

  void _syncController(TextEditingController controller, String value) {
    if (controller.text != value) {
      controller.text = value;
    }
  }
}
