package com.pos_backend.venta.domain.usecase;

import com.pos_backend.venta.domain.model.ReciboCaja;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.gateway.ReciboCajaGateway;
import com.pos_backend.venta.domain.model.gateway.ReciboContabilidadGateway;
import com.pos_backend.venta.domain.model.gateway.VentaGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class ReciboCajaUseCase {

    private final ReciboCajaGateway reciboCajaGateway;
    private final VentaGateway ventaGateway;
    private final ReciboContabilidadGateway contabilidadGateway;

    public List<ReciboCaja> listar(String empresaId) {
        return reciboCajaGateway.listar(empresaId);
    }

    public List<ReciboCaja> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta) {
        return reciboCajaGateway.buscarPorRango(empresaId, desde, hasta);
    }

    public ReciboCaja buscarPorId(Long id, String empresaId) {
        ReciboCaja r = reciboCajaGateway.buscarPorId(id, empresaId);
        if (r == null) throw new NoSuchElementException("Recibo de caja no encontrado");
        return r;
    }

    /** Facturas del cliente que todavía tienen saldo por cobrar. */
    public List<Venta> carteraPendiente(Long clienteId, String empresaId) {
        return ventaGateway.listarVentas(empresaId).stream()
                .filter(v -> "REGISTRADA".equals(v.getEstado()))
                .filter(v -> v.getSaldoPendiente() != null && v.getSaldoPendiente() > 0)
                .filter(v -> clienteId == null || clienteId.equals(v.getClienteId()))
                .toList();
    }

    public ReciboCaja registrar(ReciboCaja recibo, String empresaId, String usuario) {
        if (recibo.getTotalRecibido() == null || recibo.getTotalRecibido() <= 0)
            throw new RuntimeException("El valor recibido debe ser mayor a 0");

        recibo.setEmpresaId(empresaId);
        recibo.setCreadoPor(usuario);
        recibo.setFecha(LocalDateTime.now());
        if (recibo.getFechaRecibido() == null) recibo.setFechaRecibido(LocalDate.now());
        if (recibo.getTipoRecibo() == null) recibo.setTipoRecibo("ABONO_CARTERA");
        recibo.setEstado("REGISTRADO");

        List<Venta> ventasAfectadas = new ArrayList<>();

        if ("ABONO_CARTERA".equals(recibo.getTipoRecibo())) {
            if (recibo.getAplicaciones() == null || recibo.getAplicaciones().isEmpty())
                throw new RuntimeException("Indica a qué facturas se aplica el abono");

            double sumaAplicada = 0;
            for (ReciboCaja.AplicacionCobro ap : recibo.getAplicaciones()) {
                Venta venta = ventaGateway.buscarVentaPorId(ap.getVentaId(), empresaId);
                if (venta == null)
                    throw new NoSuchElementException("La venta " + ap.getVentaId() + " no existe");

                double saldo = venta.getSaldoPendiente() != null ? venta.getSaldoPendiente() : 0;
                if (saldo <= 0)
                    throw new RuntimeException("La venta " + venta.getNumeroVenta() + " ya está pagada");
                if (ap.getValorAplicado() == null || ap.getValorAplicado() <= 0)
                    throw new RuntimeException("El valor aplicado a cada factura debe ser mayor a 0");
                // No se puede abonar más de lo que se debe: evita saldos negativos.
                if (ap.getValorAplicado() > saldo)
                    throw new RuntimeException("A la venta " + venta.getNumeroVenta() + " le debes aplicar máximo "
                            + saldo + ", no " + ap.getValorAplicado());

                ap.setNumeroVenta(venta.getNumeroVenta());
                ap.setSaldoAnterior(saldo);
                ap.setSaldoNuevo(redondear(saldo - ap.getValorAplicado()));
                sumaAplicada += ap.getValorAplicado();

                venta.setSaldoPendiente(ap.getSaldoNuevo());
                ventasAfectadas.add(venta);
            }

            if (Math.abs(sumaAplicada - recibo.getTotalRecibido()) > 1.0)
                throw new RuntimeException("Lo aplicado a las facturas (" + sumaAplicada
                        + ") no coincide con el total recibido (" + recibo.getTotalRecibido() + ")");
        }

        // Se bajan los saldos solo después de validar todo.
        ventasAfectadas.forEach(ventaGateway::guardarVenta);

        recibo.setNumeroRecibo(reciboCajaGateway.generarSiguienteNumero(empresaId));
        ReciboCaja guardado = reciboCajaGateway.guardar(recibo);

        // Contabilización: entra plata, baja la cartera del cliente.
        try {
            contabilidadGateway.generarAsientoRecibo(guardado, empresaId);
        } catch (RuntimeException e) {
            // Compensación: se devuelven los saldos y se marca el recibo con error.
            for (ReciboCaja.AplicacionCobro ap : recibo.getAplicaciones() != null ? recibo.getAplicaciones() : List.<ReciboCaja.AplicacionCobro>of()) {
                Venta v = ventaGateway.buscarVentaPorId(ap.getVentaId(), empresaId);
                if (v != null) {
                    v.setSaldoPendiente(ap.getSaldoAnterior());
                    ventaGateway.guardarVenta(v);
                }
            }
            guardado.setEstado("ERROR_CONTABILIZACION");
            reciboCajaGateway.guardar(guardado);
            throw new RuntimeException("El recibo no se pudo contabilizar y fue revertido: " + e.getMessage());
        }

        return guardado;
    }

    public void anular(Long id, String empresaId) {
        ReciboCaja r = buscarPorId(id, empresaId);
        if ("ANULADO".equals(r.getEstado()))
            throw new RuntimeException("Este recibo ya está anulado");

        // Al anular, la deuda vuelve a la factura.
        if (r.getAplicaciones() != null) {
            for (ReciboCaja.AplicacionCobro ap : r.getAplicaciones()) {
                Venta venta = ventaGateway.buscarVentaPorId(ap.getVentaId(), empresaId);
                if (venta != null) {
                    double saldo = venta.getSaldoPendiente() != null ? venta.getSaldoPendiente() : 0;
                    venta.setSaldoPendiente(redondear(saldo + ap.getValorAplicado()));
                    ventaGateway.guardarVenta(venta);
                }
            }
        }

        r.setEstado("ANULADO");
        reciboCajaGateway.guardar(r);
    }

    private double redondear(double v) { return Math.round(v); }
}
