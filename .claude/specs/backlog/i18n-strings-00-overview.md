# I18n strings — extract hardcoded RU strings to resources — epic overview
Epic: i18n-strings
Order: 00 of 05
Status: backlog
Depends-on: testability (эпик извлекал Content из тех же экранов — i18n идёт ПОСЛЕ него)
Date: 2026-07-06

## Goal
Вынести захардкоженный русский UI-текст в ресурсы и ввести UiText-паттерн для строк во ViewModel-ах. Реальный объём (по grounding-скауту) — **301 строка кириллицы в 22 файлах**, а не ~82 из первичного аудита. Готовит проект к локализации и убирает дублирование меток. Вне скоупа: реальный второй язык (values-en), локализация seed-контента, извлечение Content (эпик testability).

## Locked decisions (из grill.md)
- D1: всё (301), но по-кластерно — 5 SPEC-ов по областям. [confirmed]
- D2: sealed UiText { Dynamic(String); Res(@StringRes id, args) }; VM отдаёт UiText.Res, composable резолвит stringResource; VM без Context. [confirmed]
- D3: 15 названий продуктов в DatabaseSeeder оставить как есть (контент БД, не UI). [confirmed]
- D4: Format.kt остаётся чистым — дни/месяцы/единицы/plural резолвятся в composable (stringResource/pluralStringResource). [assumption]
- D5: PdfReportRenderer зовёт context.getString напрямую (уже держит Context); метки приёмов пищи объединяются с UI через общий ресурс. [assumption]
- D6: единый res/values/strings.xml (RU-дефолт), values-ru не заводим до второго языка; SPEC-и добавляют разные ключи. [assumption]

## SPECs (run via /mp --feature --next in Order)
| Order | File | Depends-on | Layers | Summary |
|---|---|---|---|---|
| 01 | `i18n-strings-01-uitext-validation.md` | — | presentation | sealed UiText + перенос VM-валидации (AddProduct/Settings) на UiText.Res |
| 02 | `i18n-strings-02-format-resources.md` | — | presentation | Format.kt дни/месяцы/единицы/plural → ресурсы; база strings.xml |
| 03 | `i18n-strings-03-today-strings.md` | 02 | presentation | TodayScreen + TodayDesignedContent (~97) → stringResource |
| 04 | `i18n-strings-04-screens-strings.md` | 02 | presentation | Остальные экраны + DailySummaryCard → stringResource; апдейт тестов |
| 05 | `i18n-strings-05-nav-pdf-strings.md` | 02 | presentation, data | BottomNavItem + PDF → ресурсы; объединение меток приёмов пищи |

## Why this ordering
01 (UiText-инфраструктура + VM-валидация) независим и устанавливает паттерн. 02 создаёт базовые ресурсы (метки приёмов пищи, единицы) и остаётся чистым Format — от него зависят 03/04/05, которые ссылаются на общие ключи меток (устранение дубля G14). **strings.xml трогают 02–05 аддитивно (разные ключи) — не жёсткий clash, но 02 первым (базовые ключи), 03–05 после.** Каждый SPEC независимо шиппится.

## Key facts (verified — полный ledger: C:\Users\Admin\AppSpecs\i18n-strings\pipeline\grounding.md)
- G1/G2: strings.xml только app_name; 301 строка в 22 файлах; TodayScreen(52)+TodayDesignedContent(45)=97
- G3: Format.kt — dayOfWeekNames :13-21, monthGenitiveNames :23-36, mealTypeLabel :61-66, единицы г/ккал/кг :44-59
- G4/G16: VM-валидация в String-полях UiState (AddProductViewModel:50-74, SettingsViewModel:149-161); sealed UiText не существует
- G5/G13/G14: PDF ~20 строк; PdfReportRenderer держит @ApplicationContext (getString напрямую); метки приёмов пищи дублируются Format↔PDF
- G6: BottomNavItem 5 лейблов :16-20
- G7: TodayDesignedContent — меню/диалоги/макросы; daysWord-plural :947-949 → pluralStringResource
- G8: stringResource нигде не используется (0)
- G10/G11: domain без RU-лейблов; DatabaseSeeder 15 продуктов — seed-data (оставляем)
- G15: ни один VM не держит Context (кроме Export — для Uri)
- G17: SettingsScreenContentTest/ExportContentTest ассертят кириллицу — обновить синхронно; G18: Roborazzi авто-ок
- G20: detekt MaxLineLength=200 — перенос в stringResource порогов не нарушит

## Implementation links
- commit: (pending)
- files:  (pending)
