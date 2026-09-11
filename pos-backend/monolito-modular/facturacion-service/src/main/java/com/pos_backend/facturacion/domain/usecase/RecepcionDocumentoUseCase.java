package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Recepción de facturas electrónicas DE PROVEEDORES y emisión de eventos
 * RADIAN sobre ellas (Resolución 000012/2021, vía Factus). Se ancla a una
 * `compra` importada desde XML que ya trae el CUFE del proveedor
 * (`compra.cufeProveedor`) — sin eso no hay nada que cargar.
 */
@RequiredArgsConstructor
public class RecepcionDocumentoUseCase {

    private final RecepcionDocumentoGateway recepcionGateway;
    private final CompraConsultaGateway compraConsultaGateway;
    private final ConfiguracionDianGateway configuracionDianGateway;
    private final FacturaElectronicaGateway facturaElectronicaGateway;

    public List<RecepcionDocumento> listar(String empresaId) {
        return recepcionGateway.listar(empresaId);
    }

    public RecepcionDocumento buscarPorCompraId(Long compraId, String empresaId) {
        RecepcionDocumento r = recepcionGateway.buscarPorCompraId(compraId, empresaId);
        if (r == null) throw new NoSuchElementException("Esta compra todavía no se ha cargado en Factus");
        return r;
    }

    /** Sube el CUFE de la factura del proveedor a Factus y resuelve su id interno. */
    public RecepcionDocumento cargar(Long compraId, String empresaId, String creadoPor) {
        RecepcionDocumento existente = recepcionGateway.buscarPorCompraId(compraId, empresaId);
        if (existente != null && existente.getBillId() != null)
            throw new RuntimeException("Esta compra ya fue cargada en Factus (id " + existente.getBillId() + ")");

        CompraRemota compra = compraConsultaGateway.buscarCompra(compraId, empresaId);
        if (compra == null) throw new NoSuchElementException("Compra no encontrada");
        if (compra.getCufeProveedor() == null || compra.getCufeProveedor().isBlank())
            throw new RuntimeException("Esta compra no tiene CUFE del proveedor — solo aplica a facturas " +
                    "importadas por XML de un proveedor electrónico, no a compras registradas a mano");
        // Resolución 000085/2022: los eventos RADIAN rigen para operaciones A CRÉDITO,
        // no de contado (verificado 2026-09-05 contra el PDF oficial de la DIAN).
        if (!Boolean.TRUE.equals(compra.getTieneCreditoProveedor()))
            throw new RuntimeException("Los eventos RADIAN solo aplican a compras a crédito (Resolución 000085/2022) — " +
                    "esta compra es de contado");

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null || !Boolean.TRUE.equals(config.getActivo()))
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        RecepcionDocumento recepcion = existente != null ? existente : new RecepcionDocumento();
        recepcion.setEmpresaId(empresaId);
        recepcion.setCompraId(compraId);
        recepcion.setCufe(compra.getCufeProveedor());
        recepcion.setFechaCarga(LocalDateTime.now());
        recepcion.setCreadoPor(creadoPor);

        try {
            String billId = facturaElectronicaGateway.cargarDocumentoRecibido(config, compra.getCufeProveedor());
            recepcion.setBillId(billId);
            recepcion.setEstado("CARGADO");
            recepcion.setRespuestaDian(null);
        } catch (RuntimeException e) {
            recepcion.setEstado("ERROR");
            recepcion.setRespuestaDian(e.getMessage());
            recepcionGateway.guardar(recepcion);
            throw new RuntimeException("No se pudo cargar la factura en Factus: " + e.getMessage());
        }

        return recepcionGateway.guardar(recepcion);
    }

    /**
     * Emite un evento RADIAN sobre una factura ya cargada. `conceptoReclamo` solo
     * se usa (y es obligatorio) cuando `evento` es RECLAMO.
     */
    public RecepcionDocumento emitirEvento(Long compraId, EventoRadian evento, ConceptoReclamoRadian conceptoReclamo,
                                            RecepcionDocumento.Persona persona, String empresaId) {
        RecepcionDocumento recepcion = recepcionGateway.buscarPorCompraId(compraId, empresaId);
        if (recepcion == null || recepcion.getBillId() == null)
            throw new RuntimeException("Primero hay que cargar esta factura en Factus antes de emitir eventos");
        if (recepcion.getEventos().stream().anyMatch(e -> e.getCodigo().equals(evento.getCodigo())))
            throw new RuntimeException("Ya se emitió el evento \"" + evento.getNombre() + "\" para esta factura");

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null || !Boolean.TRUE.equals(config.getActivo()))
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        FacturaElectronicaGateway.ResultadoEventoRadian resultado = facturaElectronicaGateway.emitirEventoRadian(
                config, recepcion.getBillId(), evento, persona, conceptoReclamo);

        RecepcionDocumento.EventoEmitido registro = new RecepcionDocumento.EventoEmitido();
        registro.setCodigo(evento.getCodigo());
        registro.setNombre(evento.getNombre());
        registro.setFecha(LocalDateTime.now());
        registro.setPersonaNombre((persona.getNombres() + " " + persona.getApellidos()).trim());
        registro.setRespuestaDian(resultado.mensaje());

        if (!resultado.exitoso()) {
            recepcion.setRespuestaDian(resultado.mensaje());
            recepcionGateway.guardar(recepcion);
            throw new RuntimeException("Factus rechazó el evento: " + resultado.mensaje());
        }

        recepcion.getEventos().add(registro);
        recepcion.setRespuestaDian(null);
        return recepcionGateway.guardar(recepcion);
    }
}
