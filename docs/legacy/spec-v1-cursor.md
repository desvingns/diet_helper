# Diet Helper — Complete Project Recreation Prompt

> **Target**: Recreate this Android calorie tracker on a new Windows machine using Cursor + Claude Sonnet.
> **Runtime**: Cursor IDE with Sonnet model (Cursor Composer / Agent mode). No Claude Code CLI required.
> **Result**: Working app + all 47 tests green + the `/dh` workflow ported to Cursor rules.

---

## How to Use This File

### Step 0 — Bootstrap Gradle Wrapper (required once)

Cursor AI cannot create binary files (`gradlew.bat`, `gradle-wrapper.jar`).
Use **Option A** — it works even if you have never installed Gradle separately.

> ⚠️ **`gradle` is not recognized** — this just means Gradle is not on PATH. That is fine.
> Do NOT install Gradle manually. Use Android Studio instead (Option A).

**Option A (recommended) — Create a blank project in Android Studio**:

> ⚠️ **IMPORTANT — Save location must match the folder you open in Cursor.**
> If Cursor is already open at `C:\diet_dnevnik`, point Android Studio to the **same** folder.
> Android Studio will ask "Directory is not empty, continue?" — answer **Yes**.

1. Open **Android Studio** → click **New Project**
2. Choose **Empty Activity** → click **Next**
3. Fill in:
   - **Name**: `DietHelper`
   - **Package name**: `com.k.shavrin.diethelper`
   - **Save location**: **the same folder you opened in Cursor** (e.g. `C:\diet_dnevnik`)
   - **Language**: `Kotlin`
   - **Minimum SDK**: `API 26 (Android 8.0)`
4. Click **Finish** and wait for Gradle sync to complete (~2 min)
5. Close Android Studio
6. Go back to Cursor and say: *"The Gradle wrapper is now present. Continue from Phase 1 verification."*

You now have `gradlew.bat`, `gradlew`, `gradle/wrapper/gradle-wrapper.jar` in the folder.
All other source files will be replaced / overwritten by Cursor AI — that is expected.

**Option B — Android Studio was created in a different folder (copy the 3 files)**:

```powershell
# Replace with the actual path of the AS-created project
$asProject = "C:\Users\$env:USERNAME\AndroidStudioProjects\DietHelper"
$target    = "C:\diet_dnevnik"   # <-- the folder Cursor is open in

Copy-Item "$asProject\gradlew.bat"                          "$target\gradlew.bat"
Copy-Item "$asProject\gradlew"                              "$target\gradlew"
Copy-Item "$asProject\gradle\wrapper\gradle-wrapper.jar"    "$target\gradle\wrapper\gradle-wrapper.jar"

# Verify (both must return True)
Test-Path "$target\gradlew.bat"
Test-Path "$target\gradle\wrapper\gradle-wrapper.jar"
```

**Option C — Download directly (no Android Studio project needed)**:

```powershell
Set-Location C:\diet_dnevnik
New-Item -ItemType Directory -Path "gradle\wrapper" -Force | Out-Null

Invoke-WebRequest -Uri "https://github.com/gradle/gradle/raw/v8.9.0/gradle/wrapper/gradle-wrapper.jar" -OutFile "gradle\wrapper\gradle-wrapper.jar"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradlew.bat" -OutFile "gradlew.bat"
Invoke-WebRequest -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.9.0/gradlew"     -OutFile "gradlew"

Test-Path "gradlew.bat"
Test-Path "gradle\wrapper\gradle-wrapper.jar"
```

**Option D** — only if standalone Gradle is already on PATH:
```powershell
gradle wrapper --gradle-version 8.9
```

### Step 1 — Open in Cursor

1. Open the project folder (containing `gradlew.bat`) in Cursor.
2. Copy this file (`PROMPT.md`) into the project root if not already there.
3. Open **Cursor Composer** (Cmd/Ctrl+Shift+I) in **Agent mode**.

### Step 2 — Run the prompt

Paste this instruction into Cursor Composer:

> "Read `PROMPT.md` in full. Then:
> 1. Create all 6 Cursor rules files from Part 10 into `.cursor/rules/`
> 2. Create `CLAUDE.md` from Part 9
> 3. Execute the Phase-by-Phase Build Plan from Part 12, starting from Phase 1"

4. Approve each terminal command when Cursor asks (Gradle build steps).
5. After all phases complete, run the verification from Part 13.

**After setup**, to add features: open Cursor Composer and type `@dh-workflow --feature <description>`

---

## Progress Tracker

> **How to use this section**
> - Mark each checkbox `[x]` when the step is verified complete.
> - If your free-model token limit runs out mid-session, copy this whole section (with your checked boxes) and paste it together with the **Resumption Prompt** (at the bottom of this section) into a new model.
> - The new model will read the full prompt, see which phases are done, and continue from the first unchecked phase.
> - **Never skip verification** — a phase is only `[x]` after the listed Gradle command succeeds.

---

### Phase 1 — Scaffolding *(Gradle + Theme + Entry Point)*

**Goal**: Project syncs in Gradle, empty screen launches on device/emulator.

Files to create:
- [ ] `settings.gradle.kts`
- [ ] `build.gradle.kts` *(root)*
- [ ] `gradle/libs.versions.toml`
- [ ] `gradle/wrapper/gradle-wrapper.properties`
- [ ] `app/build.gradle.kts`
- [ ] `app/src/main/AndroidManifest.xml`
- [ ] `app/src/main/res/values/strings.xml`
- [ ] `app/src/main/res/values/themes.xml`
- [ ] `config/detekt/detekt.yml`
- [ ] `CLAUDE.md`
- [ ] `.cursor/rules/project-context.mdc`
- [ ] `.cursor/rules/dh-workflow.mdc`
- [ ] `.cursor/rules/dh-developer.mdc`
- [ ] `.cursor/rules/dh-tester.mdc`
- [ ] `.cursor/rules/dh-runner.mdc`
- [ ] `.cursor/rules/dh-docs.mdc`
- [ ] `DietHelperApplication.kt` *(@HiltAndroidApp)*
- [ ] `MainActivity.kt` *(@AndroidEntryPoint, empty Scaffold)*
- [ ] `presentation/theme/Color.kt`
- [ ] `presentation/theme/Type.kt`
- [ ] `presentation/theme/Theme.kt`

Verification:
- [ ] `.\gradlew.bat :app:assembleDebug --no-daemon` → **BUILD SUCCESSFUL**

---

### Phase 2 — Domain Layer *(pure Kotlin, zero Android imports)*

**Goal**: All domain models, repository interfaces, and use cases compile without errors.

Files to create:
- [ ] `domain/model/MealType.kt`
- [ ] `domain/model/DayStatus.kt`
- [ ] `domain/model/Product.kt`
- [ ] `domain/model/FoodEntry.kt`
- [ ] `domain/model/DailyGoals.kt` *(with DEFAULT companion)*
- [ ] `domain/model/DailySummary.kt` *(with EMPTY companion)*
- [ ] `domain/model/WeightEntry.kt`
- [ ] `domain/model/HistoryItem.kt`
- [ ] `domain/model/SavedMeal.kt`
- [ ] `domain/model/SavedMealItem.kt`
- [ ] `domain/repository/ProductRepository.kt`
- [ ] `domain/repository/FoodEntryRepository.kt`
- [ ] `domain/repository/WeightRepository.kt`
- [ ] `domain/repository/GoalsRepository.kt`
- [ ] `domain/repository/SavedMealRepository.kt`
- [ ] `domain/usecase/product/GetAllProductsUseCase.kt`
- [ ] `domain/usecase/product/SearchProductsUseCase.kt`
- [ ] `domain/usecase/product/AddProductUseCase.kt`
- [ ] `domain/usecase/product/ToggleFavoriteUseCase.kt`
- [ ] `domain/usecase/foodentry/GetFoodEntriesForDayUseCase.kt`
- [ ] `domain/usecase/foodentry/AddFoodEntryUseCase.kt`
- [ ] `domain/usecase/foodentry/UpdateFoodEntryUseCase.kt`
- [ ] `domain/usecase/foodentry/DeleteFoodEntryUseCase.kt`
- [ ] `domain/usecase/foodentry/CopyFoodEntryToDayUseCase.kt`
- [ ] `domain/usecase/foodentry/GetDailySummaryUseCase.kt` *(with toSummary() companion)*
- [ ] `domain/usecase/foodentry/GetHistoryUseCase.kt` *(flatMapLatest on distinct dates)*
- [ ] `domain/usecase/foodentry/GetStreakUseCase.kt` *(last 90 days, 30% calorie threshold)*
- [ ] `domain/usecase/foodentry/GetWeekDayStatusesUseCase.kt` *(computeDayStatus companion)*
- [ ] `domain/usecase/goals/GetDailyGoalsUseCase.kt`
- [ ] `domain/usecase/goals/SaveDailyGoalsUseCase.kt`
- [ ] `domain/usecase/weight/GetAllWeightEntriesUseCase.kt`
- [ ] `domain/usecase/weight/UpsertWeightEntryUseCase.kt`
- [ ] `domain/usecase/weight/DeleteWeightEntryUseCase.kt`
- [ ] `domain/usecase/savedmeal/GetSavedMealsUseCase.kt`
- [ ] `domain/usecase/savedmeal/SaveMealUseCase.kt`
- [ ] `domain/usecase/savedmeal/DeleteSavedMealUseCase.kt`
- [ ] `domain/usecase/savedmeal/AddSavedMealEntriesUseCase.kt`

Verification:
- [ ] `.\gradlew.bat :app:compileDebugKotlin --no-daemon` → **BUILD SUCCESSFUL** *(no domain errors)*

---

### Phase 3 — Data Layer: Room *(entities, DAOs, TypeConverters, mappers)*

**Goal**: KSP generates Room code without errors. All entities and DAOs compile.

Files to create:
- [ ] `data/local/converter/Converters.kt` *(LocalDate ↔ Long via epochDay)*
- [ ] `data/local/entity/ProductEntity.kt`
- [ ] `data/local/entity/FoodEntryEntity.kt` *(FK → products.id ON DELETE CASCADE)*
- [ ] `data/local/entity/FoodEntryWithProduct.kt` *(@Transaction + @Relation)*
- [ ] `data/local/entity/WeightEntryEntity.kt` *(unique index on date)*
- [ ] `data/local/entity/SavedMealEntity.kt`
- [ ] `data/local/entity/SavedMealItemEntity.kt` *(FK → saved_meals + products)*
- [ ] `data/local/entity/SavedMealItemWithProduct.kt`
- [ ] `data/local/entity/SavedMealWithItems.kt`
- [ ] `data/local/dao/ProductDao.kt`
- [ ] `data/local/dao/FoodEntryDao.kt`
- [ ] `data/local/dao/WeightEntryDao.kt` *(@Upsert)*
- [ ] `data/local/dao/SavedMealDao.kt`
- [ ] `data/local/DietHelperDatabase.kt` *(version=2, MIGRATION_1_2)*
- [ ] `data/mapper/ProductMapper.kt`
- [ ] `data/mapper/FoodEntryMapper.kt`
- [ ] `data/mapper/WeightEntryMapper.kt`
- [ ] `data/mapper/SavedMealMapper.kt`

Verification:
- [ ] `.\gradlew.bat :app:kspDebugKotlin --no-daemon` → **BUILD SUCCESSFUL** *(Room schema generated)*

---

### Phase 4 — Data Layer: DataStore + Repository Implementations

**Goal**: All 5 repository implementations compile. DataStore wraps DailyGoals correctly.

Files to create:
- [ ] `data/local/GoalsDataSource.kt` *(7 float keys + is_seeded boolean key)*
- [ ] `data/repository/ProductRepositoryImpl.kt`
- [ ] `data/repository/FoodEntryRepositoryImpl.kt`
- [ ] `data/repository/WeightRepositoryImpl.kt`
- [ ] `data/repository/GoalsRepositoryImpl.kt`
- [ ] `data/repository/SavedMealRepositoryImpl.kt` *(@Singleton, upsert by name)*

Verification:
- [ ] `.\gradlew.bat :app:compileDebugKotlin --no-daemon` → **BUILD SUCCESSFUL** *(no data layer errors)*

---

### Phase 5 — Hilt DI *(Dependency Injection graph)*

**Goal**: Hilt component is generated successfully. App assembles.

Files to create:
- [ ] `di/DatabaseModule.kt` *(provides DB + 4 DAOs; addMigrations + fallbackToDestructiveMigration)*
- [ ] `di/DataStoreModule.kt` *(provides DataStore\<Preferences\>)*
- [ ] `di/RepositoryModule.kt` *(@Binds for all 5 repositories)*

Verification:
- [ ] `.\gradlew.bat :app:assembleDebug --no-daemon` → **BUILD SUCCESSFUL** *(Hilt component generated)*

---

### Phase 6 — Navigation + Shell UI

**Goal**: App launches with 4 bottom tabs switching without crash.

Files to create:
- [ ] `presentation/navigation/Routes.kt` *(7 routes with helper functions)*
- [ ] `presentation/navigation/BottomNavItem.kt` *(Today / History / Weight / Settings)*
- [ ] `presentation/navigation/AppNavHost.kt` *(NavHost with all 7 destinations)*
- [ ] `MainActivity.kt` *(updated: Scaffold + NavigationBar + AppNavHost)*
- [ ] `presentation/util/InMemoryMealClipboard.kt` *(@Singleton, ClipboardSnapshot data class)*
- [ ] Placeholder composables for all 5 screens

Verification:
- [ ] `.\gradlew.bat :app:assembleDebug --no-daemon` → **BUILD SUCCESSFUL**
- [ ] App launches on device/emulator, 4 tabs switch without crash

---

### Phase 7 — Today + Product Screens *(core food-logging flow)*

**Goal**: Full end-to-end flow "search product → add grams → entry appears in diary" works.

Files to create:
- [ ] `presentation/util/Format.kt` *(date/nutrition/weight formatting, Russian)*
- [ ] `presentation/util/MacroColorUtil.kt` *(macroProgressColor, caloriesProgressColor)*
- [ ] `presentation/components/DailySummaryCard.kt`
- [ ] `presentation/screen/today/TodayUiState.kt` *(sealed: Loading / Success / Error)*
- [ ] `presentation/screen/today/TodayViewModel.kt` *(combine 5 flows; date nav; copy/paste/save meal)*
- [ ] `presentation/screen/today/TodayScreen.kt` *(TodayScreen + TodayContent with readOnly param)*
- [ ] `presentation/screen/product/ProductUiState.kt`
- [ ] `presentation/screen/product/ProductViewModel.kt` *(debounce 300ms, saved meals tab)*
- [ ] `presentation/screen/product/ProductSearchScreen.kt` *(tab bar: Products + Saved Meals)*
- [ ] `presentation/screen/product/AddProductUiState.kt`
- [ ] `presentation/screen/product/AddProductViewModel.kt`
- [ ] `presentation/screen/product/AddProductScreen.kt`

Verification:
- [ ] `.\gradlew.bat :app:assembleDebug --no-daemon` → **BUILD SUCCESSFUL**
- [ ] Manual test: add food entry, data persists after app restart

---

### Phase 8 — History + Weight + Settings Screens

**Goal**: All 4 tabs fully functional. Settings persist. Weight deltas compute correctly.

Files to create:
- [ ] `presentation/screen/history/HistoryUiState.kt`
- [ ] `presentation/screen/history/HistoryViewModel.kt`
- [ ] `presentation/screen/history/HistoryScreen.kt`
- [ ] `presentation/screen/history/HistoryDayViewModel.kt`
- [ ] `presentation/screen/history/HistoryDayScreen.kt` *(reuses TodayContent(readOnly=true))*
- [ ] `presentation/screen/weight/WeightUiState.kt`
- [ ] `presentation/screen/weight/WeightViewModel.kt`
- [ ] `presentation/screen/weight/WeightScreen.kt`
- [ ] `presentation/screen/settings/SettingsUiState.kt`
- [ ] `presentation/screen/settings/SettingsViewModel.kt`
- [ ] `presentation/screen/settings/SettingsScreen.kt`
- [ ] `DietHelperApplication.kt` *(updated: inject DatabaseSeeder, call seedIfNeeded() in onCreate)*
- [ ] `data/local/DatabaseSeeder.kt` *(15 default Russian products)*
- [ ] `presentation/screen/Previews.kt` *(@Preview light+dark for all screens)*

Verification:
- [ ] `.\gradlew.bat :app:assembleDebug --no-daemon` → **BUILD SUCCESSFUL**
- [ ] Manual test: History shows past days, Weight deltas correct, Settings persist after restart

---

### Phase 9 — Tests *(47 test files)*

**Goal**: All unit/DAO/Compose/Screenshot tests pass.

Test infrastructure (verbatim from Part 11):
- [ ] `test/util/MainDispatcherRule.kt`
- [ ] `test/data/FakeProductRepository.kt`
- [ ] `test/data/FakeFoodEntryRepository.kt`
- [ ] `test/data/FakeWeightRepository.kt`
- [ ] `test/data/FakeGoalsRepository.kt`
- [ ] `test/data/FakeSavedMealRepository.kt`

DAO tests *(Robolectric + in-memory Room)*:
- [ ] `test/data/local/converter/ConvertersTest.kt`
- [ ] `test/data/local/dao/ProductDaoTest.kt`
- [ ] `test/data/local/dao/FoodEntryDaoTest.kt`
- [ ] `test/data/local/dao/WeightEntryDaoTest.kt`
- [ ] `test/data/local/dao/SavedMealDaoTest.kt`

Repository implementation tests:
- [ ] `test/data/repository/ProductRepositoryImplTest.kt`
- [ ] `test/data/repository/FoodEntryRepositoryImplTest.kt`
- [ ] `test/data/repository/WeightRepositoryImplTest.kt`
- [ ] `test/data/repository/GoalsRepositoryImplTest.kt`
- [ ] `test/data/repository/SavedMealRepositoryImplTest.kt`

UseCase tests:
- [ ] `test/domain/usecase/AddFoodEntryUseCaseTest.kt`
- [ ] `test/domain/usecase/FoodEntryUseCasesTest.kt`
- [ ] `test/domain/usecase/GetDailySummaryUseCaseTest.kt`
- [ ] `test/domain/usecase/GetFoodEntriesForDayUseCaseTest.kt`
- [ ] `test/domain/usecase/GetHistoryUseCaseTest.kt`
- [ ] `test/domain/usecase/GetStreakUseCaseTest.kt`
- [ ] `test/domain/usecase/GetWeekDayStatusesUseCaseTest.kt`
- [ ] `test/domain/usecase/SearchProductsUseCaseTest.kt`
- [ ] `test/domain/usecase/ProductUseCasesTest.kt`
- [ ] `test/domain/usecase/UpsertWeightEntryUseCaseTest.kt`
- [ ] `test/domain/usecase/WeightUseCasesTest.kt`
- [ ] `test/domain/usecase/GoalsUseCasesTest.kt`
- [ ] `test/domain/usecase/GetSavedMealsUseCaseTest.kt`
- [ ] `test/domain/usecase/SaveMealUseCaseTest.kt`
- [ ] `test/domain/usecase/DeleteSavedMealUseCaseTest.kt`
- [ ] `test/domain/usecase/AddSavedMealEntriesUseCaseTest.kt`

ViewModel tests:
- [ ] `test/presentation/screen/today/TodayViewModelTest.kt`
- [ ] `test/presentation/screen/history/HistoryViewModelTest.kt`
- [ ] `test/presentation/screen/history/HistoryDayViewModelTest.kt`
- [ ] `test/presentation/screen/product/ProductViewModelTest.kt`
- [ ] `test/presentation/screen/product/AddProductViewModelTest.kt`
- [ ] `test/presentation/screen/weight/WeightViewModelTest.kt`
- [ ] `test/presentation/screen/settings/SettingsViewModelTest.kt`

Compose UI tests:
- [ ] `test/presentation/screen/today/TodayScreenContentTest.kt`
- [ ] `test/presentation/screen/settings/SettingsScreenContentTest.kt`
- [ ] `test/presentation/components/DailySummaryCardTest.kt`

Util tests:
- [ ] `test/presentation/util/FormatTest.kt`
- [ ] `test/presentation/util/MacroColorUtilTest.kt`
- [ ] `test/presentation/util/InMemoryMealClipboardTest.kt`

Screenshot tests *(Roborazzi)*:
- [ ] `test/presentation/screenshot/DailySummaryCardScreenshotTest.kt`
- [ ] `test/presentation/screenshot/SettingsScreenScreenshotTest.kt`

Verification:
- [ ] `.\gradlew.bat :app:testDebugUnitTest --no-daemon` → **all tests PASSED / 0 FAILED**
- [ ] `.\gradlew.bat :app:recordRoborazziDebug --no-daemon` → **BUILD SUCCESSFUL** *(snapshots recorded)*
- [ ] `.\gradlew.bat :app:verifyRoborazziDebug --no-daemon` → **BUILD SUCCESSFUL**

---

### Phase 10 — Polish + Detekt

**Goal**: Code quality passes. Release build succeeds.

Steps:
- [ ] Add `@Preview` (light + dark) annotations to all Screen composables
- [ ] Create `app/proguard-rules.pro` *(empty file)*
- [ ] Fix all Detekt violations

Verification:
- [ ] `.\gradlew.bat :app:detekt --no-daemon` → **BUILD SUCCESSFUL**
- [ ] `.\gradlew.bat :app:assembleRelease --no-daemon` → **BUILD SUCCESSFUL**

---

### Resumption Prompt (copy this when switching to a new model)

When your current model runs out of tokens, start a fresh session with the new model and paste:

```
Read PROMPT.md in full.

Then look at the Progress Tracker section.
Find the first unchecked [ ] item in the first uncompleted Phase.
Continue the build from exactly that point.

Do NOT re-create files that are already checked [x].
Do NOT re-run verifications that are already checked [x].

After completing each step, tell me which checkbox to mark as done.
```

> **Tip**: Before switching models, run the current phase's verification command one more time
> to make sure the checkboxes reflect the true state of the codebase.

---

## Part 1: Prerequisites

Install on the new machine before starting:

1. **Android Studio** (latest stable, e.g. Ladybug 2024.2.x)
   - During install, Android Studio bundles JBR (JetBrains Runtime) — note its path.
   - Default locations:
     - `C:\Program Files\Android\Android Studio\jbr` (typical Windows install)
     - Or check: Android Studio → File → Project Structure → SDK → JDK location
2. **Android SDK** via Android Studio SDK Manager — install API 35 (Android 15)
3. **Git** for Windows
4. **Cursor IDE** — cursor.sh

> **JAVA_HOME note**: The `dh-runner` Cursor rule uses `<PATH_TO_ANDROID_STUDIO_JBR>` as a placeholder.
> After setup, open `.cursor/rules/dh-runner.mdc` and replace it with your actual JBR path.

---

## Part 2: Tech Stack (Exact Versions)

```
Kotlin:                2.1.20
AGP:                   8.5.2
Gradle Wrapper:        8.9
KSP:                   2.1.20-1.0.32

Compose BOM:           2024.09.03
  compose-ui:          1.7.x  (from BOM)
  material3:           1.3.x  (from BOM)
  navigation-compose:  2.8.x  (from BOM)

Hilt:                  2.55
hilt-navigation-compose: 1.2.0
Room:                  2.7.1
DataStore Preferences: 1.1.1
Coroutines:            1.9.0
Lifecycle:             2.8.6
core-ktx:              1.13.1
activity-compose:      1.9.2

Testing:
  JUnit:               4.13.2
  Turbine:             1.1.0
  kotlinx-coroutines-test: 1.9.0
  Robolectric:         4.13
  Roborazzi:           1.25.0
  androidx-test-core:  1.6.1
  androidx-test-runner:  1.6.2
  androidx-test-ext-junit: 1.2.1
  androidx-arch-core-testing: 2.2.0

Code Quality:
  Detekt:              1.23.7
```

Package name: `com.k.shavrin.diethelper`
Application name: `Рацион` (Russian for "Diet / Ration")
minSdk: 26 · targetSdk: 35 · JVM target: 17

---

## Part 3: Project Structure (Full Tree)

```
<project-root>/
├── .cursor/
│   └── rules/
│       ├── project-context.mdc   ← always-on project context (= CLAUDE.md)
│       ├── dh-workflow.mdc       ← main orchestration workflow (invoke: @dh-workflow)
│       ├── dh-developer.mdc      ← code implementation patterns
│       ├── dh-tester.mdc         ← test writing patterns
│       ├── dh-runner.mdc         ← build/test verification
│       └── dh-docs.mdc           ← documentation maintenance
├── config/
│   └── detekt/
│       └── detekt.yml
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/values/strings.xml
│       │   ├── res/values/themes.xml
│       │   └── java/com/k/shavrin/diethelper/
│       │       ├── DietHelperApplication.kt
│       │       ├── MainActivity.kt
│       │       ├── data/
│       │       │   ├── local/
│       │       │   │   ├── converter/Converters.kt
│       │       │   │   ├── dao/FoodEntryDao.kt
│       │       │   │   ├── dao/ProductDao.kt
│       │       │   │   ├── dao/SavedMealDao.kt
│       │       │   │   ├── dao/WeightEntryDao.kt
│       │       │   │   ├── entity/FoodEntryEntity.kt
│       │       │   │   ├── entity/FoodEntryWithProduct.kt
│       │       │   │   ├── entity/ProductEntity.kt
│       │       │   │   ├── entity/SavedMealEntity.kt
│       │       │   │   ├── entity/SavedMealItemEntity.kt
│       │       │   │   ├── entity/SavedMealItemWithProduct.kt
│       │       │   │   ├── entity/SavedMealWithItems.kt
│       │       │   │   ├── entity/WeightEntryEntity.kt
│       │       │   │   ├── DatabaseSeeder.kt
│       │       │   │   ├── DietHelperDatabase.kt
│       │       │   │   └── GoalsDataSource.kt
│       │       │   ├── mapper/
│       │       │   │   ├── FoodEntryMapper.kt
│       │       │   │   ├── ProductMapper.kt
│       │       │   │   ├── SavedMealMapper.kt
│       │       │   │   └── WeightEntryMapper.kt
│       │       │   └── repository/
│       │       │       ├── FoodEntryRepositoryImpl.kt
│       │       │       ├── GoalsRepositoryImpl.kt
│       │       │       ├── ProductRepositoryImpl.kt
│       │       │       ├── SavedMealRepositoryImpl.kt
│       │       │       └── WeightRepositoryImpl.kt
│       │       ├── di/
│       │       │   ├── DatabaseModule.kt
│       │       │   ├── DataStoreModule.kt
│       │       │   └── RepositoryModule.kt
│       │       ├── domain/
│       │       │   ├── model/
│       │       │   │   ├── DailyGoals.kt
│       │       │   │   ├── DailySummary.kt
│       │       │   │   ├── DayStatus.kt
│       │       │   │   ├── FoodEntry.kt
│       │       │   │   ├── HistoryItem.kt
│       │       │   │   ├── MealType.kt
│       │       │   │   ├── Product.kt
│       │       │   │   ├── SavedMeal.kt
│       │       │   │   ├── SavedMealItem.kt
│       │       │   │   └── WeightEntry.kt
│       │       │   ├── repository/
│       │       │   │   ├── FoodEntryRepository.kt
│       │       │   │   ├── GoalsRepository.kt
│       │       │   │   ├── ProductRepository.kt
│       │       │   │   ├── SavedMealRepository.kt
│       │       │   │   └── WeightRepository.kt
│       │       │   └── usecase/
│       │       │       ├── foodentry/
│       │       │       │   ├── AddFoodEntryUseCase.kt
│       │       │       │   ├── CopyFoodEntryToDayUseCase.kt
│       │       │       │   ├── DeleteFoodEntryUseCase.kt
│       │       │       │   ├── GetDailySummaryUseCase.kt
│       │       │       │   ├── GetFoodEntriesForDayUseCase.kt
│       │       │       │   ├── GetHistoryUseCase.kt
│       │       │       │   ├── GetStreakUseCase.kt
│       │       │       │   ├── GetWeekDayStatusesUseCase.kt
│       │       │       │   └── UpdateFoodEntryUseCase.kt
│       │       │       ├── goals/
│       │       │       │   ├── GetDailyGoalsUseCase.kt
│       │       │       │   └── SaveDailyGoalsUseCase.kt
│       │       │       ├── product/
│       │       │       │   ├── AddProductUseCase.kt
│       │       │       │   ├── GetAllProductsUseCase.kt
│       │       │       │   ├── SearchProductsUseCase.kt
│       │       │       │   └── ToggleFavoriteUseCase.kt
│       │       │       ├── savedmeal/
│       │       │       │   ├── AddSavedMealEntriesUseCase.kt
│       │       │       │   ├── DeleteSavedMealUseCase.kt
│       │       │       │   ├── GetSavedMealsUseCase.kt
│       │       │       │   └── SaveMealUseCase.kt
│       │       │       └── weight/
│       │       │           ├── DeleteWeightEntryUseCase.kt
│       │       │           ├── GetAllWeightEntriesUseCase.kt
│       │       │           └── UpsertWeightEntryUseCase.kt
│       │       └── presentation/
│       │           ├── components/DailySummaryCard.kt
│       │           ├── navigation/
│       │           │   ├── AppNavHost.kt
│       │           │   ├── BottomNavItem.kt
│       │           │   └── Routes.kt
│       │           ├── screen/
│       │           │   ├── Previews.kt
│       │           │   ├── history/
│       │           │   │   ├── HistoryDayScreen.kt
│       │           │   │   ├── HistoryDayViewModel.kt
│       │           │   │   ├── HistoryScreen.kt
│       │           │   │   ├── HistoryUiState.kt
│       │           │   │   └── HistoryViewModel.kt
│       │           │   ├── product/
│       │           │   │   ├── AddProductScreen.kt
│       │           │   │   ├── AddProductUiState.kt
│       │           │   │   ├── AddProductViewModel.kt
│       │           │   │   ├── ProductSearchScreen.kt
│       │           │   │   ├── ProductUiState.kt
│       │           │   │   └── ProductViewModel.kt
│       │           │   ├── settings/
│       │           │   │   ├── SettingsScreen.kt
│       │           │   │   ├── SettingsUiState.kt
│       │           │   │   └── SettingsViewModel.kt
│       │           │   ├── today/
│       │           │   │   ├── TodayScreen.kt
│       │           │   │   ├── TodayUiState.kt
│       │           │   │   └── TodayViewModel.kt
│       │           │   └── weight/
│       │           │       ├── WeightScreen.kt
│       │           │       ├── WeightUiState.kt
│       │           │       └── WeightViewModel.kt
│       │           ├── theme/
│       │           │   ├── Color.kt
│       │           │   ├── Theme.kt
│       │           │   └── Type.kt
│       │           └── util/
│       │               ├── Format.kt
│       │               ├── InMemoryMealClipboard.kt
│       │               └── MacroColorUtil.kt
│       └── test/
│           └── java/com/k/shavrin/diethelper/
│               ├── util/MainDispatcherRule.kt
│               ├── data/
│               │   ├── FakeFoodEntryRepository.kt
│               │   ├── FakeGoalsRepository.kt
│               │   ├── FakeProductRepository.kt
│               │   ├── FakeSavedMealRepository.kt
│               │   ├── FakeWeightRepository.kt
│               │   ├── local/converter/ConvertersTest.kt
│               │   ├── local/dao/FoodEntryDaoTest.kt
│               │   ├── local/dao/ProductDaoTest.kt
│               │   ├── local/dao/SavedMealDaoTest.kt
│               │   ├── local/dao/WeightEntryDaoTest.kt
│               │   └── repository/
│               │       ├── FoodEntryRepositoryImplTest.kt
│               │       ├── GoalsRepositoryImplTest.kt
│               │       ├── ProductRepositoryImplTest.kt
│               │       ├── SavedMealRepositoryImplTest.kt
│               │       └── WeightRepositoryImplTest.kt
│               ├── domain/usecase/
│               │   ├── AddFoodEntryUseCaseTest.kt
│               │   ├── AddSavedMealEntriesUseCaseTest.kt
│               │   ├── DeleteSavedMealUseCaseTest.kt
│               │   ├── FoodEntryUseCasesTest.kt
│               │   ├── GetDailySummaryUseCaseTest.kt
│               │   ├── GetFoodEntriesForDayUseCaseTest.kt
│               │   ├── GetHistoryUseCaseTest.kt
│               │   ├── GetSavedMealsUseCaseTest.kt
│               │   ├── GetStreakUseCaseTest.kt
│               │   ├── GetWeekDayStatusesUseCaseTest.kt
│               │   ├── GoalsUseCasesTest.kt
│               │   ├── ProductUseCasesTest.kt
│               │   ├── SaveMealUseCaseTest.kt
│               │   ├── SearchProductsUseCaseTest.kt
│               │   ├── UpsertWeightEntryUseCaseTest.kt
│               │   └── WeightUseCasesTest.kt
│               └── presentation/
│                   ├── components/DailySummaryCardTest.kt
│                   ├── screen/
│                   │   ├── history/HistoryDayViewModelTest.kt
│                   │   ├── history/HistoryViewModelTest.kt
│                   │   ├── product/AddProductViewModelTest.kt
│                   │   ├── product/ProductViewModelTest.kt
│                   │   ├── settings/SettingsScreenContentTest.kt
│                   │   ├── settings/SettingsViewModelTest.kt
│                   │   ├── today/TodayScreenContentTest.kt
│                   │   ├── today/TodayViewModelTest.kt
│                   │   └── weight/WeightViewModelTest.kt
│                   ├── screenshot/
│                   │   ├── DailySummaryCardScreenshotTest.kt
│                   │   └── SettingsScreenScreenshotTest.kt
│                   └── util/
│                       ├── FormatTest.kt
│                       ├── InMemoryMealClipboardTest.kt
│                       └── MacroColorUtilTest.kt
├── build.gradle.kts              ← root
├── settings.gradle.kts
├── CLAUDE.md
└── PROMPT.md    ← this file
```

---

## Part 4: Architecture Rules (Non-Negotiable)

### Clean Architecture — 3 layers

```
domain   ←   data   ←   presentation
```

- **`domain/`** — pure Kotlin, zero Android dependencies.
  - `model/` — data classes (no Room annotations, no Android imports)
  - `repository/` — interfaces only
  - `usecase/` — one class per use case, `operator fun invoke()`
- **`data/`** — knows about `domain`, never about `presentation`
  - `local/entity/` — Room `@Entity` data classes
  - `local/dao/` — `@Dao` interfaces
  - `local/converter/` — `@TypeConverter` for `LocalDate ↔ Long`
  - `mapper/` — extension functions only, one file per entity
  - `repository/` — `*Impl` classes implementing domain interfaces
- **`presentation/`** — knows about `domain`, never imports `data`
  - ViewModels depend on UseCases, never on repositories directly
  - Each screen: `<Name>UiState.kt` + `<Name>ViewModel.kt` + `<Name>Screen.kt`
  - Screen composable exposes `<Name>Content(state, onXxx…)` for testability
  - `<Name>Screen` is a thin Hilt wrapper calling `hiltViewModel()` and `<Name>Content`

### General coding rules
- No `LiveData` — only `StateFlow` / `Flow`
- No mocks in tests — only Fakes using `MutableStateFlow`
- No `TODO`, `FIXME`, placeholder, or stub code — every file is complete
- Mappers are extension functions (not class methods)
- Russian strings hardcoded in UI (not in `strings.xml`), `strings.xml` only for `app_name`
- `@HiltViewModel` on every ViewModel; `hiltViewModel()` in composables
- `@Singleton` on repository implementations and service classes

---

## Part 5: Domain Models (Exact Kotlin Code)

```kotlin
// MealType.kt
enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }

// DayStatus.kt
enum class DayStatus { FUTURE, GRAY_LOGGED, GREEN, YELLOW, RED }

// Product.kt
data class Product(
    val id: Long = 0,
    val name: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbsPer100g: Float,
    val isFavorite: Boolean = false
)

// FoodEntry.kt
data class FoodEntry(
    val id: Long = 0,
    val productId: Long,
    val product: Product,        // always populated via Room @Relation
    val date: LocalDate,         // java.time.LocalDate
    val mealType: MealType,
    val multiplier: Float        // 1.0f = 100 g, 1.5f = 150 g
)

// DailyGoals.kt
data class DailyGoals(
    val calories: Float,
    val proteinMin: Float,
    val proteinMax: Float,
    val fatMin: Float,
    val fatMax: Float,
    val carbsMin: Float,
    val carbsMax: Float
) {
    companion object {
        val DEFAULT = DailyGoals(
            calories = 2000f,
            proteinMin = 120f, proteinMax = 180f,
            fatMin = 55f,      fatMax = 80f,
            carbsMin = 200f,   carbsMax = 280f
        )
    }
}

// DailySummary.kt
data class DailySummary(
    val totalCalories: Float,
    val totalProtein: Float,
    val totalFat: Float,
    val totalCarbs: Float
) {
    companion object { val EMPTY = DailySummary(0f, 0f, 0f, 0f) }
}

// WeightEntry.kt
data class WeightEntry(
    val id: Long = 0,
    val date: LocalDate,
    val weightKg: Float
)

// HistoryItem.kt
data class HistoryItem(val date: LocalDate, val totalCalories: Float)

// SavedMeal.kt
data class SavedMeal(val id: Long = 0, val name: String, val items: List<SavedMealItem>)

// SavedMealItem.kt
data class SavedMealItem(
    val id: Long = 0,
    val savedMealId: Long,
    val productId: Long,
    val product: Product,
    val multiplier: Float
)
```

### Repository interfaces (signatures only — implement in `data/repository/`)

```kotlin
interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    fun searchProducts(query: String): Flow<List<Product>>
    suspend fun getProductById(id: Long): Product?
    suspend fun addProduct(product: Product): Long
    suspend fun updateProduct(product: Product)
    suspend fun deleteProduct(product: Product)
    suspend fun toggleFavorite(productId: Long, isFavorite: Boolean)
}

interface FoodEntryRepository {
    fun getEntriesForDay(date: LocalDate): Flow<List<FoodEntry>>
    fun getDistinctDatesDescending(): Flow<List<LocalDate>>
    fun getEntriesForDates(dates: List<LocalDate>): Flow<List<FoodEntry>>
    suspend fun addEntry(entry: FoodEntry): Long
    suspend fun updateEntry(entry: FoodEntry)
    suspend fun deleteEntry(entry: FoodEntry)
    suspend fun copyEntryToDay(entry: FoodEntry, targetDate: LocalDate)
}

interface WeightRepository {
    fun getAllEntries(): Flow<List<WeightEntry>>
    suspend fun getEntryByDate(date: LocalDate): WeightEntry?
    suspend fun upsertEntry(date: LocalDate, weightKg: Float)
    suspend fun deleteEntry(entry: WeightEntry)
}

interface GoalsRepository {
    fun getDailyGoals(): Flow<DailyGoals>
    suspend fun saveGoals(goals: DailyGoals)
}

interface SavedMealRepository {
    fun getSavedMeals(): Flow<List<SavedMeal>>
    suspend fun saveMeal(name: String, items: List<SavedMealItem>)
    suspend fun deleteMeal(id: Long)
}
```

---

## Part 6: Room Database Schema

### TypeConverter (LocalDate ↔ Long)
`Converters.kt` — `LocalDate.toEpochDay()` ↔ `LocalDate.ofEpochDay(value)`

### Entities

**`ProductEntity`** — table `products`
```
id: Long (PK autoGenerate), name: String, caloriesPer100g: Float,
proteinPer100g: Float, fatPer100g: Float, carbsPer100g: Float, isFavorite: Boolean
```

**`FoodEntryEntity`** — table `food_entries`
```
id: Long (PK autoGenerate), productId: Long (FK → products.id ON DELETE CASCADE),
date: LocalDate, mealType: String (enum name), multiplier: Float
Indices: (productId), (date)
```

**`FoodEntryWithProduct`** — @Transaction data class
```kotlin
@Embedded val entry: FoodEntryEntity
@Relation(parentColumn = "productId", entityColumn = "id") val product: ProductEntity
```

**`WeightEntryEntity`** — table `weight_entries`
```
id: Long (PK autoGenerate), date: LocalDate, weightKg: Float
Unique index on (date)
```

**`SavedMealEntity`** — table `saved_meals`
```
id: Long (PK autoGenerate), name: String
```

**`SavedMealItemEntity`** — table `saved_meal_items`
```
id: Long (PK autoGenerate)
savedMealId: Long (FK → saved_meals.id ON DELETE CASCADE)
productId: Long (FK → products.id ON DELETE CASCADE)
multiplier: Float
Indices: (savedMealId), (productId)
```

**`SavedMealItemWithProduct`** — @Transaction data class
```kotlin
@Embedded val item: SavedMealItemEntity
@Relation(parentColumn = "productId", entityColumn = "id") val product: ProductEntity
```

**`SavedMealWithItems`** — @Transaction data class
```kotlin
@Embedded val meal: SavedMealEntity
@Relation(entity = SavedMealItemEntity::class, parentColumn = "id", entityColumn = "savedMealId")
val items: List<SavedMealItemWithProduct>
```

### DAOs (key query signatures)

**`ProductDao`**
```kotlin
@Query("SELECT * FROM products ORDER BY isFavorite DESC, name COLLATE NOCASE ASC")
fun getAllProducts(): Flow<List<ProductEntity>>

@Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' COLLATE NOCASE ORDER BY isFavorite DESC, name COLLATE NOCASE ASC")
fun searchProducts(query: String): Flow<List<ProductEntity>>

@Query("SELECT * FROM products WHERE id = :id")
suspend fun getProductById(id: Long): ProductEntity?

@Insert suspend fun insertProduct(product: ProductEntity): Long
@Update suspend fun updateProduct(product: ProductEntity)
@Delete suspend fun deleteProduct(product: ProductEntity)
@Query("UPDATE products SET isFavorite = :isFavorite WHERE id = :id")
suspend fun setFavorite(id: Long, isFavorite: Boolean)
```

**`FoodEntryDao`**
```kotlin
@Transaction @Query("SELECT * FROM food_entries WHERE date = :date")
fun getEntriesForDate(date: LocalDate): Flow<List<FoodEntryWithProduct>>

@Transaction @Query("SELECT * FROM food_entries WHERE date IN (:dates)")
fun getEntriesForDates(dates: List<LocalDate>): Flow<List<FoodEntryWithProduct>>

@Query("SELECT DISTINCT date FROM food_entries ORDER BY date DESC")
fun getDistinctDatesDescending(): Flow<List<LocalDate>>

@Insert suspend fun insertEntry(entry: FoodEntryEntity): Long
@Update suspend fun updateEntry(entry: FoodEntryEntity)
@Delete suspend fun deleteEntry(entry: FoodEntryEntity)
```

**`WeightEntryDao`**
```kotlin
@Query("SELECT * FROM weight_entries ORDER BY date DESC")
fun getAllEntries(): Flow<List<WeightEntryEntity>>

@Query("SELECT * FROM weight_entries WHERE date = :date LIMIT 1")
suspend fun getEntryByDate(date: LocalDate): WeightEntryEntity?

@Upsert suspend fun upsertEntry(entry: WeightEntryEntity): Long
@Delete suspend fun deleteEntry(entry: WeightEntryEntity)
```

**`SavedMealDao`**
```kotlin
@Transaction @Query("SELECT * FROM saved_meals ORDER BY name COLLATE NOCASE ASC")
fun getAllWithItems(): Flow<List<SavedMealWithItems>>

@Query("SELECT * FROM saved_meals WHERE name = :name LIMIT 1")
suspend fun getByName(name: String): SavedMealEntity?

@Insert suspend fun insertMeal(entity: SavedMealEntity): Long
@Insert suspend fun insertItems(items: List<SavedMealItemEntity>)
@Query("DELETE FROM saved_meals WHERE name = :name")
suspend fun deleteByName(name: String)
@Query("DELETE FROM saved_meals WHERE id = :id")
suspend fun deleteById(id: Long)
```

### Database class

```kotlin
@Database(
    entities = [ProductEntity::class, FoodEntryEntity::class, WeightEntryEntity::class,
                SavedMealEntity::class, SavedMealItemEntity::class],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DietHelperDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao
    abstract fun foodEntryDao(): FoodEntryDao
    abstract fun weightEntryDao(): WeightEntryDao
    abstract fun savedMealDao(): SavedMealDao

    companion object {
        const val NAME = "diet_helper.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS saved_meals (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS saved_meal_items (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, savedMealId INTEGER NOT NULL, productId INTEGER NOT NULL, multiplier REAL NOT NULL, FOREIGN KEY(savedMealId) REFERENCES saved_meals(id) ON DELETE CASCADE, FOREIGN KEY(productId) REFERENCES products(id) ON DELETE CASCADE)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_meal_items_savedMealId ON saved_meal_items(savedMealId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_saved_meal_items_productId ON saved_meal_items(productId)")
            }
        }
    }
}
```

> **⚠️ CRITICAL**: `DatabaseModule.kt` must call `.addMigrations(DietHelperDatabase.MIGRATION_1_2)` AND `.fallbackToDestructiveMigration()`.

---

## Part 7: Screens and Features

### Navigation routes

```kotlin
object Routes {
    const val TODAY         = "today"
    const val HISTORY       = "history"
    const val WEIGHT        = "weight"
    const val SETTINGS      = "settings"
    const val PRODUCT_SEARCH = "product_search/{date}/{mealType}"
    const val ADD_PRODUCT   = "add_product?name={name}"
    const val HISTORY_DAY   = "history_day/{date}"

    fun productSearch(date: String, mealType: String) = "product_search/$date/$mealType"
    fun addProduct(name: String = "") = "add_product?name=$name"
    fun historyDay(date: String) = "history_day/$date"
}
```

Bottom nav: Today (Icons.Filled.Today) | History (Icons.Filled.History) |
Weight (Icons.Filled.MonitorWeight) | Settings (Icons.Filled.Settings)

### Today Screen (`today`)

**Purpose**: Daily food diary.

**UiState.Success fields**:
```
date: LocalDate
sections: Map<MealType, List<FoodEntry>>
sectionCalories/Protein/Fat/Carbs: Map<MealType, Float>  (per-section macro totals)
summary: DailySummary
goals: DailyGoals
weekStatuses: List<Pair<LocalDate, DayStatus>>  (Mon–Sun of current week)
streak: Int                                       (consecutive days ≥ 30% calorie goal)
clipboard: ClipboardSnapshot?                     (InMemoryMealClipboard state)
```

**Behaviors**:
- `WeekDateHeader` at top: current day name + date, row of 7 `WeekDayCircle` colored by DayStatus
  - GREEN = calories < goal AND macros in [min, max]
  - YELLOW = not green but calories < 125% goal AND macros in ±25% of range
  - RED = over goal or outside macro range
  - GRAY_LOGGED = < 30% of calorie goal logged
  - FUTURE = day after today
- `StreakRow` shows consecutive logged days (backwards from yesterday + today if ≥ 30% goal)
- 4 meal sections (BREAKFAST / LUNCH / DINNER / SNACK), always visible
- Each section is collapsible/expandable (tap header to toggle)
- Expanded section shows action bar: Copy | Paste | Save buttons
  - **Copy**: stores all entries of the section into `InMemoryMealClipboard`
  - **Paste**: creates new entries from clipboard into this section
  - **Save**: shows dialog to name the meal → persists to Room as `SavedMeal`
- Long-press an entry → DropdownMenu: "Изменить граммы" | "Удалить" | "Скопировать на другой день"
- Date navigation: `←` `→` buttons; forward blocked on today
- `DailySummaryCard` at bottom with calories + macro progress bars
- History Day Screen reuses `TodayContent(readOnly = true)` — no editing

**InMemoryMealClipboard**:
```kotlin
@Singleton
class InMemoryMealClipboard @Inject constructor() {
    data class ClipboardSnapshot(
        val entries: List<FoodEntry>,
        val sourceMealType: MealType,
        val sourceDate: LocalDate
    )
    private val _state = MutableStateFlow<ClipboardSnapshot?>(null)
    val state: StateFlow<ClipboardSnapshot?> = _state.asStateFlow()
    fun copy(snapshot: ClipboardSnapshot) { _state.value = snapshot }
    fun clear() { _state.value = null }
}
```

### Product Search Screen (`product_search/{date}/{mealType}`)

**Behaviors**:
- Tab bar: "Продукты" | "Сохранённые"
- **Products tab**: search field, debounce 300 ms, `flatMapLatest`
  - Empty query → all products (favorites first, then alphabetical)
  - Tap product → dialog: enter grams (default 100) → create `FoodEntry` → pop back
  - Star icon → toggle `isFavorite`
  - If query non-empty and no exact name match → "Добавить «{query}» как новый продукт" button
- **Saved Meals tab**: list of saved meals
  - Tap → dialog to confirm → `AddSavedMealEntriesUseCase` adds all items to the day
  - Swipe-to-delete with confirmation dialog

### Add Product Screen (`add_product?name={name}`)

5 `OutlinedTextField` fields: Название, Калории, Белки, Жиры, Углеводы (all per 100g).
Validation on submit: name not blank; calories > 0; protein/fat/carbs ≥ 0.
Errors shown as `supportingText`. Comma or period accepted as decimal separator.

### History Screen (`history`)

`LazyColumn` of dates with food entries, sorted descending. Tap → `history_day/{date}`.

### History Day Screen (`history_day/{date}`)

Reuses `TodayContent(readOnly = true)`. `TopAppBar` with date + back button.

### Weight Screen (`weight`)

- OutlinedTextField + Save button at top. If entry exists for today → pre-filled (upsert).
- `LazyColumn` sorted descending with date, weight, delta (green for decrease, red for increase).
- Long-press entry → confirmation dialog → delete.

### Settings Screen (`settings`)

7 fields: Калории + min/max for Белки / Жиры / Углеводы.
Pre-filled from DataStore. Save button (not live-save). Validation: all > 0, max > min.
Warning tooltips if macro min/max sum doesn't match calorie target.

### DailySummaryCard

Reusable composable. Shows total calories + protein/fat/carbs with `LinearProgressIndicator`
(6dp height). Color-coded progress via `macroProgressColor()` / `caloriesProgressColor()`.

### GoalsDataSource (DataStore keys)

```kotlin
floatPreferencesKey("goal_calories")     // default 2000f
floatPreferencesKey("goal_protein_min")  // default 120f
floatPreferencesKey("goal_protein_max")  // default 180f
floatPreferencesKey("goal_fat_min")      // default 55f
floatPreferencesKey("goal_fat_max")      // default 80f
floatPreferencesKey("goal_carbs_min")    // default 200f
floatPreferencesKey("goal_carbs_max")    // default 280f
booleanPreferencesKey("is_seeded")       // tracks first-launch seed
```

### DatabaseSeeder (15 default products, seeded once on first launch)

```
Гречка варёная, Куриная грудка варёная, Яйцо куриное, Творог 5%, Молоко 2.5%,
Хлеб ржаной, Овсянка на воде, Банан, Яблоко, Помидор, Огурец,
Рис варёный, Картофель варёный, Сыр российский, Лосось запечённый
```

Use `is_seeded` DataStore key to guard against re-seeding on subsequent launches.

---

## Part 8: Build Configuration Files (Verbatim)

### `settings.gradle.kts`

```kotlin
pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Diet Helper"
include(":app")
```

### `build.gradle.kts` (root)

```kotlin
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.detekt) apply false
    alias(libs.plugins.roborazzi) apply false
}
```

### `gradle/libs.versions.toml`

```toml
[versions]
agp = "8.5.2"
kotlin = "2.1.20"
ksp = "2.1.20-1.0.32"
composeBom = "2024.09.03"
hilt = "2.55"
hiltNavCompose = "1.2.0"
room = "2.7.1"
datastore = "1.1.1"
coroutines = "1.9.0"
lifecycle = "2.8.6"
coreKtx = "1.13.1"
activityCompose = "1.9.2"
junit = "4.13.2"
turbine = "1.1.0"
detekt = "1.23.7"
robolectric = "4.13"
roborazzi = "1.25.0"
androidxTestCore = "1.6.1"
androidxTestRunner = "1.6.2"
androidxTestExtJunit = "1.2.1"
archCoreTesting = "2.2.0"

[libraries]
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycle" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycle" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }

androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-compose-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
androidx-compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-compose-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-compose-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose" }

hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-android-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavCompose" }

androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }

androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

kotlinx-coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }

junit = { group = "junit", name = "junit", version.ref = "junit" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }

detekt-formatting = { group = "io.gitlab.arturbosch.detekt", name = "detekt-formatting", version.ref = "detekt" }

robolectric = { group = "org.robolectric", name = "robolectric", version.ref = "robolectric" }

androidx-room-testing = { group = "androidx.room", name = "room-testing", version.ref = "room" }

androidx-test-core = { group = "androidx.test", name = "core-ktx", version.ref = "androidxTestCore" }
androidx-test-runner = { group = "androidx.test", name = "runner", version.ref = "androidxTestRunner" }
androidx-test-ext-junit = { group = "androidx.test.ext", name = "junit", version.ref = "androidxTestExtJunit" }
androidx-arch-core-testing = { group = "androidx.arch.core", name = "core-testing", version.ref = "archCoreTesting" }

androidx-compose-ui-test-junit4 = { group = "androidx.compose.ui", name = "ui-test-junit4" }
androidx-compose-ui-test-manifest = { group = "androidx.compose.ui", name = "ui-test-manifest" }

roborazzi = { group = "io.github.takahirom.roborazzi", name = "roborazzi", version.ref = "roborazzi" }
roborazzi-compose = { group = "io.github.takahirom.roborazzi", name = "roborazzi-compose", version.ref = "roborazzi" }
roborazzi-junit-rule = { group = "io.github.takahirom.roborazzi", name = "roborazzi-junit-rule", version.ref = "roborazzi" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
kotlin-compose = { id = "org.jetbrains.kotlin.plugin.compose", version.ref = "kotlin" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
detekt = { id = "io.gitlab.arturbosch.detekt", version.ref = "detekt" }
roborazzi = { id = "io.github.takahirom.roborazzi", version.ref = "roborazzi" }
```

### `gradle/wrapper/gradle-wrapper.properties`

```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.9-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

### `app/build.gradle.kts`

```kotlin
import org.gradle.testing.jacoco.tasks.JacocoReport

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.detekt)
    alias(libs.plugins.roborazzi)
}

android {
    namespace = "com.k.shavrin.diethelper"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.k.shavrin.diethelper"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
        debug {
            isMinifyEnabled = false
            enableUnitTestCoverage = true
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            all { it.jvmArgs("-XX:+EnableDynamicAgentLoading") }
        }
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    implementation(libs.androidx.datastore.preferences)

    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.arch.core.testing)
    testImplementation(libs.androidx.room.testing)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.roborazzi)
    testImplementation(libs.roborazzi.compose)
    testImplementation(libs.roborazzi.junit.rule)

    debugImplementation(libs.androidx.compose.ui.test.manifest)

    detektPlugins(libs.detekt.formatting)
}

roborazzi {
    outputDir.set(file("src/test/snapshots"))
}

detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    autoCorrect = false
}

tasks.register<JacocoReport>("jacocoUnitTestReport") {
    dependsOn("testDebugUnitTest")
    group = "reporting"
    description = "Generate JaCoCo coverage report for unit tests"
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
    val excludes = listOf(
        "**/R.class", "**/R\$*.class", "**/BuildConfig.class",
        "**/Manifest*.*", "**/*Test*.*", "android/**/*.*",
        "**/*Hilt_*", "**/hilt_aggregated_deps/**",
        "**/Dagger*.*", "**/*_Factory.class",
        "**/*_MembersInjector.class", "**/*MembersInjector\$*.*",
        "**/DietHelperDatabase_Impl*", "**/*Dao_Impl*"
    )
    classDirectories.setFrom(fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/debug")) { exclude(excludes) })
    sourceDirectories.setFrom(layout.projectDirectory.dir("src/main/java"))
    executionData.setFrom(fileTree(layout.buildDirectory) { include("outputs/unit_test_code_coverage/debugUnitTest/*.exec") })
}
```

### `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <application
        android:name=".DietHelperApplication"
        android:allowBackup="true"
        android:icon="@android:drawable/sym_def_app_icon"
        android:label="@string/app_name"
        android:roundIcon="@android:drawable/sym_def_app_icon"
        android:supportsRtl="true"
        android:theme="@style/Theme.DietHelper"
        tools:targetApi="31">
        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:label="@string/app_name"
            android:theme="@style/Theme.DietHelper">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>
</manifest>
```

### `app/src/main/res/values/strings.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">Рацион</string>
</resources>
```

### `app/src/main/res/values/themes.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.DietHelper" parent="android:Theme.Material.NoActionBar" />
</resources>
```

### `config/detekt/detekt.yml`

```yaml
complexity:
  TooManyFunctions:
    thresholdInClasses: 25
    thresholdInObjects: 25
    thresholdInInterfaces: 15
  LongParameterList:
    functionThreshold: 10
    constructorThreshold: 10
  LongMethod:
    threshold: 120
  ComplexCondition:
    threshold: 8
  CyclomaticComplexMethod:
    threshold: 20
  NestedBlockDepth:
    threshold: 5

naming:
  FunctionNaming:
    functionPattern: '^([a-z$][a-zA-Z0-9$]*)|(`.*`)$'
    ignoreAnnotated:
      - 'Composable'
  TopLevelPropertyNaming:
    constantPattern: '[A-Z][_A-Z0-9]*|[A-Za-z][A-Za-z0-9]*'

style:
  MagicNumber:
    active: false
  MaxLineLength:
    maxLineLength: 200
    ignoreAnnotated:
      - 'Composable'
      - 'Preview'
  WildcardImport:
    active: false
  UnusedPrivateMember:
    ignoreAnnotated:
      - 'Preview'
  UnusedParameter:
    ignoreAnnotated:
      - 'Composable'
  ReturnCount:
    max: 4
  ForbiddenComment:
    active: true
    comments:
      - reason: "Use a TODO-tracker, not FIXME in code"
        value: "FIXME:"

exceptions:
  TooGenericExceptionCaught:
    active: false
  SwallowedException:
    active: false

formatting:
  active: true
  android: true
  autoCorrect: false
  ArgumentListWrapping:
    active: false
  PropertyWrapping:
    active: false
  Wrapping:
    active: false
  MultiLineIfElse:
    active: false
  Indentation:
    active: false
  MaximumLineLength:
    maxLineLength: 200
```

---

## Part 9: CLAUDE.md (Verbatim)

Create this file at the project root as `CLAUDE.md`:

```markdown
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
  model/          — pure Kotlin data classes (Product, FoodEntry, WeightEntry, SavedMeal,
                    SavedMealItem, DailyGoals, DailySummary, HistoryItem, MealType, DayStatus)
  repository/     — interfaces
  usecase/        — one class per use case
data/
  local/
    entity/       — Room entities
    dao/          — DAOs
    converter/    — Converters.kt: LocalDate → Long epochDay
    DietHelperDatabase.kt (v2)
    GoalsDataSource.kt
    DatabaseSeeder.kt
  mapper/         — entity ↔ domain mappers
  repository/     — *Impl classes
di/               — Hilt modules (DatabaseModule, DataStoreModule, RepositoryModule)
presentation/
  navigation/     — Routes, BottomNavItem, AppNavHost
  screen/         — today, product, history, weight, settings
  components/     — DailySummaryCard
  theme/          — Color, Type, Theme
  util/           — Format.kt, InMemoryMealClipboard.kt, MacroColorUtil.kt
```

## Key Technical Decisions
- `DailyGoals` stored in **DataStore Preferences**, not Room
- `LocalDate` → `Long` (epochDay) via Room `TypeConverter`
- `FoodEntryWithProduct` uses `@Transaction + @Relation` for reactive join
- `SavedMeal` → `SavedMealItem` via 1:N Room relation; queries reactive via `@Transaction`
- `InMemoryMealClipboard` transient (lost on app exit); no persistence overhead
- Single-activity app, Navigation Compose with bottom nav
- ViewModels injected via `hiltViewModel()`
- DB version = 2 with explicit MIGRATION_1_2 (adds saved_meals + saved_meal_items tables)

## Build
```powershell
# Set JAVA_HOME to Android Studio's JBR before running Gradle
$env:JAVA_HOME = "<PATH_TO_ANDROID_STUDIO_JBR>"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixDomain.tmpDir=C:\tmp"
$env:TEMP = "C:\tmp"; $env:TMP = "C:\tmp"
New-Item -ItemType Directory -Path "C:\tmp" -Force | Out-Null

.\gradlew.bat :app:kspDebugKotlin --no-daemon    # KSP code generation
.\gradlew.bat :app:assembleDebug --no-daemon      # Full debug build
.\gradlew.bat :app:testDebugUnitTest --no-daemon  # Unit tests
.\gradlew.bat :app:detekt --no-daemon             # Code quality
```

## Testing Stack
- JUnit 4, Turbine 1.1.0, Robolectric 4.13, Roborazzi 1.25.0
- Fakes only (no MockK, no mocks)
- `@Config(sdk = [34], application = android.app.Application::class)` on all DAO/VM/Compose tests
- Snapshot output: `app/src/test/snapshots/`

## Screens & Navigation
| Route | Screen |
|-------|--------|
| `today` | Today (food diary) |
| `product_search/{date}/{mealType}` | Product search |
| `add_product?name={name}` | Add custom product |
| `history` | Calendar-based history list |
| `history_day/{date}` | Day detail (read-only) |
| `weight` | Weight tracking chart |
| `settings` | Daily goals |
```

---

## Part 10: Cursor Rules Files (Verbatim)

Create the directory `.cursor/rules/` and write each file below.

> **Formatting note**: Each file's content is shown inside a code block. Where the content
> itself contains triple-backtick code blocks (e.g. powershell or kotlin examples), those
> are part of the target file's content — they are **not** closing the outer block.
> Read each section from its `###` header to the next `---` separator.

---

### `.cursor/rules/project-context.mdc`

```markdown
---
description: Always-on project context for Diet Helper Android app. Apply to every conversation.
globs:
alwaysApply: true
---

# Diet Helper — Project Context

Android calorie tracker. Clean Architecture + Hilt + Room + Compose + Kotlin.
Package: `com.k.shavrin.diethelper`
Working directory: the project root (where CLAUDE.md lives).

## Layer Rules (enforce strictly)
- domain/ — pure Kotlin, zero Android imports
- data/ — knows domain, never presentation
- presentation/ — knows domain, never data
- ViewModels depend on UseCases only
- Every screen has: UiState.kt + ViewModel.kt + Screen.kt
- Screen exposes `<Name>Content(state, onXxx)` for testability; `<Name>Screen` is thin Hilt wrapper

## Coding Rules
- No LiveData — StateFlow/Flow only
- No MockK — Fakes using MutableStateFlow
- No TODO/FIXME in production code
- Mappers are extension functions in separate files
- Russian strings hardcoded in UI (not strings.xml)
- @HiltViewModel + hiltViewModel() everywhere
- @Singleton on repos and services

## Key Tech Facts
- Room DB version 2; MIGRATION_1_2 must be registered in DatabaseModule
- LocalDate ↔ Long via TypeConverter (epochDay)
- SavedMeal uses 1:N relation via @Transaction + @Relation
- InMemoryMealClipboard is @Singleton injected in TodayViewModel
- DailyGoals stored in DataStore, not Room
- @Config(application = android.app.Application::class) mandatory on all DAO/Compose tests

## Build Commands (set JAVA_HOME first)
```powershell
$env:JAVA_HOME = "<PATH_TO_ANDROID_STUDIO_JBR>"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixDomain.tmpDir=C:\tmp"
$env:TEMP = "C:\tmp"; $env:TMP = "C:\tmp"
New-Item -ItemType Directory -Path "C:\tmp" -Force | Out-Null
```
```

---

### `.cursor/rules/dh-workflow.mdc`

```markdown
---
description: Main dh workflow — orchestrates feature/bugfix development. Invoke with @dh-workflow.
globs:
alwaysApply: false
---

# dh Workflow — Diet Helper

Use this rule when the user asks to implement a feature or fix a bug in diet_helper.

Usage:
  @dh-workflow --feature <description>
  @dh-workflow --bugfix  <description>

---

## Workflow: --feature

### Phase 1 — Spec

Explore the relevant codebase area. Ask ≤3 questions to clarify:
- Affected screen(s)? New screen or extension?
- New use case or extend existing?
- New Room entity / DataStore key needed?
- UI validation rules? Edge states (loading/empty/error)?

Output SPEC and wait for user approval:

```
=== SPEC ===
TASK: feature
WHAT: [one sentence]
LAYERS: [domain] [data] [presentation]
CHANGED_HINT: [files to read]
TEST_TYPES: unit [dao] [compose-ui] [screenshot]
CONSTRAINTS: [specific rules or "none"]
```

**Do not proceed until user confirms SPEC.**

### Phase 2 — Implement (follow @dh-developer patterns)

Implement code bottom-up: domain → data → presentation.
Read existing similar files first. Match patterns exactly.
Write ONE smoke test per new use case.

### Phase 3 — Write Tests (follow @dh-tester patterns)

For each TEST_TYPE in SPEC:
- unit: ViewModels, UseCases, Repository impls
- dao: Room DAO methods with in-memory DB
- compose-ui: Screen composables via Compose testing
- screenshot: Roborazzi snapshots

### Phase 4 — Verify (follow @dh-runner patterns)

Run tests and detekt. Fix any failures before reporting.

### Phase 5 — Docs (follow @dh-docs patterns)

Update DOCUMENTATION.md (if it exists) with new feature info.

### Phase 6 — Report

```
✅ feat: [description]
   Tests: [N passed]
   Detekt: ok
   Files: [list]
```

---

## Workflow: --bugfix

### Phase 1 — Locate
Read bug description. Ask only if reproduction steps unclear.

### Phase 2 — Fix
Fix root cause. Write regression test (red→green).

### Phase 3 — Verify
Run tests. Fix any failures.

### Phase 4 — Report
```
🐛 fix: [description]
   Root cause: [one sentence]
   Tests: [N passed]
```

---

## Rules
- Never skip the SPEC approval step for features.
- Never write code outside SPEC scope.
- Fix all test failures before reporting success.
- Maximum 3 clarifying questions before generating SPEC.
```

---

### `.cursor/rules/dh-developer.mdc`

```markdown
---
description: Code implementation patterns for diet_helper. Apply when implementing features or fixes.
globs: ["**/*.kt"]
alwaysApply: false
---

# Developer Patterns — diet_helper

## Before Writing Any Code

1. Read CLAUDE.md (project root) for tech stack and layer rules.
2. Read the files listed in SPEC CHANGED_HINT.
3. Read 1–2 similar existing files to match patterns exactly.

## Layer Order (always bottom-up)

1. `domain/model/` — new data classes if needed (zero Android imports)
2. `domain/repository/` — new interface methods if needed
3. `domain/usecase/` — one class per use case
4. `data/local/entity/` + `data/local/dao/` — if new persistence needed
5. `data/repository/` — implement new interface methods
6. `di/` — update Hilt modules if new bindings needed
7. `presentation/screen/<name>/` — UiState → ViewModel → Screen

## UseCase Pattern

```kotlin
class GetXxxUseCase @Inject constructor(private val repository: XxxRepository) {
    operator fun invoke(/* params */): Flow<Xxx> = repository.getXxx()
}

class DoXxxUseCase @Inject constructor(private val repository: XxxRepository) {
    suspend operator fun invoke(/* params */) = repository.doXxx(/* params */)
}
```

## ViewModel Pattern

```kotlin
@HiltViewModel
class XxxViewModel @Inject constructor(
    private val getXxx: GetXxxUseCase,
    private val doXxx: DoXxxUseCase,
) : ViewModel() {

    val uiState: StateFlow<XxxUiState> = getXxx()
        .map { data -> XxxUiState.Success(data) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), XxxUiState.Loading)

    fun onAction() { viewModelScope.launch { doXxx() } }
}
```

## Screen Pattern

```kotlin
@Composable
fun XxxScreen(viewModel: XxxViewModel = hiltViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    XxxContent(state = state, onAction = viewModel::onAction)
}

@Composable
fun XxxContent(state: XxxUiState, onAction: () -> Unit) {
    // full UI implementation here
}
```

## Mapper Pattern

```kotlin
// In data/mapper/XxxMapper.kt — extension functions only
fun XxxEntity.toDomain(): Xxx = Xxx(id = id, name = name)
fun Xxx.toEntity(): XxxEntity = XxxEntity(id = id, name = name)
fun List<XxxEntity>.toDomain(): List<Xxx> = map { it.toDomain() }
```

## Commit Format

```
feat: <imperative verb> <what>   (≤72 chars, no period)
fix:  <imperative verb> <what>
```
```

---

### `.cursor/rules/dh-tester.mdc`

```markdown
---
description: Test writing patterns for diet_helper. Apply when writing tests.
globs: ["**/*Test.kt"]
alwaysApply: false
---

# Tester Patterns — diet_helper

## Non-Negotiable Rules

**Fakes only — never MockK or any mocking library.**
- Use existing Fakes from `app/src/test/.../data/Fake*.kt`
- New repo without Fake: create `Fake<Name>Repository` with `MutableStateFlow` + `fun seed(...)`

**Naming**:
- Test class: `<TestedClass>Test`
- Method: backtick BDD — `` `returns zero when list is empty`() ``

**Never**: `@Ignore`, loosening assertions, deleting failing tests.

## Test Type: unit (ViewModel)

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class XxxViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepo: FakeXxxRepository
    private lateinit var viewModel: XxxViewModel

    @Before fun setUp() {
        fakeRepo = FakeXxxRepository()
        viewModel = XxxViewModel(GetXxxUseCase(fakeRepo))
    }

    @Test
    fun `description`() = runTest {
        fakeRepo.seed(listOf(/* test data */))
        viewModel.uiState.test {
            val state = awaitItem() as XxxUiState.Success
            assertEquals(expected, state.field)
        }
    }
}
```

## Test Type: unit (UseCase)

```kotlin
class XxxUseCaseTest {
    private val fakeRepo = FakeXxxRepository()
    private val useCase = XxxUseCase(fakeRepo)

    @Test
    fun `returns empty list when repository is empty`() = runTest {
        val result = useCase().first()
        assertTrue(result.isEmpty())
    }
}
```

## Test Type: dao

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class XxxDaoTest {
    private lateinit var db: DietHelperDatabase
    private lateinit var dao: XxxDao

    @Before fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DietHelperDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.xxxDao()
    }

    @After fun tearDown() { db.close() }
}
```

**⚠️ CRITICAL**: `application = android.app.Application::class` is mandatory.
Without it, `DietHelperApplication.onCreate()` causes `IllegalStateException` with in-memory DB.

**@Upsert note**: `@Upsert` resolves by PRIMARY KEY, not UNIQUE index.
For update tests: capture id from first insert, reuse it:
```kotlin
val id = dao.upsertEntry(entity.copy(id = 0))
dao.upsertEntry(entity.copy(id = id, field = newValue))
```

## Test Type: compose-ui

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class XxxScreenContentTest {
    @get:Rule val composeTestRule = createComposeRule()

    private fun defaultState() = XxxUiState.Success(/* sensible defaults */)

    @Test
    fun `shows title`() {
        composeTestRule.setContent {
            DietHelperTheme { XxxContent(state = defaultState(), onXxx = {}) }
        }
        composeTestRule.onNodeWithText("Expected Title").assertIsDisplayed()
    }
}
```

**Cover**: title shown, field values displayed, error messages visible,
button enabled/disabled states, success banner shown/hidden.

## Test Type: screenshot (Roborazzi)

```kotlin
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class XxxScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()
    private val roborazziOptions = RoborazziOptions(
        recordOptions = RoborazziOptions.RecordOptions(resizeScale = 0.5)
    )

    @Test
    fun `Xxx light theme`() {
        composeTestRule.setContent { DietHelperTheme(darkTheme = false) { XxxContent(...) } }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `Xxx dark theme`() {
        composeTestRule.setContent { DietHelperTheme(darkTheme = true) { XxxContent(...) } }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }
}
```

Snapshots stored in `app/src/test/snapshots/`.
Run `.\gradlew.bat :app:recordRoborazziDebug` first, then `verifyRoborazziDebug`.
```

---

### `.cursor/rules/dh-runner.mdc`

```markdown
---
description: Build and test verification for diet_helper. Apply when running Gradle tasks.
globs:
alwaysApply: false
---

# Runner — diet_helper

## Environment Setup (apply before every Gradle command)

```powershell
$env:JAVA_HOME = "<PATH_TO_ANDROID_STUDIO_JBR>"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixDomain.tmpDir=C:\tmp"
$env:TEMP = "C:\tmp"; $env:TMP = "C:\tmp"
New-Item -ItemType Directory -Path "C:\tmp" -Force | Out-Null
```

> **⚠️ Replace `<PATH_TO_ANDROID_STUDIO_JBR>`** with the actual path.
> Find it: Android Studio → File → Project Structure → SDK → JDK location.
> Example: `C:\Program Files\Android\Android Studio\jbr`

## Step 1 — KSP Code Generation

```powershell
.\gradlew.bat :app:kspDebugKotlin --no-daemon 2>&1 |
  Select-String -Pattern "error:|Error|Exception|FAILED|BUILD" |
  Select-Object -Last 40
```

Run this after changing Room/Hilt annotations.

## Step 2 — Unit Tests

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon 2>&1 |
  Select-String -Pattern "PASSED|FAILED|ERROR|tests|BUILD" |
  Select-Object -Last 40
```

Parse: count PASSED and FAILED. If BUILD FAILED → collect error lines.

## Step 3 — Detekt

```powershell
.\gradlew.bat :app:detekt --no-daemon 2>&1 |
  Select-String -Pattern "error|violation|BUILD" |
  Select-Object -Last 20
```

If BUILD SUCCESSFUL → "ok". If violations → count and collect messages.

## Step 4 — Screenshots (only when new screenshots were written)

```powershell
# Record baselines first
.\gradlew.bat :app:recordRoborazziDebug --no-daemon 2>&1 |
  Select-String -Pattern "FAILED|BUILD" | Select-Object -Last 10

# Then verify
.\gradlew.bat :app:verifyRoborazziDebug --no-daemon 2>&1 |
  Select-String -Pattern "FAILED|BUILD" | Select-Object -Last 10
```

## Success Criteria

All tests green + detekt clean before reporting any feature/fix complete.
```

---

### `.cursor/rules/dh-docs.mdc`

```markdown
---
description: Documentation maintenance for diet_helper. Apply when updating docs after features/fixes.
globs: ["DOCUMENTATION.md", "CLAUDE.md"]
alwaysApply: false
---

# Docs — diet_helper

Maintain `DOCUMENTATION.md` (product docs) and `CLAUDE.md` (developer cheatsheet).

## DOCUMENTATION.md — What to Update

### After a feature

| New item | Section to update |
|----------|-------------------|
| New screen | **Screens** — add subsection: purpose, key behaviours, UiState fields |
| New user flow | **User Flows** — add numbered steps |
| New domain model/field | **Domain Model** — update table |
| New architectural decision | **Architecture Decisions Log** — Date, Decision, Reason |
| Any completed iteration | **Feature Changelog** — add "Iteration N" heading |

### After a bugfix

| Fixed item | Section |
|------------|---------|
| Design gap found | **Architecture Decisions Log** |
| No structural change | **Feature Changelog** only — `- fix: [description]` |

### Changelog format

```markdown
### Iteration N — [Theme]
- feat: [what was added]
- fix: [what was fixed]
```

## CLAUDE.md — What to Update

Only update when there is genuinely new developer-facing information:
- New screen route → **Screens & Navigation** table
- New domain model → **Architecture** model list
- New build command → **Build** section
- New tech decision → **Key Technical Decisions**

## Rules

- Add ≤10 lines per update in DOCUMENTATION.md. No prose padding.
- Add ≤5 lines per update in CLAUDE.md. Facts only.
- Never delete or rewrite existing content in either file.
- Never duplicate content between the two files.
- If nothing is genuinely new → output "No documentation update needed." and stop.
```

---

## Part 11: Test Infrastructure (Verbatim Files)

Create these files exactly as shown.

### `app/src/test/java/com/k/shavrin/diethelper/util/MainDispatcherRule.kt`

```kotlin
package com.k.shavrin.diethelper.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class MainDispatcherRule(
    val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) { Dispatchers.setMain(testDispatcher) }
    override fun finished(description: Description) { Dispatchers.resetMain() }
}
```

### `app/src/test/java/com/k/shavrin/diethelper/data/FakeProductRepository.kt`

```kotlin
package com.k.shavrin.diethelper.data

import com.k.shavrin.diethelper.domain.model.Product
import com.k.shavrin.diethelper.domain.repository.ProductRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeProductRepository : ProductRepository {

    private val products = MutableStateFlow<List<Product>>(emptyList())
    private var nextId = 1L

    fun seed(initial: List<Product>) {
        products.value = initial.mapIndexed { index, product ->
            if (product.id == 0L) product.copy(id = (index + 1).toLong()) else product
        }
        nextId = (products.value.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override fun getAllProducts(): Flow<List<Product>> = products.map { list ->
        list.sortedWith(compareByDescending<Product> { it.isFavorite }.thenBy { it.name.lowercase() })
    }

    override fun searchProducts(query: String): Flow<List<Product>> = products.map { list ->
        list.filter { it.name.contains(query, ignoreCase = true) }
            .sortedWith(compareByDescending<Product> { it.isFavorite }.thenBy { it.name.lowercase() })
    }

    override suspend fun getProductById(id: Long): Product? =
        products.value.firstOrNull { it.id == id }

    override suspend fun addProduct(product: Product): Long {
        val id = nextId++
        products.update { it + product.copy(id = id) }
        return id
    }

    override suspend fun updateProduct(product: Product) {
        products.update { list -> list.map { if (it.id == product.id) product else it } }
    }

    override suspend fun deleteProduct(product: Product) {
        products.update { list -> list.filterNot { it.id == product.id } }
    }

    override suspend fun toggleFavorite(productId: Long, isFavorite: Boolean) {
        products.update { list ->
            list.map { if (it.id == productId) it.copy(isFavorite = isFavorite) else it }
        }
    }
}
```

### `app/src/test/java/com/k/shavrin/diethelper/data/FakeFoodEntryRepository.kt`

```kotlin
package com.k.shavrin.diethelper.data

import com.k.shavrin.diethelper.domain.model.FoodEntry
import com.k.shavrin.diethelper.domain.repository.FoodEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class FakeFoodEntryRepository : FoodEntryRepository {

    private val entries = MutableStateFlow<List<FoodEntry>>(emptyList())
    private var nextId = 1L

    fun seed(initial: List<FoodEntry>) {
        entries.value = initial.mapIndexed { index, entry ->
            if (entry.id == 0L) entry.copy(id = (index + 1).toLong()) else entry
        }
        nextId = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override fun getEntriesForDay(date: LocalDate): Flow<List<FoodEntry>> =
        entries.map { list -> list.filter { it.date == date } }

    override fun getDistinctDatesDescending(): Flow<List<LocalDate>> =
        entries.map { list -> list.map { it.date }.distinct().sortedDescending() }

    override fun getEntriesForDates(dates: List<LocalDate>): Flow<List<FoodEntry>> =
        entries.map { list -> list.filter { it.date in dates } }

    override suspend fun addEntry(entry: FoodEntry): Long {
        val id = nextId++
        entries.update { it + entry.copy(id = id) }
        return id
    }

    override suspend fun updateEntry(entry: FoodEntry) {
        entries.update { list -> list.map { if (it.id == entry.id) entry else it } }
    }

    override suspend fun deleteEntry(entry: FoodEntry) {
        entries.update { list -> list.filterNot { it.id == entry.id } }
    }

    override suspend fun copyEntryToDay(entry: FoodEntry, targetDate: LocalDate) {
        addEntry(entry.copy(id = 0L, date = targetDate))
    }
}
```

### `app/src/test/java/com/k/shavrin/diethelper/data/FakeWeightRepository.kt`

```kotlin
package com.k.shavrin.diethelper.data

import com.k.shavrin.diethelper.domain.model.WeightEntry
import com.k.shavrin.diethelper.domain.repository.WeightRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.LocalDate

class FakeWeightRepository : WeightRepository {

    private val entries = MutableStateFlow<List<WeightEntry>>(emptyList())
    private var nextId = 1L

    fun seed(initial: List<WeightEntry>) {
        entries.value = initial.mapIndexed { index, entry ->
            if (entry.id == 0L) entry.copy(id = (index + 1).toLong()) else entry
        }
        nextId = (entries.value.maxOfOrNull { it.id } ?: 0L) + 1
    }

    override fun getAllEntries(): Flow<List<WeightEntry>> =
        entries.map { list -> list.sortedByDescending { it.date } }

    override suspend fun getEntryByDate(date: LocalDate): WeightEntry? =
        entries.value.firstOrNull { it.date == date }

    override suspend fun upsertEntry(date: LocalDate, weightKg: Float) {
        val existing = entries.value.firstOrNull { it.date == date }
        if (existing != null) {
            entries.update { list ->
                list.map { if (it.id == existing.id) it.copy(weightKg = weightKg) else it }
            }
        } else {
            val id = nextId++
            entries.update { it + WeightEntry(id = id, date = date, weightKg = weightKg) }
        }
    }

    override suspend fun deleteEntry(entry: WeightEntry) {
        entries.update { list -> list.filterNot { it.id == entry.id } }
    }
}
```

### `app/src/test/java/com/k/shavrin/diethelper/data/FakeGoalsRepository.kt`

```kotlin
package com.k.shavrin.diethelper.data

import com.k.shavrin.diethelper.domain.model.DailyGoals
import com.k.shavrin.diethelper.domain.repository.GoalsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeGoalsRepository(
    initial: DailyGoals = DailyGoals.DEFAULT
) : GoalsRepository {

    private val goals = MutableStateFlow(initial)

    override fun getDailyGoals(): Flow<DailyGoals> = goals
    override suspend fun saveGoals(goals: DailyGoals) { this.goals.value = goals }

    val current: DailyGoals get() = goals.value
}
```

### `app/src/test/java/com/k/shavrin/diethelper/data/FakeSavedMealRepository.kt`

```kotlin
package com.k.shavrin.diethelper.data

import com.k.shavrin.diethelper.domain.model.SavedMeal
import com.k.shavrin.diethelper.domain.model.SavedMealItem
import com.k.shavrin.diethelper.domain.repository.SavedMealRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeSavedMealRepository : SavedMealRepository {

    private val meals = MutableStateFlow<List<SavedMeal>>(emptyList())
    private var nextId = 1L

    override fun getSavedMeals(): Flow<List<SavedMeal>> =
        meals.map { list -> list.sortedBy { it.name.lowercase() } }

    override suspend fun saveMeal(name: String, items: List<SavedMealItem>) {
        meals.update { list -> list.filterNot { it.name.equals(name, ignoreCase = false) } }
        val id = nextId++
        meals.update { list ->
            list + SavedMeal(
                id = id,
                name = name,
                items = items.mapIndexed { index, item ->
                    item.copy(id = (nextId + index), savedMealId = id)
                }
            )
        }
    }

    override suspend fun deleteMeal(id: Long) {
        meals.update { list -> list.filterNot { it.id == id } }
    }
}
```

---

## Part 12: Phase-by-Phase Build Plan

> Execute each phase in order. Verify before proceeding to the next.

---

### Phase 1 — Scaffolding (Gradle + Theme + Entry Point)

**Create**:
- `settings.gradle.kts`, `build.gradle.kts` (root) — from Part 8
- `gradle/libs.versions.toml` — from Part 8
- `gradle/wrapper/gradle-wrapper.properties` — from Part 8
- `app/build.gradle.kts` — from Part 8
- `app/src/main/AndroidManifest.xml` — from Part 8
- `app/src/main/res/values/strings.xml` — from Part 8
- `app/src/main/res/values/themes.xml` — from Part 8
- `config/detekt/detekt.yml` — from Part 8
- `CLAUDE.md` — from Part 9
- All 6 `.cursor/rules/*.mdc` files — from Part 10
- `DietHelperApplication.kt` — `@HiltAndroidApp`, empty onCreate
- `MainActivity.kt` — `@AndroidEntryPoint`, empty `setContent { DietHelperTheme {} }`
- `presentation/theme/Color.kt` — Material3 green palette (Green40/80, GreenGrey40/80, Lime40/80)
- `presentation/theme/Type.kt` — standard Material3 typography
- `presentation/theme/Theme.kt` — `DietHelperTheme(darkTheme, content)` with `dynamicColor = false`

**Verify**: `.\gradlew.bat :app:assembleDebug --no-daemon` — BUILD SUCCESSFUL

---

### Phase 2 — Domain Layer

**Create all files in `domain/`**:
- All 10 domain models from Part 5 (exact code)
- All 5 repository interfaces from Part 5
- All 22 use cases (see Part 3 tree)
  - Use case bodies from Part 5 descriptions
  - `GetDailySummaryUseCase` — `List<FoodEntry>.toSummary()` companion
  - `GetHistoryUseCase` — `flatMapLatest` on distinct dates
  - `GetStreakUseCase` — check last 90 days, count backward from yesterday + today
  - `GetWeekDayStatusesUseCase` — `combine` entries + goals, `computeDayStatus()` companion

**Verify**: `.\gradlew.bat :app:compileDebugKotlin --no-daemon` — no domain errors

---

### Phase 3 — Data Layer: Room

**Create all files in `data/local/`**:
- `converter/Converters.kt` — `@TypeConverter` LocalDate ↔ Long via `epochDay`
- All 8 entity files from Part 6 (exact schema)
- All 4 DAO files from Part 6 (exact queries)
- `DietHelperDatabase.kt` from Part 6 (exact code including MIGRATION_1_2)
- All 4 mapper files in `data/mapper/` (extension functions)

**Verify**: `.\gradlew.bat :app:kspDebugKotlin --no-daemon` — Room schema generated, no errors

---

### Phase 4 — Data Layer: DataStore + Repository Implementations

**Create**:
- `data/local/GoalsDataSource.kt` — DataStore wrapper with all 7 keys + `is_seeded` key
- `data/repository/ProductRepositoryImpl.kt`
- `data/repository/FoodEntryRepositoryImpl.kt`
- `data/repository/WeightRepositoryImpl.kt`
- `data/repository/GoalsRepositoryImpl.kt`
- `data/repository/SavedMealRepositoryImpl.kt` — `@Singleton`, upsert by name

**Verify**: `.\gradlew.bat :app:compileDebugKotlin --no-daemon` — no data layer errors

---

### Phase 5 — Hilt DI

**Create**:
- `di/DatabaseModule.kt` — provides DB + all 4 DAOs as `@Singleton`
  - Must call `.addMigrations(DietHelperDatabase.MIGRATION_1_2).fallbackToDestructiveMigration()`
- `di/DataStoreModule.kt` — provides `DataStore<Preferences>` as `@Singleton`
- `di/RepositoryModule.kt` — `@Binds` all 5 repository implementations as `@Singleton`

**Verify**: `.\gradlew.bat :app:assembleDebug --no-daemon` — Hilt component generated

---

### Phase 6 — Navigation + Shell UI

**Create**:
- `presentation/navigation/Routes.kt` — from Part 7
- `presentation/navigation/BottomNavItem.kt` — sealed class, 4 items with icons and Russian labels
- `presentation/navigation/AppNavHost.kt` — `NavHost` with all 7 destinations
- `MainActivity.kt` (replace) — `Scaffold` + `NavigationBar` + `AppNavHost`
- Placeholder screens for all 5 tabs (just `Text("Сегодня")` etc.)
- `presentation/util/InMemoryMealClipboard.kt` — from Part 7 spec

**Verify**: `.\gradlew.bat :app:assembleDebug --no-daemon` — app builds, 4 tabs switch

---

### Phase 7 — Today + Product Screens (Core Flow)

**Create**:
- `presentation/util/Format.kt` — date/nutrition/weight formatting, Russian locale
- `presentation/util/MacroColorUtil.kt` — `macroProgressColor()`, `caloriesProgressColor()`
- `presentation/components/DailySummaryCard.kt` — from Part 7 spec
- `presentation/screen/today/TodayUiState.kt` — sealed interface with Loading/Success/Error
- `presentation/screen/today/TodayViewModel.kt` — full implementation:
  - `combine` of entries, goals, week statuses, streak, clipboard
  - date navigation (no future dates)
  - meal section expansion tracking
  - copy/paste/save meal actions
- `presentation/screen/today/TodayScreen.kt`:
  - `TodayScreen` (Hilt wrapper)
  - `TodayContent(state, onXxx..., readOnly: Boolean = false)` (public composable)
  - `WeekDateHeader`, `StreakRow`, expandable meal sections, action bar, entry dropdown
- `presentation/screen/product/ProductUiState.kt`
- `presentation/screen/product/ProductViewModel.kt` — debounced search, saved meals
- `presentation/screen/product/ProductSearchScreen.kt` — tab bar, search, saved meals tab
- `presentation/screen/product/AddProductUiState.kt`
- `presentation/screen/product/AddProductViewModel.kt`
- `presentation/screen/product/AddProductScreen.kt`

**Verify**: Full add-food flow works end-to-end. Data persists after app restart.

---

### Phase 8 — History + Weight + Settings Screens

**Create**:
- `presentation/screen/history/HistoryUiState.kt`
- `presentation/screen/history/HistoryViewModel.kt`
- `presentation/screen/history/HistoryScreen.kt`
- `presentation/screen/history/HistoryDayViewModel.kt`
- `presentation/screen/history/HistoryDayScreen.kt` — reuses `TodayContent(readOnly = true)`
- `presentation/screen/weight/WeightUiState.kt`
- `presentation/screen/weight/WeightViewModel.kt`
- `presentation/screen/weight/WeightScreen.kt`
- `presentation/screen/settings/SettingsUiState.kt`
- `presentation/screen/settings/SettingsViewModel.kt`
- `presentation/screen/settings/SettingsScreen.kt`
- `DietHelperApplication.kt` (replace) — inject `DatabaseSeeder`, call `seedIfNeeded()` in `onCreate`
- `data/local/DatabaseSeeder.kt` — 15 default products (see Part 7)

**Verify**: All 4 tabs functional. Settings persist. Weight deltas correct.

---

### Phase 9 — Test Infrastructure + All Tests

**First, create test infrastructure** (Part 11 verbatim):
- `MainDispatcherRule.kt`
- All 5 `Fake*Repository.kt` files

**Then create all test files** (47 total, see Part 3 tree). For each test file:
- Use patterns from `@dh-tester` rules (Part 10)
- DAO tests: `@Config(sdk = [34], application = android.app.Application::class)`
- ViewModel tests: `@RunWith(RobolectricTestRunner::class)` + `MainDispatcherRule`
- Compose UI tests: `createComposeRule()` + `DietHelperTheme { XxxContent(...) }`
- Screenshot tests: `captureRoboImage()` for light + dark themes

**Run and fix until all pass**:
```powershell
$env:JAVA_HOME = "<PATH_TO_ANDROID_STUDIO_JBR>"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixDomain.tmpDir=C:\tmp"
$env:TEMP = "C:\tmp"; $env:TMP = "C:\tmp"
New-Item -ItemType Directory -Path "C:\tmp" -Force | Out-Null
.\gradlew.bat :app:testDebugUnitTest --no-daemon
```

**For screenshots** — record baselines first:
```powershell
.\gradlew.bat :app:recordRoborazziDebug --no-daemon
.\gradlew.bat :app:verifyRoborazziDebug --no-daemon
```

---

### Phase 10 — Polish + Detekt

- Add `@Preview` annotations (light + dark) to all Screen composables
- Run detekt and fix all violations:
  ```powershell
  .\gradlew.bat :app:detekt --no-daemon
  ```
- Create `app/proguard-rules.pro` (empty file is fine for now)
- Verify release build: `.\gradlew.bat :app:assembleRelease --no-daemon`

---

## Part 13: Verification Checklist

After all 10 phases, confirm:

```powershell
# Set up environment (replace <PATH_TO_ANDROID_STUDIO_JBR>)
$env:JAVA_HOME = "<PATH_TO_ANDROID_STUDIO_JBR>"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixDomain.tmpDir=C:\tmp"
$env:TEMP = "C:\tmp"; $env:TMP = "C:\tmp"
New-Item -ItemType Directory -Path "C:\tmp" -Force | Out-Null

# 1. All unit tests pass
.\gradlew.bat :app:testDebugUnitTest --no-daemon

# 2. Detekt clean
.\gradlew.bat :app:detekt --no-daemon

# 3. Screenshot tests pass (after recording baselines)
.\gradlew.bat :app:recordRoborazziDebug --no-daemon
.\gradlew.bat :app:verifyRoborazziDebug --no-daemon

# 4. Debug build succeeds
.\gradlew.bat :app:assembleDebug --no-daemon

# 5. Release build succeeds (R8 minification)
.\gradlew.bat :app:assembleRelease --no-daemon
```

**Expected final result**:
- ✅ 47 tests PASSED / 0 FAILED
- ✅ Detekt: BUILD SUCCESSFUL
- ✅ Screenshots: BUILD SUCCESSFUL
- ✅ assembleDebug: BUILD SUCCESSFUL
- ✅ assembleRelease: BUILD SUCCESSFUL
- ✅ `.cursor/rules/` contains 6 `.mdc` files
- ✅ `@dh-workflow` works in Cursor Composer when referenced as `@dh-workflow --feature ...`

---

## Appendix: Known Pitfalls

| Pitfall | Fix |
|---------|-----|
| `IllegalStateException: Illegal connection pointer` on DAO tests | Add `@Config(application = android.app.Application::class)` |
| `Unable to establish loopback connection` (JBR 21 on Windows) | Set `$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixDomain.tmpDir=C:\tmp"` and `$env:TEMP = "C:\tmp"` |
| `@Upsert` doesn't update existing entry | Capture id from first insert; reuse it for the update call |
| `Room schema version mismatch` | Register `MIGRATION_1_2` in `DatabaseModule` via `.addMigrations(...)`. Also add `.fallbackToDestructiveMigration()` as a safety net for dev builds. Both together. |
| `Hilt component generation fails` | Run `kspDebugKotlin` first, then `assembleDebug` |
| Screenshot diffs fail on first run | Run `recordRoborazziDebug` before `verifyRoborazziDebug` |
| Cursor rules not picked up | Ensure files are in `.cursor/rules/` (not `.cursorrules`), use `.mdc` extension |
