# DispatcherModule: инжектируемый @IoDispatcher
Epic: data-safety
Order: 05 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: refactor
PLATFORM: android
WHAT: Захардкоженный Dispatchers.IO заменяется инжектируемым диспетчером в двух существующих точках использования — тесты получают возможность подменять диспетчер (нужно SPEC 06).
LAYERS: di, data
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/di/DispatcherModule.kt — НОВЫЙ: @Qualifier annotation class IoDispatcher + @Provides @IoDispatcher fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO, @InstallIn(SingletonComponent) по образцу соседних модулей (G33)
  - app/src/main/java/com/k/shavrin/diethelper/data/pdf/PdfReportRenderer.kt:45 — конструктор получает @IoDispatcher private val ioDispatcher: CoroutineDispatcher; withContext(Dispatchers.IO) → withContext(ioDispatcher) (G11, G20)
  - app/src/main/java/com/k/shavrin/diethelper/DietHelperApplication.kt:18 — scope сидера строится на инжектированном диспетчере: @Inject @IoDispatcher lateinit var ioDispatcher (field-injection в @HiltAndroidApp Application, scope создаётся в onCreate после super) (G7, G11; assumption — точный способ внедрения в Application выбрать при реализации)
TEST_TYPES: unit
CONSTRAINTS:
  - Скоуп строго 2 точки (locked decision D4): ViewModels/UseCases НЕ трогать
  - ⚠ Same-file clash: PdfReportRenderer.kt также трогает SPEC 06 — этот SPEC выполняется ПЕРВЫМ, 06 строго после
  - Существующие тесты PdfReportLayoutTest/Export* остаются зелёными (G36); рендерер получает возможность принимать StandardTestDispatcher в новых тестах
  - Педагогический комментарий у квалифайера: зачем инжекция диспетчеров и почему НЕ во ViewModels (D4)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Injectable IO dispatcher
  Covers epic data-safety, SPEC 05.

  @data-safety-05
  Scenario: PDF rendering still runs off the main thread
    Given the user requests a PDF export
    When the report is rendered
    Then the UI remains responsive during rendering
    And the export completes as before

  @data-safety-05
  Scenario: Tests can control the renderer's dispatcher
    Given a renderer test provides a test dispatcher
    When the test invokes rendering
    Then the rendering runs on the provided dispatcher deterministically
```

## Gap / context
Dispatchers.IO захардкожен в Application и PdfReportRenderer (G11) — failure-path тест рендера (SPEC 06) без инжекции недетерминирован.

## Implementation links
- commit: (pending)
- files:  (pending)
