package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.venta.domain.model.NotaDebitoVenta;
import com.pos_backend.venta.domain.model.gateway.NotaDebitoVentaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository @RequiredArgsConstructor
public class NotaDebitoVentaDataGatewayImpl implements NotaDebitoVentaGateway {

    private final NotaDebitoVentaDataJpaRepository repository;

    @Override
    public NotaDebitoVenta guardar(NotaDebitoVenta n) {
        NotaDebitoVentaData d = new NotaDebitoVentaData();
        BeanUtils.copyProperties(n, d);
        return toDomain(repository.save(d));
    }

    @Override
    public NotaDebitoVenta buscarPorId(Long id, String empresaId) {
        return repository.findByNotaDebitoIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override
    public List<NotaDebitoVenta> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByNotaDebitoIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<NotaDebitoVenta> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta) {
        return repository.findByEmpresaIdAndFechaBetween(empresaId, desde.atStartOfDay(), hasta.atTime(23, 59, 59))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public String generarSiguienteNumero(String empresaId) {
        return "NDV-" + String.format("%05d", repository.countByEmpresaId(empresaId) + 1);
    }

    private NotaDebitoVenta toDomain(NotaDebitoVentaData d) {
        NotaDebitoVenta n = new NotaDebitoVenta();
        BeanUtils.copyProperties(d, n);
        return n;
    }
}
