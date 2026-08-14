import 'package:flutter/material.dart';

import 'app_theme.dart';
import 'data/baby_assistant_api.dart';
import 'models/daily_summary.dart';
import 'models/growth_timeline.dart';

class TimelinePage extends StatefulWidget {
  const TimelinePage({super.key, required this.api, this.onBack});

  final BabyAssistantApi? api;
  final VoidCallback? onBack;

  @override
  State<TimelinePage> createState() => _TimelinePageState();
}

class _TimelinePageState extends State<TimelinePage> {
  GrowthTimeline? _timeline;
  bool _loading = true;
  String? _error;

  @override
  void initState() {
    super.initState();
    _load();
  }

  Future<void> _load() async {
    final api = widget.api;
    if (api == null) {
      setState(() {
        _loading = false;
        _error = 'OFFLINE';
      });
      return;
    }
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      final babies = await api.fetchBabies();
      if (babies.isEmpty) throw const BabyAssistantApiException(404, 'No baby');
      final timeline = await api.fetchGrowthTimeline(babyId: babies.first.id);
      if (mounted) setState(() => _timeline = timeline);
    } catch (_) {
      if (mounted) setState(() => _error = 'UNAVAILABLE');
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  @override
  Widget build(BuildContext context) {
    return SafeArea(
      bottom: false,
      child: RefreshIndicator(
        onRefresh: _load,
        color: AppColors.coral,
        child: ListView(
          physics: const AlwaysScrollableScrollPhysics(
            parent: BouncingScrollPhysics(),
          ),
          padding: const EdgeInsets.fromLTRB(18, 18, 18, 130),
          children: [
            _TimelineHeader(onBack: widget.onBack),
            const SizedBox(height: 18),
            if (_loading && _timeline == null)
              const Padding(
                padding: EdgeInsets.only(top: 100),
                child: Center(child: CircularProgressIndicator()),
              )
            else if (_error != null && _timeline == null)
              _TimelineError(onRetry: _load)
            else ...[
              _TimelineOverview(timeline: _timeline!),
              const SizedBox(height: 14),
              for (final summary in _timeline!.days) ...[
                _TimelineDayCard(summary: summary),
                const SizedBox(height: 10),
              ],
            ],
          ],
        ),
      ),
    );
  }
}

class _TimelineHeader extends StatelessWidget {
  const _TimelineHeader({this.onBack});

  final VoidCallback? onBack;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        IconButton(
          key: const Key('timeline-back-button'),
          tooltip: '返回首页',
          onPressed: onBack ?? () => Navigator.of(context).maybePop(),
          icon: const Icon(Icons.arrow_back_rounded),
        ),
        const SizedBox(width: 2),
        const Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'GROWTH LOG',
                style: TextStyle(
                  fontFamily: 'serif',
                  fontSize: 31,
                  fontWeight: FontWeight.w900,
                  letterSpacing: -1.2,
                ),
              ),
              SizedBox(height: 3),
              Text(
                '\u{6700}\u{8fd1} 7 \u{5929}\u{7684}\u{771f}\u{5b9e}\u{517b}\u{80b2}\u{8bb0}\u{5f55}',
                style: TextStyle(color: AppColors.muted, fontSize: 12),
              ),
            ],
          ),
        ),
        Container(
          width: 42,
          height: 42,
          decoration: const BoxDecoration(
            color: AppColors.blue,
            shape: BoxShape.circle,
          ),
          child: const Icon(Icons.insights_rounded, color: AppColors.ink),
        ),
      ],
    );
  }
}

class _TimelineOverview extends StatelessWidget {
  const _TimelineOverview({required this.timeline});

  final GrowthTimeline timeline;

  @override
  Widget build(BuildContext context) {
    final feeding = timeline.days.fold(
      0,
      (total, day) => total + day.feedingMl,
    );
    final diapers = timeline.days.fold(
      0,
      (total, day) => total + day.diaperCount,
    );
    final sleep = timeline.days.fold(
      0,
      (total, day) => total + day.sleepMinutes,
    );
    return Container(
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: AppColors.ink,
        borderRadius: BorderRadius.circular(27),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          const Text(
            '\u{8fd9} 7 \u{5929}\u{7684}\u{7d2f}\u{8ba1}',
            style: TextStyle(color: Colors.white, fontWeight: FontWeight.w800),
          ),
          const SizedBox(height: 15),
          Row(
            children: [
              _OverviewMetric(value: '$feeding', label: 'ml \u{5976}\u{91cf}'),
              const SizedBox(width: 18),
              _OverviewMetric(
                value: '$diapers',
                label: '\u{6b21}\u{5c3f}\u{5e03}',
              ),
              const SizedBox(width: 18),
              _OverviewMetric(
                value: _sleepLabel(sleep),
                label: '\u{7761}\u{7720}',
              ),
            ],
          ),
        ],
      ),
    );
  }
}

class _OverviewMetric extends StatelessWidget {
  const _OverviewMetric({required this.value, required this.label});

  final String value;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Expanded(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            value,
            style: const TextStyle(
              color: Colors.white,
              fontSize: 23,
              fontWeight: FontWeight.w300,
              letterSpacing: -1,
            ),
          ),
          Text(
            label,
            style: const TextStyle(color: Color(0xFFB4B2AD), fontSize: 10),
          ),
        ],
      ),
    );
  }
}

class _TimelineDayCard extends StatelessWidget {
  const _TimelineDayCard({required this.summary});

  final DailySummary summary;

  @override
  Widget build(BuildContext context) {
    final isToday = _sameDay(summary.date, DateTime.now());
    return Container(
      padding: const EdgeInsets.fromLTRB(17, 15, 17, 15),
      decoration: BoxDecoration(
        color: isToday ? AppColors.peach : AppColors.paper,
        borderRadius: BorderRadius.circular(24),
      ),
      child: Row(
        children: [
          Container(
            width: 43,
            height: 43,
            alignment: Alignment.center,
            decoration: BoxDecoration(
              color: isToday
                  ? Colors.white.withValues(alpha: .62)
                  : AppColors.canvas,
              shape: BoxShape.circle,
            ),
            child: Text(
              '${summary.date.month}/${summary.date.day}',
              style: const TextStyle(fontSize: 11, fontWeight: FontWeight.w800),
            ),
          ),
          const SizedBox(width: 13),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  isToday
                      ? '\u{4eca}\u{5929}'
                      : '\u{517b}\u{80b2}\u{8bb0}\u{5f55}',
                  style: const TextStyle(fontWeight: FontWeight.w800),
                ),
                const SizedBox(height: 5),
                Text(
                  '${summary.feedingMl} ml \u{5976}\u{91cf}  \u{00b7}  ${summary.diaperCount}\u{6b21}\u{5c3f}\u{5e03}  \u{00b7}  ${_sleepLabel(summary.sleepMinutes)}',
                  style: const TextStyle(color: AppColors.muted, fontSize: 11),
                ),
              ],
            ),
          ),
          Icon(
            summary.sleepInProgress
                ? Icons.bedtime_rounded
                : Icons.chevron_right_rounded,
            color: summary.sleepInProgress ? AppColors.ink : AppColors.muted,
          ),
        ],
      ),
    );
  }
}

class _TimelineError extends StatelessWidget {
  const _TimelineError({required this.onRetry});

  final Future<void> Function() onRetry;

  @override
  Widget build(BuildContext context) {
    return Padding(
      padding: const EdgeInsets.only(top: 90),
      child: Center(
        child: Column(
          children: [
            const Icon(
              Icons.cloud_off_rounded,
              color: AppColors.muted,
              size: 38,
            ),
            const SizedBox(height: 12),
            const Text(
              '\u{6682}\u{65f6}\u{65e0}\u{6cd5}\u{8bfb}\u{53d6}\u{6210}\u{957f}\u{8bb0}\u{5f55}',
            ),
            const SizedBox(height: 10),
            TextButton(
              onPressed: _loadAgain,
              child: const Text('\u{91cd}\u{8bd5}'),
            ),
          ],
        ),
      ),
    );
  }

  void _loadAgain() {
    onRetry();
  }
}

String _sleepLabel(int minutes) =>
    '${minutes ~/ 60}\u{5c0f}\u{65f6}${minutes % 60}\u{5206}';

bool _sameDay(DateTime left, DateTime right) =>
    left.year == right.year &&
    left.month == right.month &&
    left.day == right.day;
