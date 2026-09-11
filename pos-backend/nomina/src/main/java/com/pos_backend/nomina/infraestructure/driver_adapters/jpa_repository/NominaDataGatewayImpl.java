package com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.nomina.domain.model.Nomina;
import com.pos_backend.nomina.domain.model.gateway.NominaGateway;
import com.pos_backend.nomina.infraestructure.mapper.NominaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository @RequiredArgsConstructor
public class NominaDataGatewayImpl implements NominaGateway {
    private final NominaDataJpaRepository repository;
    private final NominaMapper mapper;

    @Override public Nomina guardar(Nomina n) { return mapper.toDomain(repository.save(mapper.toData(n))); }
    @Override public Nomina buscarPorId(Long id, String empresaId) {
        return repository.findByNominaIdAndEmpresaId(id, empresaId).map(mapper::toDomain).orElse(null);
    }
    @Override public List<Nomina> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByAnioDescMesDesc(empresaId).stream().map(mapper::toDomain).toList();
    }
    @Override public boolean existePeriodo(Integer anio, Integer mes, String empresaId) {
        return repository.existsByAnioAndMesAndEmpresaIdAndEstadoNot(anio, mes, empresaId, "ANULADA");
    }
    @Override public String generarSiguienteNumero(String empresaId) {
        return "NOM-" + String.format("%05d", repository.countByEmpresaId(empresaId) + 1);
    }
}
