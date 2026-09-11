package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Emisión de notas débito electrónicas (DIAN), transmitidas a través de Factus.
 *
 * ponytail: a diferencia de NotaCreditoUseCase, esta no toca stock ni genera
 * asiento contable — una nota débito de ventas casi nunca devuelve mercancía, y
 * el asiento automático queda pendiente (mismo criterio ya documentado en
 * NotaDebitoVenta.java del lado de venta-service). Agregar cuando haga falta.
 */
@RequiredArgsConstructor
public class NotaDebitoUseCase {

    private final NotaDebitoGateway notaDebitoGateway;
    private final FacturaGateway facturaGateway;
    private final ConfiguracionDianGateway configuracionDianGateway;
    private final FacturaElectronicaGateway facturaElectronicaGateway;
    private final VentaConsultaGateway ventaConsultaGateway;
    private final ClienteConsultaGateway clienteConsultaGateway;
    private final EmpresaConsultaGateway empresaConsultaGateway;

    public List<NotaDebito> listar(String empresaId) {
        return notaDebitoGateway.listar(empresaId);
    }

    public NotaDebito buscarPorId(Long id, String empresaId) {
        NotaDebito n = notaDebitoGateway.buscarPorId(id, empresaId);
        if (n == null) throw new NoSuchElementException("Nota débito no encontrada");
        return n;
    }

    public List<NotaDebito> listarPorFactura(Long facturaId, String empresaId) {
        return notaDebitoGateway.listarPorFactura(facturaId, empresaId);
    }

    /** Borra de Factus una nota débito que quedó pendiente/rechazada, para poder reintentar. */
    public void eliminarNoValidada(Long notaDebitoId, String empresaId) {
        NotaDebito nota = notaDebitoGateway.buscarPorId(notaDebitoId, empresaId);
        if (nota == null)
            throw new NoSuchElementException("Nota débito no encontrada");
        if ("ACEPTADA".equals(nota.getEstado()))
            throw new RuntimeException("No se puede eliminar una nota débito ya aceptada por la DIAN");
        if (nota.getReferenceCode() == null || nota.getReferenceCode().isBlank())
            throw new RuntimeException("Esta nota no tiene código de referencia guardado — es de antes de este cambio, elimínala manualmente si hace falta");

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null)
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        facturaElectronicaGateway.eliminarNoValidada(config, "NOTA_DEBITO", nota.getReferenceCode());
        notaDebitoGateway.eliminar(notaDebitoId, empresaId);
    }

    public NotaDebito emitir(NotaDebito nota, String empresaId, String creadoPor) {
        // 1. La factura debe existir y estar aceptada por la DIAN.
        Factura factura = facturaGateway.buscarPorId(nota.getFacturaId(), empresaId);
        if (factura == null)
            throw new NoSuchElementException("La factura que quieres cargar no existe");
        if (!"ACEPTADA".equals(factura.getEstado()))
            throw new RuntimeException("Solo se puede emitir una nota débito sobre una factura ACEPTADA por la DIAN. " +
                    "Esta factura está en estado " + factura.getEstado() + ".");

        // 2. El concepto debe ser uno de los válidos de la tabla de Factus.
        ConceptoNotaDebito concepto = ConceptoNotaDebito.porCodigo(nota.getConceptoCodigo());

        // 3. Debe tener ítems y valores coherentes.
        if (nota.getItems() == null || nota.getItems().isEmpty())
            throw new RuntimeException("La nota débito debe tener al menos un ítem");

        double subtotal = 0, iva = 0;
        for (NotaDebito.ItemNotaDebito item : nota.getItems()) {
            if (item.getCantidad() == null || item.getCantidad() <= 0)
                throw new RuntimeException("La cantidad de cada ítem debe ser mayor a 0");
            double base = item.getCantidad() * safe(item.getPrecioUnitario());
            double ivaItem = base * (safe(item.getPorcentajeIva()) / 100.0);
            item.setValorIva(redondear(ivaItem));
            item.setValorTotal(redondear(base + ivaItem));
            subtotal += base;
            iva += ivaItem;
        }
        nota.setSubtotal(redondear(subtotal));
        nota.setTotalIva(redondear(iva));
        nota.setTotal(redondear(subtotal + iva));

        nota.setEmpresaId(empresaId);
        nota.setNumeroFactura(factura.getNumeroFactura());
        nota.setCufeFactura(factura.getCufe());
        nota.setConceptoDescripcion(concepto.getDescripcion());
        nota.setFechaEmision(LocalDateTime.now());
        nota.setCreadoPor(creadoPor);
        nota.setEstado("ERROR");

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null || !Boolean.TRUE.equals(config.getActivo()))
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        String referenceCode = "ND-" + factura.getNumeroFactura() + "-" + System.currentTimeMillis();
        nota.setReferenceCode(referenceCode);

        VentaRemota venta = ventaConsultaGateway.buscarVenta(factura.getVentaId(), empresaId);
        ClienteRemoto cliente = venta != null && venta.getClienteId() != null
                ? clienteConsultaGateway.buscarCliente(venta.getClienteId(), empresaId)
                : ClienteRemoto.consumidorFinalGenerico();
        EmpresaRemota empresa = empresaConsultaGateway.buscarEmpresa(empresaId);

        try {
            nota.setFechaEnvio(LocalDateTime.now());
            FacturaElectronicaGateway.ResultadoEmision resultado = facturaElectronicaGateway.emitirNotaDebito(
                    config, cliente, empresa, nota, referenceCode);

            nota.setNumeroNota(resultado.numeroDocumento());
            nota.setCude(resultado.cufeOCude());
            nota.setQrUrl(resultado.qrUrl());
            nota.setUrlDocumento(resultado.urlDocumento());
            nota.setAmbiente(resultado.ambiente());
            nota.setRespuestaDian(resultado.mensaje());
            nota.setEstado(resultado.aceptada() ? "ACEPTADA" : "RECHAZADA");
        } catch (RuntimeException e) {
            nota.setRespuestaDian(e.getMessage());
            notaDebitoGateway.guardar(nota);
            throw new RuntimeException("Factus rechazó la nota débito: " + e.getMessage());
        }

        if (!"ACEPTADA".equals(nota.getEstado())) {
            notaDebitoGateway.guardar(nota);
            throw new RuntimeException("Factus no validó la nota débito: " + nota.getRespuestaDian());
        }

        return notaDebitoGateway.guardar(nota);
    }

    private double safe(Double v) { return v != null ? v : 0.0; }
    private double redondear(double v) { return Math.round(v * 100.0) / 100.0; }
}
