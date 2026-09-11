package com.pos_backend.cliente.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.cliente.domain.model.Cliente;
import com.pos_backend.cliente.domain.model.gateway.ClienteGateway;
import com.pos_backend.cliente.infraestructure.mapper.ClienteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ClienteDataGatewayImpl implements ClienteGateway {

    private final ClienteDataJpaRepository clienteDataJpaRepository;
    private final ClienteMapper clienteMapper;

    @Override
    public Cliente guardarCliente(Cliente cliente) {
        ClienteData saved = clienteDataJpaRepository
                .save(clienteMapper.toClienteData(cliente));
        return clienteMapper.toCliente(saved);
    }

    @Override
    public Cliente buscarClientePorId(Long clienteId, String empresaId) {
        return clienteDataJpaRepository.findByClienteIdAndEmpresaId(clienteId, empresaId)
                .map(clienteMapper::toCliente)
                .orElse(null);
    }

    @Override
    public Cliente buscarClientePorDocumento(String numeroDocumento, String empresaId) {
        return clienteDataJpaRepository.findByNumeroDocumentoAndEmpresaId(numeroDocumento, empresaId)
                .map(clienteMapper::toCliente)
                .orElse(null);
    }

    @Override
    public List<Cliente> listarClientes(String empresaId) {
        return clienteDataJpaRepository.findByEmpresaId(empresaId)
                .stream().map(clienteMapper::toCliente).toList();
    }

    @Override
    public List<Cliente> listarClientesActivos(String empresaId) {
        return clienteDataJpaRepository.findByEmpresaIdAndActivoTrue(empresaId)
                .stream().map(clienteMapper::toCliente).toList();
    }

    @Override
    public void eliminarCliente(Long clienteId, String empresaId) {
        clienteDataJpaRepository.findByClienteIdAndEmpresaId(clienteId, empresaId)
                .ifPresent(clienteDataJpaRepository::delete);
    }

    @Override
    public boolean existePorDocumento(String numeroDocumento, String empresaId) {
        return clienteDataJpaRepository.existsByNumeroDocumentoIgnoreCaseAndEmpresaId(numeroDocumento, empresaId);
    }
}
