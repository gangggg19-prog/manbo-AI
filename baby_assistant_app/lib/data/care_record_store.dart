import 'package:shared_preferences/shared_preferences.dart';

import '../models/care_record.dart';

class CareRecordStore {
  static const _storageKey = 'care_records_v1';

  Future<List<CareRecord>> load() async {
    final preferences = await SharedPreferences.getInstance();
    final storedRecords = preferences.getStringList(_storageKey) ?? const [];
    return storedRecords.map(CareRecord.decode).toList()
      ..sort((a, b) => b.recordedAt.compareTo(a.recordedAt));
  }

  Future<void> save(List<CareRecord> records) async {
    final preferences = await SharedPreferences.getInstance();
    await preferences.setStringList(
      _storageKey,
      records.map((record) => record.encode()).toList(),
    );
  }
}
