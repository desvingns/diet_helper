# Data safety — Room/DataStore/IO hardening — epic overview
Epic: data-safety
Order: 00 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## Goal
Закрыть риски потери и порчи пользовательских данных, найденные аудитом 2026-07-06: тихое стирание БД при будущей миграции, нетранзакционные записи (сидер, сохранённые блюда), непойманные IOException DataStore/PDF, бэкап без правил, падение на кривых nav-аргументах. Только data/di/manifest-слои и error-пути; UI-тексты и новые фичи — вне скоупа (эпики i18n-strings, ux-polish).

## Locked decisions (из grill.md)
- D1: обработка ошибок — точечно по домашней конвенции: reads → `.catch` на краях Flow + sealed `UiState.Error`; writes/one-shot → try/catch во ViewModel (паттерн ExportViewModel). Без Result-обёрток. [confirmed]
- D2: миграции fail-fast везде: убрать `fallbackToDestructiveMigration()`, `exportSchema=true`, коммитить `schemas/` (история начинается с v2 — v1 невосстановима). [confirmed]
- D3: бэкап остаётся включённым + явные `dataExtractionRules.xml`/`fullBackupContent.xml`: включить БД и DataStore, исключить cache (PDF). [confirmed]
- D4: DispatcherProvider минимально — `@IoDispatcher` квалифайер, инжект ровно в 2 точки (PdfReportRenderer, scope сидера). ViewModels не трогаем. [confirmed]
- D5: атомарность сида — `db.withTransaction { вставки }`, флаг isSeeded после коммита; rollback делает повторный сид безопасным. [assumption]
- O1 (deferred): ошибки записи DataStore в Settings пока не показываем пользователю (логируем); видимый snackbar — эпик ux-polish. [assumption]

## SPECs (run via /mp --feature --next in Order)
| Order | File | Depends-on | Layers | Summary |
|---|---|---|---|---|
| 01 | `data-safety-01-migration-policy.md` | — | data, di, build | Fail-fast миграции: убрать destructive fallback, exportSchema=true, закоммитить schemas/ |
| 02 | `data-safety-02-atomic-seeder.md` | — | data | Транзакционный DatabaseSeeder + первые unit-тесты сидера |
| 03 | `data-safety-03-transactional-saved-meal.md` | — | data | @Transaction replace-by-name в SavedMealDao; репозиторий делегирует |
| 04 | `data-safety-04-datastore-io-hardening.md` | — | data | IOException в GoalsDataSource: read-fallback + write-surfacing |
| 05 | `data-safety-05-dispatcher-module.md` | — | di, data | DispatcherModule с @IoDispatcher; инжект в renderer + scope сидера |
| 06 | `data-safety-06-pdf-failure-cleanup.md` | 05 | data | Удаление частичного PDF при сбое рендера + failure-path тест |
| 07 | `data-safety-07-safe-nav-args.md` | — | presentation | Безопасный парс date/mealType в ProductViewModel |
| 08 | `data-safety-08-backup-rules.md` | — | platform | dataExtractionRules + fullBackupContent (БД+DataStore in, cache out) |

## Why this ordering
Foundation-first: чистые data-спеки (01–04) не зависят друг от друга; 05 вводит DI-инфраструктуру диспетчеров, от которой зависит тестируемость 06; **05 и 06 трогают один файл `PdfReportRenderer.kt` — строго последовательно**; 07 (presentation) и 08 (manifest) независимы и замыкают эпик. Каждый SPEC независимо шиппится.

## Key facts (verified — полный ledger: C:\Users\Admin\AppSpecs\data-safety\pipeline\grounding.md)
- G1: @Database version=2, exportSchema=false — `data/local/DietHelperDatabase.kt:19-29`
- G3: `fallbackToDestructiveMigration()` — `di/DatabaseModule.kt:32`; G4: addMigrations(MIGRATION_1_2) — `:31`
- G5/G6: сидер: isSeeded-флаг в DataStore, setSeeded ПОСЛЕ всех insert — `data/local/DatabaseSeeder.kt:14-18`, `GoalsDataSource.kt:44-60`
- G8/G9: saveMeal = deleteByName→insertMeal→insertItems без транзакции — `data/repository/SavedMealRepositoryImpl.kt:23-34`, `dao/SavedMealDao.kt:15-33`
- G10: `.gitignore:26` игнорирует schemas/; ksp-аргумента schemaLocation нет
- G11: Dispatchers.IO захардкожен в `DietHelperApplication.kt:18` и `data/pdf/PdfReportRenderer.kt:45`
- G15/G16: GoalsDataSource без обработки IOException — `GoalsDataSource.kt:20-30,32-42`
- G21/G22: PDF пишется в cacheDir/reports, частичный файл при сбое не удаляется — `PdfReportRenderer.kt:483-491`
- G23: allowBackup=true без правил — `AndroidManifest.xml:7`
- G28/G29: небезопасный парс nav-аргументов — `ProductViewModel.kt:43-48`
- G32: домашний паттерн ошибок (Flow.catch → UiState.Error; try/catch в VM)
- G36: CI-гейты: testDebugUnitTest, verifyRoborazziDebug, jacocoUnitTestReport, detekt, lintDebug, assembleDebug
- G41: у ReportRenderer нет фейка; G14: FakeSavedMealDao внутри SavedMealRepositoryImplTest — обновлять при изменении DAO

## Implementation links
- commit: (pending)
- files:  (pending)
