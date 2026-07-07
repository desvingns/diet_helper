# Переходы навигации (enter/exit)
Epic: ux-polish
Order: 06 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Переходы между экранами получают согласованную анимацию (enter/exit) вместо дефолтной — навигация ощущается плавной и намеренной.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/navigation/AppNavHost.kt:27-114 — задать enterTransition/exitTransition/popEnter/popExit на NavHost (или per-composable): напр. fadeIn+slideInHorizontally / fadeOut; согласованные длительности (G14)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Только базовые enter/exit (G14, out-of-scope: shared-element)
  - Независимый SPEC — трогает только AppNavHost.kt
  - Переходы не должны ломать back-stack/launchSingleTop/restoreState (существующее поведение bottom-nav)
  - Длительности умеренные (не задерживать навигацию); свериться с Material motion durations
=== END SPEC ===

## Acceptance
```gherkin
Feature: Animated screen transitions
  Covers epic ux-polish, SPEC 06.

  @ux-polish-06
  Scenario: Navigating forward animates the transition
    Given the user is on a root screen
    When they navigate to a detail screen
    Then the transition is animated rather than an instant cut

  @ux-polish-06
  Scenario: Back navigation animates in reverse
    Given the user is on a detail screen
    When they navigate back
    Then the reverse transition is animated

  @ux-polish-06 @edge
  Scenario: Bottom-nav switching preserves state
    Given the user switches between bottom-nav tabs
    When a tab is reselected
    Then its previous state is restored without a jarring cut
```

## Gap / context
Nav-переходы дефолтные (G14): смена экранов выглядит резким «переключением», без ощущения продуманного движения.

## Implementation links
- commit: (pending)
- files:  (pending)
