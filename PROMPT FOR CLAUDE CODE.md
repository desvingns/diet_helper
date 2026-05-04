# Рацион — Android Calorie Tracker

## Как использовать этот файл

Это единый документ спецификации + план генерации.
- **Разделы 1–7** — базовый контекст, обязательный для каждой фазы.
- **Раздел 8** — 10 фаз. Каждая фаза — отдельный промт для отдельной сессии Claude Code.
  В начале каждой сессии скопируй **разделы 1–7** + нужную фазу из раздела 8.

---

## 1. Цель

Offline-first Android-приложение для учёта калорий и веса. Учебный проект, но с production-качеством архитектуры. Весь интерфейс на русском языке.

---

## 2. Технологический стек (точные версии)

```
Kotlin:               2.0.21
AGP:                  8.5.2
Gradle Wrapper:       8.9
Compose BOM:          2024.09.03
  ├── compose-ui:     1.7.x  (определяется BOM)
  ├── material3:      1.3.x  (определяется BOM)
  └── navigation-compose: 2.8.x (определяется BOM)
Room:                 2.6.1
Hilt:                 2.52
hilt-navigation-compose: 1.2.0
DataStore Preferences: 1.1.1
Coroutines:           1.9.0
Lifecycle (ViewModel, Runtime): 2.8.6
KSP:                  2.0.21-1.0.26

Тестирование:
  JUnit:              4.13.2
  MockK:              1.13.12
  Turbine:            1.1.0
  kotlinx-coroutines-test: 1.9.0
```

---

## 3. Идентификация проекта и сборка

```
Package name:         com.k.shavrin.diethelper
Application name:     "Рацион"
Min SDK:              26
Target SDK:           35
Compile SDK:          35
Version code:         1
Version name:         "1.0"

Build system:         Version catalog (gradle/libs.versions.toml)
Annotation processing: KSP (НЕ KAPT)
Kotlin JVM target:    17
Java compile target:  17
Minify:               только для release
Compose compiler:     встроен в Kotlin 2.0 — отдельный плагин НЕ нужен
buildFeatures { compose = true }
Структура:            single-module (только модуль app)
```

---

## 4. Архитектура

### Clean Architecture — три слоя

```
com.k.shavrin.diethelper
├── di/
│   ├── DatabaseModule.kt
│   ├── DataStoreModule.kt
│   └── RepositoryModule.kt
├── domain/
│   ├── model/
│   ├── repository/          # только интерфейсы
│   └── usecase/
├── data/
│   ├── local/
│   │   ├── entity/
│   │   ├── dao/
│   │   └── converter/
│   ├── repository/          # реализации интерфейсов
│   └── mapper/              # extension functions Entity ↔ Domain
└── presentation/
    ├── navigation/
    ├── theme/
    └── screen/
        ├── today/
        ├── history/
        ├── weight/
        ├── settings/
        └── product/
```

### Правила слоёв

- `domain` — чистый Kotlin, **ноль** Android-зависимостей
- `data` — знает о `domain`, не знает о `presentation`
- `presentation` — знает о `domain`, **не знает** о `data`
- ViewModel зависит только от UseCase, никогда напрямую от Repository
- Mapper — extension functions, по одному файлу на entity

---

## 5. Доменные модели (точные типы)

```kotlin
// domain/model/MealType.kt
enum class MealType { BREAKFAST, LUNCH, DINNER, SNACK }

// domain/model/Product.kt
data class Product(
    val id: Long = 0,
    val name: String,
    val caloriesPer100g: Float,
    val proteinPer100g: Float,
    val fatPer100g: Float,
    val carbsPer100g: Float,
    val isFavorite: Boolean = false
)

// domain/model/FoodEntry.kt
data class FoodEntry(
    val id: Long = 0,
    val productId: Long,
    val product: Product,       // всегда заполнен, никогда null
    val date: LocalDate,        // java.time.LocalDate
    val mealType: MealType,
    val multiplier: Float       // 1.0 = 100 г, 1.5 = 150 г
)

// domain/model/DailySummary.kt
data class DailySummary(
    val totalCalories: Float,
    val totalProtein: Float,
    val totalFat: Float,
    val totalCarbs: Float
)

// domain/model/DailyGoals.kt
data class DailyGoals(
    val calories: Float,
    val protein: Float,
    val fat: Float,
    val carbs: Float
)

// domain/model/WeightEntry.kt
data class WeightEntry(
    val id: Long = 0,
    val date: LocalDate,
    val weightKg: Float
)
```

### Room entities — принципы хранения

- `LocalDate` → `Long` через `TypeConverter` (метод `toEpochDay()` / `ofEpochDay()`)
- `MealType` → `String` через mapper (`.name` / `enumValueOf`)
- `Float` для всех нутриентов и веса
- `Long` с `autoGenerate = true` для всех id
- `FoodEntryEntity` имеет FOREIGN KEY → `ProductEntity.id`, `onDelete = CASCADE`
- `FoodEntryWithProduct` — `@Transaction` data class с `@Relation` для реактивного join
  (при изменении Product все связанные FoodEntry автоматически пересчитываются в Flow)

### Хранение дневных целей

`DataStore Preferences` — **не Room**, **не SharedPreferences**.

```kotlin
// Ключи в GoalsDataSource:
floatPreferencesKey("goal_calories")  // default: 2000f
floatPreferencesKey("goal_protein")   // default: 150f
floatPreferencesKey("goal_fat")       // default: 67f
floatPreferencesKey("goal_carbs")     // default: 250f
```

---

## 6. Навигация

```kotlin
// presentation/navigation/Routes.kt
object Routes {
    const val TODAY    = "today"
    const val HISTORY  = "history"
    const val WEIGHT   = "weight"
    const val SETTINGS = "settings"

    // date: ISO строка "yyyy-MM-dd", mealType: имя enum
    const val PRODUCT_SEARCH = "product_search/{date}/{mealType}"
    const val ADD_PRODUCT    = "add_product?name={name}"

    fun productSearch(date: String, mealType: String) = "product_search/$date/$mealType"
    fun addProduct(name: String = "") = "add_product?name=$name"
}
```

- `AppNavHost` в `presentation/navigation/AppNavHost.kt` — содержит весь `NavHost`
- `MainActivity` — `@AndroidEntryPoint`, `Scaffold` + `NavigationBar` + `AppNavHost`
- Bottom navigation: 4 пункта (Today, History, Weight, Settings)
- `ProductSearchScreen` и `AddProductScreen` — полноэкранные, **не** BottomSheet

---

## 7. Описание экранов (точное)

### Today Screen

- Вверху: строка с датой + кнопки `←` `→` для переключения дней
- Переход вперёд **заблокирован**, если текущая дата = сегодня
- По умолчанию открывается сегодняшняя дата
- 4 секции (Breakfast / Lunch / Dinner / Snack) — отображаются всегда, даже пустые
- Заголовок секции: название приёма пищи + суммарные калории секции + `IconButton(+)`
- Строка еды: название продукта, граммы (`multiplier * 100`), калории, макросы
- **Long-press** на строку еды → `DropdownMenu`:
  - "Изменить граммы" → `AlertDialog` с `OutlinedTextField` (предзаполнен текущими граммами)
  - "Удалить" → `AlertDialog` подтверждения → удалить
  - "Скопировать на другой день" → `DatePickerDialog` → скопировать запись на ту же секцию целевой даты
- Нет FAB — добавление только через `+` в заголовке секции
- Тап `+` → навигация на `ProductSearchScreen(date, mealType)`
- Внизу: `DailySummaryCard` — итоговые калории + макросы, `LinearProgressIndicator` от целей

### Product Search Screen

Аргументы навигации: `date: String`, `mealType: String`

- `OutlinedTextField` вверху для поиска
- Debounce **300 мс** перед запросом к БД (`flatMapLatest`)
- Пустой запрос → сначала избранное, затем все продукты по алфавиту
- Каждая строка: название + ккал/100г + `IconButton` (звёздочка для избранного)
- Тап на продукт → `AlertDialog` "Добавить в [секция]": `OutlinedTextField` для граммов (default: 100), кнопка "Добавить" → создаёт `FoodEntry`, pop back
- Если запрос непустой и **нет точного совпадения** по имени → в конце списка кнопка "Добавить «{query}» как новый продукт" → навигация на `AddProductScreen(name=query)`
- Empty state (нет ни одного продукта в БД): иконка + текст "Нет продуктов" + кнопка "Добавить"
- Состояния UI: `Loading`, `Success(products, query)`, `Error`

### Add Product Screen

Аргумент навигации: `name: String` (опциональный, предзаполняет поле "Название")

- 5 полей: Название (String), Калории (Float), Белки (Float), Жиры (Float), Углеводы (Float)
- Валидация при сабмите:
  - Название: не пустое
  - Калории: > 0
  - Белки, жиры, углеводы: >= 0
  - Ошибки через `supportingText` на каждом `OutlinedTextField`
- Кнопка "Сохранить" → валидация → сохранить → pop back

### History Screen

- `LazyColumn` дат, у которых есть хотя бы одна запись еды, отсортированный **по убыванию**
- Каждый элемент: дата + суммарные калории за день
- Тап → `HistoryDayScreen(date)` — read-only копия Today-экрана для этой даты
  - Нет кнопок `+`, нет long-press меню
- Empty state: текст "История пуста" по центру

### Weight Screen

- Вверху: `OutlinedTextField` для веса (Float, placeholder "Вес в кг") + кнопка "Сохранить"
- Если запись на сегодня уже есть → поле предзаполнено текущим значением (upsert)
- `LazyColumn` записей веса, отсортированный **по убыванию**
- Каждая строка: дата, вес в кг, дельта от предыдущей записи
  - "+X.X кг" зелёным / "−X.X кг" красным / "—" если первая запись
- Long-press на строку → `AlertDialog` подтверждения → удалить

### Settings Screen

- 4 `OutlinedTextField`: Калории (ккал), Белки (г), Жиры (г), Углеводы (г)
- Предзаполнены из DataStore при открытии экрана
- Кнопка "Сохранить" — сохраняет всё разом, **не** live-save
- Валидация: все поля > 0, не пустые; ошибки через `supportingText`

---

## 7.1 Общие UI/UX правила

- Material 3, Dark Theme через `dynamicColor = false`, явные цвета в `Color.kt`
- Каждый экран: состояния `Loading` (CircularProgressIndicator по центру), `Success`, `Error`
- Empty states: иконка Material + текст — для всех `LazyColumn`
- Все диалоги — `AlertDialog` composable (Material3)
- Русские строки — прямо в коде (hardcoded), `strings.xml` только для app name и launcher

---

## 7.2 Паттерны состояний и корутин

```kotlin
// Sealed class на каждый экран:
sealed class XxxUiState {
    object Loading : XxxUiState()
    data class Success(/* все нужные данные */) : XxxUiState()
    data class Error(val message: String) : XxxUiState()
}

// ViewModel:
val uiState: StateFlow<XxxUiState> = someFlow
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), XxxUiState.Loading)

// Поиск продуктов в ProductViewModel:
private val _searchQuery = MutableStateFlow("")
val products = _searchQuery
    .debounce(300)
    .flatMapLatest { query -> repository.searchProducts(query) }
    .stateIn(...)

// DailySummary — реактивно из двух источников:
val summary = combine(
    foodEntryRepository.getEntriesForDay(date),
    goalsRepository.getDailyGoals()
) { entries, goals -> computeSummary(entries, goals) }
```

---

## 7.3 Тестирование

```
Зависимости:
  junit:junit:4.13.2
  io.mockk:mockk:1.13.12
  app.cash.turbine:turbine:1.1.0
  org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0

Подход:
  - Fakes (не моки) для репозиториев: FakeProductRepository implements ProductRepository
  - UseCases тестируются с Fake репозиториями
  - ViewModels тестируются с Fake UseCase (через конструктор)
  - Flow проверяется через turbine: test { }
  - MainDispatcherRule с UnconfinedTestDispatcher в каждом тестовом классе
  - Один тестовый файл на один production-класс

Тестируемые классы:
  - data/repository/ProductRepositoryImplTest
  - data/repository/FoodEntryRepositoryImplTest
  - domain/usecase/GetFoodEntriesForDayUseCaseTest
  - domain/usecase/AddFoodEntryUseCaseTest
  - domain/usecase/GetDailySummaryUseCaseTest
  - domain/usecase/SearchProductsUseCaseTest
  - presentation/screen/today/TodayViewModelTest
  - presentation/screen/settings/SettingsViewModelTest
  - presentation/screen/weight/WeightViewModelTest
```

---

## 7.4 Общие правила качества кода

- Ни одного `TODO`, `FIXME`, placeholder, заглушки
- Каждый файл — полный, компилируемый, со всеми импортами
- Никаких `LiveData` — только `StateFlow` / `Flow`
- Mapper — extension functions в отдельных файлах (не методы в классах)
- `@Stable` / `@Immutable` на Compose state классах там, где Compose не может вывести сам
- Нет логики в UI — только в ViewModel / UseCase

---

## 7.5 Формат вывода (ОБЯЗАТЕЛЬНЫЙ)

Сначала выведи **полное дерево файлов**.
Затем выведи каждый файл строго в формате:

```
=== FILE: app/src/main/java/com/k/shavrin/diethelper/domain/model/Product.kt ===
[полный текст файла]
=== END FILE ===
```

Обязательные файлы для каждой фазы:
- Все `.kt` файлы из scope фазы — полностью
- Если фаза меняет `app/build.gradle.kts` — вывести его целиком

Обязательные файлы в Фазе 1:
- `settings.gradle.kts`
- `build.gradle.kts` (корневой)
- `gradle/libs.versions.toml`
- `gradle/wrapper/gradle-wrapper.properties`
- `app/build.gradle.kts`
- `app/src/main/AndroidManifest.xml`

---

---

# 8. Фазы генерации

> **Как использовать**: Скопируй разделы 1–7 (выше) + соответствующую фазу ниже и отправь в новую сессию Claude Code. Каждая фаза независима и верифицируется перед переходом к следующей.

---

## Фаза 1 — Scaffolding (Gradle + Theme + Entry Point)

**Задача**: Создать проект, который открывается в Android Studio, синхронизирует Gradle и запускает пустой экран.

**Сгенерируй следующие файлы:**

```
settings.gradle.kts
build.gradle.kts                                    (корневой)
gradle/libs.versions.toml
gradle/wrapper/gradle-wrapper.properties
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/com/k/shavrin/diethelper/
  DietHelperApplication.kt                          (@HiltAndroidApp)
  MainActivity.kt                                   (@AndroidEntryPoint, пустой Scaffold)
  presentation/theme/
    Color.kt                                        (Material3 цвета, light + dark)
    Type.kt                                         (типография)
    Theme.kt                                        (DietHelperTheme, dynamicColor=false)
```

**Что НЕ генерировать**: Room, Hilt модули, бизнес-логика, navigation, экраны.

**Верификация**: `./gradlew assembleDebug` завершается без ошибок. Приложение запускается и показывает пустой экран.

---

## Фаза 2 — Domain Layer

**Задача**: Полный доменный слой — чистый Kotlin, без Android-зависимостей.

**Сгенерируй следующие файлы:**

```
domain/model/
  MealType.kt
  Product.kt
  FoodEntry.kt
  DailySummary.kt
  DailyGoals.kt
  WeightEntry.kt
domain/repository/
  ProductRepository.kt         (interface: Flow<List<Product>>, suspend fun add/update/delete)
  FoodEntryRepository.kt       (interface: Flow<List<FoodEntry>> by date, suspend fun CRUD + copy)
  WeightRepository.kt          (interface: Flow<List<WeightEntry>>, suspend fun upsert/delete)
  GoalsRepository.kt           (interface: Flow<DailyGoals>, suspend fun save)
domain/usecase/
  product/
    GetAllProductsUseCase.kt
    SearchProductsUseCase.kt
    AddProductUseCase.kt
    ToggleFavoriteUseCase.kt
  foodentry/
    GetFoodEntriesForDayUseCase.kt
    AddFoodEntryUseCase.kt
    UpdateFoodEntryUseCase.kt
    DeleteFoodEntryUseCase.kt
    CopyFoodEntryToDayUseCase.kt
    GetDailySummaryUseCase.kt
  weight/
    GetAllWeightEntriesUseCase.kt
    UpsertWeightEntryUseCase.kt
    DeleteWeightEntryUseCase.kt
  goals/
    GetDailyGoalsUseCase.kt
    SaveDailyGoalsUseCase.kt
```

**Верификация**: `./gradlew :app:compileDebugKotlin` — нет ошибок в пакете `domain`.

---

## Фаза 3 — Data Layer: Room

**Задача**: Полный слой Room — entities, DAOs, database, TypeConverters, mappers.

**Сгенерируй следующие файлы:**

```
data/local/
  converter/
    Converters.kt              (LocalDate ↔ Long epochDay)
  entity/
    ProductEntity.kt
    FoodEntryEntity.kt         (с FOREIGN KEY и @ForeignKey annotation)
    FoodEntryWithProduct.kt    (@Transaction data class с @Relation к ProductEntity)
    WeightEntryEntity.kt
  dao/
    ProductDao.kt              (insert/update/delete/getAll/search/getById)
    FoodEntryDao.kt            (insert/update/delete/getByDate — возвращает Flow<List<FoodEntryWithProduct>>)
    WeightEntryDao.kt          (insert/update/delete/getAll/getByDate)
  DietHelperDatabase.kt        (@Database, version=1, fallbackToDestructiveMigration, все 3 entity)
data/mapper/
  ProductMapper.kt             (ProductEntity ↔ Product, extension functions)
  FoodEntryMapper.kt           (FoodEntryWithProduct ↔ FoodEntry)
  WeightEntryMapper.kt         (WeightEntryEntity ↔ WeightEntry)
```

**Верификация**: `./gradlew :app:kspDebugKotlin` — Room schema сгенерирована, нет ошибок.

---

## Фаза 4 — Data Layer: DataStore + Repository Implementations

**Задача**: Реализации всех 4 интерфейсов из Фазы 2.

**Сгенерируй следующие файлы:**

```
data/local/
  GoalsDataSource.kt           (обёртка над DataStore<Preferences>)
data/repository/
  ProductRepositoryImpl.kt
  FoodEntryRepositoryImpl.kt
  WeightRepositoryImpl.kt
  GoalsRepositoryImpl.kt
```

**Верификация**: `./gradlew :app:compileDebugKotlin` — нет ошибок в пакете `data`.

---

## Фаза 5 — Hilt DI

**Задача**: Полный граф зависимостей Hilt.

**Сгенерируй следующие файлы:**

```
di/
  DatabaseModule.kt            (@Module @InstallIn(SingletonComponent), provides Database + все DAO как @Singleton)
  DataStoreModule.kt           (@Module, provides DataStore<Preferences> как @Singleton)
  RepositoryModule.kt          (@Module, @Binds все 4 репозитория как @Singleton)
```

**Верификация**: `./gradlew assembleDebug` — Hilt компонент генерируется без ошибок.

---

## Фаза 6 — Navigation + Shell UI

**Задача**: Рабочая навигация с bottom bar и placeholder-экранами.

**Сгенерируй следующие файлы:**

```
presentation/navigation/
  Routes.kt
  BottomNavItem.kt             (sealed class, 4 элемента с иконками и лейблами)
  AppNavHost.kt                (NavHost со всеми destination включая ProductSearch и AddProduct)
MainActivity.kt                (обновлённый: Scaffold + NavigationBar + AppNavHost)
presentation/screen/
  today/TodayScreen.kt         (placeholder: Text("Сегодня"))
  history/HistoryScreen.kt     (placeholder)
  weight/WeightScreen.kt       (placeholder)
  settings/SettingsScreen.kt   (placeholder)
  product/ProductSearchScreen.kt (placeholder)
  product/AddProductScreen.kt  (placeholder)
```

**Верификация**: Приложение запускается, 4 вкладки переключаются, нет краша.

---

## Фаза 7 — Today + Product Screens (core flow)

**Задача**: Полная реализация главного flow — добавление, редактирование, удаление записей еды.

**Сгенерируй следующие файлы:**

```
presentation/screen/today/
  TodayUiState.kt
  TodayViewModel.kt
  TodayScreen.kt               (полная реализация, заменяет placeholder)
presentation/screen/product/
  ProductUiState.kt
  ProductViewModel.kt
  ProductSearchScreen.kt       (полная реализация)
  AddProductUiState.kt
  AddProductViewModel.kt
  AddProductScreen.kt          (полная реализация)
```

**Детали TodayScreen**: навигация по датам ←/→, 4 секции всегда видны, заголовок секции с "+" и суммой калорий, строки еды, long-press → DropdownMenu (изменить граммы / удалить / скопировать на день), DailySummaryCard внизу с прогресс-барами.

**Детали ProductSearchScreen**: debounce 300мс, избранное при пустом запросе, тап → диалог с граммами, кнопка добавить новый продукт, empty state.

**Детали AddProductScreen**: 5 полей, валидация, supportingText для ошибок.

**Верификация**: Полный flow "добавить еду" работает end-to-end. Данные сохраняются после перезапуска.

---

## Фаза 8 — History + Weight + Settings Screens

**Задача**: Полная реализация оставшихся трёх экранов.

**Сгенерируй следующие файлы:**

```
presentation/screen/history/
  HistoryUiState.kt
  HistoryViewModel.kt
  HistoryScreen.kt             (список дат, descending, тап → HistoryDayScreen)
  HistoryDayScreen.kt          (read-only Today для выбранной даты, без "+" и без меню)
presentation/screen/weight/
  WeightUiState.kt
  WeightViewModel.kt
  WeightScreen.kt              (upsert сегодняшнего веса, список с дельтами, long-press удалить)
presentation/screen/settings/
  SettingsUiState.kt
  SettingsViewModel.kt
  SettingsScreen.kt            (4 поля, кнопка Сохранить, валидация)
```

**Верификация**: Все 4 вкладки полностью функциональны. Настройки сохраняются. История показывает прошлые дни. Дельты веса считаются правильно.

---

## Фаза 9 — Unit Tests

**Задача**: Тестовое покрытие для всех указанных классов.

**Сгенерируй следующие файлы:**

```
src/test/java/com/k/shavrin/diethelper/
  util/MainDispatcherRule.kt
  data/
    FakeProductRepository.kt
    FakeFoodEntryRepository.kt
    FakeWeightRepository.kt
    FakeGoalsRepository.kt
    repository/ProductRepositoryImplTest.kt
    repository/FoodEntryRepositoryImplTest.kt
  domain/usecase/
    GetFoodEntriesForDayUseCaseTest.kt
    AddFoodEntryUseCaseTest.kt
    GetDailySummaryUseCaseTest.kt
    SearchProductsUseCaseTest.kt
  presentation/screen/
    today/TodayViewModelTest.kt
    settings/SettingsViewModelTest.kt
    weight/WeightViewModelTest.kt
```

**Паттерн**: каждый тест использует `@get:Rule val mainDispatcherRule = MainDispatcherRule()`, Fakes для зависимостей, `turbine` для проверки Flow.

**Верификация**: `./gradlew :app:testDebugUnitTest` — все тесты зелёные.

---

## Фаза 10 — Polish + Seed Data

**Задача**: Seed-данные для демонстрации, финальная полировка.

**Сгенерируй следующие файлы:**

```
data/local/DatabaseSeeder.kt   (15 популярных продуктов с реальными нутриентами на русском)
DietHelperApplication.kt       (обновлённый: запускает DatabaseSeeder при первом запуске)
app/src/main/res/values/strings.xml  (app_name и launcher-строки)
```

**Продукты для seed** (реальные данные нутриентов):
Гречка варёная, Куриная грудка варёная, Яйцо куриное, Творог 5%, Молоко 2.5%, Хлеб ржаной, Овсянка на воде, Банан, Яблоко, Помидор, Огурец, Рис варёный, Картофель варёный, Сыр российский, Лосось запечённый.

**Правило first-launch**: использовать `DataStore` boolean ключ `"is_seeded"` (не SharedPreferences).

**Дополнительно**: добавить `@Preview` аннотации (light + dark) на все Composable-экраны.

**Верификация**: Чистая установка → продукты видны в поиске. Smoke test всех фич. `assembleRelease` без ошибок R8.
