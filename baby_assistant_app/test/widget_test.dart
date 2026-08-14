import 'package:baby_assistant_app/main.dart';
import 'package:flutter/material.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  setUp(() => SharedPreferences.setMockInitialValues({}));

  testWidgets('喂奶表单会保存自定义奶量', (tester) async {
    await tester.pumpWidget(const BabyAssistantApp(useServer: false));
    await tester.pumpAndSettle();

    await tester.tap(find.byKey(const Key('bottle-card')));
    await tester.pumpAndSettle();
    await tester.tap(find.text('记录一顿奶'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.byKey(const Key('feeding-amount-input')),
      '120',
    );
    await tester.tap(find.byKey(const Key('save-feeding-record')));
    await tester.pumpAndSettle();

    expect(find.text('已记录奶瓶喂养 120 ml'), findsOneWidget);
    await tester.drag(find.byType(CustomScrollView), const Offset(0, -700));
    await tester.pumpAndSettle();
    expect(find.text('奶瓶喂养'), findsOneWidget);

    await tester.tap(find.text('奶瓶喂养'));
    await tester.pumpAndSettle();
    await tester.enterText(
      find.byKey(const Key('feeding-amount-input')),
      '100',
    );
    await tester.tap(find.byKey(const Key('save-feeding-record')));
    await tester.pumpAndSettle();

    expect(find.text('已更新奶瓶记录'), findsOneWidget);
    expect(find.text('100 ml'), findsOneWidget);

    await tester.drag(find.byType(Dismissible).first, const Offset(-500, 0));
    await tester.pumpAndSettle();
    await tester.tap(find.text('删除'));
    await tester.pumpAndSettle();

    expect(find.text('已删除这条记录'), findsOneWidget);
    expect(find.text('100 ml'), findsNothing);
  });

  for (final size in [
    const Size(320, 640),
    const Size(360, 800),
    const Size(411, 891),
  ]) {
    testWidgets('首页适配 ${size.width.toInt()} 宽手机', (tester) async {
      await tester.binding.setSurfaceSize(size);
      await tester.pumpWidget(const BabyAssistantApp(useServer: false));
      await tester.pumpAndSettle();

      expect(tester.takeException(), isNull);
      await tester.binding.setSurfaceSize(null);
    });
  }
  testWidgets('bottom growth button opens the timeline page', (tester) async {
    await tester.binding.setSurfaceSize(const Size(360, 760));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    await tester.pumpWidget(const BabyAssistantApp());
    await tester.pumpAndSettle();

    await tester.tap(find.byIcon(Icons.insights_rounded));
    await tester.pumpAndSettle();

    expect(find.text('GROWTH LOG'), findsOneWidget);
  });

  testWidgets('family chat button opens and returns home', (tester) async {
    await tester.binding.setSurfaceSize(const Size(360, 760));
    addTearDown(() => tester.binding.setSurfaceSize(null));
    await tester.pumpWidget(const BabyAssistantApp(useServer: false));
    await tester.pumpAndSettle();

    await tester.tap(find.byIcon(Icons.forum_rounded));
    await tester.pumpAndSettle();
    expect(find.byKey(const Key('family-chat-back-button')), findsOneWidget);

    await tester.tap(find.byKey(const Key('family-chat-back-button')));
    await tester.pumpAndSettle();
    expect(find.byKey(const Key('bottle-card')), findsOneWidget);
  });
}
