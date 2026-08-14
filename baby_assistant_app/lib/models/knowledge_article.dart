class KnowledgeArticle {
  const KnowledgeArticle({
    required this.slug,
    required this.title,
    required this.category,
    required this.minAgeMonths,
    required this.maxAgeMonths,
    required this.content,
    required this.sourceName,
    required this.sourceUrl,
  });
  final String slug, title, category, content, sourceName, sourceUrl;
  final int minAgeMonths;
  final int? maxAgeMonths;
  factory KnowledgeArticle.fromApiJson(Map<String, dynamic> json) =>
      KnowledgeArticle(
        slug: json['slug'] as String,
        title: json['title'] as String,
        category: json['category'] as String,
        minAgeMonths: json['minAgeMonths'] as int,
        maxAgeMonths: json['maxAgeMonths'] as int?,
        content: json['content'] as String,
        sourceName: json['sourceName'] as String,
        sourceUrl: json['sourceUrl'] as String,
      );
  String get ageLabel => maxAgeMonths == null
      ? '$minAgeMonths 月龄起'
      : '$minAgeMonths–$maxAgeMonths 月龄';
}
