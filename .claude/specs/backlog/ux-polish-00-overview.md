# UX polish — icon, splash, undo, IME, motion, feedback — epic overview
Epic: ux-polish
Order: 00 of 08
Status: backlog
Depends-on: testability + i18n-strings + a11y (правки поверх извлечённых Content, строки undo/ошибок — ресурсы, haptics/семантика согласованы)
Date: 2026-07-06

## Goal
Довести пользовательский опыт до продуктового уровня: кастомная adaptive-иконка + splash (сейчас системная заглушка), обратимое удаление (undo-snackbar вместо fire-and-forget), видимая ошибка ввода веса, IME-actions/фокус в формах, dynamic color (opt-in), плавные nav-переходы, тактильная отдача, previews остальных экранов. Вне скоупа: дизайн рисунка иконки (готовит пользователь), Settings-тумблер dynamic color, pull-to-refresh (данные реактивны из Room).

## Locked decisions (из grill.md)
- D1: undo — re-insert по 'Отменить'; удаляем сразу, держим сущность в памяти VM; confirm-диалоги заменяются snackbar-ом. DAO/схему не трогаем. [confirmed]
- D2: dynamic color — параметр в DietHelperTheme (API 31+ fallback), дефолт OFF; брендовый зелёный сохраняется. [confirmed]
- D3: иконка — пользователь готовит ассет; SPEC описывает только подключение (adaptive-структура, манифест, splash). [confirmed]
- D4: 8 когезивных SPEC-ов по одному концерну. [assumption]
- D5: линейный порядок 01→08 из-за общих файлов (MainActivity: 01,02; WeightScreen: 02,03,04,07). [assumption]

## SPECs (run via /mp --feature --next in Order)
| Order | File | Depends-on | Layers | Summary |
|---|---|---|---|---|
| 01 | `ux-polish-01-app-icon-splash.md` | — | platform, presentation | Adaptive-иконка (ассет пользователя) + core-splashscreen |
| 02 | `ux-polish-02-undo-snackbar.md` | 01 | presentation | SnackbarHost + re-insert undo, замена confirm-диалогов |
| 03 | `ux-polish-03-weight-input-validation.md` | 02 | presentation | Видимая ошибка невалидного ввода веса |
| 04 | `ux-polish-04-form-ime-focus.md` | 03 | presentation | IME-actions (Next/Done) + порядок фокуса в формах |
| 05 | `ux-polish-05-dynamic-color.md` | — | presentation | dynamicColor-параметр темы (дефолт OFF) |
| 06 | `ux-polish-06-nav-transitions.md` | — | presentation | enter/exit-переходы навигации |
| 07 | `ux-polish-07-haptics.md` | 02, 04 | presentation | Тактильная отдача на удаление/сохранение/копирование |
| 08 | `ux-polish-08-screen-previews.md` | — | presentation | @Preview для 5 экранов без превью |

## Why this ordering
Линейная секвенция из-за пересечения файлов: 01 и 02 трогают MainActivity (splash + SnackbarHost) → 01 первым; 02/03/04/07 пересекаются на WeightScreen и delete/save-сайтах → строгая цепочка 02→03→04, 07 ПОСЛЕДНИМ (depends-on 02+04, поверх стабильных сайтов). 05 (Theme.kt), 06 (AppNavHost.kt), 08 (Previews.kt) независимы — трогают уникальные файлы, могут идти в любой момент. Каждый SPEC независимо шиппится.

## Key facts (verified — полный ledger: C:\Users\Admin\AppSpecs\ux-polish\pipeline\grounding.md)
- G1/G6: системная иконка-заглушка; mipmap-каталогов нет; палитра Color.kt — бренд-якорь
- G2/G4/G7: голая Material-тема; core-splashscreen нет; dynamic color только API 31+, splash деградирует 26-30
- G3: DietHelperTheme без dynamicColor-параметра, захардкоженный зелёный — Theme.kt:21-32
- G8/G9/G10: 4 потока delete-confirm (TodayDesignedContent:868, TodayScreen:834, WeightScreen:147, ProductSearchScreen:240); методы удаления fire-and-forget; SnackbarHost нет (MainActivity:37)
- G11/G12: молчаливый невалидный ввод веса (WeightViewModel:61-67); Settings/AddProduct отслеживают *Error — образец
- G13: IME-actions не заданы нигде; FocusRequester не используется
- G14: nav-transitions дефолтные (AppNavHost:27-114)
- G15: haptics — 0 использований
- G16: previews только Today/DailySummary/EmptyState/Weight/History; нет ProductSearch/AddProduct/Settings/Stats/Export
- G18: точка SnackbarHost — MainActivity Scaffold:37 (single-activity root)

## Implementation links
- commit: (pending)
- files:  (pending)
