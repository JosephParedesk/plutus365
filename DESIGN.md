---
name: Plutus365
description: SaaS de punto de venta y ERP para PYMEs colombianas — el libro mayor digital de tu negocio.
colors:
  primary: "#4E6F3A"
  primary-hover: "#689F38"
  primary-light: "#C5E1A5"
  accent: "#8BC34A"
  accent-bg: "#A5D66A"
  warning: "#FBC02D"
  warning-light: "#ECEFEA"
  danger: "#ff4d4f"
  danger-light: "#fff1f0"
  sidebar-bg: "#FFFFFF"
  sidebar-active-bg: "#4E6F3A"
  sidebar-active-text: "#FFFFFF"
  sidebar-text: "#6B746C"
  auth-panel-bg: "#4E6F3A"
  heading: "#2E3A2F"
  text-secondary: "#6B746C"
  border: "#ECEFEA"
  page-bg: "#F6F8F5"
  card-bg: "#FFFFFF"
typography:
  display:
    fontFamily: "'Public Sans Variable', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, sans-serif"
    fontSize: "22px"
    fontWeight: 800
    lineHeight: 1.3
  title:
    fontFamily: "'Public Sans Variable', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, sans-serif"
    fontSize: "14px"
    fontWeight: 600
    lineHeight: 1.4
  body:
    fontFamily: "'Public Sans Variable', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, sans-serif"
    fontSize: "14px"
    fontWeight: 400
    lineHeight: 1.5
  label:
    fontFamily: "'Public Sans Variable', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, sans-serif"
    fontSize: "12px"
    fontWeight: 600
    lineHeight: 1.4
    letterSpacing: "normal"
rounded:
  sm: "10px"
  md: "14px"
  lg: "16px"
  xl: "18px"
  2xl: "22px"
  3xl: "24px"
spacing:
  xs: "8px"
  sm: "12px"
  md: "16px"
  lg: "20px"
  xl: "24px"
shadow:
  soft: "0 2px 6px rgba(0,0,0,.03), 0 10px 30px rgba(0,0,0,.08)"
  large: "0 4px 10px rgba(0,0,0,.04), 0 20px 45px rgba(0,0,0,.10)"
components:
  button-primary:
    backgroundColor: "{colors.primary}"
    textColor: "#FFFFFF"
    rounded: "{rounded.md}"
    height: "36px"
    padding: "0 16px"
    boxShadow: "none"
  button-primary-hover:
    backgroundColor: "{colors.primary-hover}"
    transform: "scale(1.02)"
  card:
    backgroundColor: "{colors.card-bg}"
    rounded: "{rounded.2xl}"
    padding: "18px"
    border: "none"
    boxShadow: "{shadow.soft}"
  sidebar-nav-item-selected:
    backgroundColor: "{colors.sidebar-active-bg}"
    textColor: "{colors.sidebar-active-text}"
    rounded: "{rounded.lg}"
---

# Design System: Plutus365

## Overview

**Rediseño Soft UI / Neumorphism moderno (agosto 2026)** — reemplaza el sistema
"Libro Mayor Digital" anterior (verde bosque oscuro, riel oscuro, sombras casi
imperceptibles) por decisión explícita del usuario: paleta cerrada de verdes +
blanco, tarjetas y paneles que **flotan** sobre el fondo vía sombra de dos capas
en vez de borde, radios grandes (16–24px), mucho espacio en blanco. El
producto sigue siendo el mismo — POS/ERP de alta densidad para PYMEs
colombianas — pero ahora se ve más "SaaS premium 2026" que "libro contable
serio". Layout, componentes, navegación y lógica quedaron intactos; este es
un cambio de piel, no de estructura.

**Key Characteristics:**
- Riel lateral **blanco** elevado (antes oscuro), ítem seleccionado con fondo
  verde muy oscuro y texto/ícono blanco.
- Tarjetas blancas de esquinas grandes (18–24px) que flotan sobre `page-bg`
  vía sombra suave de dos capas, sin borde visible.
- Un único acento de marca reservado para relleno con texto/ícono blanco
  (botón primario, sidebar seleccionado); el verde "principal" pedido por el
  usuario queda para acentos que no llevan texto encima (líneas de gráfica,
  glow de foco).
- Una sola familia (Public Sans, autohospedada) en toda la app — sin cambios
  respecto al sistema anterior, el rediseño es de color/elevación, no de tipo.
- Micro-movimiento `all .25s ease` en tarjetas/botones: `translateY(-2px)` al
  hover de tarjeta, `scale(1.02)` al hover de botón, `translateY(1px)` al click.

## Colors

Paleta **cerrada** pedida por el usuario — verdes + blanco, sin violeta.
Dos excepciones deliberadas, documentadas también en `colors.ts`:

1. **El verde "principal" (#8BC34A) no lleva texto/ícono blanco encima.** Da
   ~2.1:1 de contraste — falla incluso el 3:1 mínimo para íconos (WCAG
   1.4.11), no solo el 4.5:1 de texto normal. Donde el verde carga texto o
   ícono blanco se usa "Verde muy oscuro" (#4E6F3A, 5.7:1) — ya está en la
   paleta dada, no es un color inventado.
2. **No hay rojo en la paleta pedida.** Un estado de peligro (anular, stock
   bajo, factura vencida) es una señal de seguridad, no una preferencia
   estética — quitarlo es un retroceso real de usabilidad. Se mantiene el
   rojo por defecto de AntD (`#ff4d4f`), única excepción a "solo esta paleta".

### Primary (rellenos con texto/ícono blanco)
- **Verde muy oscuro** (#4E6F3A): botón primario, sidebar seleccionado, checkbox marcado. Es el único color con permiso de "actúa aquí".
- **Verde oscuro** (#689F38): hover de primario — un paso más claro, no más oscuro.
- **Verde claro** (#C5E1A5): fondo de ícono/chip "positivo".

### Accent (sin texto encima)
- **Verde principal** (#8BC34A): líneas de gráfica, glow de foco, resaltados decorativos.
- **Verde de fondos** (#A5D66A): barras de gráfica.

### Estado
- **Ámbar** (#FBC02D): alertas/estados pendientes.
- **Rojo** (#ff4d4f): peligro/error — excepción deliberada, ver arriba.

### Neutral
- **Encabezado** (#2E3A2F): texto principal, valores de KPI.
- **Texto secundario** (#6B746C): también reemplaza al antiguo "texto muted" — un gris más pálido daba <2:1 de contraste.
- **Borde** (#ECEFEA): solo para separadores estructurales puntuales (divisor del sidebar, fila de tabla); las tarjetas ya no llevan borde, ver Elevation.
- **Fondo de página** (#F6F8F5): el lienzo detrás de todas las tarjetas.
- **Fondo de tarjeta** (#FFFFFF): toda superficie elevada.

### Named Rules
**The White-Text Exception Rule.** El verde "principal" (#8BC34A) de la paleta pedida nunca lleva texto o ícono blanco encima — para eso se usa "Verde muy oscuro" (#4E6F3A). El principal queda para líneas de gráfica, glows y resaltados sin texto.

**The Neutral Shadow Rule** (reemplaza a la antigua "Tinted Shadow Rule"). Las sombras ya no se tiñen del color de marca — son neutras de dos capas: `0 2px 6px rgba(0,0,0,.03), 0 10px 30px rgba(0,0,0,.08)` en reposo, `0 4px 10px rgba(0,0,0,.04), 0 20px 45px rgba(0,0,0,.10)` en modales/dropdowns. Fórmula literal pedida por el usuario.

## Typography

Sin cambios respecto al sistema anterior — este rediseño es de color/elevación,
no de tipografía. Ver `colors.ts`/`antdTheme.ts` para la familia (Public Sans
Variable, autohospedada, una sola familia en toda la app).

## Layout

Sin cambios de estructura. Riel lateral fijo de 72px, layout de dos paneles en
auth, grid `auto-fit` en el contenido — todo igual, solo cambió cómo se ven
los bloques (blanco flotante en vez de superficie plana con borde).

## Elevation & Depth

**Sistema invertido respecto al anterior:** antes la profundidad era casi
silenciosa y el borde de 1px hacía la separación primaria; ahora **todo
flota** — cero bordes visibles en tarjetas/paneles/inputs/modales, la sombra
de dos capas es el único mecanismo de separación.

### Shadow Vocabulary
- **Soft** (`0 2px 6px rgba(0,0,0,.03), 0 10px 30px rgba(0,0,0,.08)`): toda tarjeta, panel, chip de acción rápida en reposo.
- **Large** (`0 4px 10px rgba(0,0,0,.04), 0 20px 45px rgba(0,0,0,.10)`): modales, dropdowns, tarjetas en hover/popular.
- **Sidebar** (`2px 0 6px rgba(0,0,0,.03), 6px 0 30px rgba(0,0,0,.06)`): el riel blanco flota sobre el contenido, no un borde derecho.

### Named Rules
**The Float Rule** (reemplaza a "Hover-Lifts"). Nada lleva borde de separación — la sombra sola indica que un bloque es independiente. `.ant-card` y `.hover-lift` usan `transition: all .25s ease`; tarjetas suben `translateY(-2px)` al hover, botones escalan `scale(1.02)`, el click siempre "hunde" con `translateY(1px)`.

**Excepción documentada:** las vistas de documento imprimible (`CompraDocumentoView.tsx`, `FacturaDocumentoView.tsx`) conservan bordes de tabla de 1px — son plantillas de factura/orden de compra pensadas para imprimirse o exportarse como PDF, no tarjetas de app; un documento de negocio sin líneas de tabla se lee roto en papel. No aplicar el Float Rule ahí.

## Shapes

Escala de radios reescalada (Growing Radius Rule sigue vigente, solo con
números más grandes): `10px` (Tag) → `14px` (Button/Input) → `16px` (nav
item, tarjetas de plan) → `18px` (Table/Dropdown) → `22px` (Card) → `24px`
(Modal). Mismo orden que antes, todo un paso más grande.

### Named Rules
**The Growing Radius Rule** (vigente, valores actualizados). El radio de esquina sigue escalando con el tamaño/importancia del componente — nunca al revés.

## Components

### Buttons
- **Shape:** radio 14px, altura de control 36px (sin cambios de tamaño), peso 600.
- **Primary:** fondo Verde muy oscuro (#4E6F3A), texto blanco. Hover `#689F38` + `scale(1.02)`; click `translateY(1px)`.
- **Secondary/Ghost:** blancos con `boxShadow: soft` en vez de borde — mismo mecanismo Float que las tarjetas.
- **Danger:** rojo AntD por defecto, reservada a Popconfirm y acciones destructivas.

### Cards / Containers
- **Corner Style:** 22px.
- **Border:** ninguno — `Card.colorBorderSecondary: 'transparent'` en el theme. Cualquier `<div>` que actúe como card (no todos son `<Card>` de AntD) necesita su propio `boxShadow: soft` inline, porque el theme solo cubre componentes AntD.
- **Shadow Strategy:** Soft en reposo, Large en hover si la tarjeta es interactiva.

### Inputs / Fields
- **Style:** radio 14px, sin borde visible (`colorBorder: 'transparent'` en el token de Input), fondo blanco.
- **Focus:** glow verde suave — `activeShadow: 0 0 0 3px accent+33% alpha` en el theme.

### Navigation
- **Style:** riel de 72px, **fondo blanco** (antes oscuro), `Menu theme="light"` (antes `"dark"` — cambiar el theme prop es obligatorio, repuntar solo los tokens no alcanza porque el algoritmo dark/light de AntD asume distinto contraste base). Ítem seleccionado: fondo Verde muy oscuro, texto/ícono blanco. Ítem sin acceso: candado + texto en `textSecondary` (NO el gris más pálido de la paleta pedida — ese da <2:1, invisible).

### Signature Component: Panel de Autenticación Dividido
Sin cambios de estructura. El panel de marca oscuro usa un token propio ahora (`colors.authPanelBg`, verde muy oscuro) en vez de reusar `sidebarBg` — ese token pasó a ser blanco para el riel del dashboard y ya no sirve para el panel oscuro de auth.

## Do's and Don'ts

### Do:
- **Do** usar `colors.ts` como única fuente de verdad — paleta cerrada, no agregar hex nuevos sin agregarlos ahí primero.
- **Do** usar Verde muy oscuro (#4E6F3A) para cualquier relleno con texto/ícono blanco encima; el verde "principal" (#8BC34A) es solo para acentos sin texto.
- **Do** usar sombra de dos capas neutra para toda separación de bloques; cero bordes en tarjetas/paneles/inputs/modales.
- **Do** escalar el radio con el tamaño del componente (Growing Radius Rule): Tag(10) < Button/Input(14) < nav/plan(16) < Table/Dropdown(18) < Card(22) < Modal(24).
- **Do** mostrar los módulos sin acceso atenuados con candado, nunca ocultos.
- **Do** respetar `prefers-reduced-motion` — los nuevos hover de `scale`/`translateY` se desactivan bajo esa media query.

### Don't:
- **Don't** usar el verde "principal" (#8BC34A) como fondo de botón o de ítem seleccionado — falla contraste con texto/ícono blanco (ver White-Text Exception Rule).
- **Don't** agregar un borde visible a una tarjeta o panel nuevo — la sombra es el único mecanismo de separación (excepto vistas de documento imprimible, ver Elevation).
- **Don't** hardcodear un hex nuevo fuera de `colors.ts`.
- **Don't** usar esquinas a escuadra (radio 0) en ningún componente de contenido.
