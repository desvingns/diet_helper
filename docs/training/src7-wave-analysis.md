# SRC7 — complete wave analysis

Checked against the frozen imported asset on 2026-10-19. All 57 populated days
are included; this is descriptive plan analysis, not observed training or a
fatigue model. Reproduce with:

```text
python scripts/analyze_workout_waves.py --out docs/training/src7-wave-analysis.json
```

## The actual structure

The source varies demands between microcycles, between days and between exercise
rows within a day. A Heavy/Medium/Light label belongs to an exercise row. A day
can contain several different labels and repeated variants of the same lift.
Judging the program only from day 1 or calling all sessions uniformly heavy
misses this structure. The owner reports that this wave structure was comfortable.

| Microcycle | Populated days | Sets including ramps | All prescribed reps | Heavy/Medium/Light exercise rows | Highest basic-lift reference intensity |
|---|---:|---:|---:|---:|---:|
| 1 | 5 | 126 | 430 | 9/6/6 | 85.0% |
| 2 | 5 | 116 | 407 | 6/10/5 | 87.4% |
| 3 | 5 | 112 | 387 | 7/8/6 | 90.9% |
| 4 | 5 | 116 | 468 | 5/10/6 | 88.3% |
| 5 | 4 | 74 | 311 | 1/7/8 | 71.4% |
| 6 | 5 | 123 | 376 | 12/8/1 | 87.2% |
| 7 | 5 | 130 | 429 | 10/5/6 | 87.6% |
| 8 | 5 | 112 | 481 | 1/13/7 | 77.7% |
| 9 | 5 | 115 | 402 | 6/10/5 | 90.5% |
| 10 | 5 | 133 | 377 | 12/5/4 | 91.0% |
| 11 | 5 | 106 | 312 | 7/9/2 | 86.2% |
| 12 | 3 | 45 | 165 | 1/4/4 | 79.2% |

Reference intensity includes the exercise/cycle factor multiplied by the group
percentage, before plate rounding. It is relative to the manually entered
reference, not to a verified current maximum. Counts include accessories and
ramp sets; repetitions in different exercises are not equivalent training doses.

## Interpretation and limits

- Cycle 5 has fewer prescribed sets and only one Heavy exercise row. It also has
  four populated days (day 4 is blank), so the total is not directly comparable
  to a five-day cycle. The data alone do not establish why that slot is blank.
- Cycle 8 has only one Heavy row and lower basic-lift peak loading, but 481
  repetitions compared with 429 in cycle 7. It illustrates a reduction in some
  intensity demands alongside substantial volume, not a universal easy week.
- Cycles 6 and 10 contain more Heavy rows; varying volume and intensity are
  separate axes. Neither a label nor total repetitions measures actual exertion.
- Cycle 12 contains three populated days. Its smaller total is incomplete
  source coverage and must not be advertised as a confirmed planned deload.
- A microcycle is a sequence, not an enforced calendar week. There is no need
  to cram remaining days into a weekend or catch up omitted work.
- The first day combines different demands: Heavy back squat and front squat,
  Medium bench, Light good mornings. Light good mornings still add work.

## Resulting product decisions

Preserve the source asset, coefficients and within-cycle sequence. Display the
wave overview without a health/fatigue score. Add factual set logging and
feedback so actual tolerance can be compared with the intended wave. Changes to
exercise choice, reps and load are explicit in each performed set; do not
silently rewrite the entire source or turn every day into a heavy session.

## Exercise options and review

A different exercise is a change of stimulus, not a coefficient conversion.
Front-squat alternatives such as a leg press or split squat can be discussed
when comfortable and appropriate; their load must be entered independently.
A chest-supported row can supply upper-back work with torso support, but is
not mechanically equivalent to a good morning or a straight-leg deadlift.
The app labels such options as choices for discussion, never automatic rehab.

Additional pull-ups, dips, back extensions and rows belong in the journal.
They should not all be added by default simply because the source already
contains Light work. Exercise preference and symptoms are separate fields.
Keep one or two changes understandable and use subsequent feedback before
adding more work. These are coaching judgments, not a trial-tested program
for this individual; see evidence.md and the private profile.
