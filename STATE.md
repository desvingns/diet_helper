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
- **Last completed:** Iteration 6 sub-iteration E — TDD red-green mode (`--tdd` flag, RED/GREEN phase sections in `dh-tester`/`dh-developer`, TDD subsection in `dh.md`) (2026-05-18, uncommitted — sub-B + sub-C + sub-D + sub-E land together in a single commit)

## Recently shipped (last 5 commits)

- 2026-05-17 `93a63d0` chore: make dh pipeline cross-platform (linux + windows)
- 2026-05-17 `18f7908` chore: ignore `.idea/` directory entirely and untrack existing files
- 2026-05-17 `1d81e2f` chore: add git push step to dh pipeline after tests pass
- 2026-05-17 `ad592ee` test: add week-navigation unit tests, testTag for today feed
- 2026-05-17 `294a360` fix: make `TodayContent` public for testability

## Known tech debt

- Eval framework for `dh-*` agents — deferred until 10+ runs of the new pipeline accumulate (then runs become the eval set)
- Tool-output sandbox (sandbox-large-output hook) — deferred; not yet a real problem, `grep | tail -40` in `dh-runner` is good enough
- No `git worktrees` for parallel agent execution — current pipeline is sequential; add when a feature genuinely needs parallel work

## Up next (head of ROADMAP)

- Iteration 6 sub-iteration F — output sandbox for gradle logs *(optional, only if `grep | tail` becomes insufficient — defer unless needed)*
- Iteration 7 — first product feature after iter 6 ships (TBD via `/dh --discuss`)
- Validate `--tdd` on 2-3 real features and decide whether to make it default
