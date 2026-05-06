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
  model/          — pure Kotlin data classes (Product, FoodEntry, WeightEntry, DailyGoals, DailySummary, HistoryItem, MealType, DayStatus)
  repository/     — interfaces (ProductRepository, FoodEntryRepository, WeightRepository, GoalsRepository)
  usecase/        — one class per use case, grouped by feature
data/
  local/
    entity/       — Room entities (ProductEntity, FoodEntryEntity, FoodEntryWithProduct, WeightEntryEntity)
    dao/          — DAOs (ProductDao, FoodEntryDao, WeightEntryDao)
    converter/    — Converters.kt: LocalDate → Long epochDay via TypeConverter
    DietHelperDatabase.kt
    GoalsDataSource.kt  — DataStore wrapper
    DatabaseSeeder.kt   — seed data on first launch
  mapper/         — entity ↔ domain mappers
  repository/     — *Impl classes
di/               — Hilt modules (DatabaseModule, DataStoreModule, RepositoryModule)
presentation/
  navigation/     — Routes, BottomNavItem, AppNavHost
  screen/         — today, product, history, weight, settings (each: Screen + ViewModel + UiState)
  components/     — shared composables (DailySummaryCard)
  theme/          — Color, Type, Theme
  util/           — Format.kt
```

## Key Technical Decisions
- `DailyGoals` stored in **DataStore Preferences**, not Room
- `LocalDate` → `Long` (epochDay) via Room `TypeConverter`
- `FoodEntryWithProduct` uses `@Transaction + @Relation` for reactive join
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
```

**JAVA_HOME** must point to `D:\For_work\AS\jbr` if running outside Android Studio.

PowerShell one-liner:
```powershell
$env:JAVA_HOME = "D:\For_work\AS\jbr"; $env:PATH = "$env:JAVA_HOME\bin;$env:PATH"; .\gradlew.bat :app:kspDebugKotlin
```

**Known issue — Gradle loopback error with JBR 21 on Windows:**
JBR 21 uses Unix domain sockets for internal NIO pipes; on some Windows configurations
this fails with `Unable to establish loopback connection`. Fix: redirect TEMP to a short path.

```powershell
$env:JAVA_HOME = "D:\For_work\AS\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixDomain.tmpDir=C:\tmp"
$env:TEMP = "C:\tmp"; $env:TMP = "C:\tmp"
New-Item -ItemType Directory -Path "C:\tmp" -Force | Out-Null
.\gradlew.bat :app:testDebugUnitTest --no-daemon
```

## Testing Stack
- JUnit 4, MockK 1.13.12, Turbine 1.1.0, kotlinx-coroutines-test

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
