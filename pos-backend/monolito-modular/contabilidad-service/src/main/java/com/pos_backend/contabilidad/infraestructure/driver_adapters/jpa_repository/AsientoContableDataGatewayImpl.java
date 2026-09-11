package com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.contabilidad.domain.model.AsientoContable;
import com.pos_backend.contabilidad.domain.model.gateway.AsientoContableGateway;
import com.pos_backend.contabilidad.infraestructure.mapper.AsientoContableMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class AsientoContableDataGatewayImpl implements AsientoContableGateway {

    private final AsientoContableDataJpaRepository repository;
    private final AsientoContableMapper mapper;

    @Override
    public AsientoContable guardar(AsientoContable asiento) {
        return mapper.toDomain(repository.save(mapper.toData(asiento)));
    }

    @Override
    public AsientoContable buscarPorId(Long asientoId, String empresaId) {
        return repository.findByAsientoIdAndEmpresaId(asientoId, empresaId).map(mapper::toDomain).orElse(null);
    }

    @Override
    public AsientoContable buscarPorOrigenYReferencia(String origen, Long referenciaId, String empresaId) {
        return repository.findByOrigenAndReferenciaIdAndEmpresaId(origen, referenciaId, empresaId)
                .map(mapper::toDomain).orElse(null);
    }

    @Override
    public List<AsientoContable> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByFechaDescAsientoIdDesc(empresaId).stream().map(mapper::toDomain).toList();
    }

    @Override
    public String generarSiguienteNumero(String empresaId) {
        long total = repository.countByEmpresaId(empresaId) + 1;
        return "AS-" + String.format("%05d", total);
    }
}
