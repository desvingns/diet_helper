# Design refresh — 2026-10-19

The subsequent [midnight plum art refresh](plum-art-direction.md) changes the
palette and adds generated illustrations. This document records the earlier
layout and interaction work that remains in place.

## Direction

A quiet, consistent visual language for food, strength training and progress:
forest graphite backgrounds, mint actions, warm sand accents, soft rose/blue
macro indicators, rounded cards and a clear heading hierarchy. Shared palette
tokens live in `presentation/theme/Color.kt`; `ScreenHeading` and `PillTabs`
provide common screen chrome without changing the Clean Architecture layers.

## User-facing changes

- Today: compact energy summary, calories remaining, daily percentage, three
  macro tiles showing actual values and target ranges. Removed the unsupported
  burned-calories placeholder. The calorie ring still shows consumed energy.
- Calendar: every date stays visible, status appears as a small dot, the selected
  day has a mint background, future days are disabled. Each day exposes its date,
  status and selection to accessibility services. Past dates use the title
  "Дневник" and expose an explicit return-to-today button.
- Navigation: consistent mint selection and a shorter "Цели" label for daily
  goals. Progress, product search and workouts share accessible pill tabs.
  Nested screens consume the parent's applied insets to avoid duplicate system-bar
  spacing around product search, export and other detail screens.
- History: dated cards with energy totals and a clear open-day affordance.
- Goals: persistent save/export actions below the scrollable form, including the
  save confirmation. These actions remain reachable on short screens.
- Weight: an explanatory empty state, visible validation and save feedback,
  decimal-comma support, IME Done, duplicate-save protection, recoverable save
  failures, and a trend for the latest 30 measurements. The chart uses actual
  date spacing, handles constant values, and exposes measurements as spoken text.
  Finite values greater than zero and at most 500 kg are accepted.
- Product search: a clear-query button.
- Launcher: custom adaptive leaf icon, including an Android 13 themed icon;
  matching launch-window and system-bar colors.

## Verification

- Debug APK assembly, Detekt and Android Lint.
- 415 JVM tests, including calendar interactions, weight-input feedback and
  recovery after a failed save.
- 11 Roborazzi references cover dark/light forms, validation, calorie states,
  the populated diary, irregular weight measurements and constant weight.
- Emulator smoke check: Today, date selection/return, product search and clear,
  catalog tabs, workouts/plan, history/day details, progress/weight, invalid weight
  feedback, pinned goals actions; normal and 130% system font scale.
- Existing Room data and workout progress were retained during APK upgrades.

Private emulator screenshots and build/check logs are under the ignored `.local/`
directory. Screenshot-test references are versioned under `app/src/test/snapshots/`.
