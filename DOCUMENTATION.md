# Diet Helper — Project Documentation

> **Maintained by:** `dh-docs` agent after every feature or bugfix.
> **Purpose:** Product features, user flows, domain concepts, architecture decisions.
> For developer setup and tech stack → see `CLAUDE.md`.

---

## App Overview

Android calorie tracker. The user logs food entries per day, tracks macronutrients (calories, protein, fat, carbs) against daily goals, and monitors body weight over time.

Learning project — architecture is intentionally verbose (Clean Architecture + Hilt + Room).

---

## Domain Model

| Entity | Fields | Storage |
|--------|--------|---------|
| `Product` | id, name, calories, protein, fat, carbs (per 100g), isFavorite | Room (`products`) |
| `FoodEntry` | id, productId, date (LocalDate), weightGrams, mealType | Room (`food_entries`) |
| `WeightEntry` | id, date (LocalDate), weightKg | Room (`weight_entries`) |
| `DailyGoals` | calories, proteinMin/Max, fatMin/Max, carbsMin/Max | DataStore Preferences |
| `DailySummary` | totalCalories, totalProtein, totalFat, totalCarbs | Computed (not stored) |
| `MealType` | BREAKFAST, LUNCH, DINNER, SNACK | Enum |
| `HistoryItem` | date, summary, entryCount | Computed from FoodEntry |
| `DayStatus` | FUTURE, GRAY_LOGGED, GREEN, YELLOW, RED | Computed enum — diet quality for one day |

**Key relationship:** `FoodEntry` → `Product` via `productId` (FK with CASCADE DELETE).
`FoodEntryWithProduct` is a Room `@Relation` join used for reactive UI queries.

---

## Screens

### Today (`route: today`)

**Purpose:** Food diary for the current day.

**Key behaviours:**
- Shows `DailySummaryCard` at the top with total calories and macro progress bars.
- Lists all food entries for today grouped implicitly (no explicit meal grouping UI).
- FAB opens the Products screen to add a new entry.
- Calories/macros turn red when they exceed the daily goal.
- Empty state shown when no entries exist for today.
- `WeekDateHeader` shows the current day name + date and a row of 7 `WeekDayCircle` items (Mon–Sun) coloured by `DayStatus`.
- `StreakRow` shows consecutive logged days counted backward from today.
- ViewModel exposes `goToDate(date)` and `goToToday()` to change the displayed date.

**UiState fields (Success):** `date`, `sections`, `sectionCalories/Protein/Fat/Carbs`, `summary`, `goals`, `weekStatuses`, `streak`

---

### Products (`route: products`)

**Purpose:** Search and select a product to log as a food entry.

**Key behaviours:**
- Search bar filters products by name (case-insensitive, COLLATE NOCASE in DB).
- Results sorted: favorites first, then alphabetically.
- Tapping a product opens a bottom sheet / dialog to input weight (grams) and meal type.
- Star icon toggles `isFavorite` immediately (no save button needed).
- FAB navigates to Add Product screen.

**UiState fields:** `query`, `products`, `isLoading`

---

### Add Product (`route: add_product`)

**Purpose:** Create a custom product not in the database.

**Key behaviours:**
- Fields: name, calories, protein, fat, carbs (all per 100g).
- Validation: all numeric fields must be non-negative numbers.
- On save: product is inserted into DB and user is navigated back.

**UiState fields:** `name`, `calories`, `protein`, `fat`, `carbs`, validation error fields, `isSaved`

---

### History (`route: history`)

**Purpose:** Calendar-based overview of past food diary days.

**Key behaviours:**
- Shows dates that have at least one food entry (derived from `getDistinctDatesDescending`).
- Tapping a date navigates to `history_day/{date}`.

**UiState fields:** `dates`, `isLoading`

---

### History Day Detail (`route: history_day/{date}`)

**Purpose:** Show all food entries for a specific past date.

**Key behaviours:**
- Same layout as Today but read-only (date is fixed, no FAB).
- Shows `DailySummaryCard` for the selected date.
- Date passed as ISO-8601 string in the route argument.

**UiState fields:** `date`, `entries`, `summary`, `goals`, `isLoading`

---

### Weight (`route: weight`)

**Purpose:** Log and visualise body weight over time.

**Key behaviours:**
- Line chart showing weight history sorted by date descending.
- Input field + Save button to log today's weight.
- If an entry for today already exists — upsert (update, not duplicate).
- Empty state when no entries exist.

**UiState fields:** `entries`, `todayWeight`, `isLoading`, `isSaved`

---

### Settings (`route: settings`)

**Purpose:** Configure daily nutrition goals.

**Key behaviours:**
- Fields: calories (target), protein min/max, fat min/max, carbs min/max.
- Validation: min must not exceed max; all values must be positive numbers.
- On save: goals written to DataStore Preferences.
- `justSaved = true` for ~2 seconds to show success banner ("Сохранено").
- Validation errors shown inline under each field.

**UiState fields:** `calories`, `proteinMin/Max`, `fatMin/Max`, `carbsMin/Max`,
  error fields (`caloriesError`, etc.), `isLoading`, `justSaved`

---

## User Flows

### Log a meal
1. Open **Today** screen.
2. Tap FAB → navigate to **Products**.
3. Search or scroll to find a product.
4. Tap product → input weight (grams) + select meal type → confirm.
5. Return to Today — new entry and updated summary are visible.

### Add a custom product
1. Open **Products** screen → tap FAB → **Add Product**.
2. Fill in name and macros per 100g → save.
3. Product appears in the list, available for logging.

### Track weight
1. Open **Weight** screen.
2. Enter today's weight → tap Save.
3. Chart updates with the new data point.

### Configure goals
1. Open **Settings** screen.
2. Adjust calorie target and macro ranges → tap Save.
3. "Сохранено" banner confirms. Today's summary card reflects new goals immediately.

---

## Architecture Decisions Log

| Date | Decision | Reason |
|------|----------|--------|
| Initial | `DailyGoals` in DataStore, not Room | Goals are a single shared record; DataStore is simpler and avoids LiveData/Flow overhead for preferences |
| Initial | `LocalDate` → `Long` (epochDay) via TypeConverter | Room doesn't support Java time natively; epochDay is stable and sortable |
| Initial | `FoodEntryWithProduct` via `@Transaction + @Relation` | Avoids N+1 queries; Room generates efficient JOIN |
| Initial | Single-activity + Navigation Compose | Modern Android approach; avoids fragment backstack complexity |
| Initial | Fakes only in tests (no MockK) | Forces realistic test doubles; caught real bugs MockK would have hidden |
| Iter 2 | Robolectric for DAO + Compose UI tests | JVM-based testing without emulator; faster CI, same confidence |
| Iter 2 | `@Config(application=Application::class)` on DAO tests | Prevents `DietHelperApplication.DatabaseSeeder` from running on Robolectric, which caused SQLite pointer conflicts |
| Iter 2 | `SettingsContent` extracted from `SettingsScreen` | `hiltViewModel()` inside a composable blocks direct Compose testing; stateless Content composable is testable without Hilt |
| Iter 2 | Roborazzi snapshots in `src/test/snapshots/` | Default `build/` output is git-ignored; committing baselines enables regression detection in CI |
| Iter 2 | JaCoCo via `enableUnitTestCoverage = true` + custom task | AGP 8.x no longer exposes exec files automatically; custom task points to correct path |
| Iter 4 | `DayStatus` computed in use-case layer, not ViewModel | Keeps presentation layer free of calorie-threshold logic; thresholds are testable in isolation |
| Iter 4 | `GetStreakUseCase` walks 90 days back at most | Practical upper bound avoids unbounded DB scan; streak display does not need longer history |

---

## Feature Changelog

### Iteration 1 — Core App
- Today screen with food diary and `DailySummaryCard`
- Products screen with search and favorites
- Add Product screen
- History screen with calendar dates
- History Day Detail screen
- Weight screen with chart
- Settings screen with goal configuration
- Hilt + Room + DataStore wiring
- 149 unit tests (ViewModel, UseCase, Repository, Converters)

### Iteration 2 — Test Automation Infrastructure
- JaCoCo coverage reporting (`jacocoUnitTestReport` Gradle task)
- Detekt static analysis (`config/detekt/detekt.yml`)
- GitHub Actions CI (unit tests + coverage + lint + detekt + build)
- Pre-commit hook (`scripts/pre-commit`)
- Room DAO tests via Robolectric (31 tests: ProductDao, FoodEntryDao, WeightEntryDao)
- Compose UI tests via Robolectric (12 tests: DailySummaryCard, SettingsContent)
- Roborazzi screenshot tests (8 PNG baselines: DailySummaryCard + SettingsContent, light/dark)
- `SettingsContent` extracted as public composable for testability

### Iteration 3 — Multi-Agent System
- `/dh` orchestrator command replaces `new_dh` skill
- `dh-developer`, `dh-tester`, `dh-runner`, `dh-docs` sub-agents
- `DOCUMENTATION.md` created as live product documentation

### Iteration 4 — Week Header and Streak
- feat: `DayStatus` enum (FUTURE / GRAY_LOGGED / GREEN / YELLOW / RED) added to domain model
- feat: `GetWeekDayStatusesUseCase` — reactive `Flow<List<Pair<LocalDate, DayStatus>>>` for Mon–Sun of the displayed week
- feat: `GetStreakUseCase` — reactive `Flow<Int>` counting consecutive logged days (up to 90 days back)
- feat: `WeekDateHeader` + `WeekDayCircle` + `StreakRow` composables replace the plain date header on Today screen
- feat: `formatWeekDateHeader` added to `Format.kt` (Russian day-of-week + genitive month)
