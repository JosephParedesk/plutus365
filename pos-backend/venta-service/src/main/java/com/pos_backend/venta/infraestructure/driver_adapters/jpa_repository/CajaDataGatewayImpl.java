package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.venta.domain.model.CajaSesion;
import com.pos_backend.venta.domain.model.gateway.CajaGateway;
import com.pos_backend.venta.infraestructure.mapper.CajaSesionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class CajaDataGatewayImpl implements CajaGateway {

    private final CajaSesionDataJpaRepository repository;
    private final CajaSesionMapper mapper;

    @Override
    public CajaSesion guardar(CajaSesion sesion) {
        return mapper.toDomain(repository.save(mapper.toData(sesion)));
    }

    @Override
    public CajaSesion buscarAbierta(String empresaId) {
        return repository.findByEmpresaIdAndEstado(empresaId, "ABIERTA").map(mapper::toDomain).orElse(null);
    }

    @Override
    public CajaSesion buscarPorId(Long cajaId, String empresaId) {
        return repository.findByCajaIdAndEmpresaId(cajaId, empresaId).map(mapper::toDomain).orElse(null);
    }

    @Override
    public List<CajaSesion> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByFechaAperturaDesc(empresaId).stream().map(mapper::toDomain).toList();
    }
}
