# Безопасный парс nav-аргументов ProductViewModel
Epic: data-safety
Order: 07 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: bugfix
PLATFORM: android
WHAT: Кривые или пустые аргументы маршрута product_search/{date}/{mealType} не роняют приложение — экран открывается с безопасными дефолтами (сегодня / завтрак).
LAYERS: presentation
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/product/ProductViewModel.kt:43-44 — date: runCatching { LocalDate.parse(...) }.getOrElse { LocalDate.now() } (G28)
  - app/src/main/java/com/k/shavrin/diethelper/presentation/screen/product/ProductViewModel.kt:46-48 — mealType: MealType.entries.firstOrNull { it.name == raw } ?: MealType.BREAKFAST вместо enumValueOf (G29)
  - app/src/test/java/com/k/shavrin/diethelper/presentation/screen/product/ProductViewModelTest.kt — кейсы: пустая строка, мусорная строка, отсутствующий аргумент → дефолты, без исключений
TEST_TYPES: unit
CONSTRAINTS:
  - Форматы аргументов создаются только Routes.productSearch (G30) и объявлены StringType без default (G31) — фактический источник кривых значений это deep-link/process-restore, лечим приёмник, НЕ меняем Routes/AppNavHost
  - LocalDate.now() здесь — временно: эпик testability вводит инжектируемый Clock и заменит вызов; оставить TODO-ссылку на эпик (TODO разрешён, FIXME запрещён detekt-ом G37); same-file clash с эпиком testability — data-safety выполняется раньше
  - Педагогический комментарий: почему fallback, а не крэш/финиш экрана (учебное обоснование выбора)
=== END SPEC ===

## Acceptance
```gherkin
Feature: Product search opens safely on malformed arguments
  Covers epic data-safety, SPEC 07.

  @data-safety-07
  Scenario: Valid arguments open the requested slot
    Given the user opens product search for date "2026-07-06" and meal "LUNCH"
    Then the search targets lunch on 2026-07-06

  @data-safety-07 @error
  Scenario Outline: Malformed arguments fall back to safe defaults
    Given the user opens product search for date "<date>" and meal "<meal>"
    Then the search opens without crashing
    And it targets breakfast on today's date

    Examples:
      | date       | meal    |
      |            |         |
      | not-a-date | DINNERX |
```

## Gap / context
enumValueOf на пустой строке кидает IllegalArgumentException, LocalDate.parse — DateTimeParseException (G28, G29): восстановление процесса или кривой deep-link роняют приложение на входе в экран поиска.

## Implementation links
- commit: (pending)
- files:  (pending)
