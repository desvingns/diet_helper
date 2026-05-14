# Diet Helper — Android Calorie Tracker

Learning project: Clean Architecture + Compose + Hilt + Room.
**Do not simplify or collapse architecture** — verbosity is intentional for educational purposes.

## Package
`com.k.shavrin.diethelper`

## Stack & Versions
- Kotlin 2.1.20, AGP 8.5.2, KSP 2.1.20-1.0.32
- Compose BOM 2024.09.03, Material3
- Hilt 2.55, hilt-navigation-compose 1.2.0
- Room 2.7.1, DataStore Preferences 1.1.1
- Coroutines 1.9.0, Lifecycle 2.8.6
- minSdk 26, targetSdk 35, JVM 17

## Architecture (Clean Architecture)
```
domain/
  model/          — pure Kotlin data classes (Product, FoodEntry, WeightEntry, SavedMeal, SavedMealItem, DailyGoals, DailySummary, HistoryItem, MealType, DayStatus)
  repository/     — interfaces (ProductRepository, FoodEntryRepository, WeightRepository, SavedMealRepository, GoalsRepository)
  usecase/        — one class per use case, grouped by feature
    savedmeal/    — GetSavedMealsUseCase, SaveMealUseCase, DeleteSavedMealUseCase, AddSavedMealEntriesUseCase
data/
  local/
    entity/       — Room entities (ProductEntity, FoodEntryEntity, FoodEntryWithProduct, WeightEntryEntity, SavedMealEntity, SavedMealItemEntity, SavedMealWithItems, SavedMealItemWithProduct)
    dao/          — DAOs (ProductDao, FoodEntryDao, WeightEntryDao, SavedMealDao)
    converter/    — Converters.kt: LocalDate → Long epochDay via TypeConverter
    DietHelperDatabase.kt (v2)
    GoalsDataSource.kt  — DataStore wrapper
    DatabaseSeeder.kt   — seed data on first launch
  mapper/         — entity ↔ domain mappers (SavedMealMapper)
  repository/     — *Impl classes (SavedMealRepositoryImpl)
di/               — Hilt modules (DatabaseModule, DataStoreModule, RepositoryModule)
presentation/
  navigation/     — Routes, BottomNavItem, AppNavHost
  screen/         — today, product, history, weight, settings (each: Screen + ViewModel + UiState)
  components/     — shared composables (DailySummaryCard)
  theme/          — Color, Type, Theme
  util/           — Format.kt, InMemoryMealClipboard.kt
```

## Key Technical Decisions
- `DailyGoals` stored in **DataStore Preferences**, not Room
- `LocalDate` → `Long` (epochDay) via Room `TypeConverter`
- `FoodEntryWithProduct` uses `@Transaction + @Relation` for reactive join
- `SavedMeal` → `SavedMealItem` via 1:N Room relation; queries reactive via `@Transaction`
- `InMemoryMealClipboard` transient (lost on app exit); no persistence overhead
- Single-activity app, Navigation Compose with bottom nav
- ViewModels injected via `hiltViewModel()`

## Build

```bash
# KSP code generation (run after changing Room/Hilt annotations)
./gradlew :app:kspDebugKotlin

# Full debug build
./gradlew :app:assembleDebug

# Unit tests
./gradlew :app:testDebugUnitTest

# Static analysis
./gradlew :app:detekt

# Screenshot tests (Roborazzi)
./gradlew :app:recordRoborazziDebug      # regenerate baselines
./gradlew :app:verifyRoborazziDebug      # compare against committed baselines
```

**JAVA_HOME** must point to a JDK 17+ runtime. Outside Android Studio, prefer its bundled JBR:

```bash
# Auto-detect Android Studio JBR (first match wins)
for c in "$HOME"/.jbr/jbr_jcef-17* /snap/android-studio/current/jbr /opt/android-studio/jbr; do
  [ -x "$c/bin/java" ] && export JAVA_HOME="$c" && export PATH="$JAVA_HOME/bin:$PATH" && break
done
```

Add the snippet to `~/.bashrc` (or equivalent) to persist it.

## Testing Stack
- JUnit 4, Turbine 1.1.0, kotlinx-coroutines-test 1.9.0
- Robolectric 4.13 (DAO + Compose UI tests on JVM)
- Roborazzi 1.25.0 (screenshot regression)
- **Fakes only — no mocking framework.** See `app/src/test/.../data/Fake*.kt`

## Screens & Navigation
| Route | Screen |
|-------|--------|
| `today` | Today (food diary for current day) |
| `products` | Product search |
| `add_product` | Add custom product |
| `history` | Calendar-based history list |
| `history_day/{date}` | Day detail from history |
| `weight` | Weight tracking chart |
| `settings` | Daily goals (calories, protein, fat, carbs) |
