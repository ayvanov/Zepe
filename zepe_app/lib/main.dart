import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:hive_flutter/hive_flutter.dart';
import 'package:shared_preferences/shared_preferences.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'core/constants/app_constants.dart';
import 'app/app.dart';
import 'core/utils/logger.dart';
import 'features/salary_calendar/data/providers.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();

  // Initialize Hive
  try {
    await Hive.initFlutter();
    await Hive.openBox(AppConstants.yearCacheBoxName);
    AppLogger.i('Hive initialized', tag: 'main');
  } catch (e) {
    AppLogger.e('Failed to initialize Hive', tag: 'main', error: e);
  }

  // Initialize SharedPreferences
  late final SharedPreferences prefs;
  try {
    prefs = await SharedPreferences.getInstance();
    AppLogger.i('SharedPreferences initialized', tag: 'main');
  } catch (e) {
    AppLogger.e('Failed to initialize SharedPreferences',
        tag: 'main', error: e,);
    prefs = await SharedPreferences.getInstance();
  }

  // Set preferred orientations
  await SystemChrome.setPreferredOrientations([
    DeviceOrientation.portraitUp,
    DeviceOrientation.portraitDown,
  ]);

  // Run app with provider overrides
  runApp(
    ProviderScope(
      overrides: [
        sharedPreferencesProvider.overrideWithValue(prefs),
      ],
      child: const ZepeApp(),
    ),
  );
}
