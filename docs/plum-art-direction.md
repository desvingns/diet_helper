# Midnight plum art refresh — 2026-10-19

## Visual direction

Near-black plum backgrounds, layered aubergine cards, lavender actions, cream
type and restrained mint completion/status accents. Macro colors remain rose,
sand and blue so their meaning stays recognizable. The light theme uses the
same violet family on pale neutral surfaces. Launcher and system colors match.

Seven original raster illustrations were generated with the built-in
`image_gen` tool (no API/CLI fallback). The six cutouts retain their generated
alpha channel. Production WebP files are resized for Android and bundled in
the APK: no network request or image loading dependency is needed.

| Asset in `app/src/main/res/drawable-nodpi/` | Use |
|---|---|
| `art_nutrition_hero.webp` | Food photograph/illustration at the top of the daily summary |
| `art_meal_breakfast.webp` | Breakfast bowl and espresso meal icon |
| `art_meal_lunch.webp` | Chicken, rice and avocado meal icon |
| `art_meal_dinner.webp` | Salmon and asparagus meal icon |
| `art_meal_snack.webp` | Yogurt and berries meal icon |
| `art_training.webp` | Dumbbells, mat and towel in the workout heading |
| `art_journey.webp` | Journal and target in history, progress and goals; empty history |

Small navigation/action icons use Material Rounded vectors for clear 24 dp
silhouettes, with lavender selection and a muted plum indicator. Illustrations
are decorative and do not duplicate spoken labels. Numeric summaries and
controls remain live Compose content, separate from the generated pixels.

Motion is limited to short navigation arrival fades (220 ms), a small forward
offset, 180 ms tab color changes and meal-card size transitions. No continuous
animation, Spine runtime or character rig is required for these interface
transitions. The illustration banner expands with text rather than fixing its
height at large system font scales.

## Verification and delivery

- Debug assembly and Detekt passed.
- 415 JVM tests passed with zero failures, errors or skipped tests.
- All 11 Roborazzi references were updated for the palette change;
  recording and a separate verification pass succeeded.
- Android Lint completed with zero errors and 49 warnings.
- Emulator upgrade retained diary and workout data. The five main tabs, four
  meal illustrations, product-search entry/return, and 130% font layouts for
  Today, goals and training were checked. The original 100% font scale was restored.
- Every production image decodes; all six cutouts retain actual transparency.
  The complete artwork set is 376 KiB.

The debug delivery is `.local/DietHelper-midnight-plum-debug.apk`. Device captures,
the populated reference preview and verification logs are under ignored `.local/`.
Versioned screenshot references remain in `app/src/test/snapshots/`.

## Final prompt set

The following shared prefixes and asset suffixes are the production prompts
passed to the built-in tool. Cutouts used `transparent_background: true`;
the nutrition banner used `false`.

### Prefix A — nutrition banner, breakfast, training

> Use case: stylized-concept. Production art for a premium Android nutrition
> and strength tracker. Tactile editorial 3D illustration, sculpted ceramic and
> soft satin materials, beautiful restrained lighting, mature and refined,
> crisp silhouette, no lettering, no logos, no watermark. Palette: lavender,
> aubergine, cream, very small mint accents.

**Nutrition banner:** Asset type: wide background banner, 1536x1024 landscape
composition. A beautifully plated bowl of colorful vegetables, avocado,
roasted chickpeas and rice, a few blueberries and a violet linen napkin, on a
midnight aubergine tabletop (#181320). Food grouping confined to the RIGHT
45 percent, upper-right three-quarter angle. LEFT 55 percent almost empty dark
plum negative space for live app text. Subtle atmospheric violet light fading
to near-black on left and bottom. No interface, no numbers, no text. Full bleed
art, no border.

**Breakfast:** Asset type: single isolated app illustration, square. A small
handmade cream ceramic bowl of oats topped with raspberries and blueberries,
a tiny lavender espresso cup tucked beside it. A compact unified still life
viewed three-quarter from above, centered, occupies 85 percent of canvas. Fine
visual detail must read at 56dp. No ground plane, no square tile, no border.
Genuinely transparent background, clean alpha cutout.

**Training:** Asset type: single isolated header illustration, square. A pair
of beautifully sculpted lavender dumbbells, a rolled violet yoga mat with mint
strap and a small warm cream towel. Compact elegant sculptural still life in
three-quarter view, asymmetrical yet balanced, objects occupy 85 percent
canvas. No ground plane, no square tile, no badge or background. Genuinely
transparent background, clean alpha cutout.

### Prefix B — lunch, dinner, snack, journey

> Use case: stylized-concept. Production asset for a premium nutrition and
> strength Android tracker. Tactile editorial 3D illustration, sculpted ceramic
> and soft satin materials, refined and mature, soft beautiful studio lighting,
> clear readable silhouette. Palette lavender, aubergine, cream with tiny
> natural mint and warm peach accents. Single compact still life centered on
> square canvas, three-quarter from above, occupies 85 percent canvas,
> readable at 56dp. No text, no logos, no watermark, no border, no background
> tile, no ground plane. Genuinely transparent background, clean alpha cutout.

**Lunch:** An appetizing cream ceramic bowl of roasted chicken slices, rice,
sliced avocado, red cherry tomatoes and greens. Beautiful simple ingredient
grouping, lavender accent on the bowl rim.

**Dinner:** A single elegant lavender ceramic dinner plate holding a roasted
salmon fillet, a few asparagus spears and a wedge of lemon. Warm appetizing
coral salmon and small mint greens, refined plated composition.

**Snack:** A tiny sculptural lavender yogurt cup with a cream swirl, two ripe
strawberries, blueberries and a few almonds at its base. Elegant compact
grouping.

**Journey:** An open cream journal with blank pages and lavender covers, a
small lavender circular target resting next to it with a mint bullseye, and a
softly curved violet ribbon bookmark. A beautiful quiet symbol of personal
goals and a daily journal. No writing, no tick marks or numbers.
