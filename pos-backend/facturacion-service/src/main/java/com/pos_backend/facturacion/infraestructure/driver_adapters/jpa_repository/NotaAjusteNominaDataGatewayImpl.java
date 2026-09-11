package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.facturacion.domain.model.NotaAjusteNomina;
import com.pos_backend.facturacion.domain.model.gateway.NotaAjusteNominaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository @RequiredArgsConstructor
public class NotaAjusteNominaDataGatewayImpl implements NotaAjusteNominaGateway {

    private final NotaAjusteNominaDataJpaRepository repository;

    @Override public NotaAjusteNomina guardar(NotaAjusteNomina n) {
        NotaAjusteNominaData d = new NotaAjusteNominaData();
        BeanUtils.copyProperties(n, d);
        return toDomain(repository.save(d));
    }

    @Override public NotaAjusteNomina buscarPorId(Long id, String empresaId) {
        return repository.findByNotaAjusteNominaIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override public NotaAjusteNomina buscarPorNominaElectronicaId(Long nominaElectronicaId, String empresaId) {
        return repository.findTopByNominaElectronicaIdAndEmpresaIdOrderByNotaAjusteNominaIdDesc(nominaElectronicaId, empresaId)
                .map(this::toDomain).orElse(null);
    }

    @Override public List<NotaAjusteNomina> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByNotaAjusteNominaIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override public void eliminar(Long id, String empresaId) {
        repository.findByNotaAjusteNominaIdAndEmpresaId(id, empresaId).ifPresent(repository::delete);
    }

    private NotaAjusteNomina toDomain(NotaAjusteNominaData d) {
        NotaAjusteNomina n = new NotaAjusteNomina();
        BeanUtils.copyProperties(d, n);
        return n;
    }
}
