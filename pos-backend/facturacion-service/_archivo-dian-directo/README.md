# Archivo: implementación DIAN directa (pre-Factus)

Copia de referencia del código que `facturacion-service` tenía **antes** de integrar
Factus (2026-08-29) — firma XAdES propia, certificado .p12, transmisión SOAP directa
a la DIAN. Se guardó acá para poder retomarla más adelante (por ejemplo, si algún
día quieren dejar de depender de un proveedor tecnológico externo).

**No compila ni se ejecuta tal cual.** No está bajo `src/`, es solo referencia — para
reactivarla hay que moverla de vuelta a `src/main/java/...`/`src/main/resources/...`,
revertir los cambios en `FacturaUseCase`, `NotaCreditoUseCase`, `ConfiguracionDian*`,
`UseCaseConfig` y el frontend para que vuelvan a usar estos gateways, y agregar de
nuevo `org.apache.santuario:xmlsec:3.0.3` en `build.gradle`.

## ⚠️ Esto NO está completo — 8 archivos se perdieron de verdad

Al hacer el cambio a Factus se borraron archivos con `rm` en una carpeta **sin git**.
De la mayoría alcancé a leer el contenido completo antes de borrarlos (por eso están
acá), pero de estos 8 **nunca leí el contenido en esa conversación** — no hay forma de
que yo los reconstruya, y lo que hay abajo simplemente no los incluye:

- `infraestructure/driver_adapters/soap/DianGatewayImpl.java`
- `infraestructure/driver_adapters/soap/DianSoapClient.java`
- `infraestructure/driver_adapters/xml/UblXmlBuilder.java`
- `infraestructure/driver_adapters/xml/XadesSigner.java`
- `infraestructure/driver_adapters/xml/CufeCalculator.java`
- `infraestructure/driver_adapters/xml/XmlHelper.java`
- `infraestructure/driver_adapters/xml/XmlFacturaGatewayImpl.java`
- `resources/templates/factura-ubl-template.xml`

Eran justo la lógica más difícil de rehacer (construcción del XML UBL 2.1, cálculo
del CUFE con SHA-384 según el anexo técnico, firma XAdES con Apache Santuario).

**Tu mejor opción para recuperarlos es la Local History de IntelliJ** (tienes
IntelliJ 2025.3 instalado y el proyecto tiene `.idea/`): abre el proyecto,
clic derecho sobre la carpeta `facturacion-service` (o busca cada archivo por
nombre) → `Local History` → `Show History`. IntelliJ guarda snapshots locales
de cada guardado *independientemente de git*, así que es probable que siga ahí
aunque el archivo ya no exista en disco. Yo no puedo leer ese historial desde
aquí (es un formato interno del IDE), pero tú sí desde la interfaz.

`CertificadoSecretoDataJpaRepository.java` tampoco se leyó nunca, pero ese sí lo
reconstruí con confianza razonable (ver el comentario dentro del archivo) porque
su único uso observado (`findById`/`save`/`existsById`) son métodos heredados de
`JpaRepository`, sin queries propias.

## Qué SÍ está completo acá (leído y verificado antes de borrar)

- Todas las interfaces de dominio (`DianGateway`, `XmlFacturaGateway`,
  `CertificadoGateway`, `ConfiguracionDianGateway` con `reservarSiguienteConsecutivo`)
- `ConfiguracionDian`, `Factura`, `NotaCredito` (modelos de dominio originales)
- `FacturaUseCase`, `NotaCreditoUseCase`, `ConfiguracionDianUseCase` (use cases completos)
- `CertificadoGatewayImpl` + `CertificadoSecretoData` (manejo del .p12 cifrado)
- `ConfiguracionDianData`/`DataGatewayImpl`/`DataJpaRepository`/`Mapper`, `FacturaData`,
  `NotaCreditoData` (persistencia)
- `ConfiguracionDianController`, `UseCaseConfig`
- `application.properties.snippet` y `env.snippet` (las líneas relevantes)
- Frontend: `FacturacionElectronicaSection.tsx` completo, y el fragmento relevante
  de `facturacionService.ts`

## Recordatorio importante

Este proyecto **no tiene git** (`git init` nunca se corrió). Sin eso, cualquier
borrado — mío o tuyo — no tiene red de seguridad real; ni siquiera la Papelera de
Windows ayuda porque `rm` la salta. Vale la pena inicializar el repo pronto.
