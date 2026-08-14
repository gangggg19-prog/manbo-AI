import 'package:flutter/material.dart';

import 'app_theme.dart';
import 'data/baby_assistant_api.dart';
import 'models/ai_chat_reply.dart';
import 'models/ai_conversation.dart';

/// Durable AI conversation UI. Java restores history after an app restart.
class AiAssistantPage extends StatefulWidget {
  const AiAssistantPage({super.key, required this.api, this.onBack});

  final BabyAssistantApi? api;
  final VoidCallback? onBack;

  @override
  State<AiAssistantPage> createState() => _AiAssistantPageState();
}

class _AiAssistantPageState extends State<AiAssistantPage> {
  final _input = TextEditingController();
  final _scroll = ScrollController();
  List<_ChatMessage> _messages = const [_welcomeMessage];
  String? _babyId;
  String? _conversationId;
  String? _status;
  bool _loading = true;
  bool _sending = false;

  static const _welcomeMessage = _ChatMessage.assistant(
    '你好，我是 Manbo 育儿助理。问我今天的喂养、睡眠或尿布记录；我会结合真实数据和这段对话继续回答。',
  );

  @override
  void initState() {
    super.initState();
    _loadConversation();
  }

  @override
  void dispose() {
    _input.dispose();
    _scroll.dispose();
    super.dispose();
  }

  Future<void> _loadConversation() async {
    final api = widget.api;
    if (api == null) {
      setState(() {
        _loading = false;
        _status = '当前是本地演示模式，请启动 Java 服务后使用 AI 助理。';
      });
      return;
    }
    try {
      final babies = await api.fetchBabies();
      if (babies.isEmpty) {
        if (mounted) {
          setState(() {
            _loading = false;
            _status = '请先创建宝宝档案。';
          });
        }
        return;
      }
      final babyId = babies.first.id;
      final conversation = await _loadOrCreateLatest(api, babyId);
      final history = await api.fetchAiMessages(
        conversationId: conversation.id,
      );
      if (!mounted) return;
      setState(() {
        _babyId = babyId;
        _conversationId = conversation.id;
        _messages = history.isEmpty
            ? const [_welcomeMessage]
            : history.map(_ChatMessage.fromHistory).toList();
        _status = null;
        _loading = false;
      });
      _scrollToEnd();
    } catch (_) {
      if (mounted) {
        setState(() {
          _loading = false;
          _status = '暂时无法读取 AI 会话，请确认 Java 服务正在运行。';
        });
      }
    }
  }

  Future<AiConversation> _loadOrCreateLatest(
    BabyAssistantApi api,
    String babyId,
  ) async {
    try {
      return await api.fetchLatestAiConversation(babyId: babyId);
    } on BabyAssistantApiException catch (error) {
      if (error.statusCode != 404) rethrow;
      return api.createAiConversation(babyId: babyId);
    }
  }

  Future<void> _startNewConversation() async {
    final api = widget.api;
    final babyId = _babyId;
    if (api == null || babyId == null || _sending) return;
    setState(() => _sending = true);
    try {
      final conversation = await api.createAiConversation(babyId: babyId);
      if (!mounted) return;
      setState(() {
        _conversationId = conversation.id;
        _messages = const [_welcomeMessage];
        _status = null;
      });
      _scrollToEnd();
    } catch (_) {
      if (mounted) setState(() => _status = '新聊天创建失败，请稍后重试。');
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  Future<void> _send([String? quickQuestion]) async {
    final question = (quickQuestion ?? _input.text).trim();
    final api = widget.api;
    final babyId = _babyId;
    final conversationId = _conversationId;
    if (question.isEmpty || _sending) return;
    if (api == null || babyId == null || conversationId == null) {
      setState(() => _status = '请先连接服务并读取宝宝档案。');
      return;
    }

    _input.clear();
    setState(() {
      _messages = [..._messages, _ChatMessage.user(question)];
      _sending = true;
    });
    _scrollToEnd();
    try {
      final reply = await api.askAi(
        babyId: babyId,
        conversationId: conversationId,
        message: question,
      );
      if (mounted) {
        setState(() {
          _conversationId = reply.conversationId;
          _messages = [
            ..._messages,
            _ChatMessage.assistant(reply.reply, reply),
          ];
        });
      }
    } on BabyAssistantApiException {
      if (mounted) {
        setState(
          () => _messages = [
            ..._messages,
            const _ChatMessage.assistant('这次没有拿到回复。请确认 Java 服务正在运行，然后再试一次。'),
          ],
        );
      }
    } catch (_) {
      if (mounted) {
        setState(
          () => _messages = [
            ..._messages,
            const _ChatMessage.assistant('网络连接暂时不可用，请稍后重试。'),
          ],
        );
      }
    } finally {
      if (mounted) setState(() => _sending = false);
      _scrollToEnd();
    }
  }

  void _scrollToEnd() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scroll.hasClients) {
        _scroll.animateTo(
          _scroll.position.maxScrollExtent,
          duration: const Duration(milliseconds: 260),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final ready = !_loading && _conversationId != null;
    return SafeArea(
      bottom: false,
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(20, 18, 14, 12),
            child: Row(
              children: [
                IconButton(
                  key: const Key('ai-back-button'),
                  tooltip: '返回首页',
                  onPressed:
                      widget.onBack ?? () => Navigator.of(context).maybePop(),
                  icon: const Icon(Icons.arrow_back_rounded),
                ),
                const SizedBox(width: 2),
                Container(
                  width: 42,
                  height: 42,
                  decoration: const BoxDecoration(
                    color: AppColors.ink,
                    shape: BoxShape.circle,
                  ),
                  child: const Icon(
                    Icons.auto_awesome_rounded,
                    color: Colors.white,
                  ),
                ),
                const SizedBox(width: 12),
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        'MANBO AI',
                        style: TextStyle(
                          fontSize: 19,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                      Text(
                        '记得今天的数据，也记得这段对话',
                        style: TextStyle(fontSize: 11, color: AppColors.muted),
                      ),
                    ],
                  ),
                ),
                IconButton(
                  key: const Key('new-ai-conversation'),
                  tooltip: '新聊天',
                  onPressed: ready && !_sending ? _startNewConversation : null,
                  icon: const Icon(Icons.edit_note_rounded),
                ),
                if (_loading)
                  const SizedBox(
                    width: 18,
                    height: 18,
                    child: CircularProgressIndicator(strokeWidth: 2),
                  ),
              ],
            ),
          ),
          if (_status != null) _Notice(message: _status!),
          Expanded(
            child: ListView(
              controller: _scroll,
              padding: const EdgeInsets.fromLTRB(18, 10, 18, 108),
              children: [
                const _ContextCard(),
                const SizedBox(height: 15),
                for (final message in _messages) ...[
                  _MessageBubble(message: message, onAction: _send),
                  const SizedBox(height: 12),
                ],
                if (_sending)
                  const Padding(
                    padding: EdgeInsets.only(left: 6),
                    child: Text(
                      'Manbo 正在整理今天的记录与聊天上下文…',
                      style: TextStyle(color: AppColors.muted, fontSize: 12),
                    ),
                  ),
                const SizedBox(height: 15),
                const Text(
                  '试着问问',
                  style: TextStyle(fontSize: 12, color: AppColors.muted),
                ),
                const SizedBox(height: 8),
                Wrap(
                  spacing: 8,
                  runSpacing: 8,
                  children: [
                    _QuestionChip('今天睡得怎么样？', () => _send('今天睡得怎么样？')),
                    _QuestionChip('那该怎么办？', () => _send('那该怎么办？')),
                    _QuestionChip('喂养节奏正常吗？', () => _send('喂养节奏正常吗？')),
                  ],
                ),
              ],
            ),
          ),
          _Composer(controller: _input, enabled: ready, onSend: _send),
        ],
      ),
    );
  }
}

class _ContextCard extends StatelessWidget {
  const _ContextCard();

  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(15),
    decoration: BoxDecoration(
      color: AppColors.blue,
      borderRadius: BorderRadius.circular(23),
    ),
    child: const Row(
      children: [
        Icon(Icons.history_rounded, size: 18),
        SizedBox(width: 10),
        Expanded(
          child: Text(
            'Java 会保存会话并提供最近聊天记录；Python 只接收必要上下文。',
            style: TextStyle(fontSize: 12, height: 1.35),
          ),
        ),
      ],
    ),
  );
}

class _Notice extends StatelessWidget {
  const _Notice({required this.message});
  final String message;

  @override
  Widget build(BuildContext context) => Padding(
    padding: const EdgeInsets.symmetric(horizontal: 18),
    child: Container(
      padding: const EdgeInsets.all(13),
      decoration: BoxDecoration(
        color: AppColors.peach.withValues(alpha: .62),
        borderRadius: BorderRadius.circular(17),
      ),
      child: Text(message, style: const TextStyle(fontSize: 12)),
    ),
  );
}

class _MessageBubble extends StatelessWidget {
  const _MessageBubble({required this.message, required this.onAction});
  final _ChatMessage message;
  final ValueChanged<String> onAction;

  @override
  Widget build(BuildContext context) {
    final isUser = message.isUser;
    final source = message.reply?.source ?? message.source;
    return Align(
      alignment: isUser ? Alignment.centerRight : Alignment.centerLeft,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 330),
        child: Container(
          padding: const EdgeInsets.all(14),
          decoration: BoxDecoration(
            color: isUser ? AppColors.ink : AppColors.paper,
            borderRadius: BorderRadius.circular(20),
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                message.text,
                style: TextStyle(
                  color: isUser ? Colors.white : AppColors.ink,
                  fontSize: 14,
                  height: 1.45,
                ),
              ),
              if (!isUser && source != null) ...[
                const SizedBox(height: 9),
                Text(
                  _sourceLabel(source),
                  style: const TextStyle(color: AppColors.muted, fontSize: 10),
                ),
              ],
              if (message.reply case final reply?) ...[
                const SizedBox(height: 10),
                Text(
                  reply.safetyNotice,
                  style: const TextStyle(
                    color: AppColors.muted,
                    fontSize: 10,
                    height: 1.3,
                  ),
                ),
                if (reply.references.isNotEmpty) ...[
                  const SizedBox(height: 9),
                  const Text(
                    '参考资料',
                    style: TextStyle(
                      color: AppColors.muted,
                      fontSize: 10,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                  const SizedBox(height: 3),
                  for (final reference in reply.references)
                    Text(
                      '${reference.title} · ${reference.sourceName}',
                      style: const TextStyle(
                        color: AppColors.muted,
                        fontSize: 10,
                        height: 1.3,
                      ),
                    ),
                ],
                if (reply.suggestedActions.isNotEmpty) ...[
                  const SizedBox(height: 9),
                  Wrap(
                    spacing: 6,
                    runSpacing: 6,
                    children: [
                      for (final action in reply.suggestedActions)
                        ActionChip(
                          label: Text(
                            action,
                            style: const TextStyle(fontSize: 10),
                          ),
                          onPressed: () => onAction(action),
                        ),
                    ],
                  ),
                ],
              ],
            ],
          ),
        ),
      ),
    );
  }

  static String _sourceLabel(String source) => switch (source) {
    'qwen' => '千问',
    'java-fallback' => '等待 Python 服务',
    _ => '本地 AI 规则建议',
  };
}

class _Composer extends StatelessWidget {
  const _Composer({
    required this.controller,
    required this.enabled,
    required this.onSend,
  });
  final TextEditingController controller;
  final bool enabled;
  final VoidCallback onSend;

  @override
  Widget build(BuildContext context) => SafeArea(
    top: false,
    child: Container(
      padding: const EdgeInsets.fromLTRB(18, 10, 18, 88),
      color: AppColors.canvas,
      child: Row(
        children: [
          Expanded(
            child: TextField(
              key: const Key('ai-question-input'),
              controller: controller,
              enabled: enabled,
              minLines: 1,
              maxLines: 3,
              textInputAction: TextInputAction.send,
              onSubmitted: (_) => onSend(),
              decoration: InputDecoration(
                hintText: enabled ? '问问 Manbo…' : '正在连接宝宝档案…',
                filled: true,
                fillColor: AppColors.paper,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(19),
                  borderSide: BorderSide.none,
                ),
              ),
            ),
          ),
          const SizedBox(width: 9),
          Material(
            color: enabled ? AppColors.ink : AppColors.muted,
            shape: const CircleBorder(),
            child: IconButton(
              key: const Key('ai-send-button'),
              onPressed: enabled ? onSend : null,
              icon: const Icon(Icons.arrow_upward_rounded, color: Colors.white),
            ),
          ),
        ],
      ),
    ),
  );
}

class _QuestionChip extends StatelessWidget {
  const _QuestionChip(this.label, this.onTap);
  final String label;
  final VoidCallback onTap;

  @override
  Widget build(BuildContext context) => ActionChip(
    label: Text(label, style: const TextStyle(fontSize: 11)),
    onPressed: onTap,
  );
}

class _ChatMessage {
  const _ChatMessage.user(this.text)
    : isUser = true,
      reply = null,
      source = null;
  const _ChatMessage.assistant(this.text, [this.reply])
    : isUser = false,
      source = null;
  const _ChatMessage.history(this.text, {required this.isUser, this.source})
    : reply = null;

  factory _ChatMessage.fromHistory(AiMessage message) => _ChatMessage.history(
    message.content,
    isUser: message.isUser,
    source: message.source,
  );

  final String text;
  final bool isUser;
  final AiChatReply? reply;
  final String? source;
}
