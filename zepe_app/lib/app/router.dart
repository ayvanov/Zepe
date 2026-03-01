import 'package:flutter/material.dart';
import 'package:go_router/go_router.dart';
import '../../features/salary_calendar/presentation/screens/home_screen.dart';
import '../../features/salary_calendar/presentation/screens/settings_screen.dart';

/// Application router configuration
class AppRouter {
  static final GoRouter router = GoRouter(
    initialLocation: '/',
    routes: [
      GoRoute(
        path: '/',
        name: 'home',
        builder: (context, state) => const HomeScreen(),
      ),
      GoRoute(
        path: '/settings',
        name: 'settings',
        builder: (context, state) => const SettingsScreen(),
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      appBar: AppBar(title: const Text('Ошибка')),
      body: Center(
        child: Text('Страница не найдена: ${state.uri.path}'),
      ),
    ),
  );
}
