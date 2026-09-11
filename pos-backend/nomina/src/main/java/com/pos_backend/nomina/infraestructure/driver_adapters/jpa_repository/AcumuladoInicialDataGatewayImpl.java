package com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.nomina.domain.model.AcumuladoInicial;
import com.pos_backend.nomina.domain.model.gateway.AcumuladoInicialGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository @RequiredArgsConstructor
public class AcumuladoInicialDataGatewayImpl implements AcumuladoInicialGateway {

    private final AcumuladoInicialDataJpaRepository repository;

    @Override public AcumuladoInicial guardar(AcumuladoInicial a) {
        AcumuladoInicialData d = new AcumuladoInicialData();
        BeanUtils.copyProperties(a, d);
        return toDomain(repository.save(d));
    }
    @Override public AcumuladoInicial buscarPorEmpleadoYAnio(Long empleadoId, Integer anio, String empresaId) {
        return repository.findByEmpleadoIdAndAnioAndEmpresaId(empleadoId, anio, empresaId)
                .map(this::toDomain).orElse(null);
    }
    @Override public List<AcumuladoInicial> listar(String empresaId, Integer anio) {
        return repository.findByEmpresaIdAndAnio(empresaId, anio).stream().map(this::toDomain).toList();
    }
    @Override public void eliminar(Long id, String empresaId) {
        repository.findByAcumuladoIdAndEmpresaId(id, empresaId).ifPresent(repository::delete);
    }
    private AcumuladoInicial toDomain(AcumuladoInicialData d) {
        AcumuladoInicial a = new AcumuladoInicial();
        BeanUtils.copyProperties(d, a);
        return a;
    }
}
