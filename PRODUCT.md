# Product

<!-- impeccable:product-schema 1 -->

## Platform

web

## Users

PYMEs colombianas (pequeñas y medianas empresas) que necesitan operar punto de
venta y llevar su ERP en un solo sistema. Multiempresa: cada empresa ve solo
sus propios datos, con planes de suscripción que habilitan distintos módulos.

Quién opera el sistema día a día varía según el cliente: en negocios pequeños
suele ser el propio dueño; en negocios con más personal, empleados con roles
dedicados operan mientras el dueño supervisa. El sistema ya modela esto con
4 roles fijos:

- **ADMIN** — acceso total a todos los módulos (típicamente el dueño).
- **CAJERO** — ventas y clientes completo, inventario solo lectura.
- **CONTADOR** — contabilidad, facturación y nómina completo; resto en lectura.
- **INVENTARIO** — inventario y compras completo, ventas solo lectura.

## Product Purpose

SaaS de punto de venta y ERP para PYMEs colombianas: ventas/POS, inventario,
compras, clientes, contabilidad con PUC colombiano, facturación electrónica
DIAN, nómina electrónica y reportes, todo en un solo sistema multiempresa.

## Positioning

Todo-en-uno más completo que las alternativas del mercado colombiano (Siigo,
Alegra, Nubox, World Office): un solo sistema integrado en vez de herramientas
sueltas por función. Los módulos están conectados entre sí — una venta
descuenta inventario y genera el asiento contable automáticamente, con
compensación si un paso falla — en lugar de vivir como integraciones externas
o anexos.

## Operating Context

Flujo típico: venta en el POS (múltiples formas de pago, caja con
apertura/cierre) → descuenta stock automáticamente → genera asiento contable
→ puede facturarse electrónicamente ante la DIAN. Compras desde proveedores
alimentan el inventario y el crédito de proveedores. Cotizaciones y
facturación recurrente alimentan ventas. Nómina se liquida mensualmente con
parámetros legales vigentes.

La facturación electrónica y la nómina electrónica están sujetas a normativa
DIAN que cambia; el proyecto ya tiene la regla de verificar la norma vigente
antes de tocar esas áreas (ver CLAUDE.md).

## Capabilities and Constraints

- Multiempresa real: cada empresa ve solo sus datos; la autoridad de permisos
  vive en el gateway (`PermisosInterceptor`), no solo en el frontend.
- Facturación electrónica corre en modo simulación (`DIAN_SIMULACION=true`):
  firma y genera CUFE/UBL localmente pero no transmite aún a la DIAN — falta
  certificado digital real y resolución de habilitación.
- Retención en la fuente de nómina se captura manual, no se calcula.
- Reportes que no se pueden calcular con los datos capturados se muestran con
  candado explicando qué falta, en vez de una pantalla vacía o un dato
  inventado.
- Pendiente: pasarela de pago (Wompi) para cobrar las suscripciones —
  todavía no integrada.

## Brand Commitments

Nombre actual del producto: **Plutus365** (nombre anterior: ODDO POS — hay
remanentes de "ODDO" en código y assets que son residuos del rename, no
intencionales).

Los assets de logo actuales (`logo.png`, `logo-oscuro.png`) todavía muestran
el nombre anterior "ODDO" y están pendientes de reemplazo por el usuario;
ningún trabajo de diseño debe generar un logo nuevo por su cuenta ni tratar
el logo actual como definitivo.

Sin voz/tono de marca confirmado más allá del nombre.

## Evidence on Hand

- `src/assets/hero.png`, `src/assets/logo.png`, `src/assets/logo-oscuro.png`
  existen pero el logo está desactualizado (ver Brand Commitments arriba).
- No hay testimonios, casos de estudio ni benchmarks reales — no inventar
  ninguno en trabajo futuro.

## Product Principles

1. **Multiempresa aislado por defecto** — cada empresa ve solo lo suyo; la
   confianza de origen de datos nunca viene del body de la request.
2. **Los módulos se compensan, no se rompen en silencio** — si un paso de una
   operación distribuida falla, se revierte lo anterior y el documento queda
   marcado con estado de error, nunca se borra.
3. **Sin números inventados** — si un reporte necesita datos que el sistema no
   captura, se dice explícitamente qué falta en vez de mostrar una pantalla
   vacía o un valor falso.
4. **DIAN se verifica, no se asume** — cualquier cambio en facturación
   electrónica, nómina electrónica o reglas DIAN requiere confirmar la norma
   vigente antes de tocar código.
5. **Todo-en-uno real, no una suma de anexos** — la integración entre módulos
   (venta → inventario → contabilidad) es el diferencial frente a la
   competencia; nuevo trabajo debe preservarla, no evitarla.
