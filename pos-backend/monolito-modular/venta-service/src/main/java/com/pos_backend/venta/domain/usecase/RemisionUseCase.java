package com.pos_backend.venta.domain.usecase;

import com.pos_backend.venta.domain.model.FormaPago;
import com.pos_backend.venta.domain.model.Remision;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.VentaItem;
import com.pos_backend.venta.domain.model.gateway.RemisionGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@RequiredArgsConstructor
public class RemisionUseCase {

    private final RemisionGateway remisionGateway;
    private final VentaUseCase ventaUseCase;

    private static final Set<String> ESTADOS_VALIDOS = Set.of("BORRADOR", "ENTREGADA", "FACTURADA", "ANULADA");

    public List<Remision> listar(String empresaId) {
        return remisionGateway.listar(empresaId);
    }

    public List<Remision> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta) {
        return remisionGateway.buscarPorRango(empresaId, desde, hasta);
    }

    public Remision buscarPorId(Long id, String empresaId) {
        Remision r = remisionGateway.buscarPorId(id, empresaId);
        if (r == null) throw new NoSuchElementException("Remisión no encontrada");
        return r;
    }

    public Remision crear(Remision remision, String empresaId, String creadoPor) {
        if (remision.getItems() == null || remision.getItems().isEmpty())
            throw new RuntimeException("La remisión debe tener al menos un producto");

        remision.setEmpresaId(empresaId);
        remision.setCreadoPor(creadoPor);
        remision.setFecha(LocalDateTime.now());
        if (remision.getEstado() == null) remision.setEstado("BORRADOR");

        calcularTotales(remision);
        remision.setNumeroRemision(remisionGateway.generarSiguienteNumero(empresaId));
        return remisionGateway.guardar(remision);
    }

    public Remision actualizar(Long id, Remision cambios, String empresaId) {
        Remision existente = buscarPorId(id, empresaId);
        if ("FACTURADA".equals(existente.getEstado()))
            throw new RuntimeException("No puedes editar una remisión que ya se facturó");
        if ("ANULADA".equals(existente.getEstado()))
            throw new RuntimeException("No puedes editar una remisión anulada");

        if (cambios.getItems() != null && !cambios.getItems().isEmpty()) existente.setItems(cambios.getItems());
        if (cambios.getClienteId() != null) existente.setClienteId(cambios.getClienteId());
        if (cambios.getClienteNombre() != null) existente.setClienteNombre(cambios.getClienteNombre());
        if (cambios.getLugarEntrega() != null) existente.setLugarEntrega(cambios.getLugarEntrega());
        if (cambios.getTransportador() != null) existente.setTransportador(cambios.getTransportador());
        if (cambios.getObservaciones() != null) existente.setObservaciones(cambios.getObservaciones());
        if (cambios.getDescuentoTotal() != null) existente.setDescuentoTotal(cambios.getDescuentoTotal());
        if (cambios.getCentroCostoId() != null) {
            existente.setCentroCostoId(cambios.getCentroCostoId());
            existente.setCentroCostoNombre(cambios.getCentroCostoNombre());
        }

        calcularTotales(existente);
        return remisionGateway.guardar(existente);
    }

    public Remision cambiarEstado(Long id, String nuevoEstado, String empresaId) {
        Remision r = buscarPorId(id, empresaId);
        if (!ESTADOS_VALIDOS.contains(nuevoEstado))
            throw new RuntimeException("Estado no válido: " + nuevoEstado);
        if ("FACTURADA".equals(r.getEstado()))
            throw new RuntimeException("La remisión ya se facturó; su estado no se puede cambiar");
        if ("FACTURADA".equals(nuevoEstado))
            throw new RuntimeException("Para facturar una remisión usa la opción de convertir en venta, no el cambio de estado");

        r.setEstado(nuevoEstado);
        return remisionGateway.guardar(r);
    }

    /**
     * Convierte la remisión en venta real. Igual que en Cotización: aquí SÍ se
     * descuenta inventario y se genera el asiento contable, porque lo hace
     * VentaUseCase — no se duplica esa lógica.
     */
    public Venta convertirEnVenta(Long id, List<FormaPago> formasPago, String empresaId, String usuario) {
        Remision r = buscarPorId(id, empresaId);

        if ("FACTURADA".equals(r.getEstado()))
            throw new RuntimeException("Esta remisión ya se facturó como la venta #" + r.getVentaGeneradaId());
        if ("ANULADA".equals(r.getEstado()))
            throw new RuntimeException("No puedes facturar una remisión anulada");
        if (formasPago == null || formasPago.isEmpty())
            throw new RuntimeException("Debes indicar al menos una forma de pago");

        Venta venta = new Venta();
        venta.setClienteId(r.getClienteId());
        venta.setClienteNombre(r.getClienteNombre());
        venta.setItems(r.getItems());
        venta.setFormasPago(formasPago);
        venta.setDescuentoTotal(r.getDescuentoTotal());
        venta.setTotalIva(r.getTotalIva());
        venta.setCentroCostoId(r.getCentroCostoId());
        venta.setCentroCostoNombre(r.getCentroCostoNombre());

        Venta generada = ventaUseCase.registrarVenta(venta, empresaId, usuario);

        // La venta ya quedó firme (con stock y contabilidad aplicados);
        // solo entonces se marca la remisión como facturada.
        r.setEstado("FACTURADA");
        r.setVentaGeneradaId(generada.getVentaId());
        remisionGateway.guardar(r);

        return generada;
    }

    private void calcularTotales(Remision r) {
        double subtotal = 0;
        for (VentaItem item : r.getItems()) {
            double valor = item.getCantidad() * safe(item.getPrecioUnitario()) - safe(item.getDescuento());
            item.setValorTotal(redondear(valor));
            subtotal += valor;
        }
        r.setSubtotal(redondear(subtotal));
        double descuento = safe(r.getDescuentoTotal());
        double iva = safe(r.getTotalIva());
        r.setTotal(redondear(subtotal - descuento + iva));
    }

    private double safe(Double v) { return v != null ? v : 0.0; }
    private double redondear(double v) { return Math.round(v); }
}
