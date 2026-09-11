# Plutus365 — Contexto del proyecto

> Nombre anterior del proyecto: ODDO POS. Referencias a "ODDO" que puedan quedar
> en código, assets o variables son remanentes del nombre anterior.

SaaS de punto de venta y ERP para PYMES colombianas. Multiempresa (cada empresa ve
solo sus datos), con planes de suscripción que habilitan módulos distintos.

---

## Stack

- **Backend**: Spring Boot 4.x, Java 21, arquitectura limpia (domain / infraestructure / application)
- **Frontend**: React 19 + TypeScript + Vite + Ant Design + Zustand
- **Base de datos**: MySQL, esquema `ecommerce` (todos los microservicios comparten instancia)
- **Gateway**: Spring MVC (no Cloud Gateway reactivo)

**El frontend usa PNPM, no npm.** Mezclarlos rompe `node_modules` con un error
confuso (`Cannot read properties of null (reading 'matches')`). Siempre `pnpm add`.

---

## Microservicios y puertos

| Servicio | Puerto | Ruta base |
|---|---|---|
| auth-service | 8080 | `/api/pos/usuario/**` |
| subscription-service | 8081 | `/api/pos/planes/**`, `/api/pos/suscripciones/**` |
| inventario-service | 8082 | `/api/surtiana/inventario/**` |
| categoria-service | 8083 | `/api/pos/categorias/**` |
| proveedor-service | 8084 | `/api/pos/proveedores/**` |
| compra-service | 8085 | `/api/pos/compras/**` |
| cliente-service | 8086 | `/api/pos/clientes/**` |
| venta-service | 8087 | `/api/pos/ventas/**` |
| empresa-service | 8088 | `/api/pos/empresa/**` |
| facturacion-service | 8089 | `/api/pos/facturacion/**` |
| **gateway** | **8090** | enruta todo |
| contabilidad-service | 8091 | `/api/pos/contabilidad/**` |
| nomina-service | 8092 | `/api/pos/nomina/**` |

El gateway inyecta `X-Empresa-Id` (extraído del JWT) en cada request downstream.
Los servicios NUNCA confían en un empresaId que venga del body.

---

## Arquitectura: convenciones obligatorias

Cada microservicio sigue esta estructura. **Respétala siempre:**

```
domain/
  model/            → POJOs de dominio con Lombok
  model/gateway/    → interfaces (puertos)
  usecase/          → lógica de negocio, SIN anotaciones de Spring
infraestructure/
  driver_adapters/jpa_repository/   → entities *Data, repos y *DataGatewayImpl
  driver_adapters/http_client/      → llamadas a otros microservicios
  entry_points/                     → controllers REST
  mapper/                           → *Data ↔ dominio
  exception/                        → GlobalExceptionHandler
application/config/UseCaseConfig    → los @Bean de los UseCase
```

Los UseCase se instancian a mano en `UseCaseConfig`, no con `@Service`.

---

## Reglas de trabajo acordadas con el usuario

1. **Verificar antes de asumir en temas DIAN.** Antes de tocar cualquier cosa de
   facturación electrónica, nómina electrónica, conexión a la DIAN, reglas de
   documentos o plazos: **buscar la norma vigente en internet**. Estas reglas
   cambian y equivocarse genera sanciones reales para el usuario.

2. **Entregar solo lo que cambia.** No mandar el microservicio completo; solo los
   archivos modificados con su ruta de paquete correcta. EXCEPCIÓN: cuando se crea
   una página nueva en el frontend, hay que entregar también `App.tsx` (ruta),
   `DashboardLayout.tsx` (menú) y los servicios que consuma — es un paquete indivisible.

3. **Ser honesto sobre lo que no se puede calcular.** Si un reporte necesita datos
   que el sistema no captura, decirlo y explicar qué falta, en vez de construir una
   pantalla que salga vacía o un número inventado.

4. **Hablar siempre en español.** Todas las respuestas al usuario van en español,
   sin importar el idioma de la instrucción o de los archivos citados. El código,
   nombres de variables/paquetes y comandos se mantienen en su forma original
   (inglés donde ya lo estén); solo la conversación con el usuario es en español.

---

## Patrones que ya están establecidos

### Compensación en operaciones distribuidas
Cuando una operación toca varios servicios (ej: venta → inventario → contabilidad),
si un paso falla se revierten los anteriores y el documento queda marcado con un
estado de error (`ERROR_INVENTARIO`, `ERROR_CONTABILIZACION`), **no se borra**.
Ver `VentaUseCase.registrarVenta` y `CompraUseCase.registrarCompra`.

### Idempotencia contable
`AsientoContableUseCase` busca por `origen + referenciaId` antes de crear. Si ya
existe el asiento, lo devuelve en vez de duplicar.

### Listas serializadas como JSON
Los ítems, formas de pago y aplicaciones se guardan como columna JSON (`TEXT`/`LONGTEXT`),
no como tablas hijas. Requiere `jackson-datatype-jsr310` para las fechas.

---

## Errores recurrentes (ya diagnosticados, no repetirlos)

- **Jackson en Spring Boot 4.1**: hay que declarar explícitamente `jackson-core`,
  `jackson-databind` y `jackson-datatype-jsr310` en `build.gradle`. No vienen transitivos.
- **`Map.of()`**: máximo 10 pares y NO acepta valores null. Para bodies grandes usar `HashMap`.
- **`Math.round(double)` devuelve `long`**: hace falta cast explícito a `(double)` para setters `Double`.
- **`ddl-auto=update` NO amplía columnas existentes.** Si se alarga un campo hay que
  correr `ALTER TABLE` a mano. Ya pasó con `estado VARCHAR(30)` y `cufe_proveedor VARCHAR(200)`.
- Tras cambiar `build.gradle`: "Reload All Gradle Projects" en IntelliJ.

---

## Módulos construidos

**Ventas / POS** — carrito, múltiples formas de pago, recibo imprimible, caja
(apertura/cierre con cálculo de efectivo esperado), centros de costo, ventas a
crédito con `saldoPendiente`, marca de obsequio.

**Cotizaciones** — flujo Borrador → Enviada → Aprobada → Convertida. Documento
imprimible profesional que trae empresa y cliente automáticamente. Al convertir
reutiliza `VentaUseCase` (descuenta stock y contabiliza).

**Facturación recurrente** — plantillas periódicas. **NO genera ventas solas**: cuando
llega la fecha quedan pendientes y el usuario confirma con un clic. Decisión
deliberada para no acumular errores silenciosos.

**Recibo de caja** — abonos de clientes a facturas a crédito, multi-factura, o anticipos.

**Compras** — 7 tipos de documento estilo Siigo (FC, DS, FCE, OC, RP, ND, AC),
importación de facturas de proveedor desde XML (UBL 2.1, parseo completo) y PDF
(best-effort con PDFBox + regex), crédito de proveedores con vencimientos.

**Inventario** — productos, filtros completos, stock automático desde compras
(crea el producto si el SKU no existe), Kárdex con histórico de movimientos.

**Contabilidad** — PUC colombiano (~197 cuentas sembradas por empresa), asientos
automáticos desde venta/compra/nota crédito/recibo de caja, Balance General,
Estado de Resultados, Flujo de Efectivo (indirecto), Cambios en Patrimonio,
centros de costo.

**Facturación electrónica** — CUFE SHA-384, UBL 2.1, firma XAdES (Santuario),
certificado .p12 cifrado AES-256-GCM. **Corre en modo simulación** (`DIAN_SIMULACION=true`):
genera y firma localmente pero NO transmite a la DIAN. Notas crédito con CUDE y
los 6 conceptos de corrección del anexo técnico.

**Nómina** — empleados, liquidación mensual con parámetros legales 2026
(SMMLV $1.750.905, auxilio transporte $249.095), exoneración Ley 1607/2012,
ARL por nivel de riesgo, provisiones, acumulados iniciales para migración.
**La retención en la fuente se captura manual**, no se calcula: depende de la
depuración individual de cada empleado.

**Reportes** — catálogo agrupado (Estados financieros, Ventas, Compras, Tributaria,
Nómina, Inventarios) con ~50 reportes. Los que no se pueden calcular aparecen con
candado y explican qué falta.

**Roles y permisos** — 4 roles (ADMIN, CAJERO, CONTADOR, INVENTARIO). La autoridad
real está en `PermisosInterceptor` del gateway (valida JWT contra matriz por ruta);
`shared/utils/permisos.ts` es solo un espejo para ocultar botones.

---

## Pendientes conocidos

- **Transmisión real a la DIAN** (facturas y nómina electrónica): requiere certificado
  digital real, `testSetId` y resolución de habilitación. El CUDE de notas crédito
  es provisional.
- **audit-service**: no empezado.
- **Wompi**: pasarela para cobrar las suscripciones, no integrada.
- **Remisión** y **Nota débito de ventas**: documentos no construidos.
- **ORI** (Otro Resultado Integral): el PUC base no tiene esas cuentas.
- Frontend de recibo de caja y switch de "venta a crédito" en el POS.
- `auth-service` devuelve el `Usuario` completo incluyendo la contraseña hasheada:
  debería usar un DTO limpio.

---

## Variables de entorno importantes

- `CERT_ENCRYPTION_KEY` — llave AES para el certificado .p12 de facturación.
  **Si se cambia, los certificados guardados dejan de poder descifrarse.**
- `DIAN_SIMULACION` — `true` mientras no haya habilitación real.
- `MAIL_USERNAME` / `MAIL_PASSWORD` / `MAIL_FROM` — credenciales SMTP de Brevo
  para el envío de correos (`venta-service`: comprobante de venta;
  `facturacion-service`: factura electrónica; `auth-service`: bienvenida y
  recuperación de contraseña — los tres usan `spring.mail.*`).
  **⚠️ ANTES DE LANZAR A PRODUCCIÓN:** hoy (2026-08-08) estas credenciales
  reales quedaron como valor por defecto directo en `application.properties`
  de los TRES servicios (pedido explícito del usuario, temporal). Hay que
  sacarlas de ahí y moverlas a `.env` (mismo patrón que `CERT_ENCRYPTION_KEY`
  en `facturacion-service/.env`) antes de que este código llegue a producción
  o a cualquier repositorio compartido/remoto.
- `FRONTEND_URL` — URL pública del frontend (`auth-service`), se usa para armar
  el link de "restablecer contraseña" que llega por correo
  (`{FRONTEND_URL}/restablecer-contrasena?token=...`). Default
  `http://localhost:5173`; hay que setearla a la URL real antes de producción
  o el link del correo va a apuntar a localhost.
