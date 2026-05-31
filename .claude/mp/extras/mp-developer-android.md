# mp-developer-android — diet_helper extras

Read **after** the plugin's `mp-developer-android` agent. diet_helper-specific overrides win on conflict.

- **Learning project.** Favour clarity over cleverness; when a non-obvious decision is made, leave a short rationale comment. The user is learning Clean Architecture + Compose.
- **UI language: Russian.** All user-facing strings in RU (code identifiers stay English).
- **Package:** `com.k.shavrin.diethelper` (note the `k.shavrin` segment). Source root: `app/src/main/java/com/k/shavrin/diethelper/`.
- **Persistence quirks:** `DailyGoals` lives in **DataStore Preferences**, not Room. `LocalDate` ↔ `Long` (epochDay) via a Room `TypeConverter`.
