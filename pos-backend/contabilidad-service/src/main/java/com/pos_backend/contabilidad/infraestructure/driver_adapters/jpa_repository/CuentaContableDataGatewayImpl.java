package com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.contabilidad.domain.model.CuentaContable;
import com.pos_backend.contabilidad.domain.model.gateway.CuentaContableGateway;
import com.pos_backend.contabilidad.infraestructure.mapper.CuentaContableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CuentaContableDataGatewayImpl implements CuentaContableGateway {

    private final CuentaContableDataJpaRepository repository;
    private final CuentaContableMapper mapper;

    @Override
    public List<CuentaContable> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByCodigoAsc(empresaId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public CuentaContable buscarPorCodigo(String codigo, String empresaId) {
        return repository.findByEmpresaIdAndCodigo(empresaId, codigo).map(mapper::toDomain).orElse(null);
    }

    @Override
    public CuentaContable guardar(CuentaContable cuenta) {
        // Si ya existe (empresaId+codigo), conserva el id para actualizar en vez de duplicar.
        repository.findByEmpresaIdAndCodigo(cuenta.getEmpresaId(), cuenta.getCodigo())
                .ifPresent(existente -> cuenta.setId(existente.getId()));
        return mapper.toDomain(repository.save(mapper.toData(cuenta)));
    }

    @Override
    public void guardarTodas(List<CuentaContable> cuentas) {
        repository.saveAll(cuentas.stream().map(mapper::toData).toList());
    }

    @Override
    public void eliminar(String codigo, String empresaId) {
        repository.deleteByEmpresaIdAndCodigo(empresaId, codigo);
    }

    @Override
    public boolean existeAlgunaCuenta(String empresaId) {
        return repository.existsByEmpresaId(empresaId);
    }

    @Override
    public boolean existeCodigo(String codigo, String empresaId) {
        return repository.existsByEmpresaIdAndCodigo(empresaId, codigo);
    }

    @Override
    public List<CuentaContable> listarHijas(String codigoPadre, String empresaId) {
        return repository.findByEmpresaIdAndCodigoPadre(empresaId, codigoPadre).stream().map(mapper::toDomain).toList();
    }
}
