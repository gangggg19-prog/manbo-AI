import 'package:shared_preferences/shared_preferences.dart';

import '../models/auth_session.dart';

/// Demo storage for the signed-in session. Production should use secure storage.
class AuthSessionStore {
  static const _storageKey = 'manbo_auth_session_v1';

  Future<AuthSession?> load() async {
    final preferences = await SharedPreferences.getInstance();
    final value = preferences.getString(_storageKey);
    if (value == null) return null;

    try {
      final session = AuthSession.decode(value);
      if (session.isExpired) {
        await preferences.remove(_storageKey);
        return null;
      }
      return session;
    } catch (_) {
      await preferences.remove(_storageKey);
      return null;
    }
  }

  Future<void> save(AuthSession session) async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.setString(_storageKey, session.encode());
  }

  Future<void> clear() async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.remove(_storageKey);
  }
}
