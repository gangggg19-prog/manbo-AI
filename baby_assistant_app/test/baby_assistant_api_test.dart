import 'dart:convert';

import 'package:baby_assistant_app/data/baby_assistant_api.dart';
import 'package:baby_assistant_app/models/care_record.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';

void main() {
  const babyId = '618b988e-9588-4c64-9da8-b010c52e3f40';
  const recordId = '8a307dc0-2e09-4d12-8eee-0f9d9574ee25';

  test('loads baby and care records from the Java API', () async {
    final client = MockClient((request) async {
      if (request.url.path == '/api/v1/babies') {
        return http.Response.bytes(
          utf8.encode(
            '[{"id":"$babyId","displayName":"小满","birthDate":"2026-04-01"}]',
          ),
          200,
          headers: const {'content-type': 'application/json; charset=utf-8'},
        );
      }
      expect(request.url.path, '/api/v1/care-records');
      expect(request.url.queryParameters['babyId'], babyId);
      expect(request.url.queryParameters['date'], '2026-07-31');
      return http.Response(
        '[{"id":"$recordId","babyId":"$babyId","type":"FEEDING","recordedAt":"2026-07-31T03:30:00Z","amountMl":120,"createdAt":"2026-07-31T03:31:00Z"}]',
        200,
      );
    });
    final api = BabyAssistantApi(
      client: client,
      baseUrl: 'http://example.test/api/v1',
    );

    final babies = await api.fetchBabies();
    final records = await api.fetchCareRecords(
      babyId: babies.single.id,
      date: DateTime(2026, 7, 31),
    );

    expect(babies.single.displayName, '小满');
    expect(records.single.type, CareRecordType.feeding);
    expect(records.single.amountMl, 120);
  });

  test('sends a feeding record using the Java request format', () async {
    final client = MockClient((request) async {
      expect(request.method, 'POST');
      expect(request.url.path, '/api/v1/care-records');
      final body = jsonDecode(request.body) as Map<String, dynamic>;
      expect(body['babyId'], babyId);
      expect(body['type'], 'FEEDING');
      expect(body['amountMl'], 150);
      return http.Response(
        '{"id":"$recordId","babyId":"$babyId","type":"FEEDING","recordedAt":"2026-07-31T03:30:00Z","amountMl":150,"createdAt":"2026-07-31T03:31:00Z"}',
        201,
      );
    });
    final api = BabyAssistantApi(
      client: client,
      baseUrl: 'http://example.test/api/v1',
    );

    final saved = await api.createCareRecord(
      babyId: babyId,
      record: CareRecord(
        id: 'local-draft',
        type: CareRecordType.feeding,
        recordedAt: DateTime.utc(2026, 7, 31, 3, 30),
        amountMl: 150,
      ),
    );

    expect(saved.id, recordId);
    expect(saved.amountMl, 150);
  });

  test('starts and ends a sleep session using the Java API format', () async {
    const sleepId = 'b5d173c4-54d3-40a4-8d20-5370c0ae205a';
    final client = MockClient((request) async {
      if (request.method == 'POST') {
        expect(request.url.path, '/api/v1/sleep-sessions');
        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['babyId'], babyId);
        expect(body['startedAt'], '2026-07-31T03:30:00.000Z');
        return http.Response(
          '{"id":"$sleepId","babyId":"$babyId","startedAt":"2026-07-31T03:30:00Z","endedAt":null}',
          201,
        );
      }
      expect(request.method, 'PATCH');
      expect(request.url.path, '/api/v1/sleep-sessions/$sleepId/end');
      final body = jsonDecode(request.body) as Map<String, dynamic>;
      expect(body['endedAt'], '2026-07-31T04:15:00.000Z');
      return http.Response(
        '{"id":"$sleepId","babyId":"$babyId","startedAt":"2026-07-31T03:30:00Z","endedAt":"2026-07-31T04:15:00Z"}',
        200,
      );
    });
    final api = BabyAssistantApi(
      client: client,
      baseUrl: 'http://example.test/api/v1',
    );

    final active = await api.startSleepSession(
      babyId: babyId,
      startedAt: DateTime.utc(2026, 7, 31, 3, 30),
    );
    final ended = await api.endSleepSession(
      sessionId: active.id,
      endedAt: DateTime.utc(2026, 7, 31, 4, 15),
    );

    expect(active.isActive, isTrue);
    expect(ended.isActive, isFalse);
    expect(ended.endedAt!.toUtc(), DateTime.utc(2026, 7, 31, 4, 15));
  });
  test('loads the real daily briefing from the Java API', () async {
    final client = MockClient((request) async {
      expect(request.method, 'GET');
      expect(request.url.path, '/api/v1/daily-summary');
      expect(request.url.queryParameters['babyId'], babyId);
      expect(request.url.queryParameters['date'], '2026-07-31');
      return http.Response(
        '{"date":"2026-07-31","feedingMl":420,"diaperCount":5,"sleepMinutes":510,"feedingDeltaMl":60,"diaperDelta":1,"sleepDeltaMinutes":30,"sleepInProgress":false,"insight":"DAILY_RECORDS_READY"}',
        200,
      );
    });
    final api = BabyAssistantApi(
      client: client,
      baseUrl: 'http://example.test/api/v1',
    );

    final summary = await api.fetchDailySummary(
      babyId: babyId,
      date: DateTime(2026, 7, 31),
    );

    expect(summary.feedingMl, 420);
    expect(summary.sleepMinutes, 510);
    expect(summary.insight, 'DAILY_RECORDS_READY');
  });
  test('loads the seven-day growth timeline from the Java API', () async {
    final client = MockClient((request) async {
      expect(request.method, 'GET');
      expect(request.url.path, '/api/v1/growth-timeline');
      expect(request.url.queryParameters['babyId'], babyId);
      expect(request.url.queryParameters['days'], '7');
      return http.Response(
        '{"startDate":"2026-07-25","endDate":"2026-07-31","days":[{"date":"2026-07-31","feedingMl":278,"diaperCount":1,"sleepMinutes":52,"feedingDeltaMl":278,"diaperDelta":1,"sleepDeltaMinutes":52,"sleepInProgress":true,"insight":"SLEEP_IN_PROGRESS"}]}',
        200,
      );
    });
    final api = BabyAssistantApi(
      client: client,
      baseUrl: 'http://example.test/api/v1',
    );

    final timeline = await api.fetchGrowthTimeline(babyId: babyId);

    expect(timeline.days, hasLength(1));
    expect(timeline.days.single.feedingMl, 278);
    expect(timeline.days.single.sleepInProgress, isTrue);
  });

  test('sends an AI question using the Java orchestration endpoint', () async {
    final client = MockClient((request) async {
      expect(request.method, 'POST');
      expect(request.url.path, '/api/v1/ai/chat');
      final body = jsonDecode(request.body) as Map<String, dynamic>;
      expect(body['babyId'], babyId);
      expect(body['message'], 'How did baby sleep today?');
      return http.Response(
        '{"conversationId":"$recordId","reply":"Today has real records.","safetyNotice":"Daily reference only.","source":"python-local-rules","suggestedActions":["View today brief"]}',
        200,
      );
    });
    final api = BabyAssistantApi(
      client: client,
      baseUrl: 'http://example.test/api/v1',
    );

    final reply = await api.askAi(
      babyId: babyId,
      conversationId: recordId,
      message: 'How did baby sleep today?',
    );

    expect(reply.source, 'python-local-rules');
    expect(reply.suggestedActions, contains('View today brief'));
  });

  test('authenticates private chat without trusting a sender name', () async {
    var requestNumber = 0;
    final client = MockClient((request) async {
      requestNumber++;
      if (requestNumber == 1) {
        expect(request.method, 'POST');
        expect(request.url.path, '/api/v1/auth/register');
        final body = jsonDecode(request.body) as Map<String, dynamic>;
        expect(body['username'], 'parent1');
        expect(body['displayName'], '妈妈');
        expect(body['password'], 'secret12');
        return http.Response.bytes(
          utf8.encode(
            jsonEncode({
              'accessToken': 'demo-token',
              'tokenType': 'Bearer',
              'expiresAt': '2026-08-14T00:00:00Z',
              'user': {
                'id': recordId,
                'username': 'parent1',
                'displayName': '妈妈',
              },
            }),
          ),
          201,
          headers: const {'content-type': 'application/json; charset=utf-8'},
        );
      }

      expect(request.method, 'POST');
      expect(request.url.path, '/api/v1/family-chat/rooms/$recordId/messages');
      expect(request.headers['authorization'], 'Bearer demo-token');
      final body = jsonDecode(request.body) as Map<String, dynamic>;
      expect(body, {'content': '刚刚喝了 120 ml 奶'});
      expect(body.containsKey('senderName'), isFalse);
      return http.Response.bytes(
        utf8.encode(
          jsonEncode({
            'id': babyId,
            'roomId': recordId,
            'senderUserId': recordId,
            'senderName': '妈妈',
            'content': '刚刚喝了 120 ml 奶',
            'sentAt': '2026-08-07T01:00:00Z',
          }),
        ),
        200,
        headers: const {'content-type': 'application/json; charset=utf-8'},
      );
    });
    final api = BabyAssistantApi(
      client: client,
      baseUrl: 'http://example.test/api/v1',
    );

    final session = await api.register(
      username: 'parent1',
      displayName: '妈妈',
      password: 'secret12',
    );
    api.setAccessToken(session.accessToken);
    final message = await api.sendFamilyChatMessage(
      roomId: recordId,
      content: '刚刚喝了 120 ml 奶',
    );

    expect(session.user.displayName, '妈妈');
    expect(message.senderUserId, recordId);
    expect(message.senderName, '妈妈');
  });

  test('generates and accepts family invites with Bearer identity', () async {
    var requestNumber = 0;
    final client = MockClient((request) async {
      requestNumber++;
      expect(request.headers['authorization'], 'Bearer demo-token');
      final body = jsonDecode(request.body) as Map<String, dynamic>;

      if (requestNumber == 1) {
        expect(request.method, 'POST');
        expect(request.url.path, '/api/v1/family-invites');
        expect(body, {'babyId': babyId});
        return http.Response(
          '{"id":"$recordId","babyId":"$babyId","inviteCode":"ABCD2345","expiresAt":"2026-08-08T00:00:00Z"}',
          201,
        );
      }

      expect(request.method, 'POST');
      expect(request.url.path, '/api/v1/family-invites/accept');
      expect(body, {'inviteCode': 'ABCD2345'});
      return http.Response(
        '{"babyId":"$babyId","userId":"$recordId","memberRole":"MEMBER","joinedAt":"2026-08-07T00:00:00Z"}',
        200,
      );
    });
    final api = BabyAssistantApi(
      client: client,
      baseUrl: 'http://example.test/api/v1',
    )..setAccessToken('demo-token');

    final invite = await api.generateFamilyInvite(babyId: babyId);
    final membership = await api.acceptFamilyInvite(inviteCode: 'abcd2345');

    expect(invite.inviteCode, 'ABCD2345');
    expect(membership.memberRole, 'MEMBER');
  });
}
