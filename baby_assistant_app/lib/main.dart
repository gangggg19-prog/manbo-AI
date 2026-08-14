import 'package:flutter/material.dart';

import 'app_theme.dart';
import 'home_page.dart';

void main() => runApp(const BabyAssistantApp());

class BabyAssistantApp extends StatelessWidget {
  const BabyAssistantApp({super.key, this.useServer = true});

  final bool useServer;

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Manbo 宝宝助手',
      debugShowCheckedModeBanner: false,
      theme: AppTheme.light,
      home: HomePage(useServer: useServer),
    );
  }
}
