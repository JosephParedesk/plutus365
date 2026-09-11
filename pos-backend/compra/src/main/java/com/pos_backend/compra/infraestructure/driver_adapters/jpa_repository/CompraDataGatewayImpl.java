package com.pos_backend.compra.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.compra.domain.model.Compra;
import com.pos_backend.compra.domain.model.gateway.CompraGateway;
import com.pos_backend.compra.infraestructure.mapper.CompraMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class CompraDataGatewayImpl implements CompraGateway {

    private final CompraDataJpaRepository compraDataJpaRepository;
    private final CompraMapper compraMapper;

    @Override
    public Compra guardarCompra(Compra compra) {
        CompraData saved = compraDataJpaRepository.save(compraMapper.toCompraData(compra));
        return compraMapper.toCompra(saved);
    }

    @Override
    public Compra buscarCompraPorId(Long compraId, String empresaId) {
        return compraDataJpaRepository.findByCompraIdAndEmpresaId(compraId, empresaId)
                .map(compraMapper::toCompra)
                .orElse(null);
    }

    @Override
    public List<Compra> listarCompras(String empresaId) {
        return compraDataJpaRepository.findByEmpresaId(empresaId)
                .stream().map(compraMapper::toCompra).toList();
    }

    @Override
    public List<Compra> buscarConFiltros(
            String empresaId, Long proveedorId, String tipoTransaccion,
            LocalDate fechaInicio, LocalDate fechaFin, String creadoPor) {

        List<CompraData> base;

        if (fechaInicio != null && fechaFin != null) {
            base = compraDataJpaRepository.findByEmpresaIdAndFechaElaboracionBetween(
                    empresaId, fechaInicio, fechaFin);
        } else {
            base = compraDataJpaRepository.findByEmpresaId(empresaId);
        }

        return base.stream()
                .filter(c -> proveedorId == null || proveedorId.equals(c.getProveedorId()))
                .filter(c -> tipoTransaccion == null || tipoTransaccion.equals(c.getTipoTransaccion()))
                .filter(c -> creadoPor == null || creadoPor.equalsIgnoreCase(c.getCreadoPor()))
                .map(compraMapper::toCompra)
                .toList();
    }

    @Override
    public void anularCompra(Long compraId, String empresaId) {
        compraDataJpaRepository.findByCompraIdAndEmpresaId(compraId, empresaId)
                .ifPresent(data -> {
                    data.setEstado("ANULADA");
                    compraDataJpaRepository.save(data);
                });
    }

    @Override
    public String generarSiguienteNumero(String empresaId, String tipoTransaccion) {
        long total = compraDataJpaRepository.countByEmpresaIdAndTipoTransaccion(empresaId, tipoTransaccion) + 1;
        String prefijo = switch (tipoTransaccion) {
            case "FACTURA_COMPRA" -> "FC";
            case "DOCUMENTO_SOPORTE" -> "DS";
            case "FACTURA_COMPRA_ELECTRONICA" -> "FCE";
            case "ORDEN_COMPRA" -> "OC";
            case "RECIBO_PAGO" -> "RP";
            case "NOTA_DEBITO" -> "ND";
            case "AJUSTE_CARTERA" -> "AC";
            default -> "CO";
        };
        return prefijo + "-" + String.format("%05d", total);
    }

    @Override
    public List<Compra> proximasAVencer(String empresaId, int dias) {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(dias);
        return compraDataJpaRepository
                .findByEmpresaIdAndTieneCreditoProveedorTrueAndSaldoPendienteGreaterThanAndFechaVencimientoCreditoBetweenAndEstadoNot(
                        empresaId, 0.0, hoy, limite, "ANULADA")
                .stream().map(compraMapper::toCompra).toList();
    }
}