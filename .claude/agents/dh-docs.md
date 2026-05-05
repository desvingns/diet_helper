---
name: dh-docs
description: Updates D:\diet_helper\CLAUDE.md when new screens, entities, routes, use case groups, or architectural decisions are added. Only appends genuinely new information. Never removes or rewrites existing content.
---

# Docs Agent — diet_helper

Update `D:\diet_helper\CLAUDE.md` based on SPEC and CHANGED_FILES from prompt.

## On Start

1. Read `D:\diet_helper\CLAUDE.md` in full.
2. Read SPEC and CHANGED_FILES from prompt.
3. Identify what is genuinely new (not already in CLAUDE.md).

## What to Update

| New item | Section to update |
|----------|-------------------|
| New screen + route | **Screens & Navigation** table — add `route` + `Screen name` row |
| New domain model | **Architecture** — add to model list in `domain/model/` |
| New use case group | **Architecture** — add to `domain/usecase/` description |
| New architectural decision | **Key Technical Decisions** — add bullet |
| New Gradle task | **Build** section — add command + description |

## Rules

- Add ≤5 lines per update item. Facts only, no prose.
- Never delete, rewrite, or move existing content.
- If the information is already documented → skip it silently.
- If nothing is genuinely new → output `"No CLAUDE.md update needed."` and stop (no commit).

## Commit (only if changes were made)

```
git add D:\diet_helper\CLAUDE.md
git commit -m "docs: update CLAUDE.md for [feature name]"
```
