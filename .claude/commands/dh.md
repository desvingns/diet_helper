Senior Kotlin/Android developer for the diet_helper repository.
Replaces the /new_dh skill. Use for ALL diet_helper tasks.

**Cross-platform.** Runs on Linux (Ubuntu) and Windows. All shell commands in this pipeline
MUST be executed through the `Bash` tool (Git Bash on Windows, native bash on Linux) — never
PowerShell. All spawned agents already declare `tools: Bash` in their frontmatter for the
same reason. Paths must never be hard-coded; use `git rev-parse --show-toplevel` or relative
paths from the repo root instead.

Usage:
  /dh --feature <description>         — new functionality (default: developer-first order)
  /dh --feature --tdd <description>   — new functionality, TDD red-green order (tester writes failing tests first)
  /dh --bugfix  <description>         — broken behaviour to fix
  /dh --discuss <topic>               — brainstorm options before committing to a SPEC (read-only, no code)

## Startup

1. Read `CLAUDE.md` (at the repository root) for tech stack and architecture.
2. Confirm task type. If flag missing → ask: "Это новая фича или баг?"

---

## Workflow: --discuss

For brainstorming approaches before committing to a SPEC. No code is written, no tests run.

### Phase 1 — Brainstorm

Spawn agent `dh-architect` with prompt:
```
Brainstorm approaches for the topic below. Return one BRAINSTORM block per your output spec.

TOPIC: [user's argument after --discuss]
```

Print the agent's full BRAINSTORM block to the user verbatim. Do not summarise it.

### Phase 2 — Optional persistence

Ask:
"Сохранить как spec-черновик в `.claude/specs/`? (y/N)"

If **N** → skip to Phase 3.

If **y** → ask: "Slug (kebab-case, короткий)?" Then write `.claude/specs/<slug>.md` using the `Write` tool with this content:

```markdown
# <Topic from BRAINSTORM, restated>
Status: brainstorm
Date: <today YYYY-MM-DD>

## Brainstorm output
<full BRAINSTORM block>

## Approved SPEC
(pending — fill in when `/dh --feature` is run for this)

## Implementation links
(pending — commit hash and changed files after implementation)
```

If a file at that path already exists → show its current content and ask whether to overwrite, append a new brainstorm section, or pick a different slug.

### Phase 3 — Report

```
💡 Brainstorm: [topic restated]
   Options surfaced: [N from BRAINSTORM]
   Recommendation: [RECOMMENDED line from BRAINSTORM]
   Saved to: [.claude/specs/<slug>.md] | not saved
   Next: /dh --feature when ready
```

---

## Workflow: --feature

### Phase 0 — Brainstorm trigger (optional)

Before exploring the codebase, evaluate the user's feature description. Trigger heuristics:

- Description longer than ~150 characters, OR
- Touches ≥2 architectural layers (e.g. "new screen + new entity" → presentation + domain + data), OR
- User signals uncertainty ("thinking about", "не уверен", "как лучше", "options for", "не знаю как")

If any trigger fires → ask:
"Это выглядит как большая фича. Запустить brainstorm перед SPEC? (y/N)"

If **y** → spawn `dh-architect` (same prompt as `--discuss` Phase 1), show the BRAINSTORM block, then ask:
"Какой вариант берём? (1 / 2 / 3 / отмена)"

- If user picks a number → proceed to Phase 1. Include the choice in `WHAT` or `CONSTRAINTS` of the SPEC so the developer knows which option was chosen.
- If user says "отмена" → stop. Do not generate a SPEC.

If no trigger fires, or user answers **N** → proceed directly to Phase 1.

### Phase 1 — Spec

Explore the relevant codebase area. Then ask ≤3 questions to close ambiguities:
- Affected screen(s)? New screen or extension?
- New use case or extend existing?
- New persistence? (Room entity / DataStore key)
- UI validation rules? Edge states (loading/empty/error)?

When answers are clear, output SPEC block and wait for user approval:

```
=== SPEC ===
TASK: feature
WHAT: [one sentence]
LAYERS: [domain] [data] [presentation]
CHANGED_HINT: [existing files to read, or "explore"]
TEST_TYPES: unit [dao] [compose-ui] [screenshot]
CONSTRAINTS: [specific rules or "none"]
```

**Do not proceed until user confirms SPEC.**

### Phase 2 — Implement

**Mode selection.** If the user passed `--tdd` after `--feature` → use the **TDD order** described at the end of this Phase (after Step 6). Otherwise use the **default order** below.

Spawn agents in sequence. Pass SPEC to each.

**Step 1 — Developer** (implement feature):
Spawn agent `dh-developer` with prompt:
```
Implement strictly per SPEC below. Return JSON: {"changed_files":[...], "commit":"hash"}

SPEC:
[paste SPEC block]
```

**Step 1.5 — Reviewer** (check layer boundaries):
Spawn agent `dh-reviewer` with prompt:
```
Check Clean Architecture boundaries for the files below.
Return JSON: {"pass": bool, "violations": [...]}

CHANGED_FILES:
[output from developer agent]
```

If Reviewer returns `pass=false` → stop immediately, show violations to user. Do NOT proceed to Tester.

**Step 2 — Tester** (write comprehensive tests):
Spawn agent `dh-tester` with prompt:
```
Write tests per SPEC and for CHANGED_FILES below.
Return JSON: {"test_files":[...], "screenshot_record_needed": bool}

SPEC:
[paste SPEC block]

CHANGED_FILES:
[output from developer agent]
```

**Step 3 — Runner** (verify everything passes):
Spawn agent `dh-runner` with prompt:
```
Run verification. screenshot_record_needed=[bool from tester]
Return JSON: {"pass": bool, "tests":"N passed/M failed", "detekt":"ok|N violations", "screenshots":"ok|skipped|N failures"}
```

**Step 4** — If Runner returns `pass=false`, attempt ONE automatic fix:

Spawn `dh-developer` with prompt:
```
Fix the failing checks below. Do NOT add new logic or change behaviour — only make the checks pass.
Return JSON: {"changed_files":[...], "commit":"hash"}

SPEC:
[original SPEC block]

FAILED CHECKS:
tests:  [tests value from Runner]
detekt: [detekt value from Runner]
errors: [errors array from Runner]
```

Then spawn `dh-runner` again with the same prompt as Step 3.
If the second run still returns `pass=false` → stop, show both failure reports to user and ask for guidance.

**Step 4.5 — Verifier** (static wiring checks + manual checklist gate before push):
Spawn agent `dh-verifier` with prompt:
```
Verify the implementation is wired into the app and generate a manual checklist.
Return JSON: {"pass": bool, "static_checks": {...}, "manual_checklist": [...]}

SPEC:
[paste SPEC block]

CHANGED_FILES:
[union of all changed files from Developer step(s)]
```

If Verifier returns `pass=false` → stop. Show `static_checks` failures to user and ask:
"Зафиксить и продолжить? Опиши как пофиксить, или запусти `/dh --bugfix`."

If Verifier returns `pass=true` → print `manual_checklist` verbatim to the user, then ask:
"Pre-push verification: прогони чеклист на эмуляторе или устройстве. Готов пушить? (y/N)"

- If user answers **y** → proceed to Step 5 (Push).
- If user answers **N** → stop. Do NOT push. Wait for user feedback before doing anything else.

**Step 5** — Push to remote (via the `Bash` tool):
```bash
# Token is provided via the GITHUB_TOKEN env var (configured in ~/.claude/settings.json,
# so it is available to every Bash invocation on both Linux and Windows/Git Bash).
# Reuse whatever remote is configured for `origin` instead of hard-coding the URL.
remote_path=$(git remote get-url origin | sed -e 's#^https://[^/]*@#https://#' -e 's#^https://##')
git push "https://x-access-token:${GITHUB_TOKEN}@${remote_path}" HEAD
```
If push fails → show error to user and continue to Step 6 without blocking.

**Step 6** — Always spawn `dh-docs` (it always refreshes `STATE.md`, even if `DOCUMENTATION.md`/`CLAUDE.md` need no changes):
```
SPEC: [paste]
CHANGED_FILES: [list]
Refresh STATE.md. Update DOCUMENTATION.md / CLAUDE.md only if genuinely new content (see dh-docs rules).
```

---

#### TDD mode (--tdd flag, optional)

If the user passed `--tdd`, replace the default Step 1..Step 6 above with the renumbered order below. Prompt formats are identical to default mode unless noted — refer to the matching default step for the full prompt template.

**Step 1 — Tester (RED phase).** Spawn `dh-tester` with this prompt:

    red_phase=true

    Write failing unit tests (ViewModel + UseCase only) for SPEC.WHAT.
    Production code does not exist yet — that's the expected red signal.
    Return JSON per RED phase mode: {"test_files":[...], "screenshot_record_needed": false, "phase":"red", "expected_failures":[...]}

    SPEC:
    [paste SPEC block]

**Step 2 — Runner (expect red).** Spawn `dh-runner` with the default Step 3 prompt. **Interpret the result yourself:**

- If `tests` reports failures AND `detekt` is `ok` AND the failures plausibly match `expected_failures` from Step 1 → red is correct, proceed to Step 3.
- If `tests` reports `0 failed` → tester didn't actually pin a contract. Stop and ask user.
- If failures look like compile errors on the **test code itself** (not on referenced-but-not-yet-existing production classes) → tester broke syntax. Stop and ask user.

**Step 3 — Developer (GREEN phase).** Spawn `dh-developer` with this prompt:

    green_phase=true
    TEST_FILES: [list from Step 1]

    Implement production code until the listed tests are green. Do not modify the tests.
    Return JSON: {"changed_files":[...], "commit":"hash"}

    SPEC:
    [paste SPEC block]

**Step 3.5 — Reviewer.** Same as default Step 1.5 (Clean Architecture boundaries on the new CHANGED_FILES).

**Step 4 — Tester (default phase, second pass).** Spawn `dh-tester` again with the default Step 2 prompt and the now-implemented CHANGED_FILES. This fills in `dao`, `compose-ui`, `screenshot` tests for any test types in SPEC.TEST_TYPES that the RED phase skipped.

**Step 5 — Runner (expect green).** Same as default Step 3. From here the chain matches the default order:

- **Step 6** — Auto-fix retry (same as default Step 4).
- **Step 6.5** — Verifier (same as default Step 4.5).
- **Step 7** — Push (same as default Step 5).
- **Step 8** — Docs (same as default Step 6).

### Phase 3 — Report

```
✅ feat: [description]
   Commit: [hash]
   Tests: [N passed]
   Detekt: ok
   Pushed: yes / failed: [reason]
   Files: [list of created/changed files]
```

---

## Workflow: --bugfix

### Phase 1 — Locate

Read bug description. If reproduction steps unclear, ask only:
- Which screen / flow?
- Actual vs expected behaviour?

Skip questions if bug location is obvious.

### Phase 2 — Fix

**Step 1 — Developer**:
Spawn agent `dh-developer` with prompt:
```
Fix bug per SPEC. Write regression test (red→green).
Return JSON: {"changed_files":[...], "commit":"hash"}

SPEC:
TASK: bugfix
WHAT: [root cause one sentence]
LAYERS: [affected layers]
CHANGED_HINT: [files to read]
TEST_TYPES: unit
CONSTRAINTS: regression test required, conventional commit fix:
```

**Step 1.5 — Reviewer** (if fix touches `presentation/` or `domain/`):
Spawn agent `dh-reviewer` with the changed files from Step 1.
If `pass=false` → stop, show violations.

**Step 2 — Runner**:
Spawn agent `dh-runner` with prompt:
```
Run verification. screenshot_record_needed=false
```

**Step 3** — If `pass=false`, attempt ONE automatic fix:

Spawn `dh-developer` with:
```
Fix the failing checks below. Do NOT change the bugfix logic — only make checks pass.
Return JSON: {"changed_files":[...], "commit":"hash"}

ORIGINAL SPEC: [bugfix SPEC block]
FAILED CHECKS: [errors from Runner]
```

Then spawn `dh-runner` again. If still `pass=false` → stop, show failures to user.

**Step 4** — Push to remote (via the `Bash` tool):
```bash
remote_path=$(git remote get-url origin | sed -e 's#^https://[^/]*@#https://#' -e 's#^https://##')
git push "https://x-access-token:${GITHUB_TOKEN}@${remote_path}" HEAD
```
If push fails → show error to user and continue without blocking.

**Step 5 — Docs** (always — refreshes STATE.md):
Spawn `dh-docs` with SPEC and CHANGED_FILES. It always refreshes `STATE.md`; it updates `DOCUMENTATION.md`/`CLAUDE.md` only if the fix reveals a new architectural decision.

### Phase 3 — Report

```
🐛 fix: [description]
   Root cause: [one sentence]
   Commit: [hash]
   Tests: [N passed]
   Detekt: ok
   Pushed: yes / failed: [reason]
```

---

## Rules

- Orchestrator NEVER writes Kotlin/Compose/Gradle code or tests.
- Orchestrator NEVER modifies application source files directly. (Writing markdown artifacts to `.claude/specs/` during `--discuss` is allowed — these are planning documents, not code.)
- All code changes happen inside spawned agents.
- If a spawned agent fails — stop the chain and report immediately.
- Maximum 3 clarifying questions before generating SPEC.
- `dh-reviewer` runs after every Developer pass, before Tester. A reviewer violation blocks the chain.
- Runner gets at most 2 runs per task (1 main + 1 retry after auto-fix). Never loop more than once.
- `dh-verifier` runs after Runner pass on `--feature` only. A static_checks failure blocks the chain; on pass, push waits for explicit user `y` after the manual checklist is shown. (`--bugfix` skips Verifier — bugfixes rarely touch wiring.)
- `--tdd` flag (only on `--feature`) reorders Phase 2: Tester writes failing unit tests first (`red_phase=true`), Runner verifies the red, then Developer implements until green (`green_phase=true`). Opt-in only; default order remains developer-first. `--bugfix` is unchanged — regression tests are written inline by the developer there.
