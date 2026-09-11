package com.pos_backend.venta.domain.usecase;

import com.pos_backend.venta.domain.model.FacturaRecurrente;
import com.pos_backend.venta.domain.model.FormaPago;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.VentaItem;
import com.pos_backend.venta.domain.model.gateway.FacturaRecurrenteGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@RequiredArgsConstructor
public class FacturaRecurrenteUseCase {

    private final FacturaRecurrenteGateway recurrenteGateway;
    private final VentaUseCase ventaUseCase;

    private static final Set<String> PERIODICIDADES =
            Set.of("MENSUAL", "BIMESTRAL", "TRIMESTRAL", "SEMESTRAL", "ANUAL");

    public List<FacturaRecurrente> listar(String empresaId) {
        return recurrenteGateway.listar(empresaId);
    }

    public FacturaRecurrente buscarPorId(Long id, String empresaId) {
        FacturaRecurrente r = recurrenteGateway.buscarPorId(id, empresaId);
        if (r == null) throw new NoSuchElementException("Recurrencia no encontrada");
        return r;
    }

    /** Las que ya cumplieron su fecha y están esperando confirmación. */
    public List<FacturaRecurrente> pendientes(String empresaId) {
        LocalDate hoy = LocalDate.now();
        return recurrenteGateway.listar(empresaId).stream()
                .filter(r -> Boolean.TRUE.equals(r.getActiva()))
                .filter(r -> r.getProximaGeneracion() != null && !r.getProximaGeneracion().isAfter(hoy))
                .filter(r -> r.getFechaFin() == null || !r.getFechaFin().isBefore(hoy))
                .toList();
    }

    public FacturaRecurrente crear(FacturaRecurrente r, String empresaId, String creadoPor) {
        validar(r);
        r.setEmpresaId(empresaId);
        r.setCreadoPor(creadoPor);
        r.setActiva(r.getActiva() != null ? r.getActiva() : true);
        r.setVecesGeneradas(0);
        calcularTotales(r);

        // La primera generación es en la fecha de inicio, o en el próximo día
        // de cobro si la fecha de inicio ya pasó.
        r.setProximaGeneracion(r.getFechaInicio() != null && !r.getFechaInicio().isBefore(LocalDate.now())
                ? r.getFechaInicio()
                : siguienteFecha(LocalDate.now(), r.getPeriodicidad(), r.getDiaGeneracion()));

        return recurrenteGateway.guardar(r);
    }

    public FacturaRecurrente actualizar(Long id, FacturaRecurrente cambios, String empresaId) {
        FacturaRecurrente existente = buscarPorId(id, empresaId);

        if (cambios.getNombre() != null) existente.setNombre(cambios.getNombre());
        if (cambios.getItems() != null && !cambios.getItems().isEmpty()) existente.setItems(cambios.getItems());
        if (cambios.getDescuentoTotal() != null) existente.setDescuentoTotal(cambios.getDescuentoTotal());
        if (cambios.getTotalIva() != null) existente.setTotalIva(cambios.getTotalIva());
        if (cambios.getFechaFin() != null) existente.setFechaFin(cambios.getFechaFin());
        if (cambios.getActiva() != null) existente.setActiva(cambios.getActiva());
        if (cambios.getObservaciones() != null) existente.setObservaciones(cambios.getObservaciones());
        if (cambios.getMetodoPagoPredeterminado() != null)
            existente.setMetodoPagoPredeterminado(cambios.getMetodoPagoPredeterminado());

        // Cambiar la periodicidad recalcula la próxima fecha desde hoy.
        if (cambios.getPeriodicidad() != null && !cambios.getPeriodicidad().equals(existente.getPeriodicidad())) {
            if (!PERIODICIDADES.contains(cambios.getPeriodicidad()))
                throw new RuntimeException("Periodicidad no válida: " + cambios.getPeriodicidad());
            existente.setPeriodicidad(cambios.getPeriodicidad());
            existente.setProximaGeneracion(siguienteFecha(LocalDate.now(),
                    existente.getPeriodicidad(), existente.getDiaGeneracion()));
        }

        calcularTotales(existente);
        return recurrenteGateway.guardar(existente);
    }

    /**
     * Genera la venta de este período. Se llama cuando el usuario confirma,
     * no automáticamente. Reutiliza VentaUseCase, así que descuenta inventario
     * y contabiliza con toda su compensación.
     */
    public Venta generarAhora(Long id, String empresaId, String usuario) {
        FacturaRecurrente r = buscarPorId(id, empresaId);

        if (!Boolean.TRUE.equals(r.getActiva()))
            throw new RuntimeException("Esta recurrencia está pausada. Actívala antes de generar.");
        if (r.getFechaFin() != null && r.getFechaFin().isBefore(LocalDate.now()))
            throw new RuntimeException("Esta recurrencia ya terminó el " + r.getFechaFin());

        Venta venta = new Venta();
        venta.setClienteId(r.getClienteId());
        venta.setClienteNombre(r.getClienteNombre());
        venta.setItems(r.getItems());
        venta.setDescuentoTotal(r.getDescuentoTotal());
        venta.setTotalIva(r.getTotalIva());
        venta.setCentroCostoId(r.getCentroCostoId());
        venta.setCentroCostoNombre(r.getCentroCostoNombre());

        String metodo = r.getMetodoPagoPredeterminado() != null ? r.getMetodoPagoPredeterminado() : "TRANSFERENCIA";
        FormaPago formaPago = new FormaPago();
        formaPago.setMetodo(metodo);
        formaPago.setValor(r.getTotal());
        venta.setFormasPago(List.of(formaPago));

        Venta generada = ventaUseCase.registrarVenta(venta, empresaId, usuario);

        // Solo se avanza el calendario si la venta quedó firme.
        r.setUltimaGeneracion(LocalDate.now());
        r.setVecesGeneradas((r.getVecesGeneradas() != null ? r.getVecesGeneradas() : 0) + 1);
        LocalDate base = r.getProximaGeneracion() != null ? r.getProximaGeneracion() : LocalDate.now();
        r.setProximaGeneracion(siguienteFecha(base, r.getPeriodicidad(), r.getDiaGeneracion()));

        // Si con esta ya se pasó de la fecha fin, se desactiva sola.
        if (r.getFechaFin() != null && r.getProximaGeneracion().isAfter(r.getFechaFin()))
            r.setActiva(false);

        recurrenteGateway.guardar(r);
        return generada;
    }

    public FacturaRecurrente cambiarEstado(Long id, boolean activa, String empresaId) {
        FacturaRecurrente r = buscarPorId(id, empresaId);
        r.setActiva(activa);
        if (activa && r.getProximaGeneracion() != null && r.getProximaGeneracion().isBefore(LocalDate.now()))
            r.setProximaGeneracion(siguienteFecha(LocalDate.now(), r.getPeriodicidad(), r.getDiaGeneracion()));
        return recurrenteGateway.guardar(r);
    }

    public void eliminar(Long id, String empresaId) {
        buscarPorId(id, empresaId);
        recurrenteGateway.eliminar(id, empresaId);
    }

    /** Avanza según la periodicidad y ajusta al día de cobro configurado. */
    private LocalDate siguienteFecha(LocalDate desde, String periodicidad, Integer diaGeneracion) {
        int meses = switch (periodicidad != null ? periodicidad : "MENSUAL") {
            case "BIMESTRAL" -> 2;
            case "TRIMESTRAL" -> 3;
            case "SEMESTRAL" -> 6;
            case "ANUAL" -> 12;
            default -> 1;
        };
        LocalDate siguiente = desde.plusMonths(meses);
        if (diaGeneracion != null && diaGeneracion >= 1 && diaGeneracion <= 28)
            siguiente = siguiente.withDayOfMonth(diaGeneracion);
        return siguiente;
    }

    private void validar(FacturaRecurrente r) {
        if (r.getNombre() == null || r.getNombre().isBlank())
            throw new RuntimeException("Ponle un nombre para identificar la recurrencia");
        if (r.getItems() == null || r.getItems().isEmpty())
            throw new RuntimeException("La recurrencia debe tener al menos un producto o servicio");
        if (r.getPeriodicidad() == null || !PERIODICIDADES.contains(r.getPeriodicidad()))
            throw new RuntimeException("Periodicidad no válida. Debe ser: " + PERIODICIDADES);
        // Se limita a 28 para que exista en todos los meses, incluido febrero.
        if (r.getDiaGeneracion() != null && (r.getDiaGeneracion() < 1 || r.getDiaGeneracion() > 28))
            throw new RuntimeException("El día de cobro debe estar entre 1 y 28, para que exista en todos los meses");
        if (r.getFechaFin() != null && r.getFechaInicio() != null && r.getFechaFin().isBefore(r.getFechaInicio()))
            throw new RuntimeException("La fecha de fin no puede ser anterior a la de inicio");
    }

    private void calcularTotales(FacturaRecurrente r) {
        double subtotal = 0;
        for (VentaItem item : r.getItems()) {
            double valor = item.getCantidad() * safe(item.getPrecioUnitario()) - safe(item.getDescuento());
            item.setValorTotal((double) Math.round(valor));
            subtotal += valor;
        }
        r.setTotal((double) Math.round(subtotal - safe(r.getDescuentoTotal()) + safe(r.getTotalIva())));
    }

    private double safe(Double v) { return v != null ? v : 0.0; }
}
