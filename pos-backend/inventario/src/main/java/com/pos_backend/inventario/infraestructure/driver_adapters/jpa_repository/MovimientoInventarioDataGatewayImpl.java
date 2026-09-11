package com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.inventario.domain.model.MovimientoInventario;
import com.pos_backend.inventario.domain.model.gateway.MovimientoInventarioGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository @RequiredArgsConstructor
public class MovimientoInventarioDataGatewayImpl implements MovimientoInventarioGateway {

    private final MovimientoInventarioDataJpaRepository repository;

    @Override
    public MovimientoInventario guardar(MovimientoInventario m) {
        MovimientoInventarioData d = new MovimientoInventarioData();
        BeanUtils.copyProperties(m, d);
        return toDomain(repository.save(d));
    }

    @Override
    public List<MovimientoInventario> listarPorSku(String sku, String empresaId) {
        return repository.findByEmpresaIdAndSkuOrderByFechaAsc(empresaId, sku).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MovimientoInventario> listarPorRango(String empresaId, LocalDateTime desde, LocalDateTime hasta) {
        return repository.findByEmpresaIdAndFechaBetweenOrderByFechaAsc(empresaId, desde, hasta)
                .stream().map(this::toDomain).toList();
    }

    private MovimientoInventario toDomain(MovimientoInventarioData d) {
        MovimientoInventario m = new MovimientoInventario();
        BeanUtils.copyProperties(d, m);
        return m;
    }
}
