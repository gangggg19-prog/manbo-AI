class FamilyChatRoom {
  const FamilyChatRoom({
    required this.id,
    required this.babyId,
    required this.title,
    required this.createdAt,
    required this.currentUserRole,
  });

  final String id;
  final String babyId;
  final String title;
  final DateTime createdAt;
  final String? currentUserRole;

  factory FamilyChatRoom.fromApiJson(Map<String, dynamic> json) =>
      FamilyChatRoom(
        id: json['id'] as String,
        babyId: json['babyId'] as String,
        title: json['title'] as String,
        createdAt: DateTime.parse(json['createdAt'] as String).toLocal(),
        currentUserRole: json['currentUserRole'] as String?,
      );
}

class FamilyChatMessage {
  const FamilyChatMessage({
    required this.id,
    required this.roomId,
    required this.senderUserId,
    required this.senderName,
    required this.content,
    required this.sentAt,
  });

  final String id;
  final String roomId;
  final String? senderUserId;
  final String senderName;
  final String content;
  final DateTime sentAt;

  factory FamilyChatMessage.fromApiJson(Map<String, dynamic> json) =>
      FamilyChatMessage(
        id: json['id'] as String,
        roomId: json['roomId'] as String,
        senderUserId: json['senderUserId'] as String?,
        senderName: json['senderName'] as String,
        content: json['content'] as String,
        sentAt: DateTime.parse(json['sentAt'] as String).toLocal(),
      );
}
