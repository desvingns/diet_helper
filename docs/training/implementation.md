# Training journal and preserved SRC7 waves — version 1.1

Implemented directly in the existing application on 2026-10-19 at the owner's
request. Validation and delivery status are recorded in STATE.md.

## Behavior

- The frozen 57-day SRC7 source remains unchanged. The plan tab now shows a
  descriptive wave overview for all 12 populated microcycles: repetitions,
  prescribed sets, exercise-row load labels and peak basic-lift reference load.
  Microcycles with four/three populated days are identified as such.
- A performed set records actual exercise/variant, weight, repetitions, optional
  RPE, unusual sensations, exercise preference and a note. Planned values are
  frozen on first recording; later changes of reference maxima do not rewrite
  recorded performance. Optional fields stay unknown when omitted.
- Renaming/selecting another exercise clears the proposed actual weight and
  requires an independent entry. The source exercise and planned values remain
  visible. Suggested variations are choices for discussion, not equivalent load
  conversions or rehabilitation prescriptions.
- Additional exercises can be logged one set at a time, including bodyweight
  pull-ups/dips/back extensions. Bodyweight does not become a fabricated zero-kg
  total, and per-hand dumbbell loads have an explicit basis.
- Sessions can finish according to the plan, modified or stopped. A modified or
  stopped session requires a reason; omitted sets are not fabricated as done.
  The source sequence advances when the session is saved.
- The journal lists dated sessions, actual set snapshots and next-day follow-up.
  Explicitly undone records remain as such; old completion flags remain legacy
  progress with no inferred actual weights or dates.
- Gym-visit minutes and social interruptions are separate optional entries.
  Training minutes include warm-up, cool-down and normal set rest, and are
  calculated only when both total and interruption duration are known. Session
  effort is separate from set RIR-based RPE.
- Feedback produces plain-language suggestions. Illness and reported unusual
  sensations take precedence over easy effort; unknown follow-up stays unknown.
  No percentage fatigue score, automatic max increase or clinical clearance is
  generated. The rule is a conservative coaching heuristic, not a validated
  individual physiological model.
- Export creates Markdown and schema-versioned JSON, then opens Android sharing.
  Export includes journal notes, unknowns, load basis, original snapshots and
  timezone. No background upload, desktop-chat creation or cloud AI call occurs.

## Persistence and architecture

Room version 4 adds `training_sessions` and `training_sets` through an explicit
3→4 migration. Existing nutrition tables and workout DataStore preferences are
preserved. Session/set writes are transactional; planned-set IDs are unique per
session, and additional sets have independent IDs. The source plan stays in the
asset, and compact legacy progress/timer settings remain in DataStore.

Clean Architecture remains intentionally verbose: separate domain models,
repository/exporter ports, Room entities/DAO, mappers, implementations, Hilt
bindings and UseCases per action. An injected clock supplies timestamps. The
start timestamp means the first logged set or manual finish, not gym arrival;
the manually entered total is independent of this timestamp.

## Deliberate scope limits

- A replacement program and an accepted weekly split are not activated. The
  owner's historical wave structure is retained while current feedback is
  collected. A detailed re-entry prescription remains a separate discussion.
- A source day is still completed once in the imported sequence. Repeating a
  finished template as a new session and editing closed actual-set details are
  future work; next-day feedback can already be updated.
- There is no bulk voice parser, automatic diet/training aggregation, validated
  AI suggestion import or computer agent bridge in this release. Phone keyboard
  input remains available for notes; no speech recognition is claimed.
- Personal chat intake/video files are Git-ignored local context and are not
  bundled in the APK or silently imported as verified sessions.
- Current extra-set editing is limited to recording and undoing; there is no
  assisted-bodyweight model or automatic effective-body-mass tonnage estimate.

## Verification targets

Tests cover actual-plan snapshot preservation, partial finish, legacy unknowns,
active-session isolation, undone rows, migration preservation, source-wave
counts, symptom/illness priority, invalid input and independent substitute loads.
Emulator QA passed a real version-3 upgrade without clearing data: all previous
table row counts and DataStore bytes were preserved. Actual sets/RPE, bodyweight
extras, partial finish, restart, next-day sequence advancement, native two-file
sharing and the JSON contents were checked. Forms passed at 100% and 130% fonts;
the latter also verified that a new day opens at the start of the list. Font
scale was restored to 100%. All 435 tests, screenshot verification, Lint, Detekt
and final debug assembly pass. The APK is versionName 1.1 / versionCode 2.
