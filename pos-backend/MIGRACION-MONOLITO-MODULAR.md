# Migración a monolito modular — plan y reglas

> Estado: PLAN, nada ejecutado todavía. Este documento se actualiza a medida que
> avanza cada módulo (marcar checkboxes).

## Por qué esto es viable con bajo riesgo

Revisé el código real antes de armar este plan (no es un plan genérico):

- **El frontend solo habla con el gateway** (`VITE_API_URL=http://localhost:8090`).
  Si las rutas no cambian, la migración es invisible para el frontend. Cero
  archivos de frontend se tocan en este plan.
- **El gateway es un proxy declarativo** (Spring Cloud Gateway MVC, rutas por
  `Path=/api/pos/x/**` → `http://localhost:PUERTO`). No hay lógica de negocio
  ahí, solo `EmpresaIdInterceptor` y `PermisosInterceptor`.
- **Cada servicio ya separa el puerto (dominio) de sus adaptadores.** Las llamadas
  entre microservicios pasan por una interfaz de dominio (`StockGateway`,
  `ContabilidadGateway`, etc.) implementada en `infraestructure/driver_adapters/http_client/`.
  Ejemplo real (`venta-service/StockGatewayImpl`): usa `RestClient` para pegarle
  a inventario-service, pero el `UseCase` de venta nunca sabe que eso es HTTP.
- **Esto significa que migrar un módulo = escribir una implementación nueva de
  la misma interfaz que llama directo al `UseCase` del otro módulo, en vez de por
  HTTP.** Cero cambios en `domain/usecase` ni en `domain/model`. Es mecánico.
- `empresaId` ya viaja como parámetro explícito en las firmas de los UseCase
  (no depende de leer el header dentro del usecase), así que no hay que inventar
  contexto compartido tipo ThreadLocal para reemplazar el `X-Empresa-Id`.

## Grafo de dependencias real (relevado del código)

Servicios "hoja" (nadie los llama por HTTP desde otro servicio, o no llaman a nadie):
`categoria`, `proveedor`, `cliente-service`, `subscription-service`, `auth`,
`contabilidad-service` (todos le pegan a este, él no le pega a nadie), `nomina`.

Servicios intermedios:
- `empresa-service` → depende de `contabilidad`
- `inventario` → depende de `categoria`, `proveedor`, `contabilidad`
- `compra` → depende de `contabilidad`, `inventario` (stock)
- `venta-service` → depende de `contabilidad`, `empresa`, `inventario` (stock)

Servicio más conectado (migrar de último):
- `facturacion-service` → depende de `cliente`, `compra`, `empresa`, `nomina`,
  `proveedor`, `venta`, `inventario` (stock), `contabilidad`

## Orden de migración

1. `categoria`, `proveedor`, `cliente-service`, `subscription-service`, `auth` (hoja, riesgo mínimo)
2. `contabilidad-service` (todos dependen de él — conviene tenerlo adentro temprano)
3. `nomina`
4. `empresa-service`
5. `inventario`
6. `compra`
7. `venta-service`
8. `facturacion-service` (último, el más conectado)
9. Apagar el proceso `gateway` cuando ya no quede ningún servicio standalone
   detrás de él (sus interceptores pasan a ser filtros del monolito)

## Arquitectura destino

- Un solo repo Gradle multi-módulo. Cada servicio actual se copia tal cual a
  `modules/<nombre>` **sin tocar una sola línea** de `domain/` ni `application/`.
- Un módulo nuevo `app` con el único `@SpringBootApplication`, que importa los
  `UseCaseConfig` de todos los módulos ya migrados.
- El módulo `gateway` se reduce a: interceptores de empresa/permisos (que pasan
  a vivir en `app` como filtros normales de Spring MVC) + las rutas que aún
  apunten a servicios standalone no migrados.
- Cada módulo migrado expone públicamente solo `domain.model` y
  `domain.usecase`; nada de `infraestructure.driver_adapters.jpa_repository.*`
  ni `.mapper.*` es visible desde otro módulo — eso sigue siendo interno,
  igual que hoy.

### Patrón: colisiones de nombre entre módulos (checklist para cada migración)

Cada microservicio standalone tenía su propio `ApplicationContext` de Spring,
así que nombres de clase repetidos entre servicios nunca chocaban. En el
monolito todos comparten un solo contexto — esto ya rompió dos veces al migrar
proveedor junto a categoria. Antes de dar por terminada la migración de
CUALQUIER módulo nuevo, revisar:

1. **`GlobalExceptionHandler`**: por default no se copia, ya existe uno
   compartido en `app/infraestructure/exception/GlobalExceptionHandler.java`.
   **Pero verificar SIEMPRE con `diff` contra el compartido antes de asumirlo**
   — pasó con `auth` (maneja excepciones de JWT, formato de respuesta
   `{"error":msg}` distinto al compartido `{"timestamp","status","error","message"}`).
   Si difiere: no fusionar (sería cambio de comportamiento), sino
   **renombrar la clase** (ej. `AuthGlobalExceptionHandler` — `@RestControllerAdvice`
   NO tiene atributo propio para fijar nombre de bean, su `value()` es alias
   de `basePackages()`, así que solo nombrar la anotación no alcanza) y
   scopearla con `@RestControllerAdvice(basePackages = "com.pos_backend.<modulo>.infraestructure.entry_points")`
   + `@Order(Ordered.HIGHEST_PRECEDENCE)` para que gane sobre el compartido
   en los tipos de excepción que ambos manejan, sin volverse ambiguo.
2. **`UseCaseConfig`**: si el microservicio origen tiene una clase con ese
   nombre (todos la tienen), agregar `@Configuration("<modulo>UseCaseConfig")`
   explícito al copiarla — si no, choca por bean id duplicado con la de
   cualquier otro módulo ya migrado.
3. Cualquier otra clase con nombre genérico (`Config`, `Mapper` sin prefijo,
   etc.) — revisar si el microservicio origen tiene alguna así antes de
   copiar tal cual.
4. **`@Bean` con nombre de método genérico** dentro de un `UseCaseConfig`
   (ej. `restTemplate()`, `objectMapper()`): el id del bean es el nombre del
   método, no el de la clase — dos módulos que declaren el mismo choca igual
   que el `@Configuration` sin nombre. `subscription-service` ya declara
   `restTemplate()`; revisar antes de migrar cualquier módulo que use
   `RestTemplate`/`RestClient` en su `http_client` (compra, venta,
   facturacion, empresa) si repite el mismo nombre de método.
5. **Cualquier clase `@Component`/`@Repository`/`@Service` con simple name
   repetido entre módulos** (no solo `Mapper`/`Config`) — ej. dos
   `NotificationGatewayImpl` en paquetes `.notification` distintos
   (`auth` y `subscription-service`) chocaron igual que `UseCaseConfig`. A
   diferencia de `GlobalExceptionHandler`, acá SÍ alcanza con nombrar la
   anotación (`@Component("<modulo>NombreDeLaClase")`) sin renombrar el
   archivo — `@Component.value()` es directamente el id del bean.

## Receta por módulo (repetir exactamente, en orden)

1. Copiar el paquete completo del servicio a `modules/<x>` tal cual (copy-paste,
   no "aprovechar para mejorar").
2. Las entidades JPA apuntan al mismo esquema MySQL `ecommerce`, mismas tablas,
   sin `ALTER TABLE`.
3. Por cada `...GatewayImpl` en `infraestructure/driver_adapters/http_client/`
   que le pegue a OTRO módulo ya migrado: crear una implementación nueva en
   `infraestructure/driver_adapters/local_client/` que llame directo al bean del
   `UseCase` del otro módulo. Borrar la implementación HTTP recién cuando la
   nueva pase las mismas pruebas.
4. Si el módulo destino todavía NO está migrado, ese `http_client` se deja como
   está — el servicio standalone sigue corriendo en su puerto viejo en paralelo.
5. Registrar los `@Bean` de `UseCaseConfig` del módulo en `app`.
6. Actualizar la ruta del gateway (`Path=/api/pos/x/**`) para que apunte al
   puerto del monolito en vez del puerto standalone.
7. Dejar el microservicio standalone viejo intacto (no borrar el código) un
   período de "quemado" (2 semanas sugerido) antes de eliminarlo.

## Reglas estrictas (no negociables)

1. **Ni una línea de `domain/usecase/**` ni `domain/model/**` cambia durante la
   migración.** Si al mover un módulo el diff toca esos paquetes, se rechaza y
   se rehace. Como el repo hoy no tiene git, el primer paso real es `git init`
   + commit del estado actual como línea base, para poder diffear cada módulo.
2. **Un módulo migrado por commit.** Nunca dos módulos en el mismo cambio —
   si algo se rompe, tiene que poder aislarse a un commit específico.
3. **Ningún cambio de comportamiento va en el mismo commit que una migración.**
   Si aparece un bug real en el camino, se anota y se arregla en un commit
   aparte, después de que la migración esté mergeada.
4. **No subir el patrón de compensación a transacción real todavía.** El
   patrón `ERROR_INVENTARIO` / `ERROR_CONTABILIZACION` (documentado en
   `CLAUDE.md`) se mantiene igual aunque en el monolito ya sería posible
   envolver todo en un `@Transactional`. Ese es un cambio de comportamiento
   real y deliberado — se evalúa aparte, después, no mezclado con la mudanza
   estructural.
5. **Nada de abstracciones nuevas "por si acaso".** Si dos módulos necesitan
   código idéntico, se duplica ahora. Se evalúa extraerlo a un módulo común
   recién cuando lo necesite un tercero (regla de tres).
6. **Los límites entre módulos se verifican con un test ArchUnit**, no a mano
   ni de memoria: ningún módulo puede importar
   `infraestructure.driver_adapters.jpa_repository.*` ni `.mapper.*` de otro
   módulo; `domain` no puede depender de Spring; los `UseCase` se siguen
   instanciando solo en `UseCaseConfig` (nunca `@Service`).
7. **Antes de migrar un módulo sin tests, escribir primero tests de
   caracterización** contra el microservicio standalone corriendo (capturar
   pares request/response reales), y correr esos mismos casos contra la
   versión in-process — la salida tiene que ser idéntica byte a byte.
8. **Rollback = mover la ruta del gateway al puerto viejo.** Por eso ningún
   servicio standalone se borra hasta cumplir el período de quemado.

## Auditoría de tests (relevada 2026-09-11)

Conteo de archivos en `src/test/java` por servicio:

| Servicio | Tests | Nota |
|---|---|---|
| inventario | 13 | cobertura real (no verificado si compila — auditar antes de migrarlo, ver el caso de auth abajo) |
| auth | 9 | **corregido al migrar (2026-09-11): 6 de 9 no compilan** contra el `Usuario`/`UseCaseConfig`/`NotificationGatewayImpl` actuales — quedaron desactualizados, no es un problema de la migración. Cobertura real = 3 (`UsuarioDataGatewayImplTest`, `EncrypterGatewayImplTest`, `GlobalExceptionHandlerTest`) |
| facturacion-service | 5 | cobertura real (no verificado si compila — auditar antes de migrarlo) |
| subscription-service, categoria, proveedor, compra, gateway | 1 c/u | **solo el smoke test `contextLoads()` generado por Spring, cero cobertura real** (confirmado en los 5) |
| nomina | 1 | test real pero acotado (`ConceptoHoraExtra`), no smoke test — igual insuficiente como red de seguridad |
| cliente-service, venta-service, empresa-service, contabilidad-service | 0 | **sin ningún test** |

Esto endurece la regla 7: `venta-service` y `contabilidad-service` son los
módulos con más lógica de negocio sensible (patrón de compensación, asientos
idempotentes) y **cero red de seguridad**. No se migran sin escribir antes
tests de caracterización — no es opcional para esos dos.

## Aviso: repos anidados aplanados (2026-09-11)

`categoria`, `compra`, `gateway`, `proveedor`, `subscription-service` y
`pos-frontend` tenían cada uno su propio `.git` interno con remoto propio en
GitHub (plantillas/proyectos sueltos de antes de unificar todo en
`C:\plutus365`). El primer `git init` los había registrado como submódulos
(un puntero al commit, no el contenido), lo que hubiera roto la regla 1 (no
se puede diffear un módulo si el repo raíz no ve su contenido real). Se les
quitó el `.git` anidado y se versionó su contenido actual como archivos
normales — se pierde el historial viejo de esos `.git` (aprobado por el
usuario), pero ningún archivo ni cambio sin commitear se tocó. Si aparece
otra carpeta con `.git` propio más adelante, aplicar el mismo tratamiento
antes de seguir.

## Checklist de progreso

- [x] `git init` + commit línea base (incluyó sacar credenciales reales de
      `application.properties` de auth/venta/facturacion a `.env`, y el
      `jwt.secret` compartido con el gateway — no estaba planeado pero era
      el mismo tipo de problema)
- [x] Auditar qué servicios tienen tests en `src/test/java` (tabla arriba)
- [x] Detectar y aplanar los `.git` anidados que quedaban como submódulos (ver aviso arriba)
- [x] Armar `app` module + estructura Gradle multi-módulo vacía (`pos-backend/monolito-modular/`, compila y el `build` corre verde)
- [x] ArchUnit: reglas verificables hoy (dominio sin Spring, UseCase sin @Service/@Component). Falta la regla de límites entre módulos — no se puede escribir en serio hasta que exista un segundo módulo real para probarla contra código de verdad
- [x] **Migrar: categoria** (2026-09-11) — `domain/`, `application/` e
      `infraestructure/` copiados byte a byte (verificado con `diff -r`, la
      única diferencia es que no se copió `CategoriaApplication.java`: el
      bootstrap de Spring Boot de un microservicio standalone no tiene lugar
      en un módulo librería, `app` es el único entry point). Probado en vivo
      contra la base de datos `ecommerce` real (GET listar, POST save, DELETE
      eliminar) por puerto 9000, todavía NO conectado al gateway.
      Bug real encontrado y arreglado (no de negocio, de config de build):
      el plugin de Spring Boot agrega el flag de compilador `-parameters`
      automáticamente; un módulo librería sin ese plugin no lo tenía, y
      `@PathVariable Long categoriaId` fallaba en runtime con "Name for
      argument ... not specified". Se agregó `-parameters` a nivel raíz para
      todos los subprojects — afecta a build.gradle, no a domain/usecase.
      El smoke test `CategoriaApplicationTests` (`@SpringBootTest`) no podía
      correr dentro del módulo `categoria` (no hay `@SpringBootApplication`
      en su mismo paquete ni uno padre ahora que vive en `com.pos_backend.app`).
      Se reemplazó por un `AppApplicationTests.contextLoads()` equivalente en
      el módulo `app`, que sí puede levantar todo el contexto real.
      Pendiente para cuando entre el segundo módulo con su propio
      `GlobalExceptionHandler`: dos `@RestControllerAdvice` con
      `@ExceptionHandler(Exception.class)` genérico van a chocar
      (ambiguous handler) — no se resuelve todavía porque con un solo
      módulo no hay conflicto real que solucionar.
- [x] **Cutover del gateway para categoria** (2026-09-11) — `gateway/application.yaml`
      ahora enruta `/api/pos/categorias/**` a `http://localhost:9000` (el
      monolito) en vez de `http://localhost:8083` (categoria-service
      standalone, que sigue levantándose para poder hacer rollback). Probado
      con un JWT real (firmado con el mismo `JWT_SECRET`, claim `empresaId`):
      cliente → gateway (puerto 8090) → monolito → MySQL real, GET/POST/DELETE
      los tres funcionan y el `empresaId` del JWT llega bien via
      `X-Empresa-Id` hasta el UseCase. Puerto fijo del monolito: **9000**
      (fuera del rango 8080-8092 de los standalone). `levantar-todo.ps1`
      actualizado para levantar también `monolito-modular` (tarea Gradle
      `:app:bootRun`, no `bootRun` a secas por ser multi-módulo) y para leer
      su `.env` nuevo (`DB_PASSWORD`, mismo patrón que el resto).
      Rollback si algo falla: volver el `uri` de esa ruta a
      `http://localhost:8083` en `gateway/application.yaml`.
- [x] **Probar el frontend real contra el cutover** (2026-09-11) — stack
      completo levantado (`levantar-todo.ps1` + `pnpm dev`), sesión inyectada
      con un JWT válido (mismo mecanismo de prueba, sin crear una empresa
      nueva en la base real) y ejercitado el módulo de Categorías dentro de
      Inventario por la UI de verdad: listar (badge mostró 12, coincide),
      crear "Prueba cutover UI" (toast "Categoría creada", badge a 13) y
      eliminar (Popconfirm, toast "Categoría eliminada", vuelta a 12).
      Confirmado por `read_network_requests` que las 8 llamadas (`save`,
      `listar` x3, `eliminar`) fueron todas a `localhost:8090` (el gateway),
      cero llamadas al puerto 8083 viejo. `categoriaService.ts` no necesitó
      ningún cambio — el cutover es completamente transparente para el
      frontend.
- [x] **Migrar: proveedor** (2026-09-11) — mismo patrón que categoria,
      `domain/`, `application/` e `infraestructure/` copiados byte a byte
      (verificado con `diff -r`). Dos colisiones reales encontradas al
      convivir con categoria en un solo contexto de Spring (ver "Patrón:
      colisiones de nombre" más abajo — aplica a TODOS los módulos que
      falten, revisar en cada uno):
      1. `GlobalExceptionHandler` duplicado (mismo problema anotado como
         pendiente al migrar categoria, ahora real). Se consolidó en uno
         solo compartido en `app/infraestructure/exception/` y se borró la
         copia de categoria (ya migrada) y no se copió la de proveedor.
      2. `UseCaseConfig` de categoria y de proveedor son dos clases con el
         mismo simple name en paquetes distintos → Spring les asigna el
         mismo bean id por default (`useCaseConfig`) y tira
         `ConflictingBeanDefinitionException`. Se le puso nombre explícito
         a cada `@Configuration` (`@Configuration("categoriaUseCaseConfig")`,
         `@Configuration("proveedorUseCaseConfig")`) — mismo bean, mismo
         wiring, solo id distinto.
      Probado en vivo contra la base real: listar, guardar, NIT duplicado
      (confirma que el exception handler compartido devuelve el mismo 400
      de siempre) y eliminar. Se volvió a probar categoria en el mismo
      arranque para confirmar que la consolidación no la rompió — sigue
      funcionando igual. Todavía NO conectado al gateway (mismo criterio
      que categoria: cutover es un paso aparte).
- [ ] Cutover del gateway para proveedor (`Path=/api/pos/proveedores/**` → puerto del monolito)
- [x] **Migrar: cliente-service** (2026-09-11) — mismo patrón, `domain/`,
      `application/` e `infraestructure/` copiados byte a byte (`diff -r`).
      Sin http_client (hoja). Solo aplicó el ajuste ya conocido de
      `@Configuration("clienteUseCaseConfig")` — nada nuevo esta vez, el
      `GlobalExceptionHandler` se omitió directo sin redescubrir el problema.
      Probado en vivo: guardar cliente natural (default `pais=Colombia`
      aplicado), guardar persona jurídica con NIT sin DV (400 con el mensaje
      de validación correcto), buscar por documento, eliminar y confirmar
      que ya no aparece en el listado. Todavía NO conectado al gateway.
- [ ] Cutover del gateway para cliente (`Path=/api/pos/clientes/**` → puerto del monolito)
- [x] **Migrar: subscription-service** (2026-09-11) — mismo patrón, `domain/`,
      `application/` e `infraestructure/` copiados byte a byte (`diff -r`,
      incluye `PlanController`, `SuscripcionController`, `NotificationGatewayImpl`
      y el DTO). `@Configuration("subscriptionUseCaseConfig")` de nuevo.
      Este módulo agregó dos cosas nuevas al `app`:
      - `notification.service.url` como propiedad de `app/application.yaml`
        (antes vivía en el `application.properties` del standalone). Sigue
        apuntando a un servicio de notificaciones que **nunca se construyó**
        (`localhost:9092/api/notification/send`) — `NotificationGatewayImpl`
        ya se traga esa excepción hoy en el standalone (falla silenciosa,
        no rompe el flujo de creación de suscripción). Se copió tal cual,
        arreglarlo es aparte.
      - Un `@Bean RestTemplate restTemplate()`. Nombre de método genérico:
        si otro módulo migrado más adelante (compra, venta, facturacion,
        empresa — los que hoy usan `RestTemplate`/`RestClient` para sus
        `http_client`) declara otro bean `restTemplate()`, va a chocar igual
        que `UseCaseConfig`. Agregado a la checklist de colisiones.
      Probado en vivo: listar planes (los 3 sembrados), módulos por plan,
      `tiene-acceso` (plan Básico sin acceso a CONTABILIDAD → `false`,
      correcto), crear suscripción (14 días gratis, estado PENDIENTE),
      buscar por usuario, cancelar.
      **Hallazgo real pero preexistente, no causado por esta migración**:
      la tabla `plan_features` no tiene constraint único, y `data.sql` corre
      con `INSERT IGNORE` en cada arranque de cualquier servicio que la
      toque — sin un UNIQUE que `IGNORE` pueda disparar, cada restart le
      duplica las 6 filas de cada plan. Hoy tiene **35 copias** de cada fila
      del plan Profesional/Empresarial en la base real (acumulado de
      reinicios de este mismo día de pruebas, standalone y monolito por
      igual — es la misma tabla). No lo limpié: este entorno no tiene
      cliente `mysql` ni conector Python instalado. Pendiente para el
      usuario: deduplicar `plan_features` a mano (dejar una fila por
      `(plan_id, modulo)`) y evaluar agregar el UNIQUE constraint — eso sí
      es un cambio de comportamiento deliberado, no se hace de paso.
- [x] **Migrar: auth** (2026-09-11) — mismo patrón, `domain/`, `application/`
      e `infraestructure/` copiados byte a byte (`diff -r`; sin http_client,
      hoja). `@Configuration("authUseCaseConfig")` de nuevo.
      Este módulo trajo el caso "raro" que el plan ya anticipaba (ver
      checklist de colisiones): el `GlobalExceptionHandler` de auth **no**
      es igual al compartido — devuelve `{"error": msg}` en vez de
      `{"timestamp","status","error","message"}`, y maneja excepciones
      específicas de JWT (`ExpiredJwtException`, `MalformedJwtException`,
      `SignatureException`) que el compartido ni conoce. Fusionarlo hubiera
      sido un cambio de comportamiento real (el frontend puede depender del
      shape `{"error":...}` para los mensajes de login). Se mantuvo aparte:
      - Renombrado a `AuthGlobalExceptionHandler` (única diferencia real de
        contenido con el original — mismos métodos, mismas respuestas).
        Fue necesario porque `@RestControllerAdvice` no tiene atributo propio
        para fijar el nombre de bean (su `value()` es alias de
        `basePackages()`, no del id de bean como en `@Component`) — a
        diferencia de `UseCaseConfig`, acá no alcanzaba con nombrar la
        anotación, había que renombrar la clase.
      - `@RestControllerAdvice(basePackages = "com.pos_backend.auth.infraestructure.entry_points")`
        + `@Order(Ordered.HIGHEST_PRECEDENCE)`: solo aplica a los
        controllers de auth, y gana la ambigüedad en `RuntimeException`/
        `NoSuchElementException` (los únicos dos tipos que auth y el
        compartido manejan igual). Verificado en vivo: login con clave
        incorrecta devuelve `{"error":"Contraseña incorrecta"}` (formato de
        auth), un `save` de categoria con nombre vacío sigue devolviendo el
        formato compartido — conviven sin pisarse.
      También chocó `NotificationGatewayImpl` (mismo simple name que la de
      `subscription-service`, en paquetes `.notification` distintos) — se
      les dio nombre de bean explícito vía `@Component("...")` a ambas (acá
      sí alcanzaba, `@Component.value()` sí es el nombre de bean).
      Auditoría de tests corregida: de los "9 tests" que contaba al
      principio, **6 no compilan** contra el código actual del standalone
      (`Usuario`, `UseCaseConfig` y `NotificationGatewayImpl` cambiaron de
      forma y los tests nunca se actualizaron — preexistente, no lo causó
      esta migración). Solo se llevaron los 3 que sí compilan y pasan
      (`UsuarioDataGatewayImplTest`, `EncrypterGatewayImplTest`,
      `GlobalExceptionHandlerTest` → renombrado `AuthGlobalExceptionHandlerTest`).
      Los otros 6 quedaron en el standalone tal cual, rotos — arreglarlos es
      tarea aparte, no de esta migración.
      Probado en vivo: registrar usuario (rol ADMIN, empresaId autogenerado
      `EMP-<cedula>`), login (JWT real), endpoint protegido por JWT
      (`/empleados`, extrae empresaId del token), login con clave incorrecta
      y eliminar. Todavía NO conectado al gateway.
- [ ] Cutover del gateway para auth (`Path=/api/pos/usuario/**` → puerto del monolito)
- [x] **Tests de caracterización de contabilidad-service** (2026-09-11, previo
      a migrar, regla 7) — 0 tests antes de esto. 54 tests nuevos con
      Mockito puro (sin Spring, sin DB) sobre los 4 UseCase, corridos primero
      contra el standalone (54/54 verdes, caracterizan el comportamiento
      ACTUAL) y llevados sin cambios al módulo migrado (54/54 verdes ahí
      también). No exhaustivos — es lo mínimo razonable para un módulo de
      este tamaño, no cobertura completa: `AsientoContableUseCaseTest` (29,
      el más importante — cubre la idempotencia de las 5 formas de generar
      un asiento automático, incluida la excepción rara de
      `generarDesdeSaldoInicialInventario` que NO es idempotente-silenciosa
      como las demás, y la validación de partida doble),
      `CuentaContableUseCaseTest` (11, inferencia de nivel/padre del código
      PUC, siembra automática, restricciones de borrado),
      `CentroCostoUseCaseTest` (8, CRUD y validaciones),
      `EstadosFinancierosUseCaseTest` (6, escenarios de balance/resultados/
      flujo/patrimonio — no exhaustivo dado lo grande de la lógica de
      clasificación por clase PUC).
- [x] **Migrar: contabilidad-service** (2026-09-11) — `domain/`,
      `application/` e `infraestructure/` copiados byte a byte (`diff -r`).
      Sin http_client (hoja — es el módulo del que todos dependen, nadie
      depende de otro desde acá). `GlobalExceptionHandler` idéntico al
      compartido (confirmado con `diff`, se omitió). Único ajuste:
      `@Configuration("contabilidadUseCaseConfig")`. Sin colisiones nuevas
      de bean esta vez (nombres de clase únicos en el proyecto).
      Probado en vivo contra la base real (`empresa-contab-test`, empresa
      ficticia nueva): listar cuentas siembra automáticamente el PUC base
      (197 cuentas, coincide con lo documentado en CLAUDE.md), crear centro
      de costo, generar asiento desde una venta simulada (cuentas correctas:
      Caja/Venta de mercancías/IVA por pagar, `totalDebe == totalHaber`),
      repetir la misma venta confirma la idempotencia (sigue en 1 asiento),
      Balance General cuadra (`"cuadra":true`).
      **No se pudo limpiar la empresa de prueba**: el propio `CuentaContableUseCase.eliminar`
      impide borrar cuentas no personalizadas (todo el PUC base sembrado lo
      es) — no hay forma de borrar esto por API, y este entorno sigue sin
      cliente `mysql`/conector Python (mismo problema que con `plan_features`
      al migrar subscription-service). Queda en la base real, aislado bajo
      `empresaId = "empresa-contab-test"` (no colisiona con ninguna empresa
      real), documentado para limpieza manual futura si hace falta.
      Todavía NO conectado al gateway.
- [ ] Cutover del gateway para contabilidad-service (`Path=/api/pos/contabilidad/**` → puerto del monolito)
- [x] **Corrección: nomina-service SÍ estaba conectado al gateway** (2026-09-11)
      — dije lo contrario al cerrar el paso de contabilidad-service, mal:
      me guie por un comentario desactualizado en `PermisosInterceptor`
      ("todavía no existe el servicio") sin verificar la config real. La
      ruta `Path=/api/pos/nomina/**` → `http://localhost:8092` ya existía en
      `gateway/application.yaml`. No hacía falta "resolver" nada, solo
      migrar y hacer el cutover normal como con los demás módulos.
- [x] **Tests de caracterización de nomina** (2026-09-11, previo a migrar,
      regla 7) — 0 tests de UseCase antes de esto (el único test existente,
      `ConceptoHoraExtra`, es de un modelo de dominio, no de un UseCase). 29
      tests con Mockito puro. Los valores esperados de `NominaUseCaseTest`
      se calcularon con un script aparte que replica la fórmula exacta
      (no a mano) — corrieron en verde a la primera contra el standalone,
      confirmando que el cálculo era el esperado. `NominaUseCaseTest` (12):
      liquidación completa con exoneración/auxilio de transporte, salario
      integral (sin auxilio, sin provisiones, IBC al 70%), salario alto no
      exonerado con nivel de riesgo ARL, prorrateo por ingreso a mitad de
      período, período duplicado, sin empleados activos, restricciones de
      anular/marcar pagada. `EmpleadoUseCaseTest` (12): validaciones de
      salario mínimo/integral, documento duplicado. `AcumuladoInicialUseCaseTest`
      (5): upsert por empleado+año.
- [x] **Migrar: nomina** (2026-09-11) — `domain/`, `application/` e
      `infraestructure/` copiados byte a byte (`diff -r`). Sin http_client
      (hoja). `GlobalExceptionHandler` es un subconjunto funcional del
      compartido (mismos 2 casos con el mismo formato de respuesta, le
      falta el catch-all de `Exception` que sí tiene el compartido) — se
      omitió como en el caso normal, no como el de auth. Único ajuste:
      `@Configuration("nominaUseCaseConfig")`.
      **Detalle real encontrado**: el `build.gradle` original de nomina usa
      Spring Boot **4.1.0**, el único de todo el proyecto — el resto
      (incluido el monolito) está en 4.0.6. No se puede tener dos versiones
      de Boot en un solo classpath, así que compiló contra el 4.0.6 del
      monolito tal cual — sin ningún cambio de código necesario, los 29
      tests pasaron a la primera. También traía `org.apache.pdfbox:pdfbox`
      como dependencia — confirmado con `grep` que ningún archivo la usa
      (dependencia muerta), no se copió al módulo del monolito.
      Probado en vivo: parámetros legales, crear empleado, liquidar (los
      valores coincidieron exactamente con los del test de caracterización),
      bloqueo de período duplicado, marcar pagada. Limpié el empleado de
      prueba; la nómina de prueba quedó en estado PAGADA (ya no se puede
      anular por API una vez pagada, por diseño) — aislada bajo
      `empresaId = "empresa-nomina-test"`, no afecta a ninguna empresa real.
- [x] **Cutover del gateway para nomina** (2026-09-11) — `gateway/application.yaml`
      ahora enruta `/api/pos/nomina/**` a `http://localhost:9000` (el
      monolito) en vez de `http://localhost:8092` (nomina-service standalone,
      que sigue levantándose para poder hacer rollback).
- [ ] Migrar: empresa-service (escribir tests de caracterización primero — 0 tests hoy)
- [ ] Migrar: inventario
- [ ] Migrar: compra
- [ ] Migrar: venta-service (escribir tests de caracterización primero — 0 tests hoy)
- [ ] Migrar: facturacion-service
- [ ] Apagar proceso gateway standalone
