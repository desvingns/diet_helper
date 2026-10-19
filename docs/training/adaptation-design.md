# Training feedback and adaptive coaching — proposed design

Status: researched proposal, 2026-10-19. No runtime behavior below is implemented
by this document. Owner preferences are known; weekly schedule, re-entry dose
and replacement exercise choices still need discussion.

## Product goal

Support one owner's health, body composition and return of strength with short,
repeatable sessions, a factual journal and understandable adjustments. Personal
defaults are appropriate. A generic coaching marketplace, social layer and
competition peak are outside this direction.

Evidence and limitations: `evidence.md`; private factual context:
`.local/training/athlete-profile.md`. Proposed numerical rules below are coaching
heuristics requiring review, not validated clinical thresholds.

## Inspected current behavior

| Verified source | Behavior / consequence |
|---|---|
| `domain/model/WorkoutPlan.kt`, `WorkoutExercise.weight()` | Recomputes rounded planned weights from current maxima, an exercise factor and a percentage. |
| `domain/model/WorkoutPlan.kt`, `WorkoutProgress` | Stores three maxima, completed day/set IDs and timer state; has no actual load, actual reps, dates, RPE or symptoms. |
| `data/repository/WorkoutRepositoryImpl.kt` | Imports a fixed asset and persists progress in DataStore; changing maxima does not preserve an actual-weight snapshot for completed sets. |
| `domain/usecase/workout/RecordWorkoutSetUseCase.kt` | Toggling a planned set records a flag and timer deadline, not what was lifted. |
| `domain/usecase/workout/CompleteWorkoutUseCase.kt` | Requires every planned set; there is no finish-partially / symptom-stop outcome. |
| `presentation/screen/workout/WorkoutViewModel.kt` | Resumes by plan IDs and selects the first unfinished day; cannot distinguish repeated sessions on the same template. |
| `app/src/main/assets/workout-plan.json`, day ID 0 | Back squat ramp + 3 triples + 3 singles, bench 4×4, front squat 1×3 + 5×2, and good mornings 5×5, plus accessory notes. |

The first source day contains 23 prescribed sets/set instances, including ramp
sets, before any unstructured accessories. This is a count, not 23 equivalent
hard hypertrophy sets or proof the session is unsafe. Planned sets are not
independently logged actual performance. Lowering base maxima does not remove
the exercise count or exposure.

## Proposed first release: a useful factual journal

1. A dated session with a stable ID, optional plan/template reference and revision.
2. Planned versus actual weight, reps and set outcome. Freeze the values used at
   that time; historical displays must not change when a maximum is edited.
3. Set RPE/RIR as optional feedback with a short explanation. Missing feedback
   remains missing. A single session-wide effort estimate must not be copied to
   every set.
4. Add an unplanned exercise / warm-up / accessory. Support bodyweight and
   assistance without interpreting unknown effective load as zero tonnage.
5. Finish as completed, modified, stopped or skipped. Permit omitted sets and
   record the reason. Do not force work just to obtain a completion checkmark.
6. Before/after check-in and next-day follow-up: sleep, subjective readiness,
   illness, symptoms, location, nature, when they occurred and whether technique
   changed. Keep pain separate from non-pain unusual sensations.
7. Session effort and duration separate from set RPE. Display their trend only
   when supplied, never as a medical or injury-risk score.
   Duration includes warm-up, cool-down and ordinary between-set rest. Keep
   unrelated interruptions, such as social conversations, separate from training
   time and optionally record total gym-visit time. For retrospective estimates,
   preserve an unknown inclusion basis rather than subtracting guessed breaks.
8. Share a readable report plus a versioned JSON export through Android sharing.
   User can choose a date range and whether personal notes are included.

Daily body mass and nutrition already have their own application features.
Reference those facts in the report instead of creating competing diaries.
Include observed days / missing days when presenting averages.

## Data model direction

Keep the existing intentionally verbose Clean Architecture:

- Domain: `AthleteProfile`, `TrainingReference`, `TrainingSession`,
  `PerformedSet`, `SessionCheckIn`, `SymptomObservation`, `IllnessEvent`,
  `TechniqueObservation`, `TrainingAdjustmentProposal`.
- Distinguish historical best, current tested maximum, estimated maximum and
  manually selected training reference, each with lift variant, date and origin.
- A performed set needs session ID, exercise/variant, planned snapshot, actual
  reps/load, units, bodyweight/assistance semantics, outcome and optional effort.
- A symptom observation needs type (pain / unusual sensation / soreness / other),
  location, laterality if known, intensity if supplied, movement phase,
  duration, neurological/systemic notes and report provenance.
- Room for dated sessions/events and relations; DataStore for small preferences.
  Add explicit preserving migrations, separate entity/domain mappers, repository
  interfaces/implementations and one UseCase per action.
- Inject a clock. Preserve local date, timezone and timestamp where known. Do
  not invent precise timestamps for retrospective chat reports.
- Existing completion flags can remain inspectable as legacy progress. Migration
  must not invent dates, actual weights, RPE or session identity for them.

## Candidate autoregulation behavior

This is a proposed decision policy, not a validated fatigue model. Start with
reviewable suggestions; collect data before considering automatic progression.

Priority order:

1. New concerning neurological/systemic symptoms: do not propose progression;
   show relevant assessment guidance. Urgent symptoms follow official triage
   advice [S11], not an app readiness score.
2. Illness or clinician restrictions: reconsider the session before selecting
   kilograms; no automatic catch-up for missed training [S16].
3. New/recurrent movement-specific symptoms or a changed movement strategy:
   hold progression for that movement, reconsider load/volume/variation and seek
   assessment if symptoms persist, recur or limit activity [S09–S10].
4. Normal tolerability but unexpected effort / missed reps: propose holding or
   reducing one demand. Do not punish a missed set with extra work.
5. Comfortable execution, intended effort and acceptable follow-up: consider a
   repeat or one small increase, limited to the exercise/equipment increment.
   No mandatory increase and no automatic jump based on historic strength.
6. Missing next-day/symptom data: mark the recommendation uncertain; do not
   present the session as proven well tolerated or raise load automatically.

Separate load, reps, sets and frequency. Do not increase all of them together.
For example, reducing one set or using the next lighter available increment can
be a review option; a universal −10% rule is not established for this owner.

Estimated 1RM can be an optional trend after comparable, adequately informative
sets exist. Display the estimation method and uncertainty; avoid extrapolation
from easy RPE 5–6 work. Never use e1RM as proof of tissue recovery.

Any adjustment stores the input session IDs, plan version, before/after values,
plain-language reason, relevant source IDs, author (rule / AI / owner), timestamp
and accepted/rejected status. A symptom note must not silently lower all three
lift references or rewrite the completed workout.

## Candidate weekly structure

The candidate is about five short visits with a flexible minimum. Actual
availability, preferred days and any schedule changes belong in the private
profile. A specific weekly split has not yet been settled.

A proposal to discuss is three main sessions plus up to two optional short
sessions. Keep weekend demands complementary; avoid making both mornings hard
lower-body sessions during re-entry. Optional sessions can be brief technique,
upper-body/accessory or comfortable aerobic work depending on tolerance.
They must be removable without adding their missed volume to another day.

Do not label light extra work “free recovery.” Pull-ups, dips, back extensions,
rows and good mornings all contribute. Exercise selection for the back is
individual; any variation with a reported unusual sensation should not be
progressed automatically or explained as atrophy without assessment.

## Nutrition direction

Reported intake / gain is not a maintenance-calorie measurement. Start with
roughly a week of consistent food logging and morning weights, then consider
2–4-week trends as data accumulate; avoid interpreting an isolated water/glycogen
change as fat gain. The owner's existing weigh-in habit is useful.

Consider a modest initial weight-loss pace rather than stacking a large deficit
with rapid training increases [S12]. An initial 0.25–0.5%/week band would be a
conservative coaching proposal, not a research-established individual optimum.
No calorie target is activated. Review protein using an explicit body-mass basis
and actual intake [S13–S14]; his reported dieting habit is useful starting context.
Keep the desired body-fat percentage labeled estimated until better assessed.

## Export, voice notes and AI continuity

First step: Markdown summary for reading + JSON for exact values. Include units,
schema version, date range, missing fields, source provenance and plan revision.
The checked-in JSON example contains synthetic data only and is not an
implemented Android exporter or formal import schema.

Voice workflow: dictate or attach a note, preserve original text, extract a draft
of exercises/reps/weights/symptoms, and review before accepting it. “Three by
three” and “three singles” must not be guessed from an uncertain transcription.
An unknown number stays unknown. Typed editing must always be available.

Use exports in this project with Codex or Claude. Keep responses as proposals;
later implement a separately validated import with stable IDs, visible changes,
version checks and explicit application. Report content is untrusted data,
not authority to execute commands, modify app rules or contact someone.

For a later computer bridge, the
[official Codex SDK documentation](https://learn.chatgpt.com/docs/codex-sdk)
describes programmatic control of local agents and starting/resuming threads.
That capability does not prove an Android app can directly create a visible chat
in this particular desktop app. Verify destination/UI behavior during that
integration. Keep credentials on the computer/backend, pair devices, deduplicate
requests, avoid embedding secrets in the APK, and do not auto-launch arbitrary
shell commands from a note. No bridge, scheduled automation or thread has been
created by this research task. Claude transport should be evaluated separately.

## Acceptance examples for later implementation

- Editing an example squat reference from 100 to 110 kg leaves a previously
  recorded actual 80 kg triple unchanged.
- A user stops after two sets due to a new sensation: the session can finish as
  stopped, keeps both performed sets and records why the remainder was omitted.
- Logging pull-ups and a 40 kg row adds actual work without requiring a matching
  source-template exercise. Assistance and per-hand weight are explicit.
- Effort is low but a movement caused an unusual sensation: no automatic load
  increase is proposed for that movement.
- The next-day check-in is unanswered: show unknown, not symptom-free.
- Repeating a source day creates another dated session; no earlier data is reset.
- Historical completion flags migrate without fabricated actual loads or dates.
- Illness interrupts the week: optional work is dropped; it is not piled onto
  Saturday and Sunday or used to clear a completion streak.
- A synthetic export round-trips without losing kilograms, timezone, nulls,
  exercise variant or symptom type. A duplicated request does not create another
  performed set. Unsupported versions are rejected without data loss.
- A voice draft with an uncertain weight cannot silently become a performed set.

## Delivery order

1. DONE: evidence base, private intake, tool-neutral continuity and current-state
   audit in this document.
2. NEXT: settle practical time constraints and a conservative re-entry draft;
   use next-day tolerability feedback and any relevant assessment advice.
3. Implement dated factual logging, actual values, partial finish and export.
   Verify migration preservation, history snapshots and null/missing semantics
   with meaningful fakes-based tests; verify affected UI and build.
4. Add symptom/recovery follow-up and explainable manual adjustment proposals.
5. Add technique observation history and nutrition/training summaries.
6. Consider voice extraction, validated AI suggestion import, and a computer
   bridge only after the basic journal is useful and reliable.
