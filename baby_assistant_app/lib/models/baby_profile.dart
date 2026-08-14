class BabyProfile {
  const BabyProfile({
    required this.id,
    required this.displayName,
    required this.birthDate,
  });

  final String id;
  final String displayName;
  final DateTime birthDate;

  factory BabyProfile.fromApiJson(Map<String, dynamic> json) {
    return BabyProfile(
      id: json['id'] as String,
      displayName: json['displayName'] as String,
      birthDate: DateTime.parse(json['birthDate'] as String),
    );
  }
}
