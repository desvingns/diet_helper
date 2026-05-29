# Diet Helper

Android-приложение для учёта калорий и БЖУ. Учебный проект — намеренно подробная архитектура для изучения Clean Architecture, Jetpack Compose, Hilt и Room.

## Возможности

- Дневник питания — добавление блюд по приёмам пищи (завтрак / обед / ужин / перекус)
- База продуктов с поиском и избранным; добавление собственных продуктов
- Ежедневный итог по калориям и макронутриентам с прогресс-барами
- История питания с постатейным просмотром по дате
- График веса
- Настройка суточных целей (калории, белки, жиры, углеводы)

## Стек

| Слой | Технология |
|------|-----------|
| UI | Jetpack Compose, Material 3 |
| DI | Hilt 2.55 |
| БД | Room 2.7.1 |
| Настройки | DataStore Preferences 1.1.1 |
| Навигация | Navigation Compose |
| Async | Coroutines + Flow |
| Язык | Kotlin 2.1.20 |

minSdk **26**, targetSdk **35**, JVM **17**

## Архитектура

```
app/src/main/java/com/k/shavrin/diethelper/
├── domain/
│   ├── model/          # Product, FoodEntry, WeightEntry, DailyGoals, DailySummary, HistoryItem, MealType
│   ├── repository/     # Интерфейсы репозиториев
│   └── usecase/        # Один класс — один сценарий
│       ├── product/
│       ├── foodentry/
│       ├── weight/
│       └── goals/
├── data/
│   ├── local/
│   │   ├── dao/        # ProductDao, FoodEntryDao, WeightEntryDao
│   │   ├── entity/     # Room-сущности
│   │   ├── converter/  # LocalDate ↔ Long (epochDay)
│   │   ├── DietHelperDatabase.kt
│   │   ├── GoalsDataSource.kt   # DataStore-обёртка
│   │   └── DatabaseSeeder.kt    # Начальный набор продуктов
│   ├── mapper/         # entity ↔ domain
│   └── repository/     # *Impl-классы
├── di/                 # Hilt-модули
└── presentation/
    ├── navigation/     # Routes, BottomNavItem, AppNavHost
    ├── screen/         # today, product, history, weight, settings
    ├── components/     # Общие composable (DailySummaryCard)
    ├── theme/
    └── util/           # Format.kt
```

**Ключевые решения:**
- `DailyGoals` — в DataStore, не в Room (пользовательские настройки, не табличные данные)
- `LocalDate` сериализуется в `Long` через `TypeConverter` (epochDay)
- `FoodEntryWithProduct` — `@Transaction + @Relation` для реактивного JOIN-а
- Single-activity, Navigation Compose, bottom nav

## Сборка

Требования: Android Studio Hedgehog+ (или JDK 17 + Android SDK).

```bash
# KSP-кодогенерация (после изменений аннотаций Room/Hilt)
./gradlew :app:kspDebugKotlin

# Debug APK
./gradlew :app:assembleDebug

# Unit-тесты
./gradlew :app:testDebugUnitTest
```

Если запускаете вне Android Studio — укажите `JAVA_HOME`:

```powershell
$env:JAVA_HOME = "D:\For_work\AS\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
.\gradlew.bat :app:assembleDebug
```

## Тесты

```
app/src/test/
├── data/
│   ├── FakeFoodEntryRepository.kt   # in-memory фейки
│   ├── FakeProductRepository.kt
│   ├── FakeWeightRepository.kt
│   ├── FakeGoalsRepository.kt
│   └── repository/                  # тесты *RepositoryImpl с MockK
├── domain/usecase/                  # тесты use case
└── presentation/screen/             # тесты ViewModel c Turbine
```

Стек: JUnit 4, MockK 1.13.12, Turbine 1.1.0, kotlinx-coroutines-test.
