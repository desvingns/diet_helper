# Project State — diet_helper

> **Live document.** Updated by `dh-docs` after each `/dh` run.
> Manual edits welcome — `dh-docs` only edits known sections and preserves the rest.
>
> Different from related files:
> - `STATE.md` (this file) — **now**: where we are today, what's mid-flight.
> - `ROADMAP.md` — **next**: planned iterations, ordered backlog.
> - `DOCUMENTATION.md` — **history**: feature changelog, architecture decisions, user flows.

---

## Now

- **Current iteration:** 6 — Workflow polish (infra, not product feature)
- **In progress:** idle
- **Last completed:** PDF export of diet report from Settings — date range + mode (DETAILED / SUMMARY_ONLY) + optional stats, A4 multi-page PdfDocument, FileProvider share (commits `3f15fbe` + `ca5fd37` + `c0763e0`, 2026-05-19, local — push pending, `GITHUB_TOKEN` missing in env)

## Recently shipped (last 5 commits)

- 2026-05-19 `c0763e0` fix: make ExportContent and ViewModel tests pass + detekt clean
- 2026-05-18 `ca5fd37` refactor: remove Android types from domain layer for PDF export
- 2026-05-18 `3f15fbe` feat: PDF export of diet report by date range
- 2026-05-18 `70c468e` New sub agents system
- 2026-05-17 `93a63d0` chore: make dh pipeline cross-platform (linux + windows)

## Known tech debt

- Eval framework for `dh-*` agents — deferred until 10+ runs of the new pipeline accumulate (then runs become the eval set)
- Tool-output sandbox (sandbox-large-output hook) — deferred; not yet a real problem, `grep | tail -40` in `dh-runner` is good enough
- No `git worktrees` for parallel agent execution — current pipeline is sequential; add when a feature genuinely needs parallel work
- `ExportViewModel` builds the share `Uri` via `Uri.Builder` instead of `FileProvider.getUriForFile` to bypass a Windows-Robolectric path-matching quirk in tests; revisit once Robolectric handles Windows authorities (added 2026-05-19)
- Three commits `3f15fbe` + `ca5fd37` + `c0763e0` are local-only — push pending until `GITHUB_TOKEN` is restored in the Bash session

## Up next (head of ROADMAP)

- Iteration 6 sub-iteration F — output sandbox for gradle logs *(optional, only if `grep | tail` becomes insufficient — defer unless needed)*
- Iteration 7 — first product feature after iter 6 ships (TBD via `/dh --discuss`)
- Validate `--tdd` on 2-3 real features and decide whether to make it default
