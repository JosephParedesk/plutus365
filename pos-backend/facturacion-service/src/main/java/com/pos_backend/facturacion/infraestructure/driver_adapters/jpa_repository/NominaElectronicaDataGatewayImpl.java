package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.facturacion.domain.model.NominaElectronica;
import com.pos_backend.facturacion.domain.model.gateway.NominaElectronicaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository @RequiredArgsConstructor
public class NominaElectronicaDataGatewayImpl implements NominaElectronicaGateway {

    private final NominaElectronicaDataJpaRepository repository;

    @Override public NominaElectronica guardar(NominaElectronica n) {
        NominaElectronicaData d = new NominaElectronicaData();
        BeanUtils.copyProperties(n, d);
        return toDomain(repository.save(d));
    }

    @Override public NominaElectronica buscarPorNominaYEmpleado(Long nominaId, Long empleadoId, String empresaId) {
        return repository.findByNominaIdAndEmpleadoIdAndEmpresaId(nominaId, empleadoId, empresaId)
                .map(this::toDomain).orElse(null);
    }

    @Override public NominaElectronica buscarPorId(Long nominaElectronicaId, String empresaId) {
        return repository.findByNominaElectronicaIdAndEmpresaId(nominaElectronicaId, empresaId)
                .map(this::toDomain).orElse(null);
    }

    @Override public List<NominaElectronica> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByNominaElectronicaIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override public List<NominaElectronica> listarPorNomina(Long nominaId, String empresaId) {
        return repository.findByNominaIdAndEmpresaId(nominaId, empresaId).stream().map(this::toDomain).toList();
    }

    @Override public void eliminar(Long nominaElectronicaId, String empresaId) {
        repository.deleteByNominaElectronicaIdAndEmpresaId(nominaElectronicaId, empresaId);
    }

    private NominaElectronica toDomain(NominaElectronicaData d) {
        NominaElectronica n = new NominaElectronica();
        BeanUtils.copyProperties(d, n);
        return n;
    }
}
