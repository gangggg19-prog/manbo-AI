class SleepSession {
  const SleepSession({
    required this.id,
    required this.babyId,
    required this.startedAt,
    required this.endedAt,
  });

  final String id;
  final String babyId;
  final DateTime startedAt;
  final DateTime? endedAt;

  bool get isActive => endedAt == null;

  factory SleepSession.fromApiJson(Map<String, dynamic> json) {
    return SleepSession(
      id: json['id'] as String,
      babyId: json['babyId'] as String,
      startedAt: DateTime.parse(json['startedAt'] as String).toLocal(),
      endedAt: json['endedAt'] == null
          ? null
          : DateTime.parse(json['endedAt'] as String).toLocal(),
    );
  }
}
