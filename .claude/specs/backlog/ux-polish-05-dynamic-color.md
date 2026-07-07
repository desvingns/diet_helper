# Dynamic color (Material You), дефолт OFF
Epic: ux-polish
Order: 05 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: DietHelperTheme получает параметр dynamicColor (Material You на API 31+ с fallback на брендовую палитру); по умолчанию выключен — фирменный зелёный сохраняется.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/theme/Theme.kt:21-32 — параметр dynamicColor: Boolean = false; при true и Build.VERSION.SDK_INT>=31 использовать dynamicDarkColorScheme(context)/dynamicLightColorScheme(context), иначе текущие Green-схемы (G3, G7; D2)
TEST_TYPES: unit
CONSTRAINTS:
  - Дефолт false (D2): брендовый зелёный — поведение по умолчанию; Roborazzi-базлайны гоняют дефолтную тему → не плывут (H6)
  - Fallback обязателен: на API <31 dynamic* недоступны — ветвление по SDK_INT (G7)
  - UI-тумблер НЕ добавляем (O1) — только параметр темы; включение — точка расширения на будущее
  - Независимый SPEC — трогает только Theme.kt, без пересечений
=== END SPEC ===

## Acceptance
```gherkin
Feature: Optional Material You theming
  Covers epic ux-polish, SPEC 05.

  @ux-polish-05
  Scenario: Brand palette is used by default
    Given dynamic color is disabled
    When the app themes its UI
    Then the branded green palette is applied

  @ux-polish-05
  Scenario: Dynamic color applies on supported devices
    Given dynamic color is enabled on an API 31+ device
    When the app themes its UI
    Then the system wallpaper-derived palette is applied

  @ux-polish-05 @edge
  Scenario: Dynamic color falls back on older devices
    Given dynamic color is enabled on an API 26–30 device
    When the app themes its UI
    Then the branded green palette is applied instead
```

## Gap / context
DietHelperTheme без dynamicColor-параметра (G3): нет опции Material You, а прямое включение уронило бы бренд и Roborazzi-базлайны — нужен явный OFF-дефолт с fallback.

## Implementation links
- commit: (pending)
- files:  (pending)
