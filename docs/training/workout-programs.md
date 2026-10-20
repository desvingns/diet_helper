# Two workout programs — version 1.2

Status: implemented and verified, 2026-10-20. This document describes
the app workflow, not medical clearance or a guaranteed six-week strength result.

## Separate blocks

- **SRC7:** the existing frozen spreadsheet import, all 57 populated days,
  original waves/factors/rounding, manually entered reference maxima.
- **Return to training:** an editable-load six-week template, five visits per
  week including one short optional upper-body visit. Main squat and light
  squat exposures, one sumo exposure and three bench exposures are distributed
  across the week. Warm-up sets are not falsely recorded as completed work.

Each program has independent completion flags, rest timer, active session,
journal view and export scope. Selection survives relaunch. Switching does not
finish, reset or copy a workout. Existing SRC7 progress and reference keys keep
their original names. Old journal rows belong to SRC7 after migration.

The newly supplied live Google Sheet could not be re-read: connector metadata
returned 403. No live synchronization or comparison to the linked revision is
claimed. The original bundled source asset remains unchanged.

## Return block

Working loads are entered locally for each week in three fields: squat, bench,
sumo. They are weights for main working sets, not percentages of historical
records, tested maxima or estimated current capacity. Light days use template
multipliers and existing 2.5 kg rounding. SRC7 settings cannot configure this
program. Private intake and owner-specific starter recommendations are not
bundled into public source or the APK; the owner enters the chosen weights.

Week1–2 emphasizes comfortable repeat work, week3 development, week4 a lighter
wave with fewer main sets and a 0.9 multiplier, week5 development and week6
controlled comparable sets. Suggested effort caps are coaching choices, not
validated post-injury thresholds. No mandatory maximum test or target record
is scheduled. Exercise and session notes explain the intended effort.

The next week has no inferred weights. The owner reviews an export with the
coach in this project and explicitly saves that week's weights, including the
same values when a repeat is appropriate. Saving is a deliberate manual action;
the app does not validate a medical assessment, receive an AI plan automatically
or monitor a chat. Missing week settings disable actual-set recording for that
week instead of displaying invented kilogram values.

The optional visit can be finished as modified with a reason and no fabricated
sets. Additional exercises use the existing actual-set form and separate load
selection. The template does not require stacking all previous pull-ups, dips,
extensions and rows onto every warm-up. Planned bodyweight pull-ups record null
external/planned weight rather than zero kilograms.

## Persistence and export

- Room 4→5 adds `training_sessions.programId TEXT NOT NULL DEFAULT 'src7'`.
  It preserves all existing rows, set relationships and notes.
- Active/latest-day lookups include program identity, so identical day and set
  keys in the two templates cannot share a session accidentally.
- Program-specific DataStore keys isolate flags/timers. Return weekly load keys
  are separate from SRC7 references. No old preference is renamed or cleared.
- UseCases reject recording from a stale selected program or an unconfigured
  week. Undo uses the recorded session's program, not whichever block is now open.
- Actual/planned weight snapshots retain existing first-write semantics.
- JSON export schema is now version2 and includes each session's program ID and
  name. The visible journal export contains the selected program. Domain export
  can explicitly request both. Markdown also identifies program per session.

## Validation scope

Tests cover colliding IDs across programs, independent progress/timers across
repository recreation, explicit weekly weights, invalid settings, rejected stale
actions, immutable snapshots, scoped exports and preserving journal migration.
Compose forms cover separate week input and planned bodyweight semantics.
Native upgrade and UI checks are recorded privately under `.local/training/`.
All 446 tests pass without skips, along with screenshot verification, Detekt,
Lint and debug assembly. A real emulator 4→5 upgrade preserved every previous
column value/row and byte-identical DataStore preferences. Native checks cover
two independent active sessions, weekly settings, partial finish, bodyweight
planned-set nulls, selected-program two-file export and forms at 100%/130% fonts.
Final delivery verification is recorded in STATE.md.
