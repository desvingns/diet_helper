# IOException-устойчивость GoalsDataSource (DataStore)
Epic: data-safety
Order: 04 of 08
Status: backlog
Depends-on: —
Date: 2026-07-06

## SPEC
=== SPEC ===
TASK: bugfix
PLATFORM: android
WHAT: Повреждение/недоступность файла DataStore не роняет приложение: чтение целей деградирует к дефолтам, ошибка записи логируется и пробрасывается по конвенции.
LAYERS: data
CHANGED_HINT:
  - app/src/main/java/com/k/shavrin/diethelper/data/local/GoalsDataSource.kt:20-30 — перед map добавить .catch { e -> if (e is IOException) emit(emptyPreferences()) else throw e } — штатный DataStore-паттерн; map после catch отдаёт дефолтные DailyGoals (G15; D1)
  - app/src/main/java/com/k/shavrin/diethelper/data/local/GoalsDataSource.kt:32-42 — saveGoals: try/catch(IOException) — залогировать и rethrow; вызывающий VM решает (по O1 Settings пока молчит) (G16; D1, O1)
  - app/src/test/java/com/k/shavrin/diethelper/data/repository/GoalsRepositoryImplTest.kt — дополнить кейсом «flow переживает IOException и отдаёт дефолты» (реальный GoalsDataSource с падающим DataStore-двойником — fakes-only)
TEST_TYPES: unit
CONSTRAINTS:
  - Флаг isSeeded/setSeeded в этом же файле (G6) трогает SPEC 02 семантически, но строки не пересекаются (44-60 vs 20-42); если 02 уже смёржен — просто rebase, конфликтов не ожидается
  - Педагогический комментарий: почему catch стоит ДО map и почему ловим только IOException (D1)
  - Не вводить Result-обёртки (locked decision D1); интерфейс GoalsRepository не меняется
=== END SPEC ===

## Acceptance
```gherkin
Feature: Goals storage survives IO failures
  Covers epic data-safety, SPEC 04.

  @data-safety-04 @error
  Scenario: Corrupted preferences fall back to default goals
    Given the goals storage file cannot be read
    When the user opens the app
    Then the daily goals show the default values
    And the app does not crash

  @data-safety-04
  Scenario: Successfully saved goals are re-emitted to observers
    Given the user is on the settings screen
    When the user saves calories "2000" as the daily goal
    Then every screen observing goals sees the value 2000

  @data-safety-04 @error
  Scenario: A failed write does not corrupt current goals
    Given saving to the goals storage fails
    When the user attempts to save new goals
    Then the previously stored goals remain in effect
```

## Gap / context
`dataStore.data` без catch (G15): первый IOException (диск, повреждение) роняет всех подписчиков flow целей — Today, Settings, Stats.

## Implementation links
- commit: (pending)
- files:  (pending)
