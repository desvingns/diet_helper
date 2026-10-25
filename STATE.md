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

- **Current iteration:** Active workout screen, version 1.4 (2026-10-25).
- **In progress:** final APK delivery to the owner's Telegram Saved Messages.
- **Version 1.4:** completed warm-up and standalone rest cards are hidden. The
  next-set card previews one more remaining set of the same exercise and shows
  elapsed time since the last completed set. Completion timestamps persist in
  Room; edits preserve them and undone sets do not count. Room 6→7 preserves
  old data with unknown historical timestamps left null. See
  `docs/training/active-session-v1.4.md`.
- **Version 1.3:** persistent warm-up/whole-session/cool-down timing; quick planned
  set confirmation; feedback on completion; dated weight entry and transactional
  weight/date edits with collision protection. Room 5→6 preserves historical data.
  Earlier generated icon candidates and exact prompts remain in
  `docs/design/icon-variants/`; the selected bowl-and-dumbbell image now backs the
  adaptive launcher icon with a purple gradient background. The transparent art
  is retained at `app/src/main/res/drawable-nodpi/ic_launcher_art.png`. See
  `docs/training/session-timers-and-weight.md`.
- **Latest tests:** all 474 tests pass without skips. Detekt, Lint, screenshot
  verification and final debug assembly pass. Native emulator checks confirm
  warm-up disappearance, same-exercise preview, the elapsed-time placeholder,
  live clock and continuation after force-stop/relaunch. Room 6→7 preserves all 271 existing emulator rows,
  every prior column value and byte-identical DataStore preferences.
- **Artifact:** `.local/MyDietHelper-training-v1.4-debug.apk`, versionName 1.4 /
  versionCode 5. Telegram delivery was explicitly requested. Private profile,
  QA data and icon concepts are not bundled in the APK.
- **Previous upgrade check:** emulator version 5→6 preserved all 270 existing rows,
  all old column values and byte-identical DataStore preferences. Native checks
  confirm quick set recording without a popup, persistent completed warm-up with
  continuing total time, automatic feedback after cool-down, past-date weigh-in
  and stable record editing.
- **Previous delivery:** the version 1.2 APK was sent to the
  owner's Telegram Saved Messages via tgsend. Source SRC7 and the six-week return block
  now have independent progress, timers, active sessions and journals. Each
  return week requires explicit working weights after review; no automatic
  weight increases or guaranteed six-week strength outcome are implemented.
- **Previous runtime validation (1.2):** all 446 tests pass without skips, screenshot
  verification, Lint, Detekt and debug assembly pass. A real emulator upgrade
  4→5 preserved all previous rows/column values and byte-identical DataStore
  preferences. Native checks cover switching/resuming independent active
  sessions, weekly inputs, partial finish, planned bodyweight sets and scoped
  MD/JSON sharing. New forms are usable at 100%/130% fonts; scale restored to 100%.
- **Previous artifact:** `MyDietHelper-training-v1.2-debug.apk`, about 18.3 MiB,
  versionName 1.2 / versionCode 3; feature commit `6594afa`. Private intake, starter recommendations and
  emulator QA data remain Git-ignored in `.local/` and are not packaged in it.
- **Previous product delivery:** two program blocks, a six-week reviewable return
  template, manual working weights for each week and program-scoped logging.
  Room version 5 has an explicit preserving 4→5 migration; old sessions belong
  to SRC7. JSON schema2 identifies the program. See
  `docs/training/workout-programs.md` for behavior and limits.
- **Previous product delivery:** version 1.1 factual sets/RPE, preferences,
  partial finish, next-day follow-up and export; original SRC7 waves unchanged.
  Its APK was delivered to own Telegram Saved Messages. All 435 tests and its
  migration 3→4 checks passed; see `docs/training/implementation.md`.
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

- Owner priority: use the separate return block, review real weekly exports,
  revise working weights/schedule and review current technique videos when
  supplied. An individualized first-week proposal is stored privately; it is
  not silently imported into the app or treated as an accepted outcome deadline.
  Keep personal context local. Automatic AI plan import remains future work.

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
