package com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.nomina.domain.model.Empleado;
import com.pos_backend.nomina.domain.model.gateway.EmpleadoGateway;
import com.pos_backend.nomina.infraestructure.mapper.EmpleadoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository @RequiredArgsConstructor
public class EmpleadoDataGatewayImpl implements EmpleadoGateway {
    private final EmpleadoDataJpaRepository repository;
    private final EmpleadoMapper mapper;

    @Override public Empleado guardar(Empleado e) { return mapper.toDomain(repository.save(mapper.toData(e))); }
    @Override public Empleado buscarPorId(Long id, String empresaId) {
        return repository.findByEmpleadoIdAndEmpresaId(id, empresaId).map(mapper::toDomain).orElse(null);
    }
    @Override public Empleado buscarPorDocumento(String doc, String empresaId) {
        return repository.findByNumeroDocumentoAndEmpresaId(doc, empresaId).map(mapper::toDomain).orElse(null);
    }
    @Override public List<Empleado> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByNombresAsc(empresaId).stream().map(mapper::toDomain).toList();
    }
    @Override public List<Empleado> listarActivos(String empresaId) {
        return repository.findByEmpresaIdAndActivoTrue(empresaId).stream().map(mapper::toDomain).toList();
    }
    @Override public void eliminar(Long id, String empresaId) {
        repository.findByEmpleadoIdAndEmpresaId(id, empresaId).ifPresent(repository::delete);
    }
}
