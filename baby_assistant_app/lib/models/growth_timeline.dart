import 'daily_summary.dart';

class GrowthTimeline {
  const GrowthTimeline({
    required this.startDate,
    required this.endDate,
    required this.days,
  });

  final DateTime startDate;
  final DateTime endDate;
  final List<DailySummary> days;

  factory GrowthTimeline.fromApiJson(Map<String, dynamic> json) {
    final payload = (json['days'] as List<dynamic>? ?? const [])
        .cast<Map<String, dynamic>>()
        .map(DailySummary.fromApiJson)
        .toList();
    return GrowthTimeline(
      startDate: DateTime.parse(json['startDate'] as String),
      endDate: DateTime.parse(json['endDate'] as String),
      days: payload,
    );
  }
}
