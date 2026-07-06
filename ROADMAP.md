# Roadmap — diet_helper

> **Planned and pending work**, ordered by intended iteration.
> Done items move to `DOCUMENTATION.md` → Feature Changelog (history).
> Current iteration and in-flight work live in `STATE.md`.
>
> Edit manually. Epics are decomposed into board SPECs by `/mp-spec --feature`;
> this file holds direction, the board (`.claude/specs/backlog/`) holds work units.

---

## Iteration 6 — Workflow polish (infra) — SHIPPED 2026-05

Sub-items A–E (memory, state artifacts, brainstorm phase, verification gate, TDD mode)
shipped under the bespoke `/dh` pipeline. Sub-item F (output sandbox) retired — `/dh`
itself was superseded by the migration to `/mp` (2026-05-31, commit `26b5ae9`;
dh-* agents archived in `.claude/_archive_pre_mp/`).

## Iteration 7 — Audit backlog (filed 2026-07-06)

Six tech epics from the full project audit, decomposed onto the SPEC board via
`/mp-spec --feature`. Implement with `/mp --feature --next` in this order
(gates ordering: coverage threshold lands only after testability adds tests;
string extraction lands after Content extraction to avoid same-file churn):

1. **data-safety** — Room/DataStore hardening: drop destructive-migration fallback,
   `exportSchema=true` + commit schemas, transactional seeder + saveMeal, DataStore
   IOException handling, PDF partial-file cleanup, `dataExtractionRules`, safe enum
   nav-arg parse, DispatcherProvider.
2. **testability** — Clock/todayProvider injection (kill 15+ prod `LocalDate.now()`),
   fix time-dependent tests, extract `Content()` for 5 screens + Compose tests +
   Roborazzi baselines, flesh out SettingsViewModelTest.
3. **quality-gates** — JaCoCo verification rule + threshold ramp, CI hardening
   (wrapper validation, coverage gate, release smoke), dependabot, androidTest
   scaffold + 1 Hilt smoke, wire `selfimprove/record-run.sh` telemetry into /mp runs.
4. **i18n-strings** — ~82 UI strings → `strings.xml`, Format.kt day/month names →
   resources, ViewModel validation messages via UiText pattern, PDF strings.
5. **a11y** — contentDescription pass, semantics for Canvas charts (donut/weight/stats)
   and progress bars, touch-target audit.
6. **ux-polish** — custom adaptive launcher icon + splash screen, undo-snackbar on
   deletes, fix silent invalid weight input, IME actions + focus order, nav
   transitions, dynamic color (opt-in), haptics, previews for remaining screens.

## Product candidates (unscheduled)

Each is its own epic: pick → dedicated `/mp-spec --feature` run → board SPECs.
Ordered by rough value/effort for a learning project:

- **Barcode scanning (ML Kit)** — 10× faster logging than typing.
- **OpenFoodFacts integration** — first network layer in the app (Retrofit/Ktor,
  caching policy, offline fallback) — the biggest architecture-learning step.
- **CSV export/import** — data portability; pairs with the existing PDF export.
- **Meal reminders** — WorkManager + notifications; habit formation.
- **File-based backup/restore (SAF)** — offline-first-friendly disaster recovery.
- **Per-portion entry** — portion presets per product instead of grams-only math.
- **BMI/TDEE calculator in Settings** — suggest goal values instead of raw numbers.
- **Weekly/monthly summary on Today** — trend awareness without opening Stats.
- **Water tracking** — adjacent habit, cheap once entry patterns exist.
- **Meal time field** — optional `LocalTime` on FoodEntry; enables timeline view.
- **Home-screen widget (Glance)** — quick-log + today's macro progress.
- **Meal photos** — camera/storage permissions, heavier scope.
- **Onboarding flow** — value grows once the features above exist.

## Retired /dh-era backlog

- Eval framework for dh-* agents — superseded by the `selfimprove/` loop and the
  marketplace-level improvement flow (`/mp --improve`).
- Tool-output sandbox, vector memory DB, git worktrees for agents — /dh-specific or
  premature; revisit only on real pain.
