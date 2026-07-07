# Тактильная отдача (haptics)
Epic: ux-polish
Order: 07 of 08
Status: backlog
Depends-on: ux-polish-02, ux-polish-04
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: feature
PLATFORM: android
WHAT: Ключевые действия (удаление, сохранение, копирование приёма пищи) сопровождаются тактильной отдачей — взаимодействие ощущается отзывчивым.
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/today/TodayScreen.kt + today/TodayDesignedContent.kt — LocalHapticFeedback.current.performHapticFeedback(...) на удаление/сохранение/копирование блюда (delete/save/copy-сайты) (G15)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/weight/WeightScreen.kt — haptic на подтверждение удаления/сохранения (G15)
  - выбрать HapticFeedbackType (LongPress для деструктивных, TextHandleMove/иное для подтверждений) — согласованно (assumption: конкретный тип на усмотрение реализации)
TEST_TYPES: compose-ui
CONSTRAINTS:
  - Depends-on ux-polish-02 + ux-polish-04: delete/save-сайты меняются undo-SPEC-ом (02), а WeightScreen.kt/ProductSearchScreen.kt правит и IME-SPEC (04) — haptics ПОСЛЕДНИМИ, поверх стабильных сайтов (H5, same-file на WeightScreen)
  - LocalHapticFeedback — впервые в проекте (G15); в composable, не в VM
  - Не злоупотреблять: haptic только на значимые действия (удаление/сохранение/копирование), не на каждый тап
  - Тестируемость ограничена (haptic — сайд-эффект платформы): Compose-тест проверяет наличие вызова через тестовый HapticFeedback, либо помечает как ручную проверку в чеклисте
=== END SPEC ===

## Acceptance
```gherkin
Feature: Haptic feedback on key actions
  Covers epic ux-polish, SPEC 07.

  @ux-polish-07
  Scenario: Deleting an entry gives haptic feedback
    Given a food entry is shown
    When the user deletes it
    Then a haptic pulse accompanies the deletion

  @ux-polish-07
  Scenario: Saving a meal gives haptic feedback
    Given the user is saving a meal
    When the save completes
    Then a haptic pulse accompanies the save

  @ux-polish-07 @edge
  Scenario: Ordinary taps do not trigger haptics
    Given the user taps a non-significant control
    When the tap is handled
    Then no haptic pulse is emitted
```

## Gap / context
Haptics отсутствуют полностью (G15): деструктивные и подтверждающие действия не дают тактильного сигнала, приложение ощущается «плоским».

## Implementation links
- commit: (pending)
- files:  (pending)
