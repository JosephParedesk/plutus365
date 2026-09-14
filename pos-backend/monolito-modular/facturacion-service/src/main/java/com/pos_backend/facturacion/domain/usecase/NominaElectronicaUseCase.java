package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Transmisión de nómina electrónica (DIAN) vía Factus — un trabajador por
 * solicitud (`/v2/payrolls`), así que se genera una por cada empleado de la
 * nómina liquidada, no una sola para todo el período.
 */
@RequiredArgsConstructor
public class NominaElectronicaUseCase {

    private final NominaElectronicaGateway nominaElectronicaGateway;
    private final NominaConsultaGateway nominaConsultaGateway;
    private final EmpleadoConsultaGateway empleadoConsultaGateway;
    private final EmpresaConsultaGateway empresaConsultaGateway;
    private final ConfiguracionDianGateway configuracionDianGateway;
    private final FacturaElectronicaGateway facturaElectronicaGateway;

    public List<NominaElectronica> listarPorNomina(Long nominaId, String empresaId) {
        return nominaElectronicaGateway.listarPorNomina(nominaId, empresaId);
    }

    public List<NominaElectronica> listar(String empresaId) {
        return nominaElectronicaGateway.listar(empresaId);
    }

    /** Borra de Factus una nómina electrónica que quedó pendiente/rechazada, para poder reintentar. */
    public void eliminarNoValidada(Long nominaElectronicaId, String empresaId) {
        NominaElectronica n = nominaElectronicaGateway.buscarPorId(nominaElectronicaId, empresaId);
        if (n == null)
            throw new NoSuchElementException("Nómina electrónica no encontrada");
        if ("ACEPTADA".equals(n.getEstado()))
            throw new RuntimeException("No se puede eliminar una nómina electrónica ya aceptada por la DIAN");
        if (n.getReferenceCode() == null || n.getReferenceCode().isBlank())
            throw new RuntimeException("Este registro no tiene código de referencia guardado — es de antes de este cambio, elimínalo manualmente si hace falta");

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null)
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        try {
            facturaElectronicaGateway.eliminarNoValidada(config, "NOMINA", n.getReferenceCode());
        } catch (RuntimeException e) {
            // Si el intento original falló antes de que Factus llegara a crear el
            // documento (ej. el módulo de nómina electrónica no estaba habilitado en
            // la cuenta, o las credenciales fallaron), no hay nada que borrar allá —
            // Factus responde "no encontrado". Eso no debe bloquear la limpieza local:
            // se borra igual para que el usuario pueda reintentar.
        }
        nominaElectronicaGateway.eliminar(nominaElectronicaId, empresaId);
    }

    public NominaElectronica generar(Long nominaId, Long empleadoId, String empresaId, String creadoPor) {
        NominaElectronica existente = nominaElectronicaGateway.buscarPorNominaYEmpleado(nominaId, empleadoId, empresaId);
        if (existente != null && "ACEPTADA".equals(existente.getEstado()))
            throw new RuntimeException("Este empleado ya tiene la nómina de este período aceptada: " + existente.getNumeroDocumento());

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null || !Boolean.TRUE.equals(config.getActivo()))
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        NominaRemota nomina = nominaConsultaGateway.buscarNomina(nominaId, empresaId);
        if (nomina == null)
            throw new NoSuchElementException("Nómina no encontrada");
        if (!"LIQUIDADA".equals(nomina.getEstado()) && !"PAGADA".equals(nomina.getEstado()))
            throw new RuntimeException("Solo se puede transmitir una nómina LIQUIDADA o PAGADA (estado actual: " + nomina.getEstado() + ")");

        NominaRemota.DetalleRemoto detalle = nomina.getDetalles() == null ? null : nomina.getDetalles().stream()
                .filter(d -> empleadoId.equals(d.getEmpleadoId())).findFirst().orElse(null);
        if (detalle == null)
            throw new NoSuchElementException("Este empleado no está en la nómina liquidada de ese período");

        EmpleadoRemota empleado = empleadoConsultaGateway.buscarEmpleado(empleadoId, empresaId);
        if (empleado == null)
            throw new NoSuchElementException("Empleado no encontrado");

        EmpresaRemota empresa = empresaConsultaGateway.buscarEmpresa(empresaId);
        if (empresa == null)
            throw new RuntimeException("Configura primero los datos generales de tu empresa antes de transmitir nómina");

        NominaElectronica n = new NominaElectronica();
        n.setEmpresaId(empresaId);
        n.setNominaId(nominaId);
        n.setEmpleadoId(empleadoId);
        n.setNombreEmpleado((empleado.getNombres() + " " + empleado.getApellidos()).trim());
        n.setAnio(nomina.getAnio());
        n.setMes(nomina.getMes());
        n.setFechaEmision(LocalDateTime.now());
        n.setCreadoPor(creadoPor);
        n.setEstado("ERROR");

        // Incluye empresaId: Factus usa una cuenta de sandbox compartida entre muchos
        // desarrolladores, y nominaId/empleadoId son enteros chicos (1, 2, 3...) que
        // fácilmente coinciden con los de otra app probando al mismo tiempo — eso
        // hace que Factus responda "Query did not return a unique result" al buscar
        // por reference_code. empresaId (el NIT/cédula real) hace el código único de
        // verdad entre empresas distintas, sin perder la idempotencia por período+empleado.
        String referenceCode = "NOM-" + empresaId + "-" + nominaId + "-" + empleadoId;
        n.setReferenceCode(referenceCode);

        try {
            n.setFechaEnvio(LocalDateTime.now());
            FacturaElectronicaGateway.ResultadoEmision resultado = facturaElectronicaGateway.emitirNomina(
                    config, empleado, empresa, nomina, detalle, referenceCode);

            n.setNumeroDocumento(resultado.numeroDocumento());
            n.setCude(resultado.cufeOCude());
            n.setQrUrl(resultado.qrUrl());
            n.setUrlDocumento(resultado.urlDocumento());
            n.setAmbiente(resultado.ambiente());
            n.setRespuestaDian(resultado.mensaje());
            n.setEstado(resultado.aceptada() ? "ACEPTADA" : "RECHAZADA");
        } catch (RuntimeException e) {
            n.setRespuestaDian(e.getMessage());
            nominaElectronicaGateway.guardar(n);
            throw new RuntimeException("Factus rechazó la nómina electrónica: " + e.getMessage());
        }

        if (!"ACEPTADA".equals(n.getEstado())) {
            nominaElectronicaGateway.guardar(n);
            throw new RuntimeException("Factus no validó la nómina electrónica: " + n.getRespuestaDian());
        }

        return nominaElectronicaGateway.guardar(n);
    }
}
