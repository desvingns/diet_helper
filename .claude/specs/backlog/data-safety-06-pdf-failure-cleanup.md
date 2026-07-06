# Очистка частичного PDF при сбое рендера
Epic: data-safety
Order: 06 of 08
Status: backlog
Depends-on: data-safety-05
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: bugfix
PLATFORM: android
WHAT: Сбой записи PDF не оставляет в cacheDir/reports битый файл, доступный через FileProvider; ошибка доходит до ExportViewModel и показывается пользователю по существующей конвенции.
LAYERS: data
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/data/pdf/PdfReportRenderer.kt:483-491 — writeDocumentToFile: обернуть FileOutputStream-запись в try/catch; при исключении file.delete() (best-effort) и rethrow (G21, G22)
  - app/src/main/java/com/k/shavrin/diethelper/data/pdf/PdfReportRenderer.kt:45-63 — убедиться, что document.close() в finally сохраняется на всех путях (G20); исключение пузырится в ExportReportUseCase → ExportViewModel.catch (G26, G35 — цепочка уже работает, менять не надо)
  - app/src/test/java/com/k/shavrin/diethelper/data/pdf/PdfReportRendererFailureTest.kt — НОВЫЙ: Robolectric-тест failure-пути: подсунуть недоступный каталог/спровоцировать IOException → файла нет после сбоя, исключение проброшено; диспетчер — тестовый через @IoDispatcher из SPEC 05
TEST_TYPES: unit
CONSTRAINTS:
  - Depends-on data-safety-05: тот же файл PdfReportRenderer.kt (same-file clash) + нужен инжектируемый диспетчер для детерминированного теста
  - У ReportRenderer нет фейка (G41) — тестируем сам PdfReportRenderer с Robolectric-контекстом, НЕ создаём мок
  - Сообщение об ошибке пользователю уже есть (ExportUiState.errorMessage G27) — тексты не менять (эпик i18n-strings)
=== END SPEC ===

## Acceptance
```gherkin
Feature: PDF export failure leaves no partial file
  Covers epic data-safety, SPEC 06.

  @data-safety-06 @error
  Scenario: A failed export leaves no partial report on disk
    Given the user requests a PDF export
    When writing the report file fails midway
    Then no report file remains in the reports cache
    And the user sees the export error message

  @data-safety-06
  Scenario: A successful export still produces a shareable report
    Given the user requests a PDF export for a valid date range
    When the report is rendered
    Then the report file exists and the share action is offered
```

## Gap / context
FileOutputStream.use без очистки (G22): битый частичный PDF остаётся в cacheDir/reports и доступен через FileProvider (G24) при следующем share.

## Implementation links
- commit: (pending)
- files:  (pending)
