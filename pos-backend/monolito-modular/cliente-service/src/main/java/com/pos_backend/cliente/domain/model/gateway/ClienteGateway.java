package com.pos_backend.cliente.domain.model.gateway;

import com.pos_backend.cliente.domain.model.Cliente;
import java.util.List;

public interface ClienteGateway {
    Cliente guardarCliente(Cliente cliente);
    Cliente buscarClientePorId(Long clienteId, String empresaId);
    Cliente buscarClientePorDocumento(String numeroDocumento, String empresaId);
    List<Cliente> listarClientes(String empresaId);
    List<Cliente> listarClientesActivos(String empresaId);
    void eliminarCliente(Long clienteId, String empresaId);
    boolean existePorDocumento(String numeroDocumento, String empresaId);
}
