class FamilyInvite {
  const FamilyInvite({
    required this.id,
    required this.babyId,
    required this.inviteCode,
    required this.expiresAt,
  });

  final String id;
  final String babyId;
  final String inviteCode;
  final DateTime expiresAt;

  factory FamilyInvite.fromApiJson(Map<String, dynamic> json) => FamilyInvite(
    id: json['id'] as String,
    babyId: json['babyId'] as String,
    inviteCode: json['inviteCode'] as String,
    expiresAt: DateTime.parse(json['expiresAt'] as String).toLocal(),
  );
}

class FamilyMembership {
  const FamilyMembership({
    required this.babyId,
    required this.userId,
    required this.memberRole,
    required this.joinedAt,
  });

  final String babyId;
  final String userId;
  final String memberRole;
  final DateTime joinedAt;

  factory FamilyMembership.fromApiJson(Map<String, dynamic> json) =>
      FamilyMembership(
        babyId: json['babyId'] as String,
        userId: json['userId'] as String,
        memberRole: json['memberRole'] as String,
        joinedAt: DateTime.parse(json['joinedAt'] as String).toLocal(),
      );
}
