# Active workout screen — version 1.4

Implemented at the owner's request on 2026-10-25. These changes are active app
behavior, not changes to the training program or coaching rules.

- Warm-up start/in-progress controls remain available; the whole warm-up card
  disappears when completion arrives.
- The separate rest countdown card and the recording explanation are removed.
- The next-set card retains its quick recording action and previews the second
  remaining set only when both upcoming sets share the same exercise ID.
  Completed sets are skipped; a different exercise stops the preview. Weights
  use the selected day's configured loads, including explicit bodyweight labels.
- A clock in that card updates every second from the latest completed set's
  persisted timestamp in the current program/session. It continues after a
  process restart. No known completion timestamp is displayed as an em dash.
- Editing a completed planned set preserves its original timestamp, including
  an unknown legacy timestamp. Undo excludes that set from the clock; completing
  it again records the new completion time. Additional completed sets also count.

Room version 7 registers the explicit preserving migration 6→7, adding nullable
`training_sets.completedAt`. Old times are not guessed from the old countdown.
Existing JSON/Markdown export formats are unchanged; they do not export the
new per-set timestamp, and there is no journal importer.

Validation: all 474 JVM/Compose tests pass without skips; Detekt, Lint,
Roborazzi verification and debug assembly pass. A real emulator upgrade from
version 1.3 preserves all 271 existing rows, every previous column value and
byte-identical DataStore preferences. Native checks cover warm-up disappearance,
the following-set preview, unknown-time placeholder and live clock after recording.
Force-stop/relaunch preserves the running elapsed value (18→32 seconds in QA).
Private QA snapshots and screenshots remain under Git-ignored `.local/training/`.

Delivery artifact: `.local/MyDietHelper-training-v1.4-debug.apk`, application
versionName 1.4 / versionCode 5. Telegram delivery status is recorded in STATE.md.
