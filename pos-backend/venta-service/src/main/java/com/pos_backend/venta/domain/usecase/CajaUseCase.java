package com.pos_backend.venta.domain.usecase;

import com.pos_backend.venta.domain.model.CajaSesion;
import com.pos_backend.venta.domain.model.FormaPago;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.gateway.CajaGateway;
import com.pos_backend.venta.domain.model.gateway.VentaGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class CajaUseCase {

    private final CajaGateway cajaGateway;
    private final VentaGateway ventaGateway;

    public CajaSesion obtenerAbierta(String empresaId) {
        return cajaGateway.buscarAbierta(empresaId);
    }

    public List<CajaSesion> listar(String empresaId) {
        return cajaGateway.listar(empresaId);
    }

    public CajaSesion abrir(Double montoApertura, String empresaId, String usuario) {
        if (cajaGateway.buscarAbierta(empresaId) != null)
            throw new RuntimeException("Ya hay una caja abierta. Ciérrala antes de abrir una nueva.");
        if (montoApertura == null || montoApertura < 0)
            throw new RuntimeException("El monto de apertura no puede ser negativo");

        CajaSesion sesion = new CajaSesion();
        sesion.setEmpresaId(empresaId);
        sesion.setFechaApertura(LocalDateTime.now());
        sesion.setMontoApertura(montoApertura);
        sesion.setEstado("ABIERTA");
        sesion.setUsuarioApertura(usuario);

        return cajaGateway.guardar(sesion);
    }

    /** Lo que debería haber en efectivo en este momento, con la caja todavía abierta. */
    public ResumenCaja calcularEsperado(String empresaId) {
        CajaSesion abierta = cajaGateway.buscarAbierta(empresaId);
        if (abierta == null)
            throw new NoSuchElementException("No hay una caja abierta");

        return calcularResumen(abierta, LocalDateTime.now());
    }

    public CajaSesion cerrar(Double montoDeclarado, String observaciones, String empresaId, String usuario) {
        CajaSesion abierta = cajaGateway.buscarAbierta(empresaId);
        if (abierta == null)
            throw new NoSuchElementException("No hay una caja abierta para cerrar");
        if (montoDeclarado == null || montoDeclarado < 0)
            throw new RuntimeException("El monto contado no puede ser negativo");

        LocalDateTime ahora = LocalDateTime.now();
        ResumenCaja resumen = calcularResumen(abierta, ahora);

        abierta.setFechaCierre(ahora);
        abierta.setMontoCierreDeclarado(montoDeclarado);
        abierta.setMontoCierreCalculado(resumen.efectivoEsperado());
        abierta.setDiferencia(montoDeclarado - resumen.efectivoEsperado());
        abierta.setTotalVentasEfectivo(resumen.totalEfectivo());
        abierta.setTotalVentasOtros(resumen.totalOtros());
        abierta.setNumeroVentas(resumen.numeroVentas());
        abierta.setEstado("CERRADA");
        abierta.setUsuarioCierre(usuario);
        abierta.setObservaciones(observaciones);

        return cajaGateway.guardar(abierta);
    }

    private ResumenCaja calcularResumen(CajaSesion sesion, LocalDateTime hasta) {
        List<Venta> ventas = ventaGateway.listarPorRangoExacto(sesion.getEmpresaId(), sesion.getFechaApertura(), hasta)
                .stream().filter(v -> "REGISTRADA".equals(v.getEstado())).toList();

        double totalEfectivo = 0, totalOtros = 0;
        for (Venta v : ventas) {
            if (v.getFormasPago() == null) continue;
            for (FormaPago fp : v.getFormasPago()) {
                if ("EFECTIVO".equals(fp.getMetodo())) totalEfectivo += fp.getValor();
                else totalOtros += fp.getValor();
            }
        }

        double efectivoEsperado = sesion.getMontoApertura() + totalEfectivo;
        return new ResumenCaja(efectivoEsperado, totalEfectivo, totalOtros, ventas.size());
    }

    public record ResumenCaja(double efectivoEsperado, double totalEfectivo, double totalOtros, int numeroVentas) {}
}
