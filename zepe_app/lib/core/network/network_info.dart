import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Abstract interface for network connectivity checking
abstract class NetworkInfo {
  Future<bool> get isConnected;
}

/// Implementation using dart:io
class NetworkInfoImpl implements NetworkInfo {
  // In a real app, you'd use connectivity_plus package
  // For now, we'll assume network is available
  @override
  Future<bool> get isConnected async {
    // TODO: Implement with connectivity_plus package
    return true;
  }
}

final networkInfoProvider = Provider<NetworkInfo>((ref) {
  return NetworkInfoImpl();
});
