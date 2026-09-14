package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.facturacion.domain.model.Factura;
import com.pos_backend.facturacion.domain.model.gateway.FacturaGateway;
import com.pos_backend.facturacion.infraestructure.mapper.FacturaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FacturaDataGatewayImpl implements FacturaGateway {

    private final FacturaDataJpaRepository repository;
    private final FacturaMapper mapper;

    @Override
    public Factura guardar(Factura factura) {
        return mapper.toDomain(repository.save(mapper.toData(factura)));
    }

    @Override
    public Factura buscarPorId(Long facturaId, String empresaId) {
        return repository.findByFacturaIdAndEmpresaId(facturaId, empresaId).map(mapper::toDomain).orElse(null);
    }

    @Override
    public Factura buscarPorVentaId(Long ventaId, String empresaId) {
        return repository.findFirstByVentaIdAndEmpresaIdOrderByFacturaIdDesc(ventaId, empresaId)
                .map(mapper::toDomain).orElse(null);
    }

    @Override
    public List<Factura> listar(String empresaId) {
        return repository.findByEmpresaId(empresaId).stream().map(mapper::toDomain).toList();
    }

    // Un deleteByX derivado (no el deleteById base de JpaRepository) necesita
    // transacción propia — sin esto tira "No EntityManager with actual
    // transaction available ... cannot reliably process 'remove' call"
    // (encontrado real al probar el flujo equivalente de nómina electrónica).
    @Override
    @Transactional
    public void eliminar(Long facturaId, String empresaId) {
        repository.deleteByFacturaIdAndEmpresaId(facturaId, empresaId);
    }
}
