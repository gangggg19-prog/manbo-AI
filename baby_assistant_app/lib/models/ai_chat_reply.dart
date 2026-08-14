class AiKnowledgeReference {
  const AiKnowledgeReference({
    required this.title,
    required this.sourceName,
    required this.sourceUrl,
  });

  final String title;
  final String sourceName;
  final String sourceUrl;

  factory AiKnowledgeReference.fromApiJson(Map<String, dynamic> json) {
    return AiKnowledgeReference(
      title: json['title'] as String,
      sourceName: json['sourceName'] as String,
      sourceUrl: json['sourceUrl'] as String,
    );
  }
}

class AiChatReply {
  const AiChatReply({
    required this.conversationId,
    required this.reply,
    required this.safetyNotice,
    required this.source,
    required this.suggestedActions,
    required this.references,
  });

  final String conversationId;
  final String reply;
  final String safetyNotice;
  final String source;
  final List<String> suggestedActions;
  final List<AiKnowledgeReference> references;

  factory AiChatReply.fromApiJson(Map<String, dynamic> json) {
    return AiChatReply(
      conversationId: json['conversationId'] as String,
      reply: json['reply'] as String,
      safetyNotice: json['safetyNotice'] as String,
      source: json['source'] as String,
      suggestedActions: (json['suggestedActions'] as List<dynamic>? ?? const [])
          .map((item) => item.toString())
          .toList(),
      references: (json['references'] as List<dynamic>? ?? const [])
          .map(
            (item) =>
                AiKnowledgeReference.fromApiJson(item as Map<String, dynamic>),
          )
          .toList(),
    );
  }
}
