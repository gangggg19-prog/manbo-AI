import 'dart:convert';

import 'package:baby_assistant_app/data/baby_assistant_api.dart';
import 'package:baby_assistant_app/family_chat_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:http/http.dart' as http;
import 'package:http/testing.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  const babyId = '618b988e-9588-4c64-9da8-b010c52e3f40';
  const userId = '8a307dc0-2e09-4d12-8eee-0f9d9574ee25';
  const roomId = '4ba14621-682b-4f22-9d32-eb6415434819';

  setUp(() {
    SharedPreferences.setMockInitialValues({
      'manbo_auth_session_v1': jsonEncode({
        'accessToken': 'demo-token',
        'expiresAt': '2099-08-14T00:00:00Z',
        'user': {'id': userId, 'username': 'parent1', 'displayName': 'Parent'},
      }),
    });
  });

  testWidgets('owner can generate a one-time family invite', (tester) async {
    final client = MockClient((request) async {
      if (request.url.path != '/api/v1/babies') {
        expect(request.headers['authorization'], 'Bearer demo-token');
      }
      switch (request.url.path) {
        case '/api/v1/auth/me':
          return http.Response(
            '{"id":"$userId","username":"parent1","displayName":"Parent"}',
            200,
          );
        case '/api/v1/babies':
          return http.Response(
            '[{"id":"$babyId","displayName":"Baby","birthDate":"2026-04-01"}]',
            200,
          );
        case '/api/v1/family-chat/rooms':
          return http.Response(
            '{"id":"$roomId","babyId":"$babyId","title":"Baby family circle","createdAt":"2026-08-07T00:00:00Z","currentUserRole":"OWNER"}',
            200,
          );
        case '/api/v1/family-chat/rooms/$roomId/messages':
          return http.Response('[]', 200);
        case '/api/v1/family-invites':
          final body = jsonDecode(request.body) as Map<String, dynamic>;
          expect(body, {'babyId': babyId});
          return http.Response(
            '{"id":"$userId","babyId":"$babyId","inviteCode":"ABCD2345","expiresAt":"2099-08-08T00:00:00Z"}',
            201,
          );
      }
      return http.Response('Not found', 404);
    });
    final api = BabyAssistantApi(
      client: client,
      baseUrl: 'http://example.test/api/v1',
    );

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: FamilyChatPage(api: api, enableRealtime: false)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.byKey(const Key('family-invite-button')), findsOneWidget);
    await tester.tap(find.byKey(const Key('family-invite-button')));
    await tester.pumpAndSettle();

    expect(
      find.byKey(const Key('family-invite-generated-code')),
      findsOneWidget,
    );
    expect(find.text('ABCD2345'), findsOneWidget);
  });

  testWidgets('invited account joins and reloads the family chat', (
    tester,
  ) async {
    var roomRequests = 0;
    final client = MockClient((request) async {
      if (request.url.path != '/api/v1/babies') {
        expect(request.headers['authorization'], 'Bearer demo-token');
      }
      switch (request.url.path) {
        case '/api/v1/auth/me':
          return http.Response(
            '{"id":"$userId","username":"parent2","displayName":"Parent"}',
            200,
          );
        case '/api/v1/babies':
          return http.Response(
            '[{"id":"$babyId","displayName":"Baby","birthDate":"2026-04-01"}]',
            200,
          );
        case '/api/v1/family-chat/rooms':
          roomRequests++;
          if (roomRequests == 1) return http.Response('Forbidden', 403);
          return http.Response(
            '{"id":"$roomId","babyId":"$babyId","title":"Baby family circle","createdAt":"2026-08-07T00:00:00Z","currentUserRole":"MEMBER"}',
            200,
          );
        case '/api/v1/family-invites/accept':
          final body = jsonDecode(request.body) as Map<String, dynamic>;
          expect(body, {'inviteCode': 'ABCD2345'});
          return http.Response(
            '{"babyId":"$babyId","userId":"$userId","memberRole":"MEMBER","joinedAt":"2026-08-07T00:00:00Z"}',
            200,
          );
        case '/api/v1/family-chat/rooms/$roomId/messages':
          return http.Response('[]', 200);
      }
      return http.Response('Not found', 404);
    });
    final api = BabyAssistantApi(
      client: client,
      baseUrl: 'http://example.test/api/v1',
    );

    await tester.pumpWidget(
      MaterialApp(
        home: Scaffold(body: FamilyChatPage(api: api, enableRealtime: false)),
      ),
    );
    await tester.pumpAndSettle();

    expect(find.byKey(const Key('family-invite-code-input')), findsOneWidget);
    expect(find.byKey(const Key('family-invite-button')), findsNothing);

    await tester.enterText(
      find.byKey(const Key('family-invite-code-input')),
      'abcd2345',
    );
    await tester.tap(find.byKey(const Key('family-invite-join-button')));
    await tester.pumpAndSettle();

    expect(find.byKey(const Key('family-chat-input')), findsOneWidget);
    expect(find.byKey(const Key('family-invite-button')), findsNothing);
    expect(roomRequests, 2);
  });
}
