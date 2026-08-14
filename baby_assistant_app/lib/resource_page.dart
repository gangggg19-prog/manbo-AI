import 'package:flutter/material.dart';
import 'app_theme.dart';
import 'data/baby_assistant_api.dart';
import 'models/knowledge_article.dart';

class ResourcePage extends StatefulWidget {
  const ResourcePage({super.key, this.api, this.onBack});
  final BabyAssistantApi? api;
  final VoidCallback? onBack;
  @override
  State<ResourcePage> createState() => _ResourcePageState();
}

class _ResourcePageState extends State<ResourcePage> {
  late final BabyAssistantApi _api;
  late Future<List<KnowledgeArticle>> _articles;
  @override
  void initState() {
    super.initState();
    _api = widget.api ?? BabyAssistantApi();
    _articles = _api.fetchKnowledgeArticles();
  }

  @override
  Widget build(BuildContext context) => Scaffold(
    backgroundColor: AppColors.canvas,
    body: SafeArea(
      child: FutureBuilder<List<KnowledgeArticle>>(
        future: _articles,
        builder: (context, snapshot) {
          if (snapshot.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snapshot.hasError) return const Center(child: Text('暂时无法读取育儿资料'));
          final articles = snapshot.data ?? const [];
          return ListView(
            padding: const EdgeInsets.fromLTRB(18, 18, 18, 110),
            children: [
              IconButton(
                key: const Key('resource-back-button'),
                tooltip: '返回首页',
                alignment: Alignment.centerLeft,
                onPressed:
                    widget.onBack ?? () => Navigator.of(context).maybePop(),
                icon: const Icon(Icons.arrow_back_rounded),
              ),
              const Text(
                '育儿资源',
                style: TextStyle(fontSize: 30, fontWeight: FontWeight.w900),
              ),
              const SizedBox(height: 6),
              const Text(
                '经过整理的资料，也会作为 Manbo 的回答依据。',
                style: TextStyle(color: AppColors.muted),
              ),
              const SizedBox(height: 20),
              for (final article in articles)
                _ArticleCard(
                  article: article,
                  onTap: () => Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (_) => _ArticleDetail(article: article),
                    ),
                  ),
                ),
            ],
          );
        },
      ),
    ),
  );
}

class _ArticleCard extends StatelessWidget {
  const _ArticleCard({required this.article, required this.onTap});
  final KnowledgeArticle article;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.only(bottom: 12),
    child: Material(
      color: AppColors.paper,
      borderRadius: BorderRadius.circular(24),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(24),
        child: Padding(
          padding: const EdgeInsets.all(18),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                article.ageLabel,
                style: const TextStyle(
                  color: AppColors.muted,
                  fontSize: 11,
                  fontWeight: FontWeight.w700,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                article.title,
                style: const TextStyle(
                  fontSize: 18,
                  fontWeight: FontWeight.w800,
                ),
              ),
              const SizedBox(height: 10),
              Text(
                article.sourceName,
                style: const TextStyle(color: AppColors.muted, fontSize: 12),
              ),
              const SizedBox(height: 4),
              const Align(
                alignment: Alignment.centerRight,
                child: Icon(Icons.arrow_outward_rounded),
              ),
            ],
          ),
        ),
      ),
    ),
  );
}

class _ArticleDetail extends StatelessWidget {
  const _ArticleDetail({required this.article});
  final KnowledgeArticle article;
  @override
  Widget build(BuildContext context) => Scaffold(
    backgroundColor: AppColors.canvas,
    appBar: AppBar(backgroundColor: AppColors.canvas),
    body: Padding(
      padding: const EdgeInsets.all(20),
      child: ListView(
        children: [
          Text(
            article.ageLabel,
            style: const TextStyle(color: AppColors.muted),
          ),
          const SizedBox(height: 10),
          Text(
            article.title,
            style: const TextStyle(fontSize: 28, fontWeight: FontWeight.w900),
          ),
          const SizedBox(height: 22),
          Text(
            article.content,
            style: const TextStyle(fontSize: 16, height: 1.65),
          ),
          const SizedBox(height: 28),
          const Text('资料来源', style: TextStyle(fontWeight: FontWeight.w800)),
          const SizedBox(height: 6),
          Text(
            article.sourceName,
            style: const TextStyle(color: AppColors.muted),
          ),
          Text(
            article.sourceUrl,
            style: const TextStyle(color: AppColors.muted, fontSize: 12),
          ),
        ],
      ),
    ),
  );
}
