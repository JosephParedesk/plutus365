package com.pos_backend.venta.domain.usecase;

import com.pos_backend.venta.domain.model.NotaDebitoVenta;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.gateway.NotaDebitoVentaGateway;
import com.pos_backend.venta.domain.model.gateway.VentaGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class NotaDebitoVentaUseCase {

    private final NotaDebitoVentaGateway notaDebitoVentaGateway;
    private final VentaGateway ventaGateway;

    public List<NotaDebitoVenta> listar(String empresaId) {
        return notaDebitoVentaGateway.listar(empresaId);
    }

    public List<NotaDebitoVenta> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta) {
        return notaDebitoVentaGateway.buscarPorRango(empresaId, desde, hasta);
    }

    public NotaDebitoVenta buscarPorId(Long id, String empresaId) {
        NotaDebitoVenta n = notaDebitoVentaGateway.buscarPorId(id, empresaId);
        if (n == null) throw new NoSuchElementException("Nota débito no encontrada");
        return n;
    }

    public NotaDebitoVenta registrar(NotaDebitoVenta nota, String empresaId, String usuario) {
        if (nota.getVentaReferenciaId() == null)
            throw new RuntimeException("Indica a qué venta aplica la nota débito");
        if (nota.getValor() == null || nota.getValor() <= 0)
            throw new RuntimeException("El valor debe ser mayor a 0");
        if (nota.getConcepto() == null || nota.getConcepto().isBlank())
            throw new RuntimeException("Indica el motivo de la nota débito");

        Venta venta = ventaGateway.buscarVentaPorId(nota.getVentaReferenciaId(), empresaId);
        if (venta == null) throw new NoSuchElementException("La venta referenciada no existe");
        if (!"REGISTRADA".equals(venta.getEstado()))
            throw new RuntimeException("No puedes aplicar una nota débito a una venta anulada");

        nota.setEmpresaId(empresaId);
        nota.setCreadoPor(usuario);
        nota.setFecha(LocalDateTime.now());
        nota.setEstado("REGISTRADA");
        nota.setNumeroVentaReferencia(venta.getNumeroVenta());
        nota.setClienteId(venta.getClienteId());
        nota.setClienteNombre(venta.getClienteNombre());
        nota.setNumeroNotaDebito(notaDebitoVentaGateway.generarSiguienteNumero(empresaId));

        // El cargo de la nota débito se suma a lo que el cliente ya debía de esta venta.
        double saldoActual = venta.getSaldoPendiente() != null ? venta.getSaldoPendiente() : 0;
        venta.setSaldoPendiente(redondear(saldoActual + nota.getValor()));
        venta.setTieneCreditoCliente(true);
        ventaGateway.guardarVenta(venta);

        return notaDebitoVentaGateway.guardar(nota);
    }

    public void anular(Long id, String empresaId) {
        NotaDebitoVenta n = buscarPorId(id, empresaId);
        if ("ANULADA".equals(n.getEstado()))
            throw new RuntimeException("Esta nota débito ya está anulada");

        // Al anular, se revierte el cargo sobre la venta.
        Venta venta = ventaGateway.buscarVentaPorId(n.getVentaReferenciaId(), empresaId);
        if (venta != null) {
            double saldoActual = venta.getSaldoPendiente() != null ? venta.getSaldoPendiente() : 0;
            venta.setSaldoPendiente(redondear(Math.max(0, saldoActual - n.getValor())));
            ventaGateway.guardarVenta(venta);
        }

        n.setEstado("ANULADA");
        notaDebitoVentaGateway.guardar(n);
    }

    private double redondear(double v) { return Math.round(v); }
}
