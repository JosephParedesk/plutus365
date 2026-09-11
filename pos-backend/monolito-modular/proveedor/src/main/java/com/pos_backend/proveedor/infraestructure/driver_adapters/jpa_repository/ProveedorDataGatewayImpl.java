package com.pos_backend.proveedor.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.proveedor.domain.model.Proveedor;
import com.pos_backend.proveedor.domain.model.gateway.ProveedorGateway;
import com.pos_backend.proveedor.infraestructure.mapper.ProveedorMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProveedorDataGatewayImpl implements ProveedorGateway {

    private final ProveedorDataJpaRepository proveedorDataJpaRepository;
    private final ProveedorMapper proveedorMapper;

    @Override
    public Proveedor guardarProveedor(Proveedor proveedor) {
        ProveedorData saved = proveedorDataJpaRepository
                .save(proveedorMapper.toProveedorData(proveedor));
        return proveedorMapper.toProveedor(saved);
    }

    @Override
    public Proveedor buscarProveedorPorId(Long proveedorId, String empresaId) {
        return proveedorDataJpaRepository.buscarPorIdVisibleParaEmpresa(proveedorId, empresaId)
                .map(proveedorMapper::toProveedor)
                .orElse(null);
    }

    @Override
    public Proveedor buscarProveedorPorNit(String nit, String empresaId) {
        return proveedorDataJpaRepository.buscarPorNitVisibleParaEmpresa(nit, empresaId)
                .map(proveedorMapper::toProveedor)
                .orElse(null);
    }

    @Override
    public List<Proveedor> listarProveedores(String empresaId) {
        return proveedorDataJpaRepository.listarVisiblesParaEmpresa(empresaId)
                .stream().map(proveedorMapper::toProveedor).toList();
    }

    @Override
    public List<Proveedor> listarProveedoresActivos(String empresaId) {
        return proveedorDataJpaRepository.listarActivosVisiblesParaEmpresa(empresaId)
                .stream().map(proveedorMapper::toProveedor).toList();
    }

    @Override
    public void eliminarProveedor(Long proveedorId) {
        proveedorDataJpaRepository.deleteById(proveedorId);
    }

    @Override
    public boolean existePorNit(String nit, String empresaId) {
        return proveedorDataJpaRepository.existsByNitVisibleParaEmpresa(nit, empresaId);
    }

    @Override
    public boolean existePorNombre(String nombre, String empresaId) {
        return proveedorDataJpaRepository.existsByNombreVisibleParaEmpresa(nombre, empresaId);
    }
}
