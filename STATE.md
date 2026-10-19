# Project State — diet_helper

> **Live document.** Updated by `mp-docs` after each `/mp` run.
> Manual edits welcome — `mp-docs` only edits known sections and preserves the rest.
>
> Different from related files:
> - `STATE.md` (this file) — **now**: where we are today, what's mid-flight.
> - `ROADMAP.md` — **next**: planned iterations, ordered backlog.
> - `DOCUMENTATION.md` — **history**: feature changelog, architecture decisions, user flows.

---

## Now

- **Current iteration:** Personal training support, version 1.1 (2026-10-19),
  implemented directly at the owner's request without MP.
- **In progress:** Telegram delivery of the verified version 1.1 APK. Weekly availability
  and an individualized replacement/re-entry prescription remain open. The
  original SRC7 waves are retained; no automatic maximum increase is active.
- **Latest runtime validation:** all 435 tests, screenshot verification, Lint,
  Detekt and debug assembly passed. Emulator upgrade 3→4 preserved all old table
  row counts and byte-identical DataStore preferences. Native checks cover actual
  sets/RPE, bodyweight extras, partial finish/day advance and MD/JSON sharing.
  Forms and session transitions also pass at 130% font; final day advancement
  resets the list to the first approach. Normal font scale was restored.
- **Latest product delivery:** preserved SRC7 waves with an overview of all 57
  populated days; dated actual-set journal, exercise changes/preferences,
  modified/stopped completion, next-day follow-up and explainable suggestions.
  Versioned Markdown/JSON exports preserve unknown values and weight snapshots.
  Room version 4 has an explicit non-destructive 3→4 migration. See
  `docs/training/implementation.md` and `docs/training/src7-wave-analysis.md`.
- **Previous visual delivery:** midnight plum/lavender design with restrained mint status accents; seven generated illustrations for the diary, meals, training and journal/goals; rounded navigation icons and short native UI transitions. Artwork is bundled offline (376 KiB total). Details and final generation prompts: `docs/plum-art-direction.md`.
- **Previous visual delivery:** cohesive screen chrome, improved diary/calendar, dated history cards, pinned goals actions, weight trend/validation and a custom adaptive launcher icon. Details: `docs/design-refresh.md`.
- **Previous product delivery:** 132 PDF diary foods; 107 Serbian catalog products (73 Lidl); live name/barcode search; 57 imported workout days, personal maxima, full plan, set progress and persistent rest timer. Explicit Room 2→3 migration preserves existing food data. Details: `docs/food-and-workouts.md`.
- **Previous pipeline work:** all 6 audit epics decomposed onto `.claude/specs/backlog/` via
  `/mp-spec --feature` (2026-07-06) — **37 SPECs across 6 epics**, each grounded (file:line
  facts), grilled (locked decisions), and light-eval-passed. Recommended implement order:
  data-safety (8) → testability (8) → quality-gates (5) → i18n-strings (5) → a11y (3) →
  ux-polish (8). Before that: full project audit + wiring hygiene (brain card + scan-list,
  memory migrated `D--diet-helper` → `D--Pet-MyDietHelper`, graphify rebuilt, dead marketplace
  path removed, board scaffolded, docs refreshed).

## Recently shipped (last 5 commits before the audit)

- 2026-05-31 `26b5ae9` chore: migrate to /mp — archive superseded dh-* agents/commands/scripts
- 2026-05-31 `f08090d` chore: wire to mobile-pipeline marketplace (mp-spec + mp-dev)
- 2026-05-30 `14e9eff` chore: graphify wiring + self-improvement loop
- 2026-05-29 `08278b7` fix: resolve merge conflicts in /dh tooling files
- 2026-05-29 `5d3bda4` update skills and agents

## Known tech debt

All of the code-level debt below is tracked as epics on the SPEC board (2026-07-06 audit):

- **data-safety epic:** destructive migration fallback removed in the 2026-10-19 product update; `exportSchema = false`
  (schema history not committed); non-transactional DatabaseSeeder and `saveMeal`
  delete-then-insert; DataStore IOException uncaught; PDF partial file not cleaned on failure;
  `allowBackup=true` without `dataExtractionRules`; unsafe `enumValueOf` nav-arg parse;
  hardcoded Dispatchers (no DispatcherProvider).
- **testability epic:** 5 screens lack `<Name>Content(...)` extraction → no Compose/Roborazzi
  tests (HistoryScreen, HistoryDayScreen scaffold, AddProductScreen, ProductSearchScreen,
  WeightScreen); 15+ hardcoded `LocalDate.now()` in production → midnight-dependent tests.
- **quality-gates epic:** JaCoCo `verificationRule` not wired (project line coverage 27.7%,
  measured 2026-05-19, target 65%); no `androidTest/` instrumented suite; CI lacks wrapper
  validation / release smoke / dependabot; `selfimprove/runs/` telemetry empty —
  `record-run.sh` not wired into /mp runs.
- **i18n-strings epic:** ~82 hardcoded RU strings across 10 presentation files;
  `strings.xml` contains only `app_name`.
- **a11y epic:** 13+ icons without `contentDescription`; Canvas charts (macro donut, weight,
  stats) and progress bars have no semantics.
- **ux-polish epic:** custom adaptive icon and launch colors, weight validation/feedback,
  weight IME Done and pinned goals actions delivered in the 2026-10-19 design refresh.
  Remaining: no undo after deletes; IME actions are still incomplete on other forms;
  haptics need a focused pass. Native navigation, tab and meal-card transitions
  were added with the midnight plum art refresh.
- Standing quirk: `ExportViewModel` builds the share `Uri` via `Uri.Builder` instead of
  `FileProvider.getUriForFile` (Windows-Robolectric path-matching); revisit when Robolectric
  handles Windows authorities.

## Up next (head of ROADMAP)

- Owner priority: collect feedback from version 1.1, discuss the practical
  training schedule and individualized re-entry approach, and review current
  technique videos when supplied. The factual journal and export are delivered;
  any automatic load changes remain future work. Keep private context local.

- Implement the backlog via `/mp --feature --next`, recommended order:
  data-safety → testability → quality-gates → i18n-strings → a11y → ux-polish.
  Note the cross-epic ordering baked into the SPECs: testability before i18n-strings
  (Content extraction before string moves); a11y and ux-polish after i18n (resource-backed
  descriptions/strings). Working files for each epic's grounding/grill/decomposition live under
  `C:\Users\Admin\AppSpecs\<epic>\pipeline\`.
- Audit corrections surfaced while filing: real hardcoded-string count is **301 in 22 files**
  (not ~82); only **4** screens need `Content()` extraction (HistoryDayScreen already reuses
  TodayContent), not 5.
- Product feature candidates live in ROADMAP → «Product candidates»; each gets its own
  `/mp-spec --feature` run when picked.
