package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Corrige o anula un documento soporte ya ACEPTADO (`/v2/adjustment-notes/validate`
 * de Factus). Los ítems y el proveedor se copian tal cual del documento original —
 * esta nota no permite renegociar cantidades distintas a las ya transmitidas.
 */
@RequiredArgsConstructor
public class NotaAjusteDocumentoSoporteUseCase {

    private final NotaAjusteDocumentoSoporteGateway notaAjusteGateway;
    private final DocumentoSoporteGateway documentoSoporteGateway;
    private final ProveedorConsultaGateway proveedorConsultaGateway;
    private final EmpresaConsultaGateway empresaConsultaGateway;
    private final ConfiguracionDianGateway configuracionDianGateway;
    private final FacturaElectronicaGateway facturaElectronicaGateway;

    public List<NotaAjusteDocumentoSoporte> listar(String empresaId) {
        return notaAjusteGateway.listar(empresaId);
    }

    public List<NotaAjusteDocumentoSoporte> listarPorDocumentoSoporte(Long documentoSoporteId, String empresaId) {
        return notaAjusteGateway.listarPorDocumentoSoporte(documentoSoporteId, empresaId);
    }

    /** Borra de Factus una nota de ajuste pendiente/rechazada, para poder reintentar. */
    public void eliminarNoValidada(Long notaAjusteId, String empresaId) {
        NotaAjusteDocumentoSoporte nota = notaAjusteGateway.buscarPorId(notaAjusteId, empresaId);
        if (nota == null) throw new NoSuchElementException("Nota de ajuste no encontrada");
        if ("ACEPTADA".equals(nota.getEstado()))
            throw new RuntimeException("No se puede eliminar una nota de ajuste ya aceptada por la DIAN");
        if (nota.getReferenceCode() == null || nota.getReferenceCode().isBlank())
            throw new RuntimeException("Esta nota no tiene código de referencia guardado — elimínala manualmente si hace falta");

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null) throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        facturaElectronicaGateway.eliminarNoValidada(config, "NOTA_AJUSTE_DOCUMENTO_SOPORTE", nota.getReferenceCode());
        notaAjusteGateway.eliminar(notaAjusteId, empresaId);
    }

    public NotaAjusteDocumentoSoporte emitir(Long documentoSoporteId, String conceptoCodigo, String observacion,
                                              String empresaId, String creadoPor) {
        DocumentoSoporte documento = documentoSoporteGateway.buscarPorId(documentoSoporteId, empresaId);
        if (documento == null) throw new NoSuchElementException("El documento soporte que quieres corregir no existe");
        if (!"ACEPTADA".equals(documento.getEstado()))
            throw new RuntimeException("Solo se puede emitir una nota de ajuste sobre un documento soporte ACEPTADO por la DIAN. " +
                    "Este documento está en estado " + documento.getEstado() + ".");
        if (documento.getNumeroDocumento() == null || documento.getNumeroDocumento().isBlank())
            throw new RuntimeException("El documento soporte no tiene número asignado por Factus todavía");

        ConceptoNotaAjuste concepto = ConceptoNotaAjuste.porCodigo(conceptoCodigo);

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null || !Boolean.TRUE.equals(config.getActivo()))
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        ProveedorRemota proveedor = proveedorConsultaGateway.buscarProveedor(documento.getProveedorId(), empresaId);
        if (proveedor == null) throw new NoSuchElementException("Proveedor del documento soporte no encontrado");
        EmpresaRemota empresa = empresaConsultaGateway.buscarEmpresa(empresaId);

        NotaAjusteDocumentoSoporte nota = new NotaAjusteDocumentoSoporte();
        nota.setEmpresaId(empresaId);
        nota.setDocumentoSoporteId(documentoSoporteId);
        nota.setNumeroDocumentoSoporte(documento.getNumeroDocumento());
        nota.setCudeDocumentoSoporte(documento.getCude());
        nota.setConceptoCodigo(concepto.getCodigo());
        nota.setConceptoDescripcion(concepto.getDescripcion());
        nota.setObservacion(observacion);
        // Se copian tal cual del documento original — Factus no permite ítems
        // nuevos en una nota de ajuste, solo corregir/anular lo ya transmitido.
        nota.setItems(documento.getItems());
        nota.setSubtotal(documento.getSubtotal());
        nota.setTotalIva(documento.getTotalIva());
        nota.setTotalRetencion(documento.getTotalRetencion());
        nota.setTotal(documento.getTotal());
        nota.setFechaEmision(LocalDateTime.now());
        nota.setCreadoPor(creadoPor);
        nota.setEstado("ERROR");

        String referenceCode = "NA-" + documento.getNumeroDocumento() + "-" + System.currentTimeMillis();
        nota.setReferenceCode(referenceCode);

        try {
            nota.setFechaEnvio(LocalDateTime.now());
            FacturaElectronicaGateway.ResultadoEmision resultado = facturaElectronicaGateway.emitirNotaAjusteDocumentoSoporte(
                    config, proveedor, empresa, nota, referenceCode);

            nota.setNumeroNota(resultado.numeroDocumento());
            nota.setCude(resultado.cufeOCude());
            nota.setQrUrl(resultado.qrUrl());
            nota.setAmbiente(resultado.ambiente());
            nota.setRespuestaDian(resultado.mensaje());
            nota.setEstado(resultado.aceptada() ? "GENERADA" : "RECHAZADA");
        } catch (RuntimeException e) {
            nota.setRespuestaDian(e.getMessage());
            notaAjusteGateway.guardar(nota);
            throw new RuntimeException("Factus rechazó la nota de ajuste: " + e.getMessage());
        }

        if (!"GENERADA".equals(nota.getEstado())) {
            notaAjusteGateway.guardar(nota);
            throw new RuntimeException("Factus no validó la nota de ajuste: " + nota.getRespuestaDian());
        }

        return notaAjusteGateway.guardar(nota);
    }
}
