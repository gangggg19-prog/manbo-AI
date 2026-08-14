class AiConversation {
  const AiConversation({
    required this.id,
    required this.babyId,
    required this.createdAt,
  });

  final String id;
  final String babyId;
  final DateTime createdAt;

  factory AiConversation.fromApiJson(Map<String, dynamic> json) =>
      AiConversation(
        id: json['id'] as String,
        babyId: json['babyId'] as String,
        createdAt: DateTime.parse(json['createdAt'] as String).toLocal(),
      );
}

class AiMessage {
  const AiMessage({
    required this.id,
    required this.conversationId,
    required this.role,
    required this.content,
    required this.source,
    required this.createdAt,
  });

  final String id;
  final String conversationId;
  final String role;
  final String content;
  final String? source;
  final DateTime createdAt;

  bool get isUser => role == 'USER';

  factory AiMessage.fromApiJson(Map<String, dynamic> json) => AiMessage(
    id: json['id'] as String,
    conversationId: json['conversationId'] as String,
    role: json['role'] as String,
    content: json['content'] as String,
    source: json['source'] as String?,
    createdAt: DateTime.parse(json['createdAt'] as String).toLocal(),
  );
}
