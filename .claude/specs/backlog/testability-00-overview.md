# Testability — Clock injection + Content extraction — epic overview
Epic: testability
Order: 00 of 08
Status: backlog
Depends-on: — (рекомендуется после эпика data-safety: его SPEC 07 трогает ProductViewModel, наш 07 — ProductSearchScreen; файлы разные, но домен смежный)
Date: 2026-07-06

## Goal
Сделать время инжектируемым (java.time.Clock) и убрать 11 продовых вызовов `LocalDate.now()` — уходят midnight-баги и недетерминированные тесты (19+ живых вызовов clock в 6 тест-файлах). Извлечь тестируемые `*Content()` из 4 экранов без них (History, AddProduct, ProductSearch, Weight) по золотому паттерну TodayContent, с Compose-тестами и Roborazzi-базлайнами. Добить минимальный SettingsViewModelTest. Вне скоупа: строки/a11y/androidTest (свои эпики).

## Locked decisions (из grill.md)
- D1: java.time.Clock через Hilt (ClockModule, @Provides Clock.systemDefaultZone()); UseCase/VM получают Clock в конструктор → LocalDate.now(clock); тесты — Clock.fixed(). Композаблы now() не зовут — граница «сегодня» приходит из UiState. [confirmed]
- D2: каждый Clock-SPEC чинит live-clock тесты СВОИХ классов; фикс-даты по прецеденту G52. [assumption]
- D3: ProductSearch — один верхнеуровневый ProductSearchContent, внутри существующие приватные под-композаблы; @Suppress("LongParameterList") по прецеденту G23. [assumption]
- D4: по 2 Roborazzi-базлайна на новый Content (populated + empty/validation), darkTheme=true. [assumption]
- D5: HistoryDayScreen извлечения НЕ требует — уже переиспользует TodayContent(readOnly=true) (G26). Аудит-оценка «5 экранов» скорректирована до 4; поправить STATE.md при реализации. [confirmed by G26]
- D6: TodayScreen:443 / TodayDesignedContent:247 перестают звать now(); TodayViewModel кладёт границу в TodayUiState (поле today или canGoForward — решает разработчик, O1). [assumption]

## SPECs (run via /mp --feature --next in Order)
| Order | File | Depends-on | Layers | Summary |
|---|---|---|---|---|
| 01 | `testability-01-clock-module.md` | — | di, domain | ClockModule + Clock в GetStreak/GetWeekDayStatuses + фикс их тестов |
| 02 | `testability-02-today-clock.md` | 01 | presentation | Clock в TodayViewModel; «сегодня» в UiState; композаблы без now() |
| 03 | `testability-03-stats-export-weight-clock.md` | 01 | presentation | Clock в Stats/Export/Weight VM + фикс их тестов |
| 04 | `testability-04-settings-viewmodel-test.md` | — | test-only | Добить SettingsViewModelTest (переходы isSaving/justSaved, валидация) |
| 05 | `testability-05-history-content.md` | — | presentation | HistoryContent + Compose-тест + 2 базлайна |
| 06 | `testability-06-addproduct-content.md` | — | presentation | AddProductContent + тест валидации + 2 базлайна |
| 07 | `testability-07-productsearch-content.md` | — | presentation | Единый ProductSearchContent (табы+поиск) + тест + 2 базлайна |
| 08 | `testability-08-weight-content.md` | 03 | presentation | WeightContent + тест + 2 базлайна |

## Why this ordering
Clock — фундамент: 01 (DI+domain) → 02/03 (VM-потребители). 04 независим. Извлечения 05–08 независимы между собой (каждый — свой файл экрана); 08 после 03, потому что WeightViewModel меняет конструктор в 03 — извлечение поверх стабильного VM избегает двойного трогания тестов. Same-file clash внутри эпика отсутствует; межэпиковый порядок: testability ДО i18n-strings (i18n массово трогает те же файлы экранов — извлечение раньше строк).

## Key facts (verified — полный ledger: C:\Users\Admin\AppSpecs\testability\pipeline\grounding.md)
- G1–G7: 11 продовых точек now(): GetStreakUseCase:16, GetWeekDayStatuses:40, TodayViewModel:48,96,102,111,122, TodayScreen:443, TodayDesignedContent:247, StatsViewModel:23-24,67-73, WeightViewModel:48,65, ExportViewModel:109
- G13/G19: DI-модули @InstallIn(SingletonComponent); абстракции времени нет
- G15–G17, G43–G44: живой clock в тестах: GetStreakUseCaseTest, GetWeekDayStatusesUseCaseTest, TodayViewModelTest, ExportViewModelTest, WeightViewModelTest, StatsViewModelTest
- G21–G23: золотой паттерн TodayScreen→TodayContent(state, колбэки) + @Suppress("LongParameterList")
- G24–G33: структура 4 экранов; G26: HistoryDayScreen уже на TodayContent; G38: ProductSearch/Weight собирают по ДВА flow — протянуть параметрами
- G34–G36, G48–G49: паттерны ContentTest и Roborazzi (qualifiers w411dp-h891dp-xxhdpi, NATIVE, resizeScale=0.5, snapshots в src/test/snapshots)
- G45–G47: дыры SettingsViewModelTest (isSaving-переход, validateRange(null,null), сброс justSaved) и гонка sync-чтения state
- G50: detekt LongMethod=120/TooManyFunctions=25 — крупные Content разбивать на приватные под-композаблы
- G52: прецеденты фикс-дат в тестах (2025-04-20; 2025-05-23)

## Implementation links
- commit: (pending)
- files:  (pending)
