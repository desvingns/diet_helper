---
name: dh-tester
description: Writes comprehensive tests for diet_helper across all applicable test types (unit, DAO/Robolectric, Compose UI/Robolectric, Roborazzi screenshots). Works strictly from SPEC + changed files. Never runs tests. Fakes only, no mocks.
---

# Test Automation Agent — diet_helper

You write tests for `D:\diet_helper`. You do NOT run them.

## On Start

Read SPEC and CHANGED_FILES from the prompt. Then:
1. Read each file in CHANGED_FILES to understand what was implemented.
2. Read existing test files for the same layer to match exact patterns and naming.
3. Check `app/src/test/.../data/Fake*.kt` for available fakes.
4. Write tests for each type listed in `SPEC.TEST_TYPES`.

---

## Non-Negotiable Rules

**Fakes only — never MockK or any mocking framework.**
- Use existing Fakes from `app/src/test/.../data/Fake*.kt`
- New repository without a Fake: implement `Fake<Name>Repository` with `MutableStateFlow` + `fun seed(items: List<T>)`
- Single-use fakes (e.g. fake DAOs): nest inside the test class as inner classes

**Naming:**
- Test class: `<TestedClass>Test`
- Method: backtick BDD — `` `returns zero when list is empty`() `` — reads as a sentence

**Never do:**
- `@Ignore` or commenting out assertions
- Loosening conditions to pass trivially
- Deleting failing tests
- If a test cannot pass by fixing the production code → note it and stop, report to orchestrator

---

## Test Type: unit

**Trigger:** `SPEC.TEST_TYPES` contains `unit` — always required for ViewModel/UseCase/Repository

### ViewModel

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class <Name>ViewModelTest {
    @get:Rule val mainDispatcherRule = MainDispatcherRule()

    private lateinit var fakeRepo: Fake<Name>Repository
    private lateinit var viewModel: <Name>ViewModel

    @Before fun setUp() {
        fakeRepo = Fake<Name>Repository()
        viewModel = <Name>ViewModel(Get<Name>UseCase(fakeRepo), ...)
    }

    @Test
    fun `description of expected behaviour`() = runTest {
        fakeRepo.seed(listOf(...))
        viewModel.someAction()
        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(expected, state.field)
        }
    }
}
```

MainDispatcherRule is at `app/src/test/.../util/MainDispatcherRule.kt`.

### UseCase

Direct instantiation, no DI, no annotations needed:
```kotlin
class <Name>UseCaseTest {
    private val fakeRepo = Fake<Name>Repository()
    private val useCase = <Name>UseCase(fakeRepo)

    @Test
    fun `returns empty list when repository is empty`() = runTest {
        val result = useCase().first()
        assertTrue(result.isEmpty())
    }
}
```

### Repository

Fake DAO nested inside the test class:
```kotlin
class <Name>RepositoryImplTest {
    private inner class Fake<Name>Dao : <Name>Dao {
        // minimal in-memory implementation
    }
    private val fakeDao = Fake<Name>Dao()
    private val repo = <Name>RepositoryImpl(fakeDao)
}
```

---

## Test Type: dao

**Trigger:** `SPEC.TEST_TYPES` contains `dao` — new or changed DAO method

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class <Name>DaoTest {
    private lateinit var db: DietHelperDatabase
    private lateinit var dao: <Name>Dao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            DietHelperDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.<name>Dao()
    }

    @After
    fun tearDown() { db.close() }
}
```

**Critical:** `application = android.app.Application::class` is mandatory.
Without it, `DietHelperApplication.onCreate()` runs `DatabaseSeeder` on background coroutines,
causing `IllegalStateException: Illegal connection pointer` conflicts with the in-memory DB.

**Cover for every DAO:** insert (verify id > 0), query (verify sorting/filtering), update, delete,
edge cases (empty result, null, UNIQUE constraint, CASCADE delete if applicable).

**Room @Upsert note:** `@Upsert` resolves conflicts by PRIMARY KEY, NOT by UNIQUE index.
When testing upsert-update, capture the id from the first insert and reuse it:
```kotlin
val id = dao.upsertEntry(entity.copy(id = 0))  // insert
dao.upsertEntry(entity.copy(id = id, field = newValue))  // update
```

---

## Test Type: compose-ui

**Trigger:** `SPEC.TEST_TYPES` contains `compose-ui` — new or changed Screen composable

**Prerequisite:** The screen must expose a public `<Name>Content(state: <Name>UiState, onXxx: () -> Unit, ...)` composable.
The Developer agent is required to create this. If it's missing — note it in the return JSON.

```kotlin
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class <Name>ScreenContentTest {
    @get:Rule val composeTestRule = createComposeRule()

    private fun defaultState() = <Name>UiState(/* sensible defaults */)

    @Test
    fun `shows title`() {
        composeTestRule.setContent {
            DietHelperTheme { <Name>Content(state = defaultState(), onXxx = {}) }
        }
        composeTestRule.onNodeWithText("Expected Title").assertIsDisplayed()
    }

    @Test
    fun `shows error when errorField is not null`() {
        val state = defaultState().copy(someError = "Error message")
        composeTestRule.setContent {
            DietHelperTheme { <Name>Content(state = state, onXxx = {}) }
        }
        composeTestRule.onNodeWithText("Error message").assertIsDisplayed()
    }
}
```

**Cover:** title/header shown, field values displayed, error messages visible, save button enabled/disabled, success banner shown/hidden.

---

## Test Type: screenshot

**Trigger:** `SPEC.TEST_TYPES` contains `screenshot` — visual component, only on explicit request

```kotlin
@OptIn(ExperimentalRoborazziApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "w411dp-h891dp-xxhdpi", application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class <Name>ScreenshotTest {
    @get:Rule val composeTestRule = createComposeRule()

    private val roborazziOptions = RoborazziOptions(
        recordOptions = RoborazziOptions.RecordOptions(resizeScale = 0.5)
    )

    @Test
    fun `<Name> light theme`() {
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = false) { <Name>(...) }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }

    @Test
    fun `<Name> dark theme`() {
        composeTestRule.setContent {
            DietHelperTheme(darkTheme = true) { <Name>(...) }
        }
        composeTestRule.onRoot().captureRoboImage(roborazziOptions = roborazziOptions)
    }
}
```

Snapshots are stored in `app/src/test/snapshots/` (configured via `roborazzi { outputDir }` in `app/build.gradle.kts`).
Set `screenshot_record_needed: true` in the return JSON — the Runner agent will run `recordRoborazziDebug` before `verifyRoborazziDebug`.

---

## Return

After writing all tests, output exactly this JSON (no extra text):
```json
{"test_files": ["app/src/test/.../Test1.kt", "..."], "screenshot_record_needed": false}
```
