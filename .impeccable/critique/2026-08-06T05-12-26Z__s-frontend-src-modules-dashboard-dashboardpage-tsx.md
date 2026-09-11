---
target: dashboard
total_score: 20
max_score: 40
na_heuristics: 
p0_count: 2
p1_count: 2
timestamp: 2026-08-06T05-12-26Z
slug: s-frontend-src-modules-dashboard-dashboardpage-tsx
---
# Design Critique: Plutus365 Dashboard

**Method: dual-agent (A: design-review agent · B: detector-evidence agent)**

## Design Health Score

| # | Heuristic | Score | Key Issue |
|---|-----------|-------|-----------|
| 1 | Visibility of System Status | 2 | Single full-page spinner blocks everything; no per-card skeleton, no "last updated," and failed fetches leave zero signal at all. |
| 2 | Match System / Real World | 3 | Strong Colombian-PYME/COP vocabulary, but the tax-date card shows static "15 JUN / 20 JUN" regardless of today's date. |
| 3 | User Control and Freedom | 2 | "Personalizar" looks like a live control but only reveals "Próximamente" on hover — an escape hatch promised, then withdrawn. |
| 4 | Consistency and Standards | 3 | Mostly faithful to colors.ts, but "Ventas − compras" (a financial delta) uses violet — DESIGN.md's documented "personas/info" accent, not a financial one. |
| 5 | Error Prevention | 1 | The loudest element on the page routes into /compras, a module CAJERO has zero access to per permisos.ts — with no gating, unlike every other entry point in the app. |
| 6 | Recognition Rather Than Recall | 3 | Icon+color+label triads work well; icon-only sidebar trades recall for space by documented design intent. |
| 7 | Flexibility and Efficiency | 1 | No date-range control, no reordering, no keyboard path to any action — same fixed view every day. |
| 8 | Aesthetic and Minimalist Design | 3 | Genuinely calm composition, undercut by a promotional banner being the loudest thing on the page plus two dead-looking-but-live affordances. |
| 9 | Error Recovery | 1 | Promise.allSettled silently drops failed calls — a down service renders identically to a real "$0, 0 transacciones" day. |
| 10 | Help and Documentation | 1 | No help affordance anywhere; the tax-date card's own "referencial" disclaimer is easy to miss against bold, countdown-styled dates. |
| **Total** | | **20/40** | **Acceptable** — solid product-specific bones, undermined by a role-access gap and a couple of quiet honesty problems. |

## Design Specificity Verdict

**Grounded, with one real self-inconsistency.** This is not a reskinned template: Intl.NumberFormat('es-CO', {currency:'COP'}), DIAN-specific tax vocabulary, and the compras-import banner's copy ("Sube el XML o PDF de la factura de tu proveedor. El sistema la contabiliza y afecta el inventario automáticamente") accurately describe this product's compensating-transaction architecture — no generic dashboard produces that sentence. Color mapping is correctly pulled from colors.ts on 3 of 4 KPIs per DESIGN.md's categorization rule.

But the page doesn't enforce the product's own role model. DashboardLayout.tsx, one file away, already solved "show a locked module, don't hide it" with puedeVer() + padlock icons. DashboardPage.tsx never imports permisos.ts at all — every CTA, including a direct route into COMPRAS, renders live for every role. That's not a generic flaw; it's a specific contradiction of a pattern this exact codebase already wrote down and follows one component away.

**Deterministic scan**: detect.mjs ran clean on DashboardPage.tsx (0 findings) and flagged one layout-transition warning in DashboardLayout.tsx:277 (transition: margin-left). Cross-checked against source: SIDEBAR_WIDTH is a hardcoded constant with no collapse mechanism anywhere in the file, so the transition is currently inert — not a live performance issue today, but it would become one the moment a collapsible-sidebar feature lands. Worth a proactive swap to transform, not urgent.

**Visual overlays**: Not available this run. Neither assessment had a browser automation tool exposed in this environment (claude-in-chrome wasn't connected), so no live navigation, no injection, and no console-based overlay ran. Nothing is highlighted in a [Human] tab — both assessments worked from precise source reconstruction instead (the page's styling is entirely inline JSX, which makes this a reliable substitute, but it is not the same as pixel-verified live evidence). Re-run with the browser tool connected if you want the visual overlay pass.

## Overall Impression

The bones are genuinely product-specific and the visual system (DESIGN.md's calm, tinted-shadow, single-accent language) is followed with real discipline in most places. The failure mode isn't generic-dashboard blandness — it's that the page quietly breaks three promises the rest of the codebase keeps: it doesn't gate by role like the sidebar does, it shows a directional claim ("sales are up") the code never computed, and it lets two controls look clickable while doing nothing. The single biggest opportunity: reuse permisos.ts here exactly as DashboardLayout.tsx already does — that one fix closes the worst heuristic gap (Error Prevention) and restores the page's honesty with its own design system.

## What's Working

1. **Correct semantic color use** — green/ventas, purple/clientes, coral/stock-bajo genuinely follow DESIGN.md's "categorization, not decoration" rule; this is real system discipline, not accidental variety.
2. **Domain-authentic copy** — the DIAN vocabulary and the compras-import banner's explanation of what the system actually does behind the scenes couldn't come from a generic template.
3. **Resilient fetch mechanics** — Promise.allSettled means one failed microservice call degrades a single card instead of crashing the whole dashboard; the mechanism is sound (its silent presentation is the problem, see P1 below).

## Priority Issues

**[P0] Import-invoice banner and "Registrar compra" bypass the app's own role model**
- Why it matters: The single largest, loudest element on the page — and a quick action — route into /compras/* unconditionally. CAJERO has zero access to COMPRAS per permisos.ts. This is the first thing a cashier sees after login, promising an action they can't complete, and it directly contradicts DESIGN.md's own Do-rule ("mostrar los módulos sin acceso... atenuados con candado en vez de ocultarlos").
- Fix: Import puedeVer(usuario?.rol, 'COMPRAS'), gate both elements behind it, reuse the sidebar's atenuado+candado treatment instead of inventing a new one.
- Suggested command: /impeccable harden

**[P0] Fabricated trend arrow on "Ventas últimos 7 días"**
- Why it matters: The green up-arrow next to the 7-day total is hardcoded — no prior-period comparison is computed anywhere. PRODUCT.md's product principle #3 is explicit: "sin números inventados." A directional claim is the same category of fabrication as a fake number, on the page most likely to set a business owner's expectations for the day.
- Fix: Compute a real delta vs. the previous 7-day window and drive icon/color from its sign, or remove the arrow until that exists.
- Suggested command: /impeccable harden

**[P1] Failed fetches render as an indistinguishable real zero**
- Why it matters: Promise.allSettled leaves state at []/0 on failure — "Ventas hoy: $0 · 0 transacciones" renders identically whether that's a true zero-sales day or venta-service being down. For financial data specifically, this is worse than a generic bug.
- Fix: Track per-section fulfilled/rejected status; render a small inline "No se pudo cargar" + retry on the affected card.
- Suggested command: /impeccable clarify

**[P1] Two affordances styled as live are actually dead**
- Why it matters: "Personalizar" is a fully-enabled-looking button revealed as inert only via hover tooltip; "Ver calendario completo" has the same green/weight styling as every real link but no onClick at all. This teaches users to distrust the one color (green) DESIGN.md reserves for "actúa aquí."
- Fix: Wire "Ver calendario completo" to a real route or delete it; give "Personalizar" a real AntD disabled state instead of a tooltip-only tell.
- Suggested command: /impeccable clarify

**[P2] Hardcoded, stale DIAN tax dates**
- Why it matters: "15 JUN / 20 JUN" are static regardless of today's date (2026-08-05 — months stale), styled boldly like real deadlines, with an easy-to-miss 11px "referencial" disclaimer. This sits in territory CLAUDE.md flags as high-stakes (DIAN mistakes carry real sanctions for the user).
- Fix: Compute the next actual dates from the DIAN calendar relative to today, or replace with the product's existing "candado" pattern for data it can't yet calculate.
- Suggested command: /impeccable harden

## Persona Red Flags

**Alex (Power User)**: No date-range control anywhere — the 7-day chart and month-to-date figures are hardcoded windows, no period comparison, no keyboard path to any action, and the one customization affordance offered ("Personalizar") is inert. Same fixed view, every day, forever.

**Sam (Accessibility-Dependent)**: The "Ver inventario" KPI link is a bare <span onClick> — no role, no keyboard focus, unreachable via Tab. textMuted (#9AA8A4) drives 11–12px captions site-wide on this page and is likely near/under WCAG AA contrast at that size. Three Progress bars in "Flujo de dinero" carry no accessible label beyond AntD defaults — a screen reader gets three unlabeled bars next to the only accessible text (the peso figures).

**PYME cajero/dueño checking between customers** (project-specific, from PRODUCT.md's operating context): the page's first-read moment is a compras-import ask, not the day's sales. The one number this persona most plausibly opened the dashboard for — "¿cuánto llevo hoy?" — is pushed to KPI position 1-of-4, competing at equal visual weight with three other cards, instead of leading the page.

## Minor Observations

- KPI values truncate with text-overflow: ellipsis and no tooltip fallback — a large COP total can silently clip on a narrow single-column layout with no way to see the exact figure.
- Detector flagged an animated margin-left transition in DashboardLayout.tsx:277 — currently inert (no collapse mechanism exists yet), but would cause layout thrash the moment a collapsible sidebar is added. Costs nothing to swap to transform now.
- LoginPage.tsx hardcodes hex literals instead of importing colors.ts (whose own header comment declares itself the required source of truth) — not this page, but the same visual system, worth flagging as drift.
- The 6-button "Acciones rápidas" grid has no internal grouping between "do a transaction" (Nueva venta, Registrar compra) and "go look at something" (Ver inventario, Ver reportes) intents.

## Questions to Consider

- If the sidebar already has a working, documented "show it but lock it" pattern for role/plan restrictions, why does the dashboard's highest-visual-weight element skip that pattern entirely?
- The chart's trend arrow is always up — was that a placeholder that shipped, or a deliberate choice? What's the intended read when a shop's sales are actually declining?
- "Personalizar" and "Ver calendario completo" both look finished and both do nothing — near-term scaffolding, or forgotten wiring? The answer changes whether this is a P1 or a non-issue.
