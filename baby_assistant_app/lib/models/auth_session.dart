import 'dart:convert';

class AuthUser {
  const AuthUser({
    required this.id,
    required this.username,
    required this.displayName,
  });

  final String id;
  final String username;
  final String displayName;

  factory AuthUser.fromJson(Map<String, dynamic> json) => AuthUser(
    id: json['id'] as String,
    username: json['username'] as String,
    displayName: json['displayName'] as String,
  );

  Map<String, dynamic> toJson() => {
    'id': id,
    'username': username,
    'displayName': displayName,
  };
}

class AuthSession {
  const AuthSession({
    required this.accessToken,
    required this.expiresAt,
    required this.user,
  });

  final String accessToken;
  final DateTime expiresAt;
  final AuthUser user;

  bool get isExpired => !expiresAt.isAfter(DateTime.now());

  factory AuthSession.fromApiJson(Map<String, dynamic> json) => AuthSession(
    accessToken: json['accessToken'] as String,
    expiresAt: DateTime.parse(json['expiresAt'] as String).toLocal(),
    user: AuthUser.fromJson(json['user'] as Map<String, dynamic>),
  );

  factory AuthSession.decode(String value) {
    final json = jsonDecode(value) as Map<String, dynamic>;
    return AuthSession(
      accessToken: json['accessToken'] as String,
      expiresAt: DateTime.parse(json['expiresAt'] as String),
      user: AuthUser.fromJson(json['user'] as Map<String, dynamic>),
    );
  }

  String encode() => jsonEncode({
    'accessToken': accessToken,
    'expiresAt': expiresAt.toIso8601String(),
    'user': user.toJson(),
  });
}
