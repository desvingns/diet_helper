# Accessibility — TalkBack, semantics, WCAG 2.2 AA — epic overview
Epic: a11y
Order: 00 of 03
Status: backlog
Depends-on: i18n-strings (новые contentDescription сразу через stringResource — a11y идёт ПОСЛЕ i18n)
Date: 2026-07-06

## Goal
Сделать приложение доступным для TalkBack и незрячих пользователей (WCAG 2.2 AA): закрыть 13 пропущенных contentDescription, озвучить 3 Canvas-графика (сейчас TalkBack видит пустоту), добавить progress-semantics и не-цветовой сигнал прогресса (WCAG 1.4.1), выправить touch-target у текст-кликабельных. Вне скоупа: редизайн палитры, RTL, live-region.

## Locked decisions (из grill.md)
- D1: Canvas — сводка в contentDescription (Modifier.semantics), цифры из state; TalkBack читает одной фразой. [confirmed]
- D2: цвет-сигнал — добавить текст/число-фолбэк к прогрессу (WCAG 1.4.1). [confirmed]
- D3: новые contentDescription сразу resource-backed (после i18n-strings). [assumption]
- D4: 3 SPEC-а — иконки → Canvas → прогресс+цвет+touch-target. [assumption]
- D5: 13 null-иконок разобрать декоративное-vs-осмысленное; истинно декоративные оставить null с комментарием (baseline 'Назад'/'Удалить' G2/G15). [assumption]

## SPECs (run via /mp --feature --next in Order)
| Order | File | Depends-on | Layers | Summary |
|---|---|---|---|---|
| 01 | `a11y-01-icon-content-descriptions.md` | — | presentation | contentDescription для 13 null-иконок (resource-backed); декоративные — осознанно null |
| 02 | `a11y-02-canvas-chart-semantics.md` | 01 | presentation | MacroDonutChart + CalorieRing + MacroBarLineChart → semantics со сводкой |
| 03 | `a11y-03-progress-color-touch.md` | 02 | presentation | progressSemantics + не-цветовой сигнал + touch-target 48dp |

## Why this ordering
Все три SPEC-а трогают TodayScreen.kt и TodayDesignedContent.kt (разные зоны: иконки / графики / прогресс), поэтому **строго последовательно (01→02→03)** во избежание конфликтов в одних файлах. Логическая прогрессия: сначала простые иконки (устанавливают resource-паттерн описаний), затем Canvas, затем прогресс+цвет+таргеты.

## Key facts (verified — полный ledger: C:\Users\Admin\AppSpecs\a11y\pipeline\grounding.md)
- G1: 13 Icon с contentDescription=null (TodayScreen:618,631,350,362; HistoryScreen:95; WeightScreen:98; ProductSearchScreen:113; SettingsScreen:172; TodayDesignedContent:424,436,554,682); Modifier.semantics нигде не используется
- G2/G15: baseline осмысленных описаний есть ('Назад' ×8, 'Удалить') — паттерн частично соблюдён
- G3: MacroDonutChart Canvas без semantics — TodayScreen:985
- G4: MacroBarLineChart Canvas без semantics — StatsScreen:356
- G5: CalorieRing Canvas без semantics — TodayDesignedContent:489
- G6: MacroRangeProgress кастомный Canvas-прогресс без progressSemantics — TodayDesignedContent:608
- G7: LinearProgressIndicator (DailySummaryCard:95) без progressSemantics, цвет — единственный сигнал
- G8: CircularProgressIndicator ×2 (ExportScreen:157, TodayScreen:1038) без label
- G9: MacroColorUtil green→red без текст-фолбэка (WCAG 1.4.1) — MacroColorUtil:10,22
- G11: текст-кликабельные без touch-target 48dp — TodayScreen:461,469
- G13: SettingsScreenContentTest ассертит contentDescription — обновлять синхронно; G14: Roborazzi авто-ок

## Implementation links
- commit: (pending)
- files:  (pending)
