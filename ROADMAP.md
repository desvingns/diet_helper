# Roadmap — diet_helper

> **Planned and pending work**, ordered by intended iteration.
> Done items move to `DOCUMENTATION.md` → Feature Changelog (history).
> Current iteration and in-flight work live in `STATE.md`.
>
> Edit manually. The future `/dh --roadmap` flag (planned in iteration 6 sub-C) will assist
> with proposing new items based on `DOCUMENTATION.md` + `STATE.md`, but final approval is manual.

---

## Iteration 6 — Workflow polish (infra)

**Goal:** strengthen the `/dh` pipeline by adding cross-session memory, brainstorm phase,
verification gate, TDD mode, and persistent state artifacts. No product features in this
iteration — it improves *how* future iterations get built.

- [x] **A — Memory infrastructure.** 9 memory files + `MEMORY.md` index in
      `~/.claude/projects/C--Pet-diet-helper/memory/`. No SessionStart hook
      (`MEMORY.md` is loaded automatically by Claude Code).
- [x] **B — State artifacts.** `STATE.md` + `ROADMAP.md` at project root, `dh-docs` updates
      `STATE.md` after every `/dh` run.
- [x] **C — Brainstorm phase.** New `dh-architect` agent (read-only: `Read, Glob, Grep`).
      New `/dh --discuss <topic>` flag — runs architect, no SPEC, no code.
      Optional Phase 0 trigger in `--feature` for large SPECs.
      New `.claude/specs/` directory for persistent brainstorm artifacts.
- [x] **D — Verification gate.** New `dh-verifier` agent — static checks (nav wired, Hilt graph,
      Room schema, Russian strings) + manual checklist generator. Inserted as Step 4.5
      in `/dh --feature` between Runner and Push. Push only after user confirmation.
- [x] **E — TDD red-green mode.** New `--tdd` flag on `/dh --feature`:
      tester writes failing tests first → runner verifies red → developer turns them green.
      `dh-tester` gets a "RED phase mode" section; `dh-developer` gets a "GREEN phase mode" section.
      Stays opt-in until validated on 2-3 real features.
- [ ] **F — Output sandbox** *(optional, only if it becomes a real problem)*. `dh-runner`
      writes full gradle log to `.claude/sandbox/<ts>-<slug>.log` on FAILED, returns a link
      in its JSON instead of a 200-line stacktrace. Add `.claude/sandbox/` to `.gitignore`.

## Iteration 7 — TBD (next product feature)

To be decided. Candidates surface here once iteration 6 ships and `/dh --discuss` is available
for proper trade-off analysis.

## Backlog (unscheduled)

- Eval framework for `dh-*` agents (skill-creator pattern) — defer until 10+ pipeline runs
  exist to use as eval set
- Full sandbox-on-hook with SQLite/FTS5 (context-mode pattern) — only if simple sandbox
  from iteration 6 sub-F proves insufficient
- Vector DB for memory (Chroma, claude-mem pattern) — overkill at ≤15 memory files;
  plain markdown + read-on-demand works
- Git worktrees for parallel agent execution (superpowers pattern) — current tasks are
  sequential; add when a feature genuinely needs parallel work
