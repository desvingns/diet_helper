# Diet Helper

Android-приложение для учёта калорий и БЖУ. Учебный проект — намеренно подробная архитектура для изучения Clean Architecture, Jetpack Compose, Hilt и Room.

## Возможности

- Дневник питания — добавление блюд по приёмам пищи (завтрак / обед / ужин / перекус)
- База продуктов с поиском и избранным; добавление собственных продуктов
- Ежедневный итог по калориям и макронутриентам с прогресс-барами
- Недельная шапка со статусами дней и счётчик серии (streak)
- Копирование приёма пищи на другой день; сохранённые блюда (наборы позиций)
- История питания с постатейным просмотром по дате
- Статистика — бар-чарты калорий и макросов за выбранный период
- График веса
- Настройка суточных целей (калории, белки, жиры, углеводы)
- Экспорт PDF-отчёта за период (подробный / только сводка, опционально со статистикой)

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
│   ├── model/          # Product, FoodEntry, WeightEntry, SavedMeal, DailyGoals,
│   │                   # DailySummary, HistoryItem, StatsDayItem, ExportConfig, ...
│   ├── repository/     # Интерфейсы репозиториев (+ ReportRenderer)
│   └── usecase/        # Один класс — один сценарий
│       ├── product/  foodentry/  weight/  goals/  savedmeal/  stats/  export/
├── data/
│   ├── local/
│   │   ├── dao/        # ProductDao, FoodEntryDao, WeightEntryDao, SavedMealDao
│   │   ├── entity/     # Room-сущности + @Relation-джоины
│   │   ├── converter/  # LocalDate ↔ Long (epochDay)
│   │   ├── DietHelperDatabase.kt (v2)
│   │   ├── GoalsDataSource.kt   # DataStore-обёртка
│   │   └── DatabaseSeeder.kt    # Начальный набор продуктов
│   ├── pdf/            # PdfReportRenderer (реализация ReportRenderer)
│   ├── mapper/         # entity ↔ domain
│   └── repository/     # *Impl-классы
├── di/                 # Hilt-модули
└── presentation/
    ├── navigation/     # Routes, BottomNavItem, AppNavHost
    ├── screen/         # today, product, history, weight, settings, stats, export
    ├── components/     # Общие composable (DailySummaryCard)
    ├── theme/
    └── util/           # Format.kt, InMemoryMealClipboard, MacroColorUtil
```

**Ключевые решения:**
- `DailyGoals` — в DataStore, не в Room (пользовательские настройки, не табличные данные)
- `LocalDate` сериализуется в `Long` через `TypeConverter` (epochDay)
- `FoodEntryWithProduct` — `@Transaction + @Relation` для реактивного JOIN-а
- PDF-рендер за интерфейсом `ReportRenderer` в domain — без Android-типов в домене
- Single-activity, Navigation Compose, bottom nav

## Сборка

Требования: JDK 17 + Android SDK (или Android Studio).
Вне Android Studio укажите `JAVA_HOME` на JBR — в `CLAUDE.md` есть кросс-платформенный
сниппет автоопределения (Linux + Git Bash на Windows).

```bash
# KSP-кодогенерация (после изменений аннотаций Room/Hilt)
./gradlew :app:kspDebugKotlin

# Debug APK
./gradlew :app:assembleDebug

# Unit-тесты
./gradlew :app:testDebugUnitTest

# Статический анализ
./gradlew :app:detekt

# Скриншот-тесты (Roborazzi)
./gradlew :app:recordRoborazziDebug   # перегенерировать базлайны
./gradlew :app:verifyRoborazziDebug   # сверить с закоммиченными
```

## Тесты

Политика: **только фейки** (StateFlow-реализации интерфейсов репозиториев) —
никаких MockK/Mockito. Никогда не `@Ignore`, не ослаблять ассерты, не удалять
падающие тесты ради зелёного прогона.

```
app/src/test/
├── data/               # Fake*-репозитории + тесты DAO (Robolectric) и *Impl
├── domain/usecase/     # Тесты use case
└── presentation/       # Тесты ViewModel (Turbine), Compose-UI (Robolectric),
                        # скриншот-тесты (Roborazzi, базлайны в src/test/snapshots)
```

Стек: JUnit 4, Turbine 1.1.0, kotlinx-coroutines-test, Robolectric 4.13,
Roborazzi 1.25.0, Detekt, JaCoCo. CI (GitHub Actions): unit-тесты +
скриншот-верификация + JaCoCo-отчёт + Detekt + Lint + сборка debug APK.

## Процесс разработки

Проект — потребитель `/mp`-пайплайна (маркетплейс `mobile-pipeline`): доска спеков
в `.claude/specs/{backlog,active,done}`, реализация через `/mp --feature --next`.
Состояние: `STATE.md` (сейчас) · `ROADMAP.md` (дальше) · `DOCUMENTATION.md` (история).
