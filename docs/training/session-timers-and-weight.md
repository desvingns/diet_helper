# Session timers and dated weight editing — version 1.3

Activation: implemented in the Android app, 2026-10-21. This is a workflow and
measurement update, not a change to either program or to working loads.

## Workout flow

- The old advice card on the workout tab is replaced by the whole-session
  stopwatch and rest timer. Advice remains in the journal after a session.
- A warm-up card precedes the first lifting set. Start creates a dated empty
  session and starts both the warm-up and whole-session timers. Completing
  warm-up freezes its duration while the whole-session timer continues.
- The main Record set button confirms the displayed plan weight/repetitions
  immediately and starts rest. It does not open a feedback form or infer RPE.
  A short explanation appears under the button. Tapping a planned set row
  still opens the factual editor for changed loads/reps, symptoms or notes;
  additional exercises retain the explicit entry form.
- A cool-down card follows the exercises. Start/finish measures it separately.
  Completing cool-down freezes the total and opens the session feedback form.
  The form collects session effort, recovery, notes, outcome and optional
  social time. Partial/stopped sessions still require a reason.
- Direct Finish workout remains available for partial sessions without a
  cool-down. The recorded finish is then the time feedback is saved.
- Phase controls and factual sets persist through process death and program
  switches. Lifting records are disabled during warm-up and after cool-down
  starts. Warm-up/cool-down never count as prescribed lifting sets.
- A completed warm-up stays completed after reopening. SRC7 and Return retain
  independent active sessions. Legacy sessions retain their true first-log
  start; the UI identifies that basis instead of inventing an arrival time.

Room 5→6 adds four nullable phase timestamps using an explicit preserving
migration. Missing historical phase timings stay null. MD/JSON exports include
phase durations and total seconds, alongside the existing optional reported
gym/social minutes. Existing schema2 fields remain compatible.

## Body weight

- The entry form defaults to today and includes a Material date picker.
  New entries can be recorded for another date; saving again updates that day.
- Tap a history row or its pencil to load the original weight/date into the
  form. Editing keeps the record ID and updates both fields transactionally.
- Moving onto an occupied date is rejected with a readable error; both old
  records remain intact and the entered draft remains available for correction.
- Weight validation and sorted chart/delta calculations remain active.

## Icon concepts

Five generated selection concepts and exact prompts live in
`../design/icon-variants/`. Open its `index.html` for numbered full-size and
small-size previews. Integration of the selected concept is a separate step.
