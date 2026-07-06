# Явные правила бэкапа (dataExtractionRules + fullBackupContent)
Epic: data-safety
Order: 08 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Google-бэкап и device-transfer переносят дневник (Room-БД) и настройки целей (DataStore), но не мусор из cache (PDF-отчёты); политика бэкапа задана явно, а не по умолчанию.
LAYERS: platform
CHANGED_HINT:
  - app/src/main/AndroidManifest.xml:7 — к allowBackup="true" добавить android:dataExtractionRules="@xml/data_extraction_rules" и android:fullBackupContent="@xml/full_backup_content" (G23; locked decision D3)
  - app/src/main/res/xml/data_extraction_rules.xml — НОВЫЙ (API 31+): cloud-backup и device-transfer, include domain="database" (diet_helper DB) + domain="file" path="datastore/" ; явный exclude для cache прописать согласно D3 — defense-in-depth и самодокументация, при том что система не бэкапит cache по умолчанию (assumption: точное имя файла БД и путь DataStore-файла *.preferences_pb уточнить при реализации)
  - app/src/main/res/xml/full_backup_content.xml — НОВЫЙ (minSdk 26 … API 30): та же политика в legacy-формате <include>/<exclude> (G23, G40)
TEST_TYPES: unit
CONSTRAINTS:
  - PDF-отчёты живут в cacheDir/reports (G21) и через cache-path FileProvider (G24) — в бэкап попадать не должны
  - Валидацию xml выполняет :app:lintDebug в CI (G36) — оба файла должны проходить lint без предупреждений backup-категории
  - TEST_TYPES unit — существующая свита остаётся зелёной; отдельных тестов бэкапа нет (ручная проверка adb backup — в чеклист верификатора)
  - Педагогический комментарий в обоих xml: чем dataExtractionRules отличается от fullBackupContent и почему нужны оба при minSdk 26 (D3)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Explicit backup policy
  Covers epic data-safety, SPEC 08.

  @data-safety-08
  Scenario: Diary survives a device transfer
    Given a user has recorded food entries and custom goals
    When the user migrates to a new device with system backup
    Then the food diary and daily goals are restored

  @data-safety-08
  Scenario: Cached reports are not part of the backup
    Given generated PDF reports exist in the app cache
    When a system backup runs
    Then the backup contains the database and settings
    But it does not contain the cached report files
```

## Gap / context
allowBackup=true без правил (G23): политика неявная, cache и будущие чувствительные файлы уходят в бэкап решением системы, а не проекта.

## Implementation links
- commit: (pending)
- files:  (pending)
