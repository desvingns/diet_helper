<<<<<<< Updated upstream
# CLAUDE.md
This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

# Diet Helper — Android Calorie Tracker
=======
# Diet Helper
>>>>>>> Stashed changes

Package: `com.k.shavrin.diethelper` | minSdk 26, targetSdk 35, JVM 17

<<<<<<< Updated upstream
## Project State Files

Three project-root markdown files track state and history (all committed):

- `STATE.md` — **live state**, refreshed by `dh-docs` after every `/dh` run. Current iteration, last completed work, recent commits, up-next.
- `ROADMAP.md` — **planned work**, ordered by iteration. Edit manually.
- `DOCUMENTATION.md` — **history**: product features, user flows, architecture decisions log.

Cross-session memory lives in `~/.claude/projects/C--Pet-diet-helper/memory/`; `MEMORY.md` is its index and is auto-loaded into every session.

## Package
`com.k.shavrin.diethelper`
=======
## Stack
Kotlin 2.1.20 · AGP 8.5.2 · KSP 2.1.20-1.0.32 · Compose BOM 2024.09.03
Material3 · Hilt 2.55 · Room 2.7.1 · DataStore 1.1.1 · Coroutines 1.9.0 · Lifecycle 2.8.6
>>>>>>> Stashed changes

## Layers
`domain/` (model, repository, usecase) → `data/` (local, mapper, repository) → `di/` → `presentation/` (navigation, screen, components, theme, util)

<<<<<<< Updated upstream
## Architecture (Clean Architecture)
```
domain/
  model/          — pure Kotlin data classes:
                    Product, FoodEntry, WeightEntry, SavedMeal, SavedMealItem,
                    DailyGoals, DailySummary, HistoryItem, MealType, DayStatus,
                    StatsDayItem, ExportConfig, ExportMode, ReportData
  repository/     — interfaces (ProductRepository, FoodEntryRepository, WeightRepository,
                    SavedMealRepository, GoalsRepository, ReportRenderer)
  usecase/        — one class per use case, grouped by feature subfolder:
    foodentry/    — AddFoodEntryUseCase, UpdateFoodEntryUseCase, DeleteFoodEntryUseCase,
                    GetFoodEntriesForDayUseCase, GetDailySummaryUseCase,
                    GetHistoryUseCase, GetWeekDayStatusesUseCase,
                    CopyFoodEntryToDayUseCase, GetStreakUseCase
    product/      — AddProductUseCase, GetAllProductsUseCase, SearchProductsUseCase,
                    ToggleFavoriteUseCase
    goals/        — GetDailyGoalsUseCase, SaveDailyGoalsUseCase
    weight/       — GetAllWeightEntriesUseCase, UpsertWeightEntryUseCase,
                    DeleteWeightEntryUseCase
    savedmeal/    — GetSavedMealsUseCase, SaveMealUseCase, DeleteSavedMealUseCase,
                    AddSavedMealEntriesUseCase
    stats/        — GetStatsRangeUseCase
    export/       — ExportReportUseCase
data/
  pdf/            — PdfReportRenderer (impl of ReportRenderer), PdfReportLayout, PdfPageContext
  local/
    entity/       — Room entities (ProductEntity, FoodEntryEntity, FoodEntryWithProduct,
                    WeightEntryEntity, SavedMealEntity, SavedMealItemEntity,
                    SavedMealWithItems, SavedMealItemWithProduct)
    dao/          — DAOs (ProductDao, FoodEntryDao, WeightEntryDao, SavedMealDao)
    converter/    — Converters.kt: LocalDate → Long epochDay via TypeConverter
    DietHelperDatabase.kt (v2)
    GoalsDataSource.kt  — DataStore wrapper
    DatabaseSeeder.kt   — seed data on first launch
  mapper/         — entity ↔ domain mappers:
                    ProductMapper, FoodEntryMapper, WeightEntryMapper, SavedMealMapper
  repository/     — *Impl classes: ProductRepositoryImpl, FoodEntryRepositoryImpl,
                    WeightRepositoryImpl, GoalsRepositoryImpl, SavedMealRepositoryImpl
di/               — Hilt modules (DatabaseModule, DataStoreModule, RepositoryModule, PdfModule)
presentation/
  navigation/     — Routes, BottomNavItem, AppNavHost
  screen/         — today, product, history (HistoryScreen + HistoryViewModel +
                    HistoryDayScreen + HistoryDayViewModel), weight, settings, stats
                    (each screen: Screen + ViewModel + UiState)
  components/     — shared composables (DailySummaryCard)
  theme/          — Color, Type, Theme
  util/           — Format.kt, InMemoryMealClipboard.kt, MacroColorUtil.kt
  Previews.kt     — standalone @Preview composables (not tied to a single screen)
```

## Key Technical Decisions
- `DailyGoals` stored in **DataStore Preferences**, not Room
- `LocalDate` → `Long` (epochDay) via Room `TypeConverter`
- `FoodEntryWithProduct` uses `@Transaction + @Relation` for reactive join
- `SavedMeal` → `SavedMealItem` via 1:N Room relation; queries reactive via `@Transaction`
- `InMemoryMealClipboard` transient (lost on app exit); no persistence overhead
- Single-activity app, Navigation Compose with bottom nav
- ViewModels injected via `hiltViewModel()`
- `FoodEntryRepository.getEntriesForDates(List<LocalDate>)` is the multi-day query used by stats and streak; it returns a single reactive `Flow` over the union of those dates
- Streak (shown on Today screen): consecutive days where calories ≥ 30% of daily goal, looking back up to 89 days; today counts if it already meets the threshold
- `MacroColorUtil` provides colour interpolation (green → red) for macro progress indicators; it is `internal` and not exported outside the `util` package
- Product favourites tracked by `isFavorite: Boolean` on `ProductEntity`; toggled via `ToggleFavoriteUseCase`
- `history` screen has **two** ViewModels: `HistoryViewModel` (calendar/list) and `HistoryDayViewModel` (single-day detail, receives date via SavedStateHandle)
- PDF export: `ReportRenderer` (domain) returns a `String` path — no Android types in domain; `ExportViewModel` converts the path to a share `Uri`
- `ExportViewModel` builds the share `Uri` via `Uri.Builder` instead of `FileProvider.getUriForFile` to bypass a Windows-Robolectric path-matching quirk; runtime behaviour is identical

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

**JAVA_HOME** must point to a JDK 17+ runtime. Outside Android Studio, prefer its bundled JBR.
The snippet below works on both Linux (Ubuntu) and Windows under Git Bash:

```bash
# Auto-detect Android Studio JBR (first match wins). Cross-platform.
for c in \
    "$HOME"/.jbr/jbr_jcef-17* \
    /snap/android-studio/current/jbr \
    /opt/android-studio/jbr \
    "/c/Program Files/Android/Android Studio/jbr" \
    "$LOCALAPPDATA/Programs/Android Studio/jbr"; do
  if [ -x "$c/bin/java" ] || [ -x "$c/bin/java.exe" ]; then
    export JAVA_HOME="$c"
    export PATH="$JAVA_HOME/bin:$PATH"
    break
  fi
done
```

Add the snippet to `~/.bashrc` on Ubuntu, or to `~/.bash_profile` in Git Bash on Windows, to
persist it. The `/dh` pipeline runs all shell commands through the `Bash` tool (Git Bash on
Windows), so no PowerShell-specific setup is required.

## Testing Stack
- JUnit 4, Turbine 1.1.0, kotlinx-coroutines-test 1.9.0
- Robolectric 4.13 (DAO + Compose UI tests on JVM)
- Roborazzi 1.25.0 (screenshot regression)
- **Fakes only — no mocking framework.** See `app/src/test/.../data/Fake*.kt`

## Screens & Navigation
| Route | Screen |
|-------|--------|
| `today` | Today (food diary for current day, shows streak) |
| `product_search/{date}/{mealType}` | Product search — launched from Today/HistoryDay with target date + meal slot |
| `add_product?name={name}` | Add custom product (optional pre-filled name query param) |
| `history` | Calendar-based history list |
| `history_day/{date}` | Day detail from history |
| `weight` | Weight tracking chart |
| `settings` | Daily goals (calories, protein, fat, carbs) |
| `statistics` | Macro/calorie bar charts over a selectable date range |
| `export` | PDF export — date range + mode (DETAILED / SUMMARY_ONLY) + optional stats; shares via FileProvider |

Route constants live in `Routes` object; helper fns: `Routes.productSearch(date, mealType)`, `Routes.addProduct(name)`, `Routes.historyDay(date)`.
=======
## Routes
| Route | Screen |
|-------|--------|
| `today` | Today (food diary) |
| `products` | Product search |
| `add_product` | Add custom product |
| `history` | Calendar history |
| `history_day/{date}` | Day detail |
| `weight` | Weight chart |
| `settings` | Daily goals |

## Build (PowerShell)
```powershell
$env:JAVA_HOME = "D:\For_work\AS\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :app:kspDebugKotlin           # KSP codegen
.\gradlew.bat :app:assembleDebug            # Debug APK
.\gradlew.bat :app:testDebugUnitTest        # Unit tests
.\gradlew.bat :app:detekt                   # Linting
```

## JBR Loopback Fix (Windows + JBR 21)
If `Unable to establish loopback connection`:
```powershell
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixDomain.tmpDir=C:\tmp"
$env:TEMP = "C:\tmp"; $env:TMP = "C:\tmp"
New-Item -ItemType Directory -Path "C:\tmp" -Force | Out-Null
```

## Architecture & Decisions
Authoritative source: see `DOCUMENTATION.md` (Domain Model, Architecture Decisions Log, Feature Changelog).
>>>>>>> Stashed changes
