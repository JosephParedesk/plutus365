package com.pos_backend.empresa.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.empresa.domain.model.Empresa;
import com.pos_backend.empresa.domain.model.gateway.EmpresaGateway;
import com.pos_backend.empresa.infraestructure.mapper.EmpresaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class EmpresaDataGatewayImpl implements EmpresaGateway {

    private final EmpresaDataJpaRepository empresaDataJpaRepository;
    private final EmpresaMapper empresaMapper;

    @Override
    public Empresa guardarEmpresa(Empresa empresa) {
        EmpresaData saved = empresaDataJpaRepository.save(empresaMapper.toEmpresaData(empresa));
        return empresaMapper.toEmpresa(saved);
    }

    @Override
    public Empresa buscarPorEmpresaId(String empresaId) {
        return empresaDataJpaRepository.findById(empresaId)
                .map(empresaMapper::toEmpresa)
                .orElse(null);
    }

    @Override
    public boolean existePorEmpresaId(String empresaId) {
        return empresaDataJpaRepository.existsById(empresaId);
    }
}
