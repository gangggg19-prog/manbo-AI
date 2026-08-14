import 'dart:convert';

import 'package:http/http.dart' as http;

import '../models/auth_session.dart';
import '../models/baby_profile.dart';
import '../models/ai_conversation.dart';
import '../models/ai_chat_reply.dart';
import '../models/care_record.dart';
import '../models/daily_summary.dart';
import '../models/growth_timeline.dart';
import '../models/family_chat.dart';
import '../models/family_invite.dart';
import '../models/knowledge_article.dart';
import '../models/sleep_session.dart';

class BabyAssistantApi {
  BabyAssistantApi({http.Client? client, String? baseUrl})
    : _client = client ?? http.Client(),
      _baseUrl =
          baseUrl ??
          const String.fromEnvironment(
            'BABY_API_BASE_URL',
            defaultValue: 'http://10.0.2.2:8080/api/v1',
          );

  final http.Client _client;
  final String _baseUrl;
  String? _accessToken;

  void setAccessToken(String? accessToken) {
    _accessToken = accessToken;
  }

  /// Token is used only for the native family-chat WebSocket handshake.
  String? get accessToken => _accessToken;

  Uri familyChatSocketUri({required String roomId}) {
    final apiUri = Uri.parse(_baseUrl);
    return apiUri.replace(
      scheme: apiUri.scheme == 'https' ? 'wss' : 'ws',
      path: '/ws/family-chat/$roomId',
      query: null,
    );
  }

  Future<AuthSession> register({
    required String username,
    required String displayName,
    required String password,
  }) async {
    final response = await _client
        .post(
          _uri('/auth/register'),
          headers: const {'Content-Type': 'application/json'},
          body: jsonEncode({
            'username': username,
            'displayName': displayName,
            'password': password,
          }),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response, expectedStatus: 201);
    return AuthSession.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<AuthSession> login({
    required String username,
    required String password,
  }) async {
    final response = await _client
        .post(
          _uri('/auth/login'),
          headers: const {'Content-Type': 'application/json'},
          body: jsonEncode({'username': username, 'password': password}),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    return AuthSession.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<AuthUser> fetchCurrentUser() async {
    final response = await _client
        .get(_uri('/auth/me'), headers: _authenticatedHeaders())
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    return AuthUser.fromJson(jsonDecode(response.body) as Map<String, dynamic>);
  }

  Future<List<BabyProfile>> fetchBabies() async {
    final response = await _client
        .get(_uri('/babies'))
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    final payload = jsonDecode(response.body) as List<dynamic>;
    return payload
        .cast<Map<String, dynamic>>()
        .map(BabyProfile.fromApiJson)
        .toList();
  }

  Future<List<CareRecord>> fetchCareRecords({
    required String babyId,
    required DateTime date,
  }) async {
    final response = await _client
        .get(
          _uri(
            '/care-records',
            queryParameters: {'babyId': babyId, 'date': _dateParameter(date)},
          ),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    final payload = jsonDecode(response.body) as List<dynamic>;
    return payload
        .cast<Map<String, dynamic>>()
        .map(CareRecord.fromApiJson)
        .toList();
  }

  Future<CareRecord> createCareRecord({
    required String babyId,
    required CareRecord record,
  }) async {
    final response = await _client
        .post(
          _uri('/care-records'),
          headers: const {'Content-Type': 'application/json'},
          body: jsonEncode(record.toCreateRequestJson(babyId)),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response, expectedStatus: 201);
    return CareRecord.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<CareRecord> updateCareRecord({
    required String babyId,
    required CareRecord record,
  }) async {
    final response = await _client
        .put(
          _uri('/care-records/${record.id}'),
          headers: const {'Content-Type': 'application/json'},
          body: jsonEncode(record.toCreateRequestJson(babyId)),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    return CareRecord.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<GrowthTimeline> fetchGrowthTimeline({
    required String babyId,
    int days = 7,
  }) async {
    final response = await _client
        .get(
          _uri(
            '/growth-timeline',
            queryParameters: {'babyId': babyId, 'days': days.toString()},
          ),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    return GrowthTimeline.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<DailySummary> fetchDailySummary({
    required String babyId,
    required DateTime date,
  }) async {
    final response = await _client
        .get(
          _uri(
            '/daily-summary',
            queryParameters: {'babyId': babyId, 'date': _dateParameter(date)},
          ),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    return DailySummary.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<List<SleepSession>> fetchSleepSessions({
    required String babyId,
    required DateTime date,
  }) async {
    final response = await _client
        .get(
          _uri(
            '/sleep-sessions',
            queryParameters: {'babyId': babyId, 'date': _dateParameter(date)},
          ),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    final payload = jsonDecode(response.body) as List<dynamic>;
    return payload
        .cast<Map<String, dynamic>>()
        .map(SleepSession.fromApiJson)
        .toList();
  }

  Future<SleepSession> startSleepSession({
    required String babyId,
    required DateTime startedAt,
  }) async {
    final response = await _client
        .post(
          _uri('/sleep-sessions'),
          headers: const {'Content-Type': 'application/json'},
          body: jsonEncode({
            'babyId': babyId,
            'startedAt': startedAt.toUtc().toIso8601String(),
          }),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response, expectedStatus: 201);
    return SleepSession.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<SleepSession> endSleepSession({
    required String sessionId,
    required DateTime endedAt,
  }) async {
    final response = await _client
        .patch(
          _uri('/sleep-sessions/$sessionId/end'),
          headers: const {'Content-Type': 'application/json'},
          body: jsonEncode({'endedAt': endedAt.toUtc().toIso8601String()}),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    return SleepSession.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<void> deleteCareRecord(String recordId) async {
    final response = await _client
        .delete(_uri('/care-records/$recordId'))
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response, expectedStatus: 204);
  }

  Future<AiConversation> createAiConversation({required String babyId}) async {
    final response = await _client
        .post(
          _uri('/ai/conversations'),
          headers: const {'Content-Type': 'application/json'},
          body: jsonEncode({'babyId': babyId}),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response, expectedStatus: 200);
    return AiConversation.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<AiConversation> fetchLatestAiConversation({
    required String babyId,
  }) async {
    final response = await _client
        .get(
          _uri('/ai/conversations/latest', queryParameters: {'babyId': babyId}),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    return AiConversation.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<List<AiMessage>> fetchAiMessages({
    required String conversationId,
  }) async {
    final response = await _client
        .get(_uri('/ai/conversations/$conversationId/messages'))
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    final payload = jsonDecode(response.body) as List<dynamic>;
    return payload
        .cast<Map<String, dynamic>>()
        .map(AiMessage.fromApiJson)
        .toList();
  }

  /// Java adds baby data and persisted conversation history before calling Python.
  Future<AiChatReply> askAi({
    required String babyId,
    required String conversationId,
    required String message,
  }) async {
    final response = await _client
        .post(
          _uri('/ai/chat'),
          headers: const {'Content-Type': 'application/json'},
          body: jsonEncode({
            'babyId': babyId,
            'conversationId': conversationId,
            'message': message,
          }),
        )
        .timeout(const Duration(seconds: 25));
    _requireSuccess(response);
    return AiChatReply.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  /// Fetches curated articles for the resource centre and AI retrieval view.
  Future<List<KnowledgeArticle>> fetchKnowledgeArticles({
    int ageMonths = 6,
    String? category,
  }) async {
    final query = <String, String>{'ageMonths': ageMonths.toString()};
    if (category != null && category.isNotEmpty) query['category'] = category;
    final response = await _client
        .get(_uri('/knowledge-articles', queryParameters: query))
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    final payload = jsonDecode(response.body) as List<dynamic>;
    return payload
        .cast<Map<String, dynamic>>()
        .map(KnowledgeArticle.fromApiJson)
        .toList();
  }

  Future<FamilyInvite> generateFamilyInvite({required String babyId}) async {
    final response = await _client
        .post(
          _uri('/family-invites'),
          headers: _authenticatedHeaders(json: true),
          body: jsonEncode({'babyId': babyId}),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response, expectedStatus: 201);
    return FamilyInvite.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<FamilyMembership> acceptFamilyInvite({
    required String inviteCode,
  }) async {
    final response = await _client
        .post(
          _uri('/family-invites/accept'),
          headers: _authenticatedHeaders(json: true),
          body: jsonEncode({'inviteCode': inviteCode.trim().toUpperCase()}),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    return FamilyMembership.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<FamilyChatRoom> createOrGetFamilyChatRoom({
    required String babyId,
  }) async {
    final response = await _client
        .post(
          _uri('/family-chat/rooms'),
          headers: _authenticatedHeaders(json: true),
          body: jsonEncode({'babyId': babyId}),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    return FamilyChatRoom.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Future<List<FamilyChatMessage>> fetchFamilyChatMessages({
    required String roomId,
  }) async {
    final response = await _client
        .get(
          _uri('/family-chat/rooms/$roomId/messages'),
          headers: _authenticatedHeaders(),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    final payload = jsonDecode(response.body) as List<dynamic>;
    return payload
        .cast<Map<String, dynamic>>()
        .map(FamilyChatMessage.fromApiJson)
        .toList();
  }

  Future<FamilyChatMessage> sendFamilyChatMessage({
    required String roomId,
    required String content,
  }) async {
    final response = await _client
        .post(
          _uri('/family-chat/rooms/$roomId/messages'),
          headers: _authenticatedHeaders(json: true),
          body: jsonEncode({'content': content}),
        )
        .timeout(const Duration(seconds: 6));
    _requireSuccess(response);
    return FamilyChatMessage.fromApiJson(
      jsonDecode(response.body) as Map<String, dynamic>,
    );
  }

  Map<String, String> _authenticatedHeaders({bool json = false}) {
    final token = _accessToken;
    if (token == null || token.isEmpty) {
      throw const BabyAssistantApiException(401, 'Sign in is required');
    }
    return {
      if (json) 'Content-Type': 'application/json',
      'Authorization': 'Bearer $token',
    };
  }

  Uri _uri(String path, {Map<String, String>? queryParameters}) {
    return Uri.parse(
      '$_baseUrl$path',
    ).replace(queryParameters: queryParameters);
  }

  String _dateParameter(DateTime date) {
    return '${date.year.toString().padLeft(4, '0')}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')}';
  }

  void _requireSuccess(http.Response response, {int? expectedStatus}) {
    final status = response.statusCode;
    if (expectedStatus != null
        ? status != expectedStatus
        : status < 200 || status >= 300) {
      throw BabyAssistantApiException(status, response.body);
    }
  }
}

class BabyAssistantApiException implements Exception {
  const BabyAssistantApiException(this.statusCode, this.body);

  final int statusCode;
  final String body;

  @override
  String toString() => 'BabyAssistantApiException(statusCode: $statusCode)';
}
