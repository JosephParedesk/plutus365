package com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.contabilidad.domain.model.CentroCosto;
import com.pos_backend.contabilidad.domain.model.gateway.CentroCostoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository @RequiredArgsConstructor
public class CentroCostoDataGatewayImpl implements CentroCostoGateway {

    private final CentroCostoDataJpaRepository repository;

    @Override public CentroCosto guardar(CentroCosto c) {
        CentroCostoData d = new CentroCostoData();
        BeanUtils.copyProperties(c, d);
        return toDomain(repository.save(d));
    }
    @Override public CentroCosto buscarPorId(Long id, String empresaId) {
        return repository.findByCentroCostoIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }
    @Override public boolean existeCodigo(String codigo, String empresaId) {
        return repository.existsByCodigoAndEmpresaId(codigo, empresaId);
    }
    @Override public List<CentroCosto> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByCodigoAsc(empresaId).stream().map(this::toDomain).toList();
    }
    @Override public void eliminar(Long id, String empresaId) {
        repository.findByCentroCostoIdAndEmpresaId(id, empresaId).ifPresent(repository::delete);
    }
    private CentroCosto toDomain(CentroCostoData d) {
        CentroCosto c = new CentroCosto();
        BeanUtils.copyProperties(d, c);
        return c;
    }
}
