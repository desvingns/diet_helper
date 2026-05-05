---
name: dh-runner
description: Runs Gradle verification tasks for diet_helper (tests, detekt, optional screenshot verify) and returns structured pass/fail JSON. Never reads or modifies source files. Minimal and fast.
---

# Runner Agent — diet_helper

Run verification tasks only. Do NOT read, write, or modify any source files.

## Environment (apply before every command)

```powershell
$env:JAVA_HOME = "D:\For_work\AS\jbr"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"
$env:JAVA_TOOL_OPTIONS = "-Djdk.net.unixDomain.tmpDir=C:\tmp"
$env:TEMP = "C:\tmp"; $env:TMP = "C:\tmp"
New-Item -ItemType Directory -Path "C:\tmp" -Force | Out-Null
Set-Location D:\diet_helper
```

## Step 1 — Unit tests (always run)

```powershell
.\gradlew.bat :app:testDebugUnitTest --no-daemon 2>&1 |
  Select-String -Pattern "PASSED|FAILED|ERROR|tests|BUILD" |
  Select-Object -Last 40
```

Parse: count PASSED and FAILED. If BUILD FAILED → collect error lines.

## Step 2 — Detekt (always run)

```powershell
.\gradlew.bat :app:detekt --no-daemon 2>&1 |
  Select-String -Pattern "error|violation|BUILD" |
  Select-Object -Last 20
```

Parse: if BUILD SUCCESSFUL → "ok". If violations → count and collect messages.

## Step 3 — Screenshots (only if `screenshot_record_needed=true` in prompt)

```powershell
# Record new baselines first
.\gradlew.bat :app:recordRoborazziDebug --no-daemon 2>&1 |
  Select-String -Pattern "FAILED|BUILD" | Select-Object -Last 10

# Then verify
.\gradlew.bat :app:verifyRoborazziDebug --no-daemon 2>&1 |
  Select-String -Pattern "FAILED|BUILD" | Select-Object -Last 10
```

If `screenshot_record_needed=false` → skip, set `"screenshots": "skipped"`.

## Return

Output exactly this JSON (no extra text):

**On success:**
```json
{"pass": true, "tests": "42 passed / 0 failed", "detekt": "ok", "screenshots": "ok|skipped"}
```

**On failure:**
```json
{"pass": false, "tests": "40 passed / 2 failed", "detekt": "3 violations", "screenshots": "skipped", "errors": ["TestClass.methodName: expected X but was Y", "..."]}
```
