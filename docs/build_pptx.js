// Generates D:\diet_helper\docs\a2a-architecture.pptx
// Run: node D:\diet_helper\docs\build_pptx.js

const pptxgen = require("pptxgenjs");

// ---------- THEME ----------
const C = {
  bgWhite:     "FFFFFF",
  bgNavy:      "1E2761",   // Title bar / hero
  bgSurface:   "F8FAFC",   // muted surface
  textDark:    "1E293B",
  textMuted:   "64748B",
  textOnNavy:  "FFFFFF",
  border:      "CBD5E1",

  opus:        "7B3FBF",   // purple
  sonnet:      "1F77B4",   // blue
  haiku:       "2CA02C",   // green
  orchestrator:"FFC107",   // yellow
  generic:     "E5E7EB",   // light gray for neutral boxes
  accent:      "14B8A6",   // teal accent
  danger:      "DC2626",   // red
  success:     "059669",   // emerald
  amber:       "F59E0B",   // amber for warnings
};

const F = {
  title:    "Calibri",
  body:     "Calibri",
  mono:     "Consolas",
};

// ---------- HELPERS ----------
const HEADER_H = 0.7;
const PAGE_W = 13.333;
const PAGE_H = 7.5;
const MARGIN = 0.5;

function addHeaderBar(slide, title, subtitle) {
  slide.addShape("rect", {
    x: 0, y: 0, w: PAGE_W, h: HEADER_H,
    fill: { color: C.bgNavy }, line: { color: C.bgNavy },
  });
  slide.addText(title, {
    x: MARGIN, y: 0.05, w: PAGE_W - 2 * MARGIN, h: HEADER_H - 0.1,
    fontSize: 28, bold: true, fontFace: F.title, color: C.textOnNavy,
    valign: "middle", align: "left", margin: 0,
  });
  if (subtitle) {
    slide.addText(subtitle, {
      x: MARGIN, y: HEADER_H + 0.08, w: PAGE_W - 2 * MARGIN, h: 0.4,
      fontSize: 14, italic: true, fontFace: F.body, color: C.textMuted,
      align: "left", margin: 0,
    });
  }
}

function modelBadge(slide, x, y, model) {
  const map = {
    Opus:   { color: C.opus,   label: "OPUS 4.7" },
    Sonnet: { color: C.sonnet, label: "SONNET 4.6" },
    Haiku:  { color: C.haiku,  label: "HAIKU 4.5" },
    Orchestrator: { color: C.orchestrator, label: "ORCHESTRATOR" },
  };
  const cfg = map[model];
  slide.addShape("roundRect", {
    x, y, w: 1.4, h: 0.32,
    fill: { color: cfg.color }, line: { color: cfg.color },
    rectRadius: 0.05,
  });
  slide.addText(cfg.label, {
    x, y, w: 1.4, h: 0.32,
    fontSize: 10, bold: true, fontFace: F.body,
    color: model === "Orchestrator" ? "1E293B" : "FFFFFF",
    align: "center", valign: "middle", margin: 0,
  });
}

// ---------- PRESENTATION ----------
const pres = new pptxgen();
pres.layout = "LAYOUT_WIDE";
pres.author = "Kirill Shavrin";
pres.title = "Diet Helper — A2A Architecture v2";

// ============================================================
// SLIDE 1 — TITLE
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgNavy };

  // Decorative top-left accent bar
  s.addShape("rect", {
    x: 0, y: 0, w: 0.3, h: PAGE_H,
    fill: { color: C.accent }, line: { color: C.accent },
  });

  // Eyebrow
  s.addText("ARCHITECTURE PROPOSAL", {
    x: 1.0, y: 2.0, w: 11, h: 0.5,
    fontSize: 16, bold: true, charSpacing: 8, fontFace: F.body,
    color: C.accent, align: "left",
  });

  // Title
  s.addText("Diet Helper", {
    x: 1.0, y: 2.6, w: 11, h: 1.1,
    fontSize: 60, bold: true, fontFace: F.title,
    color: C.textOnNavy, align: "left", margin: 0,
  });
  s.addText("A2A Architecture v2", {
    x: 1.0, y: 3.7, w: 11, h: 0.9,
    fontSize: 44, fontFace: F.title,
    color: "CADCFC", align: "left", margin: 0,
  });

  // Subtitle
  s.addText("Multi-agent pipeline для Android learning-проекта", {
    x: 1.0, y: 4.8, w: 11, h: 0.6,
    fontSize: 20, italic: true, fontFace: F.body,
    color: "CBD5E1", align: "left",
  });

  // Footer line — author + date
  s.addShape("line", {
    x: 1.0, y: 6.4, w: 4, h: 0,
    line: { color: C.accent, width: 2 },
  });
  s.addText([
    { text: "Kirill Shavrin", options: { bold: true, color: C.textOnNavy } },
    { text: "   •   ",        options: { color: C.textMuted } },
    { text: "2026-05-11",     options: { color: "CBD5E1" } },
  ], {
    x: 1.0, y: 6.5, w: 11, h: 0.5,
    fontSize: 16, fontFace: F.body, align: "left",
  });
}

// ============================================================
// SLIDE 2 — Почему v2
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "Почему v2", "Что не работает сегодня и как меняется");

  const colW = (PAGE_W - 2 * MARGIN - 0.4) / 2;
  const colY = 1.7;
  const colH = 5.2;

  // LEFT — Problems
  s.addShape("roundRect", {
    x: MARGIN, y: colY, w: colW, h: colH,
    fill: { color: "FEF2F2" }, line: { color: C.danger, width: 2 },
    rectRadius: 0.1,
  });
  s.addShape("rect", {
    x: MARGIN, y: colY, w: 0.15, h: colH,
    fill: { color: C.danger }, line: { color: C.danger },
  });
  s.addText("Проблемы текущей системы", {
    x: MARGIN + 0.4, y: colY + 0.2, w: colW - 0.6, h: 0.5,
    fontSize: 22, bold: true, fontFace: F.title, color: C.danger,
    align: "left", margin: 0,
  });
  s.addText([
    { text: "4 агента закрывают только реализацию, тесты и доки", options: { bullet: true, breakLine: true } },
    { text: "Нет фаз spec-generation, code-review, knowledge-update", options: { bullet: true, breakLine: true } },
    { text: "PROMPT.md в корне — 99 КБ исторического Cursor-spec", options: { bullet: true, breakLine: true } },
    { text: "CLAUDE.md 96 строк с дублями DOCUMENTATION.md", options: { bullet: true, breakLine: true } },
    { text: "Memory: 1 файл, stale (Kotlin 2.0.21 vs реальный 2.1.20)", options: { bullet: true, breakLine: true } },
    { text: "D:\\For_Claude (UFL QA context) leak в diet_helper", options: { bullet: true } },
  ], {
    x: MARGIN + 0.4, y: colY + 0.85, w: colW - 0.6, h: colH - 1.0,
    fontSize: 15, fontFace: F.body, color: C.textDark,
    paraSpaceAfter: 8, valign: "top",
  });

  // RIGHT — Solution
  const rX = MARGIN + colW + 0.4;
  s.addShape("roundRect", {
    x: rX, y: colY, w: colW, h: colH,
    fill: { color: "F0FDF4" }, line: { color: C.success, width: 2 },
    rectRadius: 0.1,
  });
  s.addShape("rect", {
    x: rX, y: colY, w: 0.15, h: colH,
    fill: { color: C.success }, line: { color: C.success },
  });
  s.addText("Решение в v2", {
    x: rX + 0.4, y: colY + 0.2, w: colW - 0.6, h: 0.5,
    fontSize: 22, bold: true, fontFace: F.title, color: C.success,
    align: "left", margin: 0,
  });
  s.addText([
    { text: "7 базовых + 1 conditional агент (вместо 14 в исходном дизайне)", options: { bullet: true, breakLine: true } },
    { text: "Каждый агент получает минимальный релевантный срез контекста", options: { bullet: true, breakLine: true } },
    { text: "Memory декомпозирована в 6 типизированных файлов", options: { bullet: true, breakLine: true } },
    { text: "CLAUDE.md ужат до 40 строк, только daily essentials", options: { bullet: true, breakLine: true } },
    { text: "PROMPT.md → docs/legacy/spec-v1-cursor.md", options: { bullet: true, breakLine: true } },
    { text: "Project-scoped exclusion для For_Claude в global rules", options: { bullet: true } },
  ], {
    x: rX + 0.4, y: colY + 0.85, w: colW - 0.6, h: colH - 1.0,
    fontSize: 15, fontFace: F.body, color: C.textDark,
    paraSpaceAfter: 8, valign: "top",
  });
}

// ============================================================
// SLIDE 3 — Pipeline overview
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "Pipeline: 8 шагов от user prompt до final report", null);

  // Pipeline boxes
  const nodes = [
    { label: "USER\nPROMPT",      color: C.generic,      textColor: C.textDark, model: null },
    { label: "/dh\norchestrator", color: C.orchestrator, textColor: C.textDark, model: null },
    { label: "dh-intake",         color: C.sonnet,       textColor: "FFFFFF",   model: "S" },
    { label: "dh-architect\n(cond.)", color: C.sonnet,   textColor: "FFFFFF",   model: "S", dashed: true },
    { label: "dh-developer",      color: C.opus,         textColor: "FFFFFF",   model: "O" },
    { label: "dh-reviewer",       color: C.sonnet,       textColor: "FFFFFF",   model: "S" },
    { label: "dh-tester",         color: C.sonnet,       textColor: "FFFFFF",   model: "S" },
    { label: "dh-runner",         color: C.haiku,        textColor: "FFFFFF",   model: "H" },
    { label: "dh-docs",           color: C.haiku,        textColor: "FFFFFF",   model: "H" },
    { label: "dh-knowledge",      color: C.sonnet,       textColor: "FFFFFF",   model: "S" },
  ];

  const boxW = 1.18;
  const boxH = 1.0;
  const gap = 0.06;
  const totalW = nodes.length * boxW + (nodes.length - 1) * gap;
  const startX = (PAGE_W - totalW) / 2;
  const boxY = 2.0;

  nodes.forEach((n, i) => {
    const x = startX + i * (boxW + gap);
    s.addShape("roundRect", {
      x, y: boxY, w: boxW, h: boxH,
      fill: { color: n.color },
      line: n.dashed
        ? { color: n.color, width: 2, dashType: "dash" }
        : { color: n.color, width: 1 },
      rectRadius: 0.08,
    });
    s.addText(n.label, {
      x, y: boxY, w: boxW, h: boxH,
      fontSize: 10, bold: true, fontFace: F.body, color: n.textColor,
      align: "center", valign: "middle", margin: 0,
    });
    if (n.model) {
      s.addShape("ellipse", {
        x: x + boxW - 0.32, y: boxY + 0.04, w: 0.28, h: 0.28,
        fill: { color: "FFFFFF" }, line: { color: "FFFFFF" },
      });
      s.addText(n.model, {
        x: x + boxW - 0.32, y: boxY + 0.04, w: 0.28, h: 0.28,
        fontSize: 11, bold: true, fontFace: F.body, color: n.color,
        align: "center", valign: "middle", margin: 0,
      });
    }
    // Arrow between this box and next
    if (i < nodes.length - 1) {
      const arrowX = x + boxW;
      const arrowY = boxY + boxH / 2;
      s.addShape("line", {
        x: arrowX, y: arrowY, w: gap, h: 0,
        line: { color: C.textMuted, width: 1.2, endArrowType: "triangle" },
      });
    }
  });

  // Step numbers under boxes
  nodes.forEach((n, i) => {
    if (i === 0) return; // skip user prompt
    const x = startX + i * (boxW + gap);
    s.addText(String(i), {
      x, y: boxY + boxH + 0.05, w: boxW, h: 0.3,
      fontSize: 11, bold: true, fontFace: F.body, color: C.textMuted,
      align: "center", margin: 0,
    });
  });

  // Legend
  const legendY = 4.5;
  s.addText("Цветовая кодировка моделей", {
    x: MARGIN, y: legendY, w: 12, h: 0.4,
    fontSize: 14, bold: true, fontFace: F.title, color: C.textDark, margin: 0,
  });
  const legend = [
    { color: C.opus,         label: "Opus 4.7 — архитектурные решения, реализация" },
    { color: C.sonnet,       label: "Sonnet 4.6 — генерация, анализ, judgment" },
    { color: C.haiku,        label: "Haiku 4.5 — механические задачи (gradle, текст по правилам)" },
    { color: C.orchestrator, label: "Orchestrator (yellow) — slash-команда, НЕ агент" },
  ];
  legend.forEach((item, i) => {
    const y = legendY + 0.5 + i * 0.4;
    s.addShape("roundRect", {
      x: MARGIN + 0.1, y: y + 0.05, w: 0.3, h: 0.22,
      fill: { color: item.color }, line: { color: item.color },
      rectRadius: 0.04,
    });
    s.addText(item.label, {
      x: MARGIN + 0.6, y, w: 11, h: 0.32,
      fontSize: 13, fontFace: F.body, color: C.textDark,
      valign: "middle", margin: 0,
    });
  });

  // Bottom note
  s.addShape("roundRect", {
    x: MARGIN, y: 6.7, w: PAGE_W - 2 * MARGIN, h: 0.55,
    fill: { color: "FEF2F2" }, line: { color: C.danger, width: 1 },
    rectRadius: 0.05,
  });
  s.addText([
    { text: "⚠  ", options: { color: C.danger, bold: true } },
    { text: "Runner failure → стоп цепи. Manual retry через ", options: { color: C.textDark } },
    { text: "/dh fix <description>", options: { fontFace: F.mono, color: C.danger, bold: true } },
    { text: ". В v2 нет автоматического retry.", options: { color: C.textDark } },
  ], {
    x: MARGIN + 0.2, y: 6.7, w: PAGE_W - 2 * MARGIN - 0.4, h: 0.55,
    fontSize: 13, fontFace: F.body, valign: "middle", margin: 0,
  });
}

// ============================================================
// SLIDE 4 — Per-agent context contracts (table)
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "Контракт: какой агент какой контекст получает", "Минимальный релевантный срез — основной принцип v2");

  const headerOpts = { bold: true, color: "FFFFFF", fill: { color: C.bgNavy }, align: "center", valign: "middle", fontFace: F.title, fontSize: 12 };
  const cell = (text, color, opts = {}) => ({
    text, options: { color: color || C.textDark, fontFace: F.body, fontSize: 11, valign: "middle", align: "left", margin: 5, ...opts },
  });
  const model = (m, color) => ({
    text: m, options: { color: "FFFFFF", fill: { color }, bold: true, align: "center", valign: "middle", fontFace: F.body, fontSize: 11, margin: 4 },
  });

  const rows = [
    [
      { text: "Агент", options: headerOpts },
      { text: "Model", options: headerOpts },
      { text: "Читает", options: headerOpts },
      { text: "НЕ читает", options: headerOpts },
      { text: "Output", options: headerOpts },
    ],
    [
      cell("dh-intake",      C.sonnet, { bold: true }),
      model("Sonnet", C.sonnet),
      cell("user prompt + Q&A + CLAUDE.md (40 строк) + DOCUMENTATION.md TOC"),
      cell("source, memory, PROMPT.md"),
      cell("SPEC { TASK, WHAT, LAYERS, CHANGED_HINT, TEST_TYPES, CONSTRAINTS }"),
    ],
    [
      cell("dh-architect",   C.sonnet, { bold: true }),
      model("Sonnet", C.sonnet),
      cell("SPEC + 1-2 similar files в domain/usecase, presentation/screen"),
      cell("tests, docs, memory, CHANGED_HINT (пусто по триггеру)"),
      cell("SPEC+ с FILE_PLAN[]"),
    ],
    [
      cell("dh-developer",   C.opus, { bold: true }),
      model("Opus", C.opus),
      cell("SPEC + CHANGED_HINT files + CLAUDE.md"),
      cell("tests, docs, memory, DOCUMENTATION.md"),
      cell("{ changed_files[], commit_hash }"),
    ],
    [
      cell("dh-reviewer",    C.sonnet, { bold: true }),
      model("Sonnet", C.sonnet),
      cell("SPEC + содержимое changed_files"),
      cell("tests, docs, memory, CLAUDE.md"),
      cell("{ issues: severity-tagged } (blocker | warning | info)"),
    ],
    [
      cell("dh-tester",      C.sonnet, { bold: true }),
      model("Sonnet", C.sonnet),
      cell("SPEC + changed_files + 1 reference test per TYPE + Fake*.kt"),
      cell("docs, memory, CLAUDE.md"),
      cell("{ test_files[], screenshot_record_needed }"),
    ],
    [
      cell("dh-runner",      C.haiku, { bold: true }),
      model("Haiku", C.haiku),
      cell("gradle output"),
      cell("вся source, docs, memory"),
      cell("{ pass, tests, detekt, screenshots, errors[] }"),
    ],
    [
      cell("dh-docs",        C.haiku, { bold: true }),
      model("Haiku", C.haiku),
      cell("SPEC + имена changed_files + DOCUMENTATION.md + CLAUDE.md"),
      cell("содержимое changed_files, memory"),
      cell("commit_hash либо \"No documentation update needed.\""),
    ],
    [
      cell("dh-knowledge",   C.sonnet, { bold: true }),
      model("Sonnet", C.sonnet),
      cell("SPEC + session recap + MEMORY.md + thematic memory files"),
      cell("source, docs, CLAUDE.md"),
      cell("{ updated[] } либо no-op с reason"),
    ],
  ];

  s.addTable(rows, {
    x: MARGIN, y: 1.85, w: PAGE_W - 2 * MARGIN,
    colW: [1.4, 1.05, 3.8, 2.8, 3.25],
    rowH: 0.55,
    border: { pt: 0.5, color: C.border },
    fontFace: F.body, fontSize: 11,
  });

  // Footer note
  s.addText([
    { text: "Ключевое: ",     options: { bold: true, color: C.bgNavy } },
    { text: "dh-developer теперь читает ТОЛЬКО CHANGED_HINT files — не сканирует весь проект. Экономия контекста 60-80%.", options: { color: C.textDark } },
  ], {
    x: MARGIN, y: 7.0, w: PAGE_W - 2 * MARGIN, h: 0.35,
    fontSize: 12, italic: true, fontFace: F.body, margin: 0,
  });
}

// ============================================================
// SLIDE 5 — dh-intake
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "dh-intake — генерация SPEC", "Каждое /dh --feature; spawn-once агент");

  // Top-left: Badge
  modelBadge(s, MARGIN, 1.7, "Sonnet");

  // Trigger
  s.addText("Триггер: каждое /dh --feature после Q&A фазы", {
    x: MARGIN + 1.6, y: 1.7, w: 8, h: 0.32,
    fontSize: 13, italic: true, color: C.textMuted, fontFace: F.body,
    valign: "middle", margin: 0,
  });

  // Input box
  const inX = MARGIN, inY = 2.3, inW = 6.0, inH = 2.3;
  s.addShape("roundRect", {
    x: inX, y: inY, w: inW, h: inH,
    fill: { color: C.bgSurface }, line: { color: C.border, width: 1 },
    rectRadius: 0.08,
  });
  s.addText("Input от orchestrator", {
    x: inX + 0.25, y: inY + 0.15, w: inW - 0.5, h: 0.4,
    fontSize: 15, bold: true, color: C.sonnet, fontFace: F.title, margin: 0,
  });
  s.addText([
    { text: "USER_PROMPT", options: { bold: true, fontFace: F.mono, color: C.textDark, breakLine: true } },
    { text: "    verbatim запрос пользователя", options: { color: C.textMuted, breakLine: true } },
    { text: "TASK_TYPE", options: { bold: true, fontFace: F.mono, color: C.textDark, breakLine: true } },
    { text: "    feature | bugfix | docs-only", options: { color: C.textMuted, breakLine: true } },
    { text: "QA_PAIRS", options: { bold: true, fontFace: F.mono, color: C.textDark, breakLine: true } },
    { text: "    3-5 обязательных {question, answer}", options: { color: C.textMuted } },
  ], {
    x: inX + 0.25, y: inY + 0.55, w: inW - 0.5, h: inH - 0.65,
    fontSize: 12, fontFace: F.body, paraSpaceAfter: 2, valign: "top",
  });

  // Reads / Doesn't read
  const rX = MARGIN + inW + 0.3, rY = 2.3, rW = PAGE_W - rX - MARGIN, rH = 2.3;
  s.addShape("roundRect", {
    x: rX, y: rY, w: rW, h: rH,
    fill: { color: "F0FDF4" }, line: { color: C.success, width: 1 },
    rectRadius: 0.08,
  });
  s.addText("Читает", {
    x: rX + 0.25, y: rY + 0.15, w: rW - 0.5, h: 0.4,
    fontSize: 15, bold: true, color: C.success, fontFace: F.title, margin: 0,
  });
  s.addText([
    { text: "CLAUDE.md — package, layers, routes (40 строк)", options: { bullet: true, breakLine: true } },
    { text: "DOCUMENTATION.md — ТОЛЬКО список ## заголовков (TOC)", options: { bullet: true } },
  ], {
    x: rX + 0.25, y: rY + 0.55, w: rW - 0.5, h: 0.8,
    fontSize: 12, fontFace: F.body, color: C.textDark, paraSpaceAfter: 4,
  });
  s.addText("НЕ читает", {
    x: rX + 0.25, y: rY + 1.4, w: rW - 0.5, h: 0.35,
    fontSize: 13, bold: true, color: C.danger, fontFace: F.title, margin: 0,
  });
  s.addText("source code, memory files, PROMPT.md, body DOCUMENTATION.md", {
    x: rX + 0.25, y: rY + 1.7, w: rW - 0.5, h: 0.5,
    fontSize: 12, fontFace: F.body, color: C.textMuted,
  });

  // Inference rules
  const infY = 4.8;
  s.addText("Inference rules", {
    x: MARGIN, y: infY, w: 6, h: 0.4,
    fontSize: 15, bold: true, color: C.bgNavy, fontFace: F.title, margin: 0,
  });
  s.addShape("roundRect", {
    x: MARGIN, y: infY + 0.4, w: 6, h: 2.0,
    fill: { color: "F8FAFC" }, line: { color: C.border, width: 1 },
    rectRadius: 0.06,
  });
  s.addText([
    { text: "New screen → ", options: { fontFace: F.mono, bold: true, color: C.textDark } },
    { text: "LAYERS+=presentation, TEST_TYPES+=compose-ui", options: { color: C.textMuted, breakLine: true } },
    { text: "New DAO query → ", options: { fontFace: F.mono, bold: true, color: C.textDark } },
    { text: "LAYERS+=data, TEST_TYPES+=dao", options: { color: C.textMuted, breakLine: true } },
    { text: "New use case → ", options: { fontFace: F.mono, bold: true, color: C.textDark } },
    { text: "LAYERS+=domain, всегда +=unit", options: { color: C.textMuted, breakLine: true } },
    { text: "Every change → ", options: { fontFace: F.mono, bold: true, color: C.textDark } },
    { text: "минимум \"unit\" в TEST_TYPES", options: { color: C.textMuted } },
  ], {
    x: MARGIN + 0.2, y: infY + 0.5, w: 5.6, h: 1.8,
    fontSize: 12, fontFace: F.body, paraSpaceAfter: 4, valign: "top",
  });

  // Output JSON
  s.addText("Output JSON", {
    x: MARGIN + 6.3, y: infY, w: 6, h: 0.4,
    fontSize: 15, bold: true, color: C.bgNavy, fontFace: F.title, margin: 0,
  });
  s.addShape("roundRect", {
    x: MARGIN + 6.3, y: infY + 0.4, w: 6, h: 2.0,
    fill: { color: "0F172A" }, line: { color: "0F172A" },
    rectRadius: 0.06,
  });
  s.addText(
`{
  "TASK": "feature",
  "WHAT": "Add export-day-to-CSV...",
  "LAYERS": ["domain","presentation"],
  "CHANGED_HINT": [".../TodayScreen.kt"],
  "TEST_TYPES": ["unit","compose-ui"],
  "CONSTRAINTS": {"csv_separator": ";"}
}`,
    {
      x: MARGIN + 6.5, y: infY + 0.5, w: 5.6, h: 1.8,
      fontSize: 12, fontFace: F.mono, color: "A5F3FC",
      valign: "top",
    }
  );
}

// ============================================================
// SLIDE 6 — dh-architect (conditional)
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "dh-architect — план файлов для сложных задач", "Условный агент: spawned только при двух условиях");

  modelBadge(s, MARGIN, 1.7, "Sonnet");

  // Trigger callout (red)
  const tY = 2.3;
  s.addShape("roundRect", {
    x: MARGIN, y: tY, w: PAGE_W - 2 * MARGIN, h: 0.85,
    fill: { color: "FEF2F2" }, line: { color: C.danger, width: 2 },
    rectRadius: 0.08,
  });
  s.addText([
    { text: "Триггер  ", options: { bold: true, color: C.danger, fontSize: 14 } },
    { text: "(оба условия должны выполняться):", options: { color: C.textDark, fontSize: 13, breakLine: true } },
    { text: "  • SPEC.LAYERS.length ≥ 3   (touches ≥3 из {domain, data, di, presentation})", options: { color: C.textDark, fontFace: F.mono, fontSize: 12, breakLine: true } },
    { text: "  • SPEC.CHANGED_HINT пустой   (новая подсистема, не extension)", options: { color: C.textDark, fontFace: F.mono, fontSize: 12 } },
  ], {
    x: MARGIN + 0.3, y: tY + 0.05, w: PAGE_W - 2 * MARGIN - 0.6, h: 0.85,
    valign: "middle", margin: 0,
  });

  // Two columns: Reads | Output
  const colY = 3.4;
  const colH = 3.5;
  const colW = (PAGE_W - 2 * MARGIN - 0.3) / 2;

  // LEFT — Reads
  s.addShape("roundRect", {
    x: MARGIN, y: colY, w: colW, h: colH,
    fill: { color: C.bgSurface }, line: { color: C.border, width: 1 },
    rectRadius: 0.08,
  });
  s.addText("Что читает (budget cap: ≤ 4 файла)", {
    x: MARGIN + 0.25, y: colY + 0.15, w: colW - 0.5, h: 0.4,
    fontSize: 15, bold: true, color: C.sonnet, fontFace: F.title, margin: 0,
  });
  s.addText([
    { text: "1 similar feature в ", options: { breakLine: false } },
    { text: "domain/usecase/", options: { fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "1 similar screen в ", options: { breakLine: false } },
    { text: "presentation/screen/", options: { fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "    (если LAYERS содержит presentation)", options: { color: C.textMuted, italic: true, breakLine: true } },
    { text: "CLAUDE.md — только секция Layers", options: { breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "НЕ читает: ", options: { bold: true, color: C.danger } },
    { text: "files в CHANGED_HINT (он пустой), tests, docs, memory", options: { color: C.textMuted } },
  ], {
    x: MARGIN + 0.25, y: colY + 0.6, w: colW - 0.5, h: colH - 0.7,
    fontSize: 12, fontFace: F.body, color: C.textDark,
    paraSpaceAfter: 6, bullet: { code: "25CF" }, valign: "top",
  });

  // RIGHT — Output
  const rX = MARGIN + colW + 0.3;
  s.addShape("roundRect", {
    x: rX, y: colY, w: colW, h: colH,
    fill: { color: "0F172A" }, line: { color: "0F172A" },
    rectRadius: 0.08,
  });
  s.addText("Output: SPEC+ с FILE_PLAN[]", {
    x: rX + 0.25, y: colY + 0.15, w: colW - 0.5, h: 0.4,
    fontSize: 15, bold: true, color: "FBBF24", fontFace: F.title, margin: 0,
  });
  s.addText(
`{
  ...все поля SPEC,
  "FILE_PLAN": [
    { "path": ".../domain/model/Foo.kt",
      "kind": "model",
      "action": "create" },
    { "path": ".../domain/repository/...",
      "kind": "repository-interface",
      "action": "create" },
    { "path": ".../data/local/dao/...",
      "kind": "dao",
      "action": "create" },
    ...
  ]
}`,
    {
      x: rX + 0.25, y: colY + 0.55, w: colW - 0.5, h: colH - 0.65,
      fontSize: 11, fontFace: F.mono, color: "A5F3FC",
      valign: "top",
    }
  );

  // Bottom note: ordering
  s.addText([
    { text: "Порядок FILE_PLAN  ", options: { bold: true, color: C.bgNavy } },
    { text: "(bottom-up): ", options: { color: C.textDark } },
    { text: "domain/model → domain/repository → domain/usecase → data/entity → data/dao → data/mapper → data/repository → di → presentation", options: { fontFace: F.mono, color: C.textMuted, fontSize: 11 } },
  ], {
    x: MARGIN, y: 7.1, w: PAGE_W - 2 * MARGIN, h: 0.3,
    fontSize: 12, fontFace: F.body, margin: 0,
  });
}

// ============================================================
// SLIDE 7 — Developer + Reviewer контраст
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "Developer (Opus) + Reviewer (Sonnet) — разные модели", "Sequential, не параллельно");

  const colY = 1.7;
  const colH = 4.4;
  const colW = (PAGE_W - 2 * MARGIN - 0.4) / 2;

  // LEFT — Developer (Opus)
  s.addShape("roundRect", {
    x: MARGIN, y: colY, w: colW, h: colH,
    fill: { color: "F5F3FF" }, line: { color: C.opus, width: 2 },
    rectRadius: 0.1,
  });
  s.addShape("rect", {
    x: MARGIN, y: colY, w: 0.18, h: colH,
    fill: { color: C.opus }, line: { color: C.opus },
  });
  s.addText("dh-developer", {
    x: MARGIN + 0.4, y: colY + 0.2, w: colW - 1.8, h: 0.5,
    fontSize: 22, bold: true, color: C.opus, fontFace: F.title, margin: 0,
  });
  modelBadge(s, MARGIN + colW - 1.65, colY + 0.25, "Opus");
  s.addText([
    { text: "Роль", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    Реализация строго по SPEC (или SPEC+ от architect).", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Читает", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    SPEC + CHANGED_HINT files + CLAUDE.md (40 строк)", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Output JSON", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    { changed_files[], commit_hash }", options: { fontFace: F.mono, color: C.opus, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Правило", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    1 smoke test per use case — не comprehensive", options: { color: C.textDark, breakLine: true } },
    { text: "    No mocks — Fakes (MutableStateFlow-based)", options: { color: C.textDark } },
  ], {
    x: MARGIN + 0.5, y: colY + 0.9, w: colW - 0.7, h: colH - 1.0,
    fontSize: 13, fontFace: F.body, paraSpaceAfter: 2, valign: "top",
  });

  // RIGHT — Reviewer (Sonnet)
  const rX = MARGIN + colW + 0.4;
  s.addShape("roundRect", {
    x: rX, y: colY, w: colW, h: colH,
    fill: { color: "EFF6FF" }, line: { color: C.sonnet, width: 2 },
    rectRadius: 0.1,
  });
  s.addShape("rect", {
    x: rX, y: colY, w: 0.18, h: colH,
    fill: { color: C.sonnet }, line: { color: C.sonnet },
  });
  s.addText("dh-reviewer", {
    x: rX + 0.4, y: colY + 0.2, w: colW - 1.8, h: 0.5,
    fontSize: 22, bold: true, color: C.sonnet, fontFace: F.title, margin: 0,
  });
  modelBadge(s, rX + colW - 1.65, colY + 0.25, "Sonnet");
  s.addText([
    { text: "Роль", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    Ловит Clean Arch violations, scope creep, naming, idioms.", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Читает", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    SPEC + полное содержимое changed_files", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Output JSON", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    { issues: [{severity, file, line, msg}] }", options: { fontFace: F.mono, color: C.sonnet, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Правило", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    warning-only — surface, не блокирует", options: { color: C.textDark, breakLine: true } },
    { text: "    severity=blocker → STOP цепи", options: { color: C.danger } },
  ], {
    x: rX + 0.5, y: colY + 0.9, w: colW - 0.7, h: colH - 1.0,
    fontSize: 13, fontFace: F.body, paraSpaceAfter: 2, valign: "top",
  });

  // Bottom rationale box
  const noteY = 6.3;
  s.addShape("roundRect", {
    x: MARGIN, y: noteY, w: PAGE_W - 2 * MARGIN, h: 1.05,
    fill: { color: "FFFBEB" }, line: { color: C.amber, width: 1.5 },
    rectRadius: 0.08,
  });
  s.addText("Почему разные модели?", {
    x: MARGIN + 0.25, y: noteY + 0.1, w: 4.5, h: 0.4,
    fontSize: 14, bold: true, color: C.amber, fontFace: F.title, margin: 0,
  });
  s.addText([
    { text: "•  Снижается same-model bias — reviewer не повторяет инерцию developer-а.\n" },
    { text: "•  Developer на Opus: prevention > cure. Качество генерации > глубина post-review.\n" },
    { text: "•  Reviewer на Sonnet: pattern detection по checklist'у — Sonnet достаточно.\n" },
    { text: "•  Если developer пишет правильно — reviewer почти всегда no-op → суммарный budget не растёт." },
  ], {
    x: MARGIN + 0.25, y: noteY + 0.4, w: PAGE_W - 2 * MARGIN - 0.5, h: 0.65,
    fontSize: 12, fontFace: F.body, color: C.textDark, valign: "top",
  });
}

// ============================================================
// SLIDE 8 — Tester + Runner
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "dh-tester → dh-runner — пишет и запускает", "Раздельные ответственности: писатель vs исполнитель");

  // Tester (top, blue)
  const tY = 1.7, tH = 2.3;
  s.addShape("roundRect", {
    x: MARGIN, y: tY, w: PAGE_W - 2 * MARGIN, h: tH,
    fill: { color: "EFF6FF" }, line: { color: C.sonnet, width: 2 },
    rectRadius: 0.1,
  });
  s.addShape("rect", {
    x: MARGIN, y: tY, w: 0.18, h: tH,
    fill: { color: C.sonnet }, line: { color: C.sonnet },
  });
  s.addText("dh-tester", {
    x: MARGIN + 0.4, y: tY + 0.15, w: 4, h: 0.5,
    fontSize: 22, bold: true, color: C.sonnet, fontFace: F.title, margin: 0,
  });
  modelBadge(s, MARGIN + 4.5, tY + 0.2, "Sonnet");
  s.addText("Writes comprehensive tests — никогда не запускает", {
    x: MARGIN + 6.0, y: tY + 0.2, w: 6, h: 0.4,
    fontSize: 13, italic: true, color: C.textMuted, fontFace: F.body, valign: "middle", margin: 0,
  });
  s.addText([
    { text: "Test types: ", options: { bold: true } },
    { text: "unit · dao · compose-ui · screenshot   (все требуют ", options: {} },
    { text: "application = android.app.Application::class", options: { fontFace: F.mono, color: C.bgNavy } },
    { text: ")", options: { breakLine: true } },
    { text: "Conventions: ", options: { bold: true } },
    { text: "Fakes only · MutableStateFlow · @Config(sdk=34) · DAO tests use in-memory Room DB", options: { breakLine: true } },
    { text: "Reads: ", options: { bold: true } },
    { text: "SPEC + changed_files + 1 reference test per TYPE + Fake*.kt из app/src/test/.../data/", options: { breakLine: true } },
    { text: "Output: ", options: { bold: true } },
    { text: "{ test_files[], screenshot_record_needed }", options: { fontFace: F.mono, color: C.sonnet } },
  ], {
    x: MARGIN + 0.5, y: tY + 0.75, w: PAGE_W - 2 * MARGIN - 0.7, h: tH - 0.85,
    fontSize: 13, fontFace: F.body, color: C.textDark, paraSpaceAfter: 4, valign: "top",
  });

  // Arrow down
  s.addShape("line", {
    x: PAGE_W / 2, y: tY + tH + 0.1, w: 0, h: 0.4,
    line: { color: C.textMuted, width: 2, endArrowType: "triangle" },
  });

  // Runner (bottom, green)
  const rY = tY + tH + 0.7, rH = 2.0;
  s.addShape("roundRect", {
    x: MARGIN, y: rY, w: PAGE_W - 2 * MARGIN, h: rH,
    fill: { color: "F0FDF4" }, line: { color: C.haiku, width: 2 },
    rectRadius: 0.1,
  });
  s.addShape("rect", {
    x: MARGIN, y: rY, w: 0.18, h: rH,
    fill: { color: C.haiku }, line: { color: C.haiku },
  });
  s.addText("dh-runner", {
    x: MARGIN + 0.4, y: rY + 0.15, w: 4, h: 0.5,
    fontSize: 22, bold: true, color: C.haiku, fontFace: F.title, margin: 0,
  });
  modelBadge(s, MARGIN + 4.5, rY + 0.2, "Haiku");
  s.addText("Executes gradle — никогда не читает source", {
    x: MARGIN + 6.0, y: rY + 0.2, w: 6, h: 0.4,
    fontSize: 13, italic: true, color: C.textMuted, fontFace: F.body, valign: "middle", margin: 0,
  });
  s.addText([
    { text: "Sequence: ", options: { bold: true } },
    { text: ":app:detekt → :app:testDebugUnitTest → (опц.) recordRoborazzi → verifyRoborazzi", options: { fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "Env setup: ", options: { bold: true } },
    { text: "JAVA_HOME=D:\\For_work\\AS\\jbr + JBR loopback fix через unixDomain.tmpDir=C:\\tmp", options: { breakLine: true } },
    { text: "Output: ", options: { bold: true } },
    { text: "{ pass, tests, detekt, screenshots, errors[] }", options: { fontFace: F.mono, color: C.haiku } },
  ], {
    x: MARGIN + 0.5, y: rY + 0.75, w: PAGE_W - 2 * MARGIN - 0.7, h: rH - 0.85,
    fontSize: 13, fontFace: F.body, color: C.textDark, paraSpaceAfter: 4, valign: "top",
  });
}

// ============================================================
// SLIDE 9 — Docs + Knowledge
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "dh-docs + dh-knowledge — финальная фаза", "Docs обновляет product/tech, Knowledge — memory/agents");

  const colY = 1.7;
  const colH = 4.6;
  const colW = (PAGE_W - 2 * MARGIN - 0.4) / 2;

  // LEFT — dh-docs (Haiku)
  s.addShape("roundRect", {
    x: MARGIN, y: colY, w: colW, h: colH,
    fill: { color: "F0FDF4" }, line: { color: C.haiku, width: 2 },
    rectRadius: 0.1,
  });
  s.addShape("rect", {
    x: MARGIN, y: colY, w: 0.18, h: colH,
    fill: { color: C.haiku }, line: { color: C.haiku },
  });
  s.addText("dh-docs", {
    x: MARGIN + 0.4, y: colY + 0.2, w: colW - 1.8, h: 0.5,
    fontSize: 22, bold: true, color: C.haiku, fontFace: F.title, margin: 0,
  });
  modelBadge(s, MARGIN + colW - 1.65, colY + 0.25, "Haiku");
  s.addText([
    { text: "Updates", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    • DOCUMENTATION.md — product (screens, flows, ADL)", options: { color: C.textDark, breakLine: true } },
    { text: "    • CLAUDE.md — tech cheatsheet (только при architecture changes)", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Reads", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    SPEC + имена changed_files + DOC.md + CLAUDE.md", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Conservative rules", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    ≤10 строк/update в DOC.md, ≤5 строк/update в CLAUDE.md", options: { color: C.textDark, breakLine: true } },
    { text: "    Never deletes content", options: { color: C.textDark, breakLine: true } },
    { text: "    \"No documentation update needed.\" — валидный output", options: { color: C.textMuted, italic: true } },
  ], {
    x: MARGIN + 0.5, y: colY + 0.9, w: colW - 0.7, h: colH - 1.0,
    fontSize: 12, fontFace: F.body, paraSpaceAfter: 2, valign: "top",
  });

  // RIGHT — dh-knowledge (Sonnet)
  const rX = MARGIN + colW + 0.4;
  s.addShape("roundRect", {
    x: rX, y: colY, w: colW, h: colH,
    fill: { color: "EFF6FF" }, line: { color: C.sonnet, width: 2 },
    rectRadius: 0.1,
  });
  s.addShape("rect", {
    x: rX, y: colY, w: 0.18, h: colH,
    fill: { color: C.sonnet }, line: { color: C.sonnet },
  });
  s.addText("dh-knowledge", {
    x: rX + 0.4, y: colY + 0.2, w: colW - 1.8, h: 0.5,
    fontSize: 22, bold: true, color: C.sonnet, fontFace: F.title, margin: 0,
  });
  modelBadge(s, rX + colW - 1.65, colY + 0.25, "Sonnet");
  s.addText([
    { text: "Updates", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    • memory/*.md (user, project, feedback, reference)", options: { color: C.textDark, breakLine: true } },
    { text: "    • agent definitions (если контракт дрейфует)", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Reads", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    MEMORY.md + thematic memory files + SESSION_RECAP от orchestrator", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "Write triggers", options: { bold: true, color: C.textDark, breakLine: true } },
    { text: "    • Новый convention принят user-ом", options: { color: C.textDark, breakLine: true } },
    { text: "    • User correction в сессии (\"don't do X\")", options: { color: C.textDark, breakLine: true } },
    { text: "    • Agent contract drift (developer игнорировал rule)", options: { color: C.textDark, breakLine: true } },
    { text: "    • Recurring pain point", options: { color: C.textDark, breakLine: true } },
    { text: "    Иначе → { updated: [], reason: ... }  (no-op в 80% случаев)", options: { color: C.textMuted, italic: true } },
  ], {
    x: rX + 0.5, y: colY + 0.9, w: colW - 0.7, h: colH - 1.0,
    fontSize: 12, fontFace: F.body, paraSpaceAfter: 2, valign: "top",
  });

  // Bottom note
  s.addShape("roundRect", {
    x: MARGIN, y: 6.5, w: PAGE_W - 2 * MARGIN, h: 0.8,
    fill: { color: "FFFBEB" }, line: { color: C.amber, width: 1.5 },
    rectRadius: 0.08,
  });
  s.addText([
    { text: "Почему knowledge на Sonnet, а не Haiku:  ", options: { bold: true, color: C.amber, fontSize: 13 } },
    { text: "решение \"что заслуживает сохранения в memory\" требует judgment по новизне и переносимости. Haiku рискует false-negative (пропустить важный feedback). Cost ограничен — 1 вызов на /dh, чаще всего no-op.", options: { color: C.textDark, fontSize: 12 } },
  ], {
    x: MARGIN + 0.25, y: 6.5, w: PAGE_W - 2 * MARGIN - 0.5, h: 0.8,
    fontFace: F.body, valign: "middle", margin: 0,
  });
}

// ============================================================
// SLIDE 10 — Memory / CLAUDE.md decomposition
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "Декомпозиция контекста", "Memory + CLAUDE.md: до vs после");

  const colY = 1.7;
  const colH = 5.4;
  const colW = (PAGE_W - 2 * MARGIN - 0.4) / 2;

  // LEFT — Before
  s.addShape("roundRect", {
    x: MARGIN, y: colY, w: colW, h: colH,
    fill: { color: "FEF2F2" }, line: { color: C.danger, width: 2 },
    rectRadius: 0.1,
  });
  s.addText("ДО", {
    x: MARGIN + 0.3, y: colY + 0.15, w: colW - 0.6, h: 0.5,
    fontSize: 26, bold: true, color: C.danger, fontFace: F.title, margin: 0,
  });
  s.addText([
    { text: "MEMORY.md", options: { bold: true, fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "    142 байта index", options: { color: C.textMuted, breakLine: true } },
    { text: "    └─ project_diet_helper.md", options: { fontFace: F.mono, color: C.textDark, breakLine: true } },
    { text: "         1.5 КБ, STALE — Kotlin 2.0.21", options: { color: C.danger, italic: true, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "CLAUDE.md", options: { bold: true, fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "    96 строк — preamble + tech stack + arch tree + decisions + build + JBR + testing + routes", options: { color: C.textDark, breakLine: true } },
    { text: "    дубли с DOCUMENTATION.md", options: { color: C.danger, italic: true, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "PROMPT.md", options: { bold: true, fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "    99 КБ в корне (исторический Cursor spec, 2704 строки)", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "D:\\For_Claude\\", options: { bold: true, fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "    UFL game context (project_ufl, team, conventions, focus)", options: { color: C.textDark, breakLine: true } },
    { text: "    leak в каждую diet_helper сессию", options: { color: C.danger, italic: true } },
  ], {
    x: MARGIN + 0.4, y: colY + 0.7, w: colW - 0.6, h: colH - 0.8,
    fontSize: 11, fontFace: F.body, paraSpaceAfter: 2, valign: "top",
  });

  // RIGHT — After
  const rX = MARGIN + colW + 0.4;
  s.addShape("roundRect", {
    x: rX, y: colY, w: colW, h: colH,
    fill: { color: "F0FDF4" }, line: { color: C.success, width: 2 },
    rectRadius: 0.1,
  });
  s.addText("ПОСЛЕ", {
    x: rX + 0.3, y: colY + 0.15, w: colW - 0.6, h: 0.5,
    fontSize: 26, bold: true, color: C.success, fontFace: F.title, margin: 0,
  });
  s.addText([
    { text: "MEMORY.md", options: { bold: true, fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "    6-строчный index, 6 типизированных файлов:", options: { color: C.textMuted, breakLine: true } },
    { text: "    ├─ user_kirill.md         (user — Android engineer)", options: { fontFace: F.mono, color: C.textDark, breakLine: true } },
    { text: "    ├─ project_diet_helper.md (project — current versions)", options: { fontFace: F.mono, color: C.textDark, breakLine: true } },
    { text: "    ├─ feedback_architecture.md (verbosity rule)", options: { fontFace: F.mono, color: C.textDark, breakLine: true } },
    { text: "    ├─ feedback_testing.md    (Fakes only, smoke vs comp.)", options: { fontFace: F.mono, color: C.textDark, breakLine: true } },
    { text: "    ├─ feedback_communication.md (RU + token line)", options: { fontFace: F.mono, color: C.textDark, breakLine: true } },
    { text: "    └─ reference_env.md       (JBR paths, gradle commands)", options: { fontFace: F.mono, color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "CLAUDE.md", options: { bold: true, fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "    40 строк — Stack · Layers · Routes · Build · JBR fix", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "docs/legacy/spec-v1-cursor.md", options: { bold: true, fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "    бывший PROMPT.md, архивирован", options: { color: C.textDark, breakLine: true } },
    { text: " ", options: { breakLine: true } },
    { text: "~/.claude/CLAUDE.md", options: { bold: true, fontFace: F.mono, color: C.bgNavy, breakLine: true } },
    { text: "    + Project-scoped exclusion: ignore For_Claude в diet_helper", options: { color: C.textDark } },
  ], {
    x: rX + 0.4, y: colY + 0.7, w: colW - 0.6, h: colH - 0.8,
    fontSize: 11, fontFace: F.body, paraSpaceAfter: 2, valign: "top",
  });
}

// ============================================================
// SLIDE 11 — Failure handling
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "Что происходит при сбое", "Никаких автоматических retry — user принимает решение");

  // Three failure points (vertical)
  const startY = 1.8;
  const stepH = 1.0;
  const stepGap = 0.15;

  const failures = [
    {
      title: "Developer возвращает error JSON",
      detail: "{ \"error\": \"blocker\", \"reason\": \"...\", \"suggested_fix\": \"...\" }",
      arrow: "STOP цепи • Surface reason пользователю",
    },
    {
      title: "Reviewer находит severity = \"blocker\"",
      detail: "issues[]: Clean Arch violation, presentation/ → data/ напрямую и т.п.",
      arrow: "STOP цепи • Surface blockers в финальном отчёте",
    },
    {
      title: "Runner возвращает pass = false",
      detail: "Compile error · Test failure · Detekt violation · Roborazzi mismatch",
      arrow: "STOP цепи • Surface errors[] пользователю",
    },
  ];

  failures.forEach((f, i) => {
    const y = startY + i * (stepH + stepGap);
    s.addShape("roundRect", {
      x: MARGIN, y, w: PAGE_W - 2 * MARGIN, h: stepH,
      fill: { color: "FEF2F2" }, line: { color: C.danger, width: 1.5 },
      rectRadius: 0.08,
    });
    s.addShape("rect", {
      x: MARGIN, y, w: 0.15, h: stepH,
      fill: { color: C.danger }, line: { color: C.danger },
    });
    s.addText(`${i + 1}.  ${f.title}`, {
      x: MARGIN + 0.4, y: y + 0.1, w: 8, h: 0.4,
      fontSize: 16, bold: true, color: C.bgNavy, fontFace: F.title, margin: 0,
    });
    s.addText(f.detail, {
      x: MARGIN + 0.4, y: y + 0.45, w: 8, h: 0.5,
      fontSize: 12, fontFace: F.mono, color: C.textMuted, margin: 0,
    });
    s.addText([
      { text: "→  ", options: { color: C.danger, bold: true } },
      { text: f.arrow, options: { color: C.textDark, bold: true } },
    ], {
      x: MARGIN + 8.5, y: y + 0.2, w: PAGE_W - MARGIN - 8.5 - MARGIN, h: 0.6,
      fontSize: 13, fontFace: F.body, valign: "middle", margin: 0,
    });
  });

  // Manual retry path (green)
  const retryY = startY + 3 * (stepH + stepGap) + 0.3;
  s.addShape("roundRect", {
    x: MARGIN, y: retryY, w: PAGE_W - 2 * MARGIN, h: 1.6,
    fill: { color: "F0FDF4" }, line: { color: C.success, width: 2 },
    rectRadius: 0.1,
  });
  s.addShape("rect", {
    x: MARGIN, y: retryY, w: 0.15, h: 1.6,
    fill: { color: C.success }, line: { color: C.success },
  });
  s.addText("Manual retry — user explicitly invokes /dh fix <description>", {
    x: MARGIN + 0.4, y: retryY + 0.15, w: PAGE_W - 2 * MARGIN - 0.6, h: 0.4,
    fontSize: 16, bold: true, color: C.success, fontFace: F.title, margin: 0,
  });
  s.addText([
    { text: "• Skip Intake — reuse SPEC из предыдущего run", options: { breakLine: true } },
    { text: "• Spawn dh-developer с SPEC + RETRY_CONTEXT = errors[] предыдущего runner", options: { breakLine: true } },
    { text: "• Затем стандартный pipeline: Review → Tester → Runner → Docs → Knowledge", options: { breakLine: true } },
    { text: "• Лимит: 2 retry на один SPEC. После — user inspects manually.", options: { color: C.danger } },
  ], {
    x: MARGIN + 0.5, y: retryY + 0.55, w: PAGE_W - 2 * MARGIN - 0.7, h: 1.0,
    fontSize: 13, fontFace: F.body, color: C.textDark, paraSpaceAfter: 4, valign: "top",
  });
}

// ============================================================
// SLIDE 12 — Roadmap
// ============================================================
{
  const s = pres.addSlide();
  s.background = { color: C.bgWhite };
  addHeaderBar(s, "Roadmap — что появится в v3", "Решено сейчас vs кандидаты на backlog vs отвергнуто");

  // Two columns: backlog | rejected
  const colY = 1.8;
  const colH = 5.0;
  const colW = (PAGE_W - 2 * MARGIN - 0.4) / 2;

  // LEFT — Backlog (?)
  s.addShape("roundRect", {
    x: MARGIN, y: colY, w: colW, h: colH,
    fill: { color: "FFFBEB" }, line: { color: C.amber, width: 2 },
    rectRadius: 0.1,
  });
  s.addText([
    { text: "?  ", options: { fontSize: 24, bold: true, color: C.amber } },
    { text: "Backlog — кандидаты на v3", options: { fontSize: 18, bold: true, color: C.amber } },
  ], {
    x: MARGIN + 0.3, y: colY + 0.15, w: colW - 0.6, h: 0.5,
    fontFace: F.title, margin: 0, valign: "middle",
  });
  const backlog = [
    { title: "Auto retry для runner failures",
      desc: "--fix-mode flag на dh-developer; лимит 1 retry, потом эскалация" },
    { title: "/dh --refactor short pipeline",
      desc: "developer + runner без SPEC, для rename/reorganize" },
    { title: "/dh --docs-only path",
      desc: "skip implementation, прямо в dh-docs (для документации без кода)" },
    { title: "Claude hooks",
      desc: "SessionStart warn о stale memory; Stop → spawn knowledge для ad-hoc edits" },
    { title: "dh-fixer как отдельный агент",
      desc: "vs --fix-mode flag на developer — оба варианта в backlog" },
  ];
  backlog.forEach((item, i) => {
    const y = colY + 0.8 + i * 0.78;
    s.addText([
      { text: "•  ", options: { color: C.amber, bold: true, fontSize: 13 } },
      { text: item.title, options: { bold: true, color: C.textDark, fontSize: 13, breakLine: true } },
      { text: "    " + item.desc, options: { color: C.textMuted, fontSize: 11 } },
    ], {
      x: MARGIN + 0.3, y, w: colW - 0.5, h: 0.78,
      fontFace: F.body, valign: "top", margin: 0,
    });
  });

  // RIGHT — Rejected (✗)
  const rX = MARGIN + colW + 0.4;
  s.addShape("roundRect", {
    x: rX, y: colY, w: colW, h: colH,
    fill: { color: "FEF2F2" }, line: { color: C.danger, width: 2 },
    rectRadius: 0.1,
  });
  s.addText([
    { text: "✗  ", options: { fontSize: 24, bold: true, color: C.danger } },
    { text: "Отвергнуто", options: { fontSize: 18, bold: true, color: C.danger } },
  ], {
    x: rX + 0.3, y: colY + 0.15, w: colW - 0.6, h: 0.5,
    fontFace: F.title, margin: 0, valign: "middle",
  });
  const rejected = [
    { title: "Trivial fast-path (typo, formatting)",
      desc: "Friction > benefit. Пользователь сам делает без /dh." },
    { title: "Static Analysis Agent",
      desc: "detekt уже в dh-runner; отдельный агент дубль." },
    { title: "QA Agent",
      desc: "Learning project — нет human QA. Reviewer + Runner закрывают." },
    { title: "tester ∥ reviewer parallel",
      desc: "Task tool спавнит sequential; parallel — иллюзия в markdown." },
    { title: "Все 14 агентов из исходного дизайна",
      desc: "Линейные 14 шагов → 50% handoff overhead. Сокращено до 8." },
  ];
  rejected.forEach((item, i) => {
    const y = colY + 0.8 + i * 0.78;
    s.addText([
      { text: "•  ", options: { color: C.danger, bold: true, fontSize: 13 } },
      { text: item.title, options: { bold: true, color: C.textDark, fontSize: 13, breakLine: true } },
      { text: "    " + item.desc, options: { color: C.textMuted, fontSize: 11 } },
    ], {
      x: rX + 0.3, y, w: colW - 0.5, h: 0.78,
      fontFace: F.body, valign: "top", margin: 0,
    });
  });

  // Footer
  s.addText([
    { text: "Принцип решения:  ", options: { bold: true, color: C.bgNavy } },
    { text: "сначала довести v2 до стабильности, потом смотреть на конкретную боль, и только тогда выбирать из backlog. Не угадывать.", options: { color: C.textDark, italic: true } },
  ], {
    x: MARGIN, y: 7.0, w: PAGE_W - 2 * MARGIN, h: 0.35,
    fontSize: 12, fontFace: F.body, margin: 0,
  });
}

// ---------- SAVE ----------
pres.writeFile({ fileName: "D:\\diet_helper\\docs\\a2a-architecture.pptx" })
  .then(fn => console.log("Saved:", fn))
  .catch(err => { console.error(err); process.exit(1); });
