import 'dart:convert';

enum CareRecordType { feeding, diaper }

class CareRecord {
  const CareRecord({
    required this.id,
    required this.type,
    required this.recordedAt,
    this.amountMl,
  });

  final String id;
  final CareRecordType type;
  final DateTime recordedAt;
  final int? amountMl;

  Map<String, Object?> toJson() => {
    'id': id,
    'type': type.name,
    'recordedAt': recordedAt.toIso8601String(),
    'amountMl': amountMl,
  };

  Map<String, Object?> toCreateRequestJson(String babyId) => {
    'babyId': babyId,
    'type': type.name.toUpperCase(),
    'recordedAt': recordedAt.toUtc().toIso8601String(),
    'amountMl': amountMl,
  };

  String encode() => jsonEncode(toJson());

  factory CareRecord.decode(String source) {
    final json = jsonDecode(source) as Map<String, dynamic>;
    return CareRecord(
      id: json['id'] as String,
      type: CareRecordType.values.byName(json['type'] as String),
      recordedAt: DateTime.parse(json['recordedAt'] as String),
      amountMl: json['amountMl'] as int?,
    );
  }

  factory CareRecord.fromApiJson(Map<String, dynamic> json) {
    return CareRecord(
      id: json['id'] as String,
      type: CareRecordType.values.byName(
        (json['type'] as String).toLowerCase(),
      ),
      recordedAt: DateTime.parse(json['recordedAt'] as String).toLocal(),
      amountMl: json['amountMl'] as int?,
    );
  }
}
