# Adaptive-иконка + splash screen
Epic: ux-polish
Order: 01 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Приложение получает кастомную adaptive-иконку (ассет предоставляет пользователь) и минимальный splash-экран через core-splashscreen; системная заглушка уходит.
LAYERS: platform, presentation
CHANGED_HINT:
  - gradle/libs.versions.toml + app/build.gradle.kts — добавить androidx.core:core-splashscreen (API 21+ бэкпорт, работает на minSdk 26) (G4)
  - app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml + ic_launcher_round.xml — НОВЫЕ: <adaptive-icon> foreground+background; background из брендового зелёного Color.kt (G6; D3)
  - app/src/main/res/mipmap-*/ic_launcher.png (mdpi..xxxhdpi) + drawable foreground vector — ПЛЕЙСХОЛДЕР: пользователь заменит своим ассетом; SPEC описывает структуру и размеры (G1, G6; D3)
  - app/src/main/AndroidManifest.xml:8,10 — android:icon/@roundIcon → @mipmap/ic_launcher(_round) вместо @android:drawable/sym_def_app_icon (G1)
  - app/src/main/res/values/themes.xml — Theme.DietHelper.Starting (postSplashScreenTheme=Theme.DietHelper), windowSplashScreenBackground + windowSplashScreenAnimatedIcon (G2)
  - app/src/main/java/com/k/shavrin/diethelper/MainActivity.kt:26 — installSplashScreen() ПЕРВОЙ строкой onCreate, до super/setContent (G5)
TEST_TYPES: unit
CONSTRAINTS:
  - Ассет иконки — вход от пользователя (D3): SPEC кладёт валидный плейсхолдер (напр. монохром из палитры), пользователь заменит; сборка не должна зависеть от финального рисунка
  - splash деградирует на API 26-30 (core-splashscreen бэкпорт) — проверить, что не ломается (G7)
  - ⚠ Same-file: MainActivity.kt правит также SPEC 02 (SnackbarHost) — этот первый (installSplashScreen в самом начале onCreate), 02 после
  - Строки (label) — из ресурсов (i18n уже прошёл, H4); монохром-иконка (themed icons, Android 13+) — опционально, не обязательна
=== END SPEC ===

## Acceptance
```gherkin
Feature: Custom launcher icon and splash
  Covers epic ux-polish, SPEC 01.

  @ux-polish-01
  Scenario: The launcher shows a custom adaptive icon
    Given the app is installed
    When the user views the launcher
    Then the app shows its custom adaptive icon, not the system placeholder

  @ux-polish-01
  Scenario: A splash screen shows on cold start
    Given the app is cold-started
    When it launches
    Then a branded splash screen is shown before the first screen

  @ux-polish-01 @edge
  Scenario: Splash degrades gracefully on older APIs
    Given a device on API 26–30
    When the app launches
    Then the splash shows without crashing
```

## Gap / context
Иконка — системная заглушка @android:drawable/sym_def_app_icon (G1), splash отсутствует (G4): первое впечатление — «недоделанное приложение».

## Implementation links
- commit: (pending)
- files:  (pending)
