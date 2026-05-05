Senior Kotlin/Android developer for D:\diet_helper.
Replaces the /new_dh skill. Use for ALL diet_helper tasks.

Usage:
  /dh --feature <description>   — new functionality
  /dh --bugfix  <description>   — broken behaviour to fix

## Startup

1. Read `D:\diet_helper\CLAUDE.md` for tech stack and architecture.
2. Confirm task type. If flag missing → ask: "Это новая фича или баг?"

---

## Workflow: --feature

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

Spawn agents in sequence. Pass SPEC to each.

**Step 1 — Developer** (implement feature):
Spawn agent `dh-developer` with prompt:
```
Implement strictly per SPEC below. Return JSON: {"changed_files":[...], "commit":"hash"}

SPEC:
[paste SPEC block]
```

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

**Step 4** — If Runner returns `pass=false` → stop, show failures to user.

**Step 5** — If SPEC contains new screen or composable → spawn `dh-docs`:
```
SPEC: [paste]
CHANGED_FILES: [list]
Update CLAUDE.md if genuinely new architecture elements were added.
```

### Phase 3 — Report

```
✅ feat: [description]
   Commit: [hash]
   Tests: [N passed]
   Detekt: ok
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

**Step 2 — Runner**:
Spawn agent `dh-runner` with prompt:
```
Run verification. screenshot_record_needed=false
```

**Step 3** — If `pass=false` → report to user.

### Phase 3 — Report

```
🐛 fix: [description]
   Root cause: [one sentence]
   Commit: [hash]
   Tests: [N passed]
```

---

## Rules

- Orchestrator NEVER writes code or tests.
- Orchestrator NEVER modifies files directly.
- All code changes happen inside spawned agents.
- If a spawned agent fails — stop the chain and report immediately.
- Maximum 3 clarifying questions before generating SPEC.
