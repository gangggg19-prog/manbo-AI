import 'dart:async';
import 'dart:convert';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:web_socket_channel/io.dart';

import 'app_theme.dart';
import 'data/auth_session_store.dart';
import 'data/baby_assistant_api.dart';
import 'models/auth_session.dart';
import 'models/family_chat.dart';
import 'models/family_invite.dart';

class FamilyChatPage extends StatefulWidget {
  const FamilyChatPage({
    super.key,
    required this.api,
    this.onBack,
    this.enableRealtime = true,
  });

  final BabyAssistantApi? api;
  final VoidCallback? onBack;
  final bool enableRealtime;

  @override
  State<FamilyChatPage> createState() => _FamilyChatPageState();
}

class _FamilyChatPageState extends State<FamilyChatPage> {
  final _input = TextEditingController();
  final _username = TextEditingController();
  final _displayName = TextEditingController();
  final _password = TextEditingController();
  final _inviteCode = TextEditingController();
  final _scroll = ScrollController();
  final _sessionStore = AuthSessionStore();
  IOWebSocketChannel? _chatSocket;
  StreamSubscription<dynamic>? _chatSocketSubscription;

  List<FamilyChatMessage> _messages = const [];
  AuthSession? _session;
  String? _babyId;
  String? _roomId;
  String? _roomRole;
  String? _status;
  String? _authError;
  String? _inviteError;
  bool _loading = true;
  bool _sending = false;
  bool _authBusy = false;
  bool _registerMode = false;
  bool _needsInvite = false;
  bool _joiningInvite = false;
  bool _generatingInvite = false;
  bool _realtimeConnected = false;

  @override
  void initState() {
    super.initState();
    _restoreSession();
  }

  @override
  void dispose() {
    _input.dispose();
    _username.dispose();
    _displayName.dispose();
    _password.dispose();
    _inviteCode.dispose();
    _scroll.dispose();
    super.dispose();
  }

  Future<void> _restoreSession() async {
    final api = widget.api;
    if (api == null) {
      if (mounted) {
        setState(() {
          _loading = false;
          _status = '请先启动 Java 服务。';
        });
      }
      return;
    }

    final saved = await _sessionStore.load();
    if (saved == null) {
      if (mounted) setState(() => _loading = false);
      return;
    }

    api.setAccessToken(saved.accessToken);
    try {
      final currentUser = await api.fetchCurrentUser();
      final session = AuthSession(
        accessToken: saved.accessToken,
        expiresAt: saved.expiresAt,
        user: currentUser,
      );
      await _sessionStore.save(session);
      if (!mounted) return;
      setState(() => _session = session);
      await _loadChat();
    } on BabyAssistantApiException catch (error) {
      if (error.statusCode == 401) {
        await _signOut(message: '登录已过期，请重新登录。');
      } else if (mounted) {
        setState(() {
          _loading = false;
          _status = '暂时连不上 Java 服务，请稍后重试。';
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() {
          _loading = false;
          _status = '暂时连不上 Java 服务，请稍后重试。';
        });
      }
    }
  }

  Future<void> _submitAuth() async {
    final api = widget.api;
    final username = _username.text.trim();
    final displayName = _displayName.text.trim();
    final password = _password.text;

    if (api == null) {
      setState(() => _authError = '请先启动 Java 服务。');
      return;
    }
    if (username.length < 3) {
      setState(() => _authError = '账号至少输入 3 个字符。');
      return;
    }
    if (_registerMode && displayName.isEmpty) {
      setState(() => _authError = '请输入家人昵称。');
      return;
    }
    if (password.length < 6) {
      setState(() => _authError = '密码至少输入 6 位。');
      return;
    }

    setState(() {
      _authBusy = true;
      _authError = null;
    });

    try {
      final session = _registerMode
          ? await api.register(
              username: username,
              displayName: displayName,
              password: password,
            )
          : await api.login(username: username, password: password);
      api.setAccessToken(session.accessToken);
      await _sessionStore.save(session);
      if (!mounted) return;
      _password.clear();
      setState(() {
        _session = session;
        _loading = true;
        _authBusy = false;
        _status = null;
      });
      await _loadChat();
    } on BabyAssistantApiException catch (error) {
      if (!mounted) return;
      setState(() {
        _authBusy = false;
        _authError = switch (error.statusCode) {
          409 => '这个账号已经注册，请直接登录。',
          401 => '账号或密码不正确。',
          _ => '操作失败，请确认 Java 服务已经启动。',
        };
      });
    } catch (_) {
      if (mounted) {
        setState(() {
          _authBusy = false;
          _authError = '连接失败，请确认 Java 服务已经启动。';
        });
      }
    }
  }

  Future<void> _loadChat() async {
    final api = widget.api;
    if (api == null || _session == null) return;

    if (mounted) {
      setState(() {
        _loading = true;
        _status = null;
      });
    }

    String? attemptedBabyId;
    try {
      final babies = await api.fetchBabies();
      if (babies.isEmpty) {
        throw const BabyAssistantApiException(404, 'No baby');
      }
      attemptedBabyId = babies.first.id;
      final room = await api.createOrGetFamilyChatRoom(babyId: attemptedBabyId);
      final messages = await api.fetchFamilyChatMessages(roomId: room.id);
      if (!mounted) return;
      setState(() {
        _babyId = room.babyId;
        _roomId = room.id;
        _roomRole = room.currentUserRole;
        _messages = messages;
        _loading = false;
        _needsInvite = false;
        _inviteError = null;
        _status = null;
      });
      _toEnd();
      unawaited(_connectRealtime(room.id));
    } on BabyAssistantApiException catch (error) {
      if (error.statusCode == 401) {
        await _signOut(message: '登录已过期，请重新登录。');
      } else if (mounted) {
        setState(() {
          _loading = false;
          _babyId = attemptedBabyId ?? _babyId;
          _roomId = null;
          _roomRole = null;
          _needsInvite = error.statusCode == 403;
          _status = error.statusCode == 403
              ? '当前账号还不是这个宝宝的家庭成员。'
              : '暂时连不上私密小圈子，请确认 Java 服务已启动。';
        });
      }
    } catch (_) {
      if (mounted) {
        setState(() {
          _loading = false;
          _status = '暂时连不上私密小圈子，请确认 Java 服务已启动。';
        });
      }
    }
  }

  Future<void> _generateInvite() async {
    final api = widget.api;
    final babyId = _babyId;
    if (api == null ||
        babyId == null ||
        _generatingInvite ||
        _roomRole != 'OWNER') {
      return;
    }

    setState(() => _generatingInvite = true);
    try {
      final invite = await api.generateFamilyInvite(babyId: babyId);
      if (!mounted) return;
      setState(() => _generatingInvite = false);
      await _showInviteDialog(invite);
    } on BabyAssistantApiException catch (error) {
      if (error.statusCode == 401) {
        await _signOut(message: '登录已过期，请重新登录。');
      } else if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text(
              error.statusCode == 403 ? '只有家庭管理员可以生成邀请码。' : '邀请码生成失败，请稍后重试。',
            ),
          ),
        );
      }
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('邀请码生成失败，请稍后重试。')));
      }
    } finally {
      if (mounted) setState(() => _generatingInvite = false);
    }
  }

  Future<void> _showInviteDialog(FamilyInvite invite) async {
    final expiry = invite.expiresAt.toString().substring(0, 16);
    await showDialog<void>(
      context: context,
      builder: (dialogContext) => AlertDialog(
        title: const Text('邀请家人'),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const Text(
              '把下面的邀请码发给家人。邀请码 24 小时内有效，只能使用一次。',
              style: TextStyle(color: AppColors.muted, fontSize: 12),
            ),
            const SizedBox(height: 18),
            Container(
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 18),
              decoration: BoxDecoration(
                color: AppColors.blue,
                borderRadius: BorderRadius.circular(18),
              ),
              child: SelectableText(
                invite.inviteCode,
                key: const Key('family-invite-generated-code'),
                textAlign: TextAlign.center,
                style: const TextStyle(
                  fontSize: 28,
                  fontWeight: FontWeight.w900,
                  letterSpacing: 4,
                ),
              ),
            ),
            const SizedBox(height: 10),
            Text(
              '有效期至 $expiry',
              textAlign: TextAlign.center,
              style: const TextStyle(color: AppColors.muted, fontSize: 11),
            ),
          ],
        ),
        actions: [
          TextButton(
            key: const Key('family-invite-copy-button'),
            onPressed: () async {
              await Clipboard.setData(ClipboardData(text: invite.inviteCode));
              if (!dialogContext.mounted) return;
              Navigator.of(dialogContext).pop();
              if (!mounted) return;
              ScaffoldMessenger.of(
                context,
              ).showSnackBar(const SnackBar(content: Text('邀请码已复制。')));
            },
            child: const Text('复制邀请码'),
          ),
          FilledButton(
            onPressed: () => Navigator.of(dialogContext).pop(),
            child: const Text('完成'),
          ),
        ],
      ),
    );
  }

  Future<void> _acceptInvite() async {
    final api = widget.api;
    final code = _inviteCode.text.trim().toUpperCase();
    if (api == null || _joiningInvite) return;
    if (!RegExp(r'^[A-Z0-9]{8}$').hasMatch(code)) {
      setState(() => _inviteError = '请输入完整的 8 位邀请码。');
      return;
    }

    setState(() {
      _joiningInvite = true;
      _inviteError = null;
    });
    try {
      await api.acceptFamilyInvite(inviteCode: code);
      if (!mounted) return;
      _inviteCode.clear();
      setState(() {
        _joiningInvite = false;
        _needsInvite = false;
        _status = null;
      });
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('已加入宝宝的家庭空间。')));
      await _loadChat();
    } on BabyAssistantApiException catch (error) {
      if (error.statusCode == 401) {
        await _signOut(message: '登录已过期，请重新登录。');
      } else if (mounted) {
        setState(() {
          _inviteError = switch (error.statusCode) {
            400 || 404 => '没有找到这个邀请码，请检查后重试。',
            409 => '邀请码已使用，或当前账号已经加入。',
            410 => '邀请码已过期，请让管理员重新生成。',
            _ => '加入失败，请稍后重试。',
          };
        });
      }
    } catch (_) {
      if (mounted) setState(() => _inviteError = '加入失败，请稍后重试。');
    } finally {
      if (mounted) setState(() => _joiningInvite = false);
    }
  }

  Future<void> _send() async {
    final api = widget.api;
    final roomId = _roomId;
    final content = _input.text.trim();
    if (api == null || roomId == null || content.isEmpty || _sending) return;

    setState(() => _sending = true);
    try {
      final message = await api.sendFamilyChatMessage(
        roomId: roomId,
        content: content,
      );
      if (!mounted) return;
      _input.clear();
      setState(() => _messages = [..._messages, message]);
      _toEnd();
    } on BabyAssistantApiException catch (error) {
      if (error.statusCode == 401) {
        await _signOut(message: '登录已过期，请重新登录。');
      } else if (mounted) {
        _showSendFailure();
      }
    } catch (_) {
      if (mounted) _showSendFailure();
    } finally {
      if (mounted) setState(() => _sending = false);
    }
  }

  Future<void> _connectRealtime(String roomId) async {
    if (!widget.enableRealtime) return;
    final api = widget.api;
    final token = api?.accessToken;
    if (api == null || token == null || token.isEmpty) return;

    await _disconnectRealtime();
    final channel = IOWebSocketChannel.connect(
      api.familyChatSocketUri(roomId: roomId),
      headers: {'Authorization': 'Bearer $token'},
    );
    _chatSocket = channel;
    _chatSocketSubscription = channel.stream.listen(
      _handleRealtimeEvent,
      onError: (Object error, StackTrace stackTrace) {
        _markRealtimeDisconnected(channel);
      },
      onDone: () => _markRealtimeDisconnected(channel),
      cancelOnError: false,
    );
    if (mounted && identical(_chatSocket, channel)) {
      setState(() => _realtimeConnected = true);
    }
  }

  void _handleRealtimeEvent(dynamic event) {
    if (event is! String) return;
    try {
      final payload = jsonDecode(event) as Map<String, dynamic>;
      if (payload['type'] == 'error') {
        if (mounted) _showSendFailure();
        return;
      }
      _appendMessageIfMissing(FamilyChatMessage.fromApiJson(payload));
    } catch (_) {
      // Ignore malformed events. The persisted HTTP history remains available.
    }
  }

  void _appendMessageIfMissing(FamilyChatMessage message) {
    if (!mounted || _messages.any((item) => item.id == message.id)) return;
    setState(() => _messages = [..._messages, message]);
    _toEnd();
  }

  void _markRealtimeDisconnected(IOWebSocketChannel channel) {
    if (mounted && identical(_chatSocket, channel)) {
      setState(() => _realtimeConnected = false);
    }
  }

  Future<void> _disconnectRealtime() async {
    final subscription = _chatSocketSubscription;
    final socket = _chatSocket;
    _chatSocketSubscription = null;
    _chatSocket = null;
    if (mounted && _realtimeConnected) {
      setState(() => _realtimeConnected = false);
    }
    await subscription?.cancel();
    await socket?.sink.close();
  }

  void _showSendFailure() {
    ScaffoldMessenger.of(
      context,
    ).showSnackBar(const SnackBar(content: Text('发送失败，请稍后重试。')));
  }

  Future<void> _signOut({String? message}) async {
    await _disconnectRealtime();
    widget.api?.setAccessToken(null);
    await _sessionStore.clear();
    if (!mounted) return;
    setState(() {
      _session = null;
      _babyId = null;
      _roomId = null;
      _roomRole = null;
      _messages = const [];
      _needsInvite = false;
      _inviteError = null;
      _loading = false;
      _sending = false;
      _status = message;
    });
  }

  void _toEnd() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (_scroll.hasClients) {
        _scroll.animateTo(
          _scroll.position.maxScrollExtent,
          duration: const Duration(milliseconds: 220),
          curve: Curves.easeOut,
        );
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    final session = _session;
    final enabled = !_loading && _roomId != null && !_sending;

    return SafeArea(
      bottom: false,
      child: Column(
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(12, 12, 12, 10),
            child: Row(
              children: [
                IconButton(
                  key: const Key('family-chat-back-button'),
                  tooltip: '返回首页',
                  onPressed:
                      widget.onBack ?? () => Navigator.of(context).maybePop(),
                  icon: const Icon(Icons.arrow_back_rounded),
                ),
                const CircleAvatar(
                  backgroundColor: AppColors.peach,
                  child: Icon(Icons.forum_rounded, color: AppColors.ink),
                ),
                const SizedBox(width: 10),
                const Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        '家人小圈子',
                        style: TextStyle(
                          fontSize: 19,
                          fontWeight: FontWeight.w900,
                        ),
                      ),
                      Text(
                        '围绕宝宝的私密共享空间',
                        style: TextStyle(fontSize: 11, color: AppColors.muted),
                      ),
                    ],
                  ),
                ),
                if (session != null && _roomRole == 'OWNER')
                  IconButton(
                    key: const Key('family-invite-button'),
                    tooltip: '邀请家人',
                    onPressed: _generatingInvite ? null : _generateInvite,
                    icon: _generatingInvite
                        ? const SizedBox.square(
                            dimension: 20,
                            child: CircularProgressIndicator(strokeWidth: 2),
                          )
                        : const Icon(Icons.person_add_alt_1_rounded),
                  ),
                if (session != null)
                  IconButton(
                    tooltip: '退出登录',
                    onPressed: _sending ? null : _signOut,
                    icon: const Icon(Icons.logout_rounded),
                  ),
              ],
            ),
          ),
          const _PrivacyCard(),
          if (session != null && _roomId != null && !_loading)
            Padding(
              padding: const EdgeInsets.fromLTRB(18, 7, 18, 0),
              child: Align(
                alignment: Alignment.centerLeft,
                child: Text(
                  _realtimeConnected ? '● 实时同步已连接' : '○ 实时同步暂未连接，历史消息仍可正常使用',
                  style: TextStyle(
                    fontSize: 10,
                    color: _realtimeConnected
                        ? Colors.green.shade700
                        : AppColors.muted,
                  ),
                ),
              ),
            ),
          if (session == null)
            Expanded(
              child: _AuthPanel(
                loading: _loading,
                registerMode: _registerMode,
                busy: _authBusy,
                status: _status,
                error: _authError,
                username: _username,
                displayName: _displayName,
                password: _password,
                onSubmit: _submitAuth,
                onToggleMode: () {
                  setState(() {
                    _registerMode = !_registerMode;
                    _authError = null;
                  });
                },
              ),
            )
          else if (_needsInvite)
            Expanded(
              child: _JoinFamilyPanel(
                controller: _inviteCode,
                joining: _joiningInvite,
                error: _inviteError,
                onJoin: _acceptInvite,
              ),
            )
          else ...[
            if (_status != null)
              Padding(
                padding: const EdgeInsets.all(18),
                child: Text(
                  _status!,
                  style: const TextStyle(color: AppColors.muted),
                ),
              ),
            Expanded(
              child: _loading
                  ? const Center(child: CircularProgressIndicator())
                  : ListView(
                      controller: _scroll,
                      padding: const EdgeInsets.fromLTRB(18, 16, 18, 12),
                      children: [
                        if (_messages.isEmpty) const _EmptyChat(),
                        for (final message in _messages)
                          _Bubble(
                            message: message,
                            currentUserId: session.user.id,
                          ),
                      ],
                    ),
            ),
            _Composer(controller: _input, enabled: enabled, onSend: _send),
          ],
        ],
      ),
    );
  }
}

class _JoinFamilyPanel extends StatelessWidget {
  const _JoinFamilyPanel({
    required this.controller,
    required this.joining,
    required this.error,
    required this.onJoin,
  });

  final TextEditingController controller;
  final bool joining;
  final String? error;
  final VoidCallback onJoin;

  @override
  Widget build(BuildContext context) => SingleChildScrollView(
    padding: const EdgeInsets.fromLTRB(18, 18, 18, 30),
    child: Container(
      padding: const EdgeInsets.all(22),
      decoration: BoxDecoration(
        color: AppColors.paper,
        borderRadius: BorderRadius.circular(24),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Icon(
            Icons.mark_email_unread_outlined,
            color: AppColors.coral,
            size: 34,
          ),
          const SizedBox(height: 14),
          const Text(
            '加入宝宝的家庭',
            textAlign: TextAlign.center,
            style: TextStyle(fontSize: 23, fontWeight: FontWeight.w900),
          ),
          const SizedBox(height: 7),
          const Text(
            '请让家庭管理员生成邀请码，然后在这里输入。成功后就能查看和发送私密消息。',
            textAlign: TextAlign.center,
            style: TextStyle(color: AppColors.muted, fontSize: 12),
          ),
          const SizedBox(height: 20),
          TextField(
            key: const Key('family-invite-code-input'),
            controller: controller,
            enabled: !joining,
            textCapitalization: TextCapitalization.characters,
            inputFormatters: [
              FilteringTextInputFormatter.allow(RegExp('[A-Za-z0-9]')),
              LengthLimitingTextInputFormatter(8),
            ],
            textAlign: TextAlign.center,
            style: const TextStyle(
              fontSize: 22,
              fontWeight: FontWeight.w800,
              letterSpacing: 3,
            ),
            onSubmitted: (_) => onJoin(),
            decoration: const InputDecoration(
              labelText: '8 位邀请码',
              hintText: '例如 ABCD2345',
            ),
          ),
          if (error != null) ...[
            const SizedBox(height: 12),
            Text(
              error!,
              textAlign: TextAlign.center,
              style: const TextStyle(color: AppColors.coral),
            ),
          ],
          const SizedBox(height: 18),
          FilledButton(
            key: const Key('family-invite-join-button'),
            onPressed: joining ? null : onJoin,
            style: FilledButton.styleFrom(
              backgroundColor: AppColors.ink,
              foregroundColor: Colors.white,
              minimumSize: const Size.fromHeight(50),
            ),
            child: Text(joining ? '正在加入…' : '加入家庭'),
          ),
        ],
      ),
    ),
  );
}

class _AuthPanel extends StatelessWidget {
  const _AuthPanel({
    required this.loading,
    required this.registerMode,
    required this.busy,
    required this.status,
    required this.error,
    required this.username,
    required this.displayName,
    required this.password,
    required this.onSubmit,
    required this.onToggleMode,
  });

  final bool loading;
  final bool registerMode;
  final bool busy;
  final String? status;
  final String? error;
  final TextEditingController username;
  final TextEditingController displayName;
  final TextEditingController password;
  final VoidCallback onSubmit;
  final VoidCallback onToggleMode;

  @override
  Widget build(BuildContext context) {
    if (loading) {
      return const Center(child: CircularProgressIndicator());
    }

    return SingleChildScrollView(
      padding: const EdgeInsets.fromLTRB(18, 18, 18, 30),
      child: Container(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: AppColors.paper,
          borderRadius: BorderRadius.circular(24),
        ),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Text(
              registerMode ? '创建家庭账号' : '登录家庭空间',
              style: const TextStyle(fontSize: 23, fontWeight: FontWeight.w900),
            ),
            const SizedBox(height: 6),
            Text(
              registerMode ? '第一个进入宝宝空间的账号会成为家庭管理员。' : '登录后才能查看和发送家人的私密消息。',
              style: const TextStyle(color: AppColors.muted, fontSize: 12),
            ),
            if (status != null) ...[
              const SizedBox(height: 12),
              Text(status!, style: const TextStyle(color: AppColors.coral)),
            ],
            const SizedBox(height: 18),
            TextField(
              key: const Key('family-auth-username'),
              controller: username,
              enabled: !busy,
              textInputAction: TextInputAction.next,
              decoration: const InputDecoration(
                labelText: '账号',
                hintText: '至少 3 个字符',
              ),
            ),
            if (registerMode) ...[
              const SizedBox(height: 12),
              TextField(
                key: const Key('family-auth-display-name'),
                controller: displayName,
                enabled: !busy,
                textInputAction: TextInputAction.next,
                decoration: const InputDecoration(
                  labelText: '家人昵称',
                  hintText: '例如：妈妈',
                ),
              ),
            ],
            const SizedBox(height: 12),
            TextField(
              key: const Key('family-auth-password'),
              controller: password,
              enabled: !busy,
              obscureText: true,
              onSubmitted: (_) => onSubmit(),
              decoration: const InputDecoration(
                labelText: '密码',
                hintText: '至少 6 位',
              ),
            ),
            if (error != null) ...[
              const SizedBox(height: 12),
              Text(error!, style: const TextStyle(color: AppColors.coral)),
            ],
            const SizedBox(height: 18),
            FilledButton(
              key: const Key('family-auth-submit'),
              onPressed: busy ? null : onSubmit,
              style: FilledButton.styleFrom(
                backgroundColor: AppColors.ink,
                foregroundColor: Colors.white,
                minimumSize: const Size.fromHeight(50),
              ),
              child: Text(busy ? '请稍候…' : (registerMode ? '注册并进入' : '登录')),
            ),
            TextButton(
              key: const Key('family-auth-toggle'),
              onPressed: busy ? null : onToggleMode,
              child: Text(registerMode ? '已有账号？直接登录' : '没有账号？先注册'),
            ),
          ],
        ),
      ),
    );
  }
}

class _PrivacyCard extends StatelessWidget {
  const _PrivacyCard();

  @override
  Widget build(BuildContext context) => Container(
    width: double.infinity,
    margin: const EdgeInsets.symmetric(horizontal: 18),
    padding: const EdgeInsets.all(12),
    decoration: BoxDecoration(
      color: AppColors.blue,
      borderRadius: BorderRadius.circular(16),
    ),
    child: const Row(
      children: [
        Icon(Icons.lock_outline_rounded, size: 16),
        SizedBox(width: 8),
        Expanded(
          child: Text(
            '只有家庭成员可以进入。消息保存在 Java 和 PostgreSQL 中。',
            style: TextStyle(fontSize: 12),
          ),
        ),
      ],
    ),
  );
}

class _EmptyChat extends StatelessWidget {
  const _EmptyChat();

  @override
  Widget build(BuildContext context) => Container(
    margin: const EdgeInsets.only(top: 50),
    padding: const EdgeInsets.all(24),
    decoration: BoxDecoration(
      color: AppColors.paper,
      borderRadius: BorderRadius.circular(24),
    ),
    child: const Column(
      children: [
        Icon(Icons.favorite_outline_rounded, color: AppColors.coral, size: 32),
        SizedBox(height: 12),
        Text('留下第一条关心', style: TextStyle(fontWeight: FontWeight.w800)),
        SizedBox(height: 5),
        Text(
          '例如：“刚刚喝了 120 ml 奶”',
          textAlign: TextAlign.center,
          style: TextStyle(fontSize: 12, color: AppColors.muted),
        ),
      ],
    ),
  );
}

class _Bubble extends StatelessWidget {
  const _Bubble({required this.message, required this.currentUserId});

  final FamilyChatMessage message;
  final String currentUserId;

  @override
  Widget build(BuildContext context) {
    final mine = message.senderUserId == currentUserId;
    final hour = message.sentAt.hour.toString().padLeft(2, '0');
    final minute = message.sentAt.minute.toString().padLeft(2, '0');
    final time = '$hour:$minute';

    return Align(
      alignment: mine ? Alignment.centerRight : Alignment.centerLeft,
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 300),
        child: Padding(
          padding: const EdgeInsets.only(bottom: 12),
          child: Column(
            crossAxisAlignment: mine
                ? CrossAxisAlignment.end
                : CrossAxisAlignment.start,
            children: [
              Text(
                mine ? '我' : message.senderName,
                style: const TextStyle(color: AppColors.muted, fontSize: 11),
              ),
              const SizedBox(height: 4),
              Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 14,
                  vertical: 11,
                ),
                decoration: BoxDecoration(
                  color: mine ? AppColors.ink : AppColors.paper,
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Text(
                  message.content,
                  style: TextStyle(color: mine ? Colors.white : AppColors.ink),
                ),
              ),
              const SizedBox(height: 4),
              Text(
                time,
                style: const TextStyle(color: AppColors.muted, fontSize: 10),
              ),
            ],
          ),
        ),
      ),
    );
  }
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
      color: AppColors.canvas,
      padding: const EdgeInsets.fromLTRB(18, 10, 18, 18),
      child: Row(
        children: [
          Expanded(
            child: TextField(
              key: const Key('family-chat-input'),
              controller: controller,
              enabled: enabled,
              minLines: 1,
              maxLines: 4,
              textInputAction: TextInputAction.send,
              onSubmitted: (_) => onSend(),
              decoration: InputDecoration(
                hintText: enabled ? '写下你的消息…' : '正在连接小圈子…',
                filled: true,
                fillColor: AppColors.paper,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(19),
                  borderSide: BorderSide.none,
                ),
              ),
            ),
          ),
          const SizedBox(width: 10),
          Material(
            color: enabled ? AppColors.ink : AppColors.muted,
            shape: const CircleBorder(),
            child: IconButton(
              key: const Key('family-chat-send-button'),
              onPressed: enabled ? onSend : null,
              icon: const Icon(Icons.arrow_upward_rounded, color: Colors.white),
            ),
          ),
        ],
      ),
    ),
  );
}
