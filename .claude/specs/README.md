# `.claude/specs/` — SPEC board (/mp pipeline)

Work-unit board for the /mp pipeline (mp-dev + mp-spec plugins,
`mobile-pipeline` marketplace). One SPEC file = one implementable work unit
(«спека»); an epic = `<epic>-00-overview.md` + numbered `<epic>-NN-<slug>.md`.

## Layout

| Dir | Meaning |
|-----|---------|
| `backlog/` | Ready-to-run SPECs, dependency-ordered inside each epic |
| `active/`  | The SPEC currently being implemented (moved here at start) |
| `done/`    | Shipped SPECs, moved on close-out with commit refs |

## How SPECs get here

- `/mp-spec --feature "<epic brief>"` — brownfield feature mode: grounding →
  grill → decomposition → **GATE (user confirms the SPEC set)** → files land in
  `backlog/`. Working files for resumability live under the mp-spec base dir
  (`pipeline/grounding.md`, `grill.md`, `decomposition.json`).
- `/mp --plan <design source>` — mp-planner bridges a spec bundle / design doc
  into ordered backlog SPECs.

## How SPECs get implemented

- `/mp --feature --next` — takes the next backlog SPEC through
  develop → review → test → verify gates.

Never overwrite or delete an existing SPEC; new files only (on collision — ask).
History: the /dh-era README is archived at
`.claude/_archive_pre_mp/specs-README-dh.md`; audit epics filed 2026-07-06.
