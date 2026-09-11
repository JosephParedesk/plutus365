package com.pos_backend.venta.domain.usecase;

import com.pos_backend.venta.domain.model.Cotizacion;
import com.pos_backend.venta.domain.model.FormaPago;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.VentaItem;
import com.pos_backend.venta.domain.model.gateway.CotizacionGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@RequiredArgsConstructor
public class CotizacionUseCase {

    private final CotizacionGateway cotizacionGateway;
    private final VentaUseCase ventaUseCase;

    private static final Set<String> ESTADOS_VALIDOS =
            Set.of("BORRADOR", "ENVIADA", "APROBADA", "RECHAZADA", "VENCIDA", "CONVERTIDA");

    public List<Cotizacion> listar(String empresaId) {
        // El vencimiento se evalúa al leer: no hace falta un proceso programado
        // que recorra la tabla marcando vencidas.
        return cotizacionGateway.listar(empresaId).stream()
                .map(this::marcarVencidaSiAplica)
                .toList();
    }

    public List<Cotizacion> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta) {
        return cotizacionGateway.buscarPorRango(empresaId, desde, hasta).stream()
                .map(this::marcarVencidaSiAplica)
                .toList();
    }

    public Cotizacion buscarPorId(Long id, String empresaId) {
        Cotizacion c = cotizacionGateway.buscarPorId(id, empresaId);
        if (c == null) throw new NoSuchElementException("Cotización no encontrada");
        return marcarVencidaSiAplica(c);
    }

    public Cotizacion crear(Cotizacion cotizacion, String empresaId, String creadoPor) {
        if (cotizacion.getItems() == null || cotizacion.getItems().isEmpty())
            throw new RuntimeException("La cotización debe tener al menos un producto");

        cotizacion.setEmpresaId(empresaId);
        cotizacion.setCreadoPor(creadoPor);
        cotizacion.setFecha(LocalDateTime.now());
        if (cotizacion.getEstado() == null) cotizacion.setEstado("BORRADOR");
        // Por defecto una cotización vale 15 días; es lo habitual en el comercio.
        if (cotizacion.getFechaVencimiento() == null)
            cotizacion.setFechaVencimiento(LocalDate.now().plusDays(15));

        // Si no se indicó asesor, se asume quien la está elaborando.
        if (cotizacion.getAsesorNombre() == null || cotizacion.getAsesorNombre().isBlank())
            cotizacion.setAsesorNombre(creadoPor);

        calcularTotales(cotizacion);
        cotizacion.setNumeroCotizacion(cotizacionGateway.generarSiguienteNumero(empresaId));
        return cotizacionGateway.guardar(cotizacion);
    }

    public Cotizacion actualizar(Long id, Cotizacion cambios, String empresaId) {
        Cotizacion existente = buscarPorId(id, empresaId);
        if ("CONVERTIDA".equals(existente.getEstado()))
            throw new RuntimeException("No puedes editar una cotización que ya se convirtió en venta");

        if (cambios.getItems() != null && !cambios.getItems().isEmpty()) existente.setItems(cambios.getItems());
        if (cambios.getClienteId() != null) existente.setClienteId(cambios.getClienteId());
        if (cambios.getClienteNombre() != null) existente.setClienteNombre(cambios.getClienteNombre());
        if (cambios.getClienteCorreo() != null) existente.setClienteCorreo(cambios.getClienteCorreo());
        if (cambios.getFechaVencimiento() != null) existente.setFechaVencimiento(cambios.getFechaVencimiento());
        if (cambios.getObservaciones() != null) existente.setObservaciones(cambios.getObservaciones());
        if (cambios.getLugarEmision() != null) existente.setLugarEmision(cambios.getLugarEmision());
        if (cambios.getContactoNombre() != null) existente.setContactoNombre(cambios.getContactoNombre());
        if (cambios.getContactoCargo() != null) existente.setContactoCargo(cambios.getContactoCargo());
        if (cambios.getFormaPago() != null) existente.setFormaPago(cambios.getFormaPago());
        if (cambios.getTiempoEntrega() != null) existente.setTiempoEntrega(cambios.getTiempoEntrega());
        if (cambios.getLugarEntrega() != null) existente.setLugarEntrega(cambios.getLugarEntrega());
        if (cambios.getTransporte() != null) existente.setTransporte(cambios.getTransporte());
        if (cambios.getTiempoFabricacion() != null) existente.setTiempoFabricacion(cambios.getTiempoFabricacion());
        if (cambios.getInstalacion() != null) existente.setInstalacion(cambios.getInstalacion());
        if (cambios.getCapacitacion() != null) existente.setCapacitacion(cambios.getCapacitacion());
        if (cambios.getGarantiaTiempo() != null) existente.setGarantiaTiempo(cambios.getGarantiaTiempo());
        if (cambios.getGarantiaCubre() != null) existente.setGarantiaCubre(cambios.getGarantiaCubre());
        if (cambios.getGarantiaNoCubre() != null) existente.setGarantiaNoCubre(cambios.getGarantiaNoCubre());
        if (cambios.getGarantiaComoHacerEfectiva() != null) existente.setGarantiaComoHacerEfectiva(cambios.getGarantiaComoHacerEfectiva());
        if (cambios.getAsesorNombre() != null) existente.setAsesorNombre(cambios.getAsesorNombre());
        if (cambios.getAsesorCargo() != null) existente.setAsesorCargo(cambios.getAsesorCargo());
        if (cambios.getAsesorTelefono() != null) existente.setAsesorTelefono(cambios.getAsesorTelefono());
        if (cambios.getAsesorCorreo() != null) existente.setAsesorCorreo(cambios.getAsesorCorreo());
        if (cambios.getDescuentoTotal() != null) existente.setDescuentoTotal(cambios.getDescuentoTotal());
        if (cambios.getCentroCostoId() != null) {
            existente.setCentroCostoId(cambios.getCentroCostoId());
            existente.setCentroCostoNombre(cambios.getCentroCostoNombre());
        }

        calcularTotales(existente);
        return cotizacionGateway.guardar(existente);
    }

    public Cotizacion cambiarEstado(Long id, String nuevoEstado, String empresaId) {
        Cotizacion c = buscarPorId(id, empresaId);
        if (!ESTADOS_VALIDOS.contains(nuevoEstado))
            throw new RuntimeException("Estado no válido: " + nuevoEstado);
        if ("CONVERTIDA".equals(c.getEstado()))
            throw new RuntimeException("La cotización ya se convirtió en venta; su estado no se puede cambiar");
        if ("CONVERTIDA".equals(nuevoEstado))
            throw new RuntimeException("Para convertir una cotización usa la opción de convertir en venta, no el cambio de estado");

        c.setEstado(nuevoEstado);
        return cotizacionGateway.guardar(c);
    }

    /**
     * Convierte la cotización en venta real. Aquí SÍ se descuenta inventario y
     * se genera el asiento contable, porque lo hace VentaUseCase — que ya tiene
     * toda esa lógica con su compensación. No se duplica nada.
     */
    public Venta convertirEnVenta(Long id, List<FormaPago> formasPago, String empresaId, String usuario) {
        Cotizacion c = buscarPorId(id, empresaId);

        if ("CONVERTIDA".equals(c.getEstado()))
            throw new RuntimeException("Esta cotización ya se convirtió en la venta #" + c.getVentaGeneradaId());
        if ("RECHAZADA".equals(c.getEstado()))
            throw new RuntimeException("No puedes convertir una cotización rechazada");
        if (formasPago == null || formasPago.isEmpty())
            throw new RuntimeException("Debes indicar al menos una forma de pago");

        Venta venta = new Venta();
        venta.setClienteId(c.getClienteId());
        venta.setClienteNombre(c.getClienteNombre());
        venta.setItems(c.getItems());
        venta.setFormasPago(formasPago);
        venta.setDescuentoTotal(c.getDescuentoTotal());
        venta.setTotalIva(c.getTotalIva());
        venta.setCentroCostoId(c.getCentroCostoId());
        venta.setCentroCostoNombre(c.getCentroCostoNombre());

        Venta generada = ventaUseCase.registrarVenta(venta, empresaId, usuario);

        // La venta ya quedó firme (con stock y contabilidad aplicados);
        // solo entonces se marca la cotización como convertida.
        c.setEstado("CONVERTIDA");
        c.setVentaGeneradaId(generada.getVentaId());
        cotizacionGateway.guardar(c);

        return generada;
    }

    /** Una cotización enviada que pasó su fecha de vencimiento se muestra como VENCIDA. */
    private Cotizacion marcarVencidaSiAplica(Cotizacion c) {
        boolean pendiente = "ENVIADA".equals(c.getEstado()) || "BORRADOR".equals(c.getEstado());
        if (pendiente && c.getFechaVencimiento() != null && c.getFechaVencimiento().isBefore(LocalDate.now()))
            c.setEstado("VENCIDA");
        return c;
    }

    private void calcularTotales(Cotizacion c) {
        double subtotal = 0;
        for (VentaItem item : c.getItems()) {
            double valor = item.getCantidad() * safe(item.getPrecioUnitario()) - safe(item.getDescuento());
            item.setValorTotal(redondear(valor));
            subtotal += valor;
        }
        c.setSubtotal(redondear(subtotal));
        double descuento = safe(c.getDescuentoTotal());
        double iva = safe(c.getTotalIva());
        c.setTotal(redondear(subtotal - descuento + iva));
    }

    private double safe(Double v) { return v != null ? v : 0.0; }
    private double redondear(double v) { return Math.round(v); }
}
