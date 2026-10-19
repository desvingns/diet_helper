# Food catalog and workouts — 2026-10-19

Implemented directly in the existing Kotlin/Compose/Clean Architecture application, without the MP pipeline.

## Food provenance

- `app/src/main/assets/diary-products.json`: 132 distinct product labels from the user's May and June 2026 FatSecret PDFs. Nutrition is extracted by column coordinates and normalized from an explicit gram or milliliter quantity. Eight foods have only a portion description; their nutrition remains per original portion. The UI, editing, totals and PDF export use that basis consistently. Rounded original totals introduce small rounding differences per 100 g. Truncated labels remain marked as truncated.
- `app/src/main/assets/regional-products.json`: 107 complete Open Food Facts records tagged `en:serbia`, including 73 with a Lidl store tag. Frozen from the public Search-a-licious API on 2026-10-19. Country/store tags are community catalog information, not a guarantee of current local stock. Individual barcode and source links are preserved.
- Open Food Facts data is available under the [Open Database License](https://world.openfoodfacts.org/terms-of-use). Attribution and source links are also available in the app. No remote pictures are bundled.
- Full-text lookup uses `https://search.openfoodfacts.org/search`, explicit search languages, country/store filters and bounded `hits`. The legacy CGI endpoint returned HTTP 503 during verification. Exact barcode lookup uses Product API v2. Search is explicit and cached for the session; no diary history or workout data is transmitted.
- Selected external products are imported by barcode into Room before diary entries reference them. Re-importing reuses the local record.
- Unicode case folding and Serbian transliteration happen in Kotlin because SQLite NOCASE does not case-fold Russian. A small vocabulary supplies Russian aliases and translates common Russian queries for Serbia. Original package names remain visible.

## Persistence

Room version 3 adds provenance and reference-unit columns with an explicit, non-destructive 2→3 migration. Existing product IDs, nutrition and favorites are preserved. The destructive fallback was removed. Catalog seeding is transactional and versioned; retries skip existing diary names and barcodes without overwriting custom foods or historical references.

Raw exports and full extracted diaries stay in ignored local folders, outside the application package and Git.

## Workout source

The source is [СРЦ7, 1 в день](https://docs.google.com/spreadsheets/d/1XUjSjMRlWZDY_bSzxbbpLvxjwk6fR6UKCpmhROFg8L8/edit#gid=848117894). This is a frozen import, not live synchronization. The sheet has 25 cycle slots, but 57 populated training days across cycles 1–12. Empty slots are omitted. The import contains 232 exercise rows and 508 set groups.

Source maxima (230/160/240 kg) are reference values for deriving coefficients and validating the import. They are not prefilled as the user's current maxima. Squat, bench and deadlift maxima must be entered before recording a set. Each exercise preserves the original base-lift coefficient and cycle progression. Positive MROUND rounding is reproduced: 2.5 kg for barbells, 1 kg for dumbbells, 0.1 kg where configured. Dumbbell weights are per hand.

DataStore persists maxima, completed sets/days and timer state. Completion requires all planned sets. Restart resumes a partially completed workout or selects the first unfinished workout. Completed workouts remain inspectable. The plan view supports cycle selection and exercise set groups.

The timer persists its deadline and paused duration, so navigation/restart does not reset it. Haptic feedback works while the screen is active; there is no Android background alarm. The active workout keeps the display on.

## Verification

All 408 unit/UI tests pass. Checks cover all 508 source weights, PDF nutrition columns, portion arithmetic, Russian/Serbian search, API array fields and incomplete nutrition, migration preservation, repeatable seeding, persisted workout state, completion guards and navigation. Android Lint, Detekt, Roborazzi verification and debug APK assembly also pass.

Five screenshot references were refreshed after visual comparison for the new dark palette and an existing export button absent from older Settings references. Previous references and comparison images were preserved under `D:/Pet/archive/MyDietHelper/2026-10-19/` and recorded in the shared archive index.

Emulator checks exercise food addition, upgrade without data loss, maxima entry, set completion, rest timer, restart/resume and both workout views. Public API smoke checks: `scripts/check_food_api.py`.
