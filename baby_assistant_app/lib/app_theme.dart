import 'package:flutter/material.dart';

abstract final class AppColors {
  static const canvas = Color(0xFFF2EFEA);
  static const ink = Color(0xFF252421);
  static const muted = Color(0xFF8F8A83);
  static const line = Color(0xFFE4E0DA);
  static const paper = Color(0xFFFDFBF8);
  static const coral = Color(0xFFFF7951);
  static const peach = Color(0xFFFFB39D);
  static const blue = Color(0xFFCFE0E5);
  static const mint = Color(0xFF47BE8A);
  static const yellow = Color(0xFFF0C85D);
}

abstract final class AppTheme {
  static ThemeData get light {
    final scheme = ColorScheme.fromSeed(
      seedColor: AppColors.coral,
      brightness: Brightness.light,
      surface: AppColors.paper,
    );

    return ThemeData(
      useMaterial3: true,
      colorScheme: scheme,
      scaffoldBackgroundColor: AppColors.canvas,
      fontFamilyFallback: const [
        'Microsoft YaHei',
        'PingFang SC',
        'Noto Sans CJK SC',
      ],
      textTheme: const TextTheme(
        headlineLarge: TextStyle(
          color: AppColors.ink,
          fontSize: 31,
          height: 1.12,
          letterSpacing: -1.2,
          fontWeight: FontWeight.w800,
        ),
        titleLarge: TextStyle(
          color: AppColors.ink,
          fontSize: 20,
          fontWeight: FontWeight.w800,
        ),
        bodyMedium: TextStyle(color: AppColors.ink, fontSize: 14, height: 1.45),
      ),
    );
  }
}
