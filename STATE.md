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

- **Current iteration:** 7 — Audit backlog (6 improvement epics filed, ready to implement)
- **In progress:** idle — backlog board is loaded; next action is `/mp --feature --next`
- **Last completed:** all 6 audit epics decomposed onto `.claude/specs/backlog/` via
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

- **data-safety epic:** `fallbackToDestructiveMigration()` active; `exportSchema = false`
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
- **ux-polish epic:** launcher icon is the Android system stub, no splash screen; no undo
  after deletes; silent invalid weight input; no IME actions / nav transitions / haptics.
- Standing quirk: `ExportViewModel` builds the share `Uri` via `Uri.Builder` instead of
  `FileProvider.getUriForFile` (Windows-Robolectric path-matching); revisit when Robolectric
  handles Windows authorities.

## Up next (head of ROADMAP)

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
