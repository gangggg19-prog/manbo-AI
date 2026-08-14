import 'package:flutter/material.dart';
import 'package:shared_preferences/shared_preferences.dart';

import 'ai_assistant_page.dart';
import 'app_theme.dart';
import 'data/baby_assistant_api.dart';
import 'family_chat_page.dart';
import 'models/baby_profile.dart';
import 'models/care_record.dart';
import 'models/daily_summary.dart';
import 'resource_page.dart';
import 'timeline_page.dart';

class HomePage extends StatefulWidget {
  const HomePage({super.key, this.useServer = true});

  final bool useServer;

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  static const _recordsKey = 'local-care-records';
  final BabyAssistantApi _api = BabyAssistantApi();
  int _selectedTab = 0;
  bool _loading = true;
  String? _error;
  BabyProfile _baby = BabyProfile(
    id: 'local-baby',
    displayName: '小满',
    birthDate: DateTime(2026, 4, 1),
  );
  List<CareRecord> _records = [];
  DailySummary? _summary;

  @override
  void initState() {
    super.initState();
    _loadHome();
  }

  Future<void> _loadHome() async {
    setState(() {
      _loading = true;
      _error = null;
    });
    try {
      if (!widget.useServer) {
        final prefs = await SharedPreferences.getInstance();
        final stored = prefs.getStringList(_recordsKey) ?? const [];
        _records = stored.map(CareRecord.decode).toList()
          ..sort((a, b) => b.recordedAt.compareTo(a.recordedAt));
        _summary = _localSummary();
      } else {
        final babies = await _api.fetchBabies();
        if (babies.isEmpty) {
          throw const BabyAssistantApiException(404, 'No baby profile');
        }
        _baby = babies.first;
        final now = DateTime.now();
        final results = await Future.wait([
          _api.fetchCareRecords(babyId: _baby.id, date: now),
          _api.fetchDailySummary(babyId: _baby.id, date: now),
        ]);
        _records = results[0] as List<CareRecord>;
        _summary = results[1] as DailySummary;
      }
    } catch (_) {
      _error = '暂时无法连接记录服务，稍后下拉刷新即可。';
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  DailySummary _localSummary() {
    final feeds = _records.where(
      (record) => record.type == CareRecordType.feeding,
    );
    final diapers = _records.where(
      (record) => record.type == CareRecordType.diaper,
    );
    return DailySummary(
      date: DateTime.now(),
      feedingMl: feeds.fold(0, (sum, record) => sum + (record.amountMl ?? 0)),
      diaperCount: diapers.length,
      sleepMinutes: 0,
      feedingDeltaMl: 0,
      diaperDelta: 0,
      sleepDeltaMinutes: 0,
      sleepInProgress: false,
      insight: 'LOCAL_RECORDS',
    );
  }

  Future<void> _saveLocal() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setStringList(
      _recordsKey,
      _records.map((record) => record.encode()).toList(),
    );
  }

  Future<void> _openRecordSheet({
    CareRecord? editing,
    CareRecordType type = CareRecordType.feeding,
  }) async {
    final amount = TextEditingController(
      text: editing?.amountMl?.toString() ?? '',
    );
    final selectedType = editing?.type ?? type;
    final result = await showModalBottomSheet<CareRecord>(
      context: context,
      isScrollControlled: true,
      backgroundColor: AppColors.paper,
      builder: (context) => Padding(
        padding: EdgeInsets.fromLTRB(
          22,
          22,
          22,
          MediaQuery.viewInsetsOf(context).bottom + 24,
        ),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(
              editing == null
                  ? (selectedType == CareRecordType.feeding
                        ? '记录一顿奶'
                        : '记录一次尿布')
                  : (selectedType == CareRecordType.feeding ? '奶瓶喂养' : '尿布记录'),
              style: Theme.of(context).textTheme.titleLarge,
            ),
            const SizedBox(height: 8),
            Text(
              selectedType == CareRecordType.feeding
                  ? '记下奶量，首页和日报会马上更新。'
                  : '记下这次尿布，让趋势更完整。',
              style: const TextStyle(color: AppColors.muted),
            ),
            if (selectedType == CareRecordType.feeding) ...[
              const SizedBox(height: 18),
              TextField(
                key: const Key('feeding-amount-input'),
                controller: amount,
                keyboardType: TextInputType.number,
                autofocus: false,
                decoration: const InputDecoration(
                  labelText: '奶量（ml）',
                  filled: true,
                ),
              ),
            ],
            const SizedBox(height: 20),
            FilledButton(
              key: const Key('save-feeding-record'),
              style: FilledButton.styleFrom(
                minimumSize: const Size.fromHeight(52),
                backgroundColor: AppColors.ink,
              ),
              onPressed: () {
                final value = int.tryParse(amount.text.trim());
                if (selectedType == CareRecordType.feeding &&
                    (value == null || value <= 0)) {
                  return;
                }
                Navigator.pop(
                  context,
                  CareRecord(
                    id:
                        editing?.id ??
                        'local-${DateTime.now().microsecondsSinceEpoch}',
                    type: selectedType,
                    recordedAt: editing?.recordedAt ?? DateTime.now(),
                    amountMl: selectedType == CareRecordType.feeding
                        ? value
                        : null,
                  ),
                );
              },
              child: Text(editing == null ? '保存记录' : '更新记录'),
            ),
          ],
        ),
      ),
    );
    if (result == null) return;

    try {
      if (widget.useServer) {
        final saved = editing == null
            ? await _api.createCareRecord(babyId: _baby.id, record: result)
            : await _api.updateCareRecord(babyId: _baby.id, record: result);
        final index = _records.indexWhere((record) => record.id == saved.id);
        if (index >= 0) {
          _records[index] = saved;
        } else {
          _records = [saved, ..._records];
        }
      } else {
        final index = _records.indexWhere((record) => record.id == result.id);
        if (index >= 0) {
          _records[index] = result;
        } else {
          _records = [result, ..._records];
        }
        await _saveLocal();
      }
      _records.sort((a, b) => b.recordedAt.compareTo(a.recordedAt));
      if (mounted) {
        setState(() => _summary = _localSummary());
        final confirmation = editing == null
            ? '\u5DF2\u8BB0\u5F55\u5976\u74F6\u5582\u517B ${result.amountMl ?? ''}${selectedType == CareRecordType.feeding ? ' ml' : ''}'
            : '\u5DF2\u66F4\u65B0\u5976\u74F6\u8BB0\u5F55';
        final messenger = ScaffoldMessenger.of(context);
        messenger.hideCurrentSnackBar();
        messenger.showSnackBar(SnackBar(content: Text(confirmation)));
      }
      if (widget.useServer) await _loadHome();
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('保存失败，请确认 Java 服务正在运行。')));
      }
    }
  }

  Future<bool> _delete(CareRecord record) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('删除这条记录？'),
        content: const Text('删除后将影响今日统计。'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('取消'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('删除'),
          ),
        ],
      ),
    );
    if (confirmed != true) return false;
    try {
      if (widget.useServer) {
        await _api.deleteCareRecord(record.id);
      } else {
        _records.removeWhere((item) => item.id == record.id);
        await _saveLocal();
      }
      if (mounted) {
        setState(() => _summary = _localSummary());
        final messenger = ScaffoldMessenger.of(context);
        messenger.hideCurrentSnackBar();
        messenger.showSnackBar(
          const SnackBar(
            content: Text('\u5DF2\u5220\u9664\u8FD9\u6761\u8BB0\u5F55'),
          ),
        );
      }
      return true;
    } catch (_) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('删除失败，请稍后重试。')));
      }
      return false;
    }
  }

  @override
  Widget build(BuildContext context) {
    if (_selectedTab == 1) {
      return Scaffold(
        backgroundColor: AppColors.canvas,
        body: TimelinePage(
          api: widget.useServer ? _api : null,
          onBack: () => setState(() => _selectedTab = 0),
        ),
      );
    }
    if (_selectedTab == 2) {
      return ResourcePage(
        api: widget.useServer ? _api : null,
        onBack: () => setState(() => _selectedTab = 0),
      );
    }
    if (_selectedTab == 4) {
      return Scaffold(
        backgroundColor: AppColors.canvas,
        body: FamilyChatPage(
          api: widget.useServer ? _api : null,
          onBack: () => setState(() => _selectedTab = 0),
        ),
      );
    }
    if (_selectedTab == 3) {
      return Scaffold(
        backgroundColor: AppColors.canvas,
        body: AiAssistantPage(
          api: widget.useServer ? _api : null,
          onBack: () => setState(() => _selectedTab = 0),
        ),
      );
    }

    final summary = _summary ?? _localSummary();
    return Scaffold(
      body: SafeArea(
        child: RefreshIndicator(
          onRefresh: _loadHome,
          color: AppColors.coral,
          child: CustomScrollView(
            physics: const AlwaysScrollableScrollPhysics(
              parent: BouncingScrollPhysics(),
            ),
            slivers: [
              SliverPadding(
                padding: const EdgeInsets.fromLTRB(18, 16, 18, 118),
                sliver: SliverList(
                  delegate: SliverChildListDelegate([
                    _HomeHeader(name: _baby.displayName),
                    const SizedBox(height: 16),
                    _BriefCard(summary: summary),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: _MetricCard(
                            key: const Key('bottle-card'),
                            color: AppColors.paper,
                            icon: Icons.water_drop_outlined,
                            label: '奶量',
                            value: '${summary.feedingMl}',
                            unit: 'ml',
                            onTap: _openRecordSheet,
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: _MetricCard(
                            color: AppColors.ink,
                            icon: Icons.bedtime_outlined,
                            label: '睡眠',
                            value: '${summary.sleepMinutes}',
                            unit: '分钟',
                            onTap: () {},
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 12),
                    Row(
                      children: [
                        Expanded(
                          child: _MetricCard(
                            color: AppColors.peach,
                            icon: Icons.baby_changing_station_outlined,
                            label: '尿布',
                            value: '${summary.diaperCount}',
                            unit: '次',
                            onTap: () =>
                                _openRecordSheet(type: CareRecordType.diaper),
                          ),
                        ),
                        const SizedBox(width: 12),
                        Expanded(
                          child: _MetricCard(
                            color: AppColors.blue,
                            icon: Icons.auto_awesome_outlined,
                            label: 'Manbo',
                            value: '问问',
                            unit: '',
                            onTap: () => setState(() => _selectedTab = 3),
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 24),
                    const Text(
                      '今天的记录',
                      style: TextStyle(
                        fontSize: 20,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    const SizedBox(height: 10),
                    if (_loading)
                      const Center(
                        child: Padding(
                          padding: EdgeInsets.all(28),
                          child: CircularProgressIndicator(),
                        ),
                      )
                    else if (_error != null)
                      _ErrorCard(message: _error!, onRetry: _loadHome)
                    else if (_records.isEmpty)
                      const _EmptyRecords()
                    else
                      ..._records.map(
                        (record) => Padding(
                          padding: const EdgeInsets.only(bottom: 10),
                          child: Dismissible(
                            key: Key('record-${record.id}'),
                            direction: DismissDirection.endToStart,
                            confirmDismiss: (_) => _delete(record),
                            background: Container(
                              alignment: Alignment.centerRight,
                              padding: const EdgeInsets.only(right: 24),
                              decoration: BoxDecoration(
                                color: AppColors.coral,
                                borderRadius: BorderRadius.circular(22),
                              ),
                              child: const Icon(
                                Icons.delete_outline,
                                color: Colors.white,
                              ),
                            ),
                            child: _RecordTile(
                              record: record,
                              onTap: () => _openRecordSheet(editing: record),
                            ),
                          ),
                        ),
                      ),
                  ]),
                ),
              ),
            ],
          ),
        ),
      ),
      bottomNavigationBar: _Navigation(
        selected: _selectedTab,
        onSelected: (tab) => setState(() => _selectedTab = tab),
      ),
    );
  }
}

class _HomeHeader extends StatelessWidget {
  const _HomeHeader({required this.name});
  final String name;
  @override
  Widget build(BuildContext context) => Row(
    children: [
      const CircleAvatar(
        backgroundColor: AppColors.ink,
        child: Icon(Icons.auto_awesome, color: Colors.white, size: 18),
      ),
      const SizedBox(width: 10),
      Expanded(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'MANBO',
              style: TextStyle(fontWeight: FontWeight.w900, letterSpacing: 1.3),
            ),
            Text(
              '$name 的成长看台',
              style: const TextStyle(color: AppColors.muted, fontSize: 12),
            ),
          ],
        ),
      ),
      const Icon(Icons.menu_rounded),
    ],
  );
}

class _BriefCard extends StatelessWidget {
  const _BriefCard({required this.summary});
  final DailySummary summary;
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(20),
    decoration: BoxDecoration(
      color: AppColors.blue,
      borderRadius: BorderRadius.circular(28),
    ),
    child: Row(
      children: [
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              const Text('今日简报', style: TextStyle(fontWeight: FontWeight.w700)),
              const SizedBox(height: 8),
              Text(
                summary.sleepInProgress ? '正在享受\n一段睡眠' : '今天，状态\n还不错',
                style: const TextStyle(
                  fontSize: 27,
                  height: 1.04,
                  fontWeight: FontWeight.w900,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                '奶量、睡眠和尿布，都在慢慢形成节奏。',
                style: const TextStyle(fontSize: 12, color: AppColors.muted),
              ),
            ],
          ),
        ),
        Image.asset(
          'assets/images/manbo.png',
          height: 116,
          errorBuilder: (_, _, _) => const Icon(Icons.face_rounded, size: 92),
        ),
      ],
    ),
  );
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({
    super.key,
    required this.color,
    required this.icon,
    required this.label,
    required this.value,
    required this.unit,
    required this.onTap,
  });
  final Color color;
  final IconData icon;
  final String label, value, unit;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) {
    final dark = color == AppColors.ink;
    final foreground = dark ? Colors.white : AppColors.ink;
    return Material(
      color: color,
      borderRadius: BorderRadius.circular(23),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(23),
        child: SizedBox(
          height: 138,
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Icon(icon, size: 18, color: foreground),
                const Spacer(),
                Text(
                  label,
                  style: TextStyle(
                    color: foreground.withValues(alpha: .72),
                    fontSize: 12,
                    fontWeight: FontWeight.w700,
                  ),
                ),
                Row(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      value,
                      style: TextStyle(
                        color: foreground,
                        fontSize: 31,
                        height: 1,
                        fontWeight: FontWeight.w800,
                      ),
                    ),
                    if (unit.isNotEmpty)
                      Padding(
                        padding: const EdgeInsets.only(left: 4, bottom: 2),
                        child: Text(
                          unit,
                          style: TextStyle(
                            color: foreground.withValues(alpha: .7),
                            fontSize: 11,
                          ),
                        ),
                      ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}

class _RecordTile extends StatelessWidget {
  const _RecordTile({required this.record, required this.onTap});
  final CareRecord record;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) {
    final feeding = record.type == CareRecordType.feeding;
    return Material(
      color: AppColors.paper,
      borderRadius: BorderRadius.circular(22),
      child: ListTile(
        onTap: onTap,
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(22)),
        leading: CircleAvatar(
          backgroundColor: feeding ? AppColors.peach : AppColors.yellow,
          child: Icon(
            feeding
                ? Icons.water_drop_outlined
                : Icons.baby_changing_station_outlined,
            color: AppColors.ink,
          ),
        ),
        title: Text(
          feeding ? '奶瓶喂养' : '尿布记录',
          style: const TextStyle(fontWeight: FontWeight.w800),
        ),
        subtitle: Text(
          '${record.recordedAt.hour.toString().padLeft(2, '0')}:${record.recordedAt.minute.toString().padLeft(2, '0')}',
        ),
        trailing: Text(
          feeding ? '${record.amountMl} ml' : '已记录',
          style: const TextStyle(fontWeight: FontWeight.w700),
        ),
      ),
    );
  }
}

class _ErrorCard extends StatelessWidget {
  const _ErrorCard({required this.message, required this.onRetry});
  final String message;
  final VoidCallback onRetry;
  @override
  Widget build(BuildContext context) => Card(
    child: Padding(
      padding: const EdgeInsets.all(18),
      child: Column(
        children: [
          Text(message),
          const SizedBox(height: 8),
          TextButton(onPressed: onRetry, child: const Text('重新连接')),
        ],
      ),
    ),
  );
}

class _EmptyRecords extends StatelessWidget {
  const _EmptyRecords();
  @override
  Widget build(BuildContext context) => Container(
    padding: const EdgeInsets.all(24),
    decoration: BoxDecoration(
      color: AppColors.paper,
      borderRadius: BorderRadius.circular(22),
    ),
    child: const Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text('从第一条记录开始', style: TextStyle(fontWeight: FontWeight.w800)),
        SizedBox(height: 5),
        Text(
          '点上方的奶量或尿布卡片，即可留下今天的日常。',
          style: TextStyle(color: AppColors.muted),
        ),
      ],
    ),
  );
}

class _Navigation extends StatelessWidget {
  const _Navigation({required this.selected, required this.onSelected});
  final int selected;
  final ValueChanged<int> onSelected;
  @override
  Widget build(BuildContext context) => SafeArea(
    top: false,
    child: Container(
      margin: const EdgeInsets.fromLTRB(18, 0, 18, 14),
      padding: const EdgeInsets.symmetric(vertical: 6),
      decoration: BoxDecoration(
        color: AppColors.paper,
        borderRadius: BorderRadius.circular(24),
        boxShadow: const [
          BoxShadow(
            color: Color(0x17000000),
            blurRadius: 18,
            offset: Offset(0, 8),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceAround,
        children: [
          _NavButton(
            icon: Icons.home_rounded,
            selected: selected == 0,
            onTap: () => onSelected(0),
          ),
          _NavButton(
            icon: Icons.insights_rounded,
            selected: selected == 1,
            onTap: () => onSelected(1),
          ),
          _NavButton(
            icon: Icons.menu_book_rounded,
            selected: selected == 2,
            onTap: () => onSelected(2),
          ),
          _NavButton(
            icon: Icons.auto_awesome_rounded,
            selected: selected == 3,
            onTap: () => onSelected(3),
          ),
          _NavButton(
            icon: Icons.forum_rounded,
            selected: selected == 4,
            onTap: () => onSelected(4),
          ),
        ],
      ),
    ),
  );
}

class _NavButton extends StatelessWidget {
  const _NavButton({
    required this.icon,
    required this.selected,
    required this.onTap,
  });
  final IconData icon;
  final bool selected;
  final VoidCallback onTap;
  @override
  Widget build(BuildContext context) => IconButton(
    onPressed: onTap,
    style: IconButton.styleFrom(
      backgroundColor: selected ? AppColors.ink : Colors.transparent,
      foregroundColor: selected ? Colors.white : AppColors.muted,
    ),
    icon: Icon(icon),
  );
}
