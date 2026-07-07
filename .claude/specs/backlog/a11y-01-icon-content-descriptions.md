# contentDescription для иконок (13 пропусков)
Epic: a11y
Order: 01 of 03
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Осмысленные иконки получают contentDescription (из ресурсов), TalkBack их озвучивает; истинно декоративные помечаются null осознанно с комментарием.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/res/values/strings.xml — ключи описаний: cd_week_status_met/missed, cd_meal_icon (параметр), cd_search, cd_add_weight, cd_export (G1)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayScreen.kt:618,631 — статусы недели Check/Close → contentDescription из ресурса (осмысленно: «цель достигнута/не достигнута»); :350,362 иконки приёмов пищи — описание или null если дублируют текст рядом (G1, G10; D5)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/history/HistoryScreen.kt:95, weight/WeightScreen.kt:98, product/ProductSearchScreen.kt:113 (Search), settings/SettingsScreen.kt:172 (FileDownload) — contentDescription из ресурса (G1, G15)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayDesignedContent.kt:424,436,554,682 — разобрать декоративное-vs-осмысленное, описание или осознанный null с комментарием (G1; D5, H1)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/settings/SettingsScreenContentTest.kt — обновить ассерты contentDescription под новые ресурсы (G13)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Depends-on i18n-strings (эпик): описания через stringResource, НЕ хардкод (D3); a11y идёт после i18n
  - ⚠ Same-file: TodayScreen.kt/TodayDesignedContent.kt правят также SPEC 02/03 — этот первый, 02→03 после (разные зоны)
  - НЕ навешивать contentDescription на чисто декоративные иконки (дублирующие текст) — это шум TalkBack; каждый null оставить с комментарием-обоснованием (D5, H1)
  - Roborazzi базлайны не затрагиваются (a11y-дерево вне пикселей, G14)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Meaningful icons are described for screen readers
  Covers epic a11y, SPEC 01.

  @a11y-01
  Scenario: Week status icons announce their meaning
    Given the today screen shows the week strip
    When a screen reader focuses a day-status icon
    Then it announces whether the day's goal was met

  @a11y-01
  Scenario: The search icon is announced
    Given the product search screen is open
    When a screen reader focuses the search icon
    Then it announces the search action

  @a11y-01 @edge
  Scenario: Purely decorative icons are skipped
    Given an icon that merely repeats adjacent text
    When a screen reader traverses the screen
    Then that icon is not announced separately
```

## Gap / context
13 иконок с contentDescription=null (G1): TalkBack пропускает статусы недели, поиск, экспорт — незрячий пользователь не понимает управление.

## Implementation links
- commit: (pending)
- files:  (pending)
