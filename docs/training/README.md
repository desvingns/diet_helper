# Personal training knowledge and product direction

Created / evidence checked: 2026-10-19.

This is the entry point for continued work on the owner's training, nutrition
and technique support. It is an initial, curated evidence base, not an
exhaustive systematic review or a claim to have found a universally best plan.

## Read next

- [Evidence and method comparison](evidence.md): 19 research/guideline anchors,
  applicability limits and practical conclusions.
- [Source registry](sources.json): stable IDs, URLs, reading scope and caveats.
- [Complete SRC7 wave analysis](src7-wave-analysis.md): all 57 populated days,
  including volume/intensity differences and incomplete-cycle caveats.
- [Version 1.1 implementation](implementation.md): shipped behavior, persistence
  and explicit scope limits; delivery verification is in STATE.md.
- [Separate workout programs, version 1.2](workout-programs.md): independent SRC7
  and return-to-training blocks, weekly working weights and migration details.
- [Product and coaching design](adaptation-design.md): current implementation,
  proposed behavior, acceptance examples and delivery order.
- [Session check-in template](templates/session-checkin.md): feedback before,
  during, after and the next morning.
- [Technique review template](templates/technique-review.md): filming guidance
  and an observation ledger with timestamps and confidence.
- [Export example](templates/coaching-export.example.json): illustrative future
  payload, distinct from the implemented runtime exporter; no importer exists yet.

Private, Git-ignored owner context lives in
`../../.local/training/athlete-profile.md` and
`../../.local/training/observations.jsonl`. These are source material for future
sessions, not data already imported into the Android app. Keep personal video
files / reviews in that local directory too. A clone alone will not include
them; an intentional private backup is needed for another computer.

## Operating agreement

1. Prioritize health, adherence, body composition and restoring strength.
   Competition preparation is a separate future goal if the owner chooses it.
2. Personalization is welcome. Challenge proposals with evidence and explain
   uncertainty; do not invent certainty, a diagnosis or an injury-risk score.
3. Preserve dated reports. Current state, historical records, estimated 1RM,
   training settings and exercise variants are different concepts.
4. Include all additional work in the workload discussion, including bodyweight
   exercises and back extensions described as a warm-up.
5. Use feedback to propose changes with reasons; keep a record of decisions.
   Unknown pain / recovery data must remain unknown.
6. Do not assume a healthy-adult training study supplies a post-injury protocol.
7. Record a technique observation before suggesting a correction. Videos cannot
   diagnose disc pathology or show internal tissue forces.

## Current deliverable status

DONE: private intake and tool-neutral continuity; 19-source evidence base;
complete SRC7 wave analysis; version 1.1 factual training journal, partial finish,
exercise changes/preferences, next-day feedback, explainable suggestions and
Markdown/JSON export. Room 3→4 migration preserves the previous data. The full
435-test suite, screenshot verification, Lint, Detekt and APK assembly passed.

DONE in 1.2: separate programs and a reviewable return template with
locally configured weekly working weights. An owner-specific first-week coaching
proposal is kept privately; it is not automatically imported or accepted.

PENDING: confirming the proposed weekly schedule and revising it from real reports;
real owner feedback/video review; repeated-template sessions, richer nutrition
exports, voice parsing, validated suggestion import and an optional agent bridge.
The original wave progression is preserved. No automatic weight progression
has been activated. Delivery status is in STATE.md.

No paywalled programs, full copyrighted books or full research articles are
bundled here. The repository contains original summaries and bibliographic
links, with the reading scope stated for each source.
