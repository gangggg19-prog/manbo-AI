class DailySummary {
  const DailySummary({
    required this.date,
    required this.feedingMl,
    required this.diaperCount,
    required this.sleepMinutes,
    required this.feedingDeltaMl,
    required this.diaperDelta,
    required this.sleepDeltaMinutes,
    required this.sleepInProgress,
    required this.insight,
  });

  final DateTime date;
  final int feedingMl;
  final int diaperCount;
  final int sleepMinutes;
  final int feedingDeltaMl;
  final int diaperDelta;
  final int sleepDeltaMinutes;
  final bool sleepInProgress;
  final String insight;

  factory DailySummary.fromApiJson(Map<String, dynamic> json) {
    int number(String key) => (json[key] as num?)?.toInt() ?? 0;
    return DailySummary(
      date: DateTime.parse(json['date'] as String),
      feedingMl: number('feedingMl'),
      diaperCount: number('diaperCount'),
      sleepMinutes: number('sleepMinutes'),
      feedingDeltaMl: number('feedingDeltaMl'),
      diaperDelta: number('diaperDelta'),
      sleepDeltaMinutes: number('sleepDeltaMinutes'),
      sleepInProgress: json['sleepInProgress'] as bool? ?? false,
      insight: json['insight'] as String? ?? 'START_RECORDING',
    );
  }
}
