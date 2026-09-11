package com.pos_backend.cliente.domain.usecase;

import com.pos_backend.cliente.domain.model.Cliente;
import com.pos_backend.cliente.domain.model.gateway.ClienteGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class ClienteUseCase {

    private final ClienteGateway clienteGateway;

    public List<Cliente> listarClientes(String empresaId) {
        return clienteGateway.listarClientesActivos(empresaId);
    }

    public Cliente buscarClientePorId(Long clienteId, String empresaId) {
        if (clienteId == null)
            throw new RuntimeException("El id del cliente es obligatorio");
        Cliente cliente = clienteGateway.buscarClientePorId(clienteId, empresaId);
        if (cliente == null)
            throw new NoSuchElementException("Cliente no encontrado");
        return cliente;
    }

    public Cliente buscarClientePorDocumento(String numeroDocumento, String empresaId) {
        if (numeroDocumento == null || numeroDocumento.isBlank())
            throw new RuntimeException("El número de documento es obligatorio");
        Cliente cliente = clienteGateway.buscarClientePorDocumento(numeroDocumento, empresaId);
        if (cliente == null)
            throw new NoSuchElementException("Cliente no encontrado con documento: " + numeroDocumento);
        return cliente;
    }

    public Cliente guardarCliente(Cliente cliente, String empresaId) {
        cliente.setEmpresaId(empresaId);
        validar(cliente);

        if (clienteGateway.existePorDocumento(cliente.getNumeroDocumento(), empresaId))
            throw new RuntimeException("Ya existe un cliente con el documento: " + cliente.getNumeroDocumento());

        if (cliente.getActivo() == null)
            cliente.setActivo(true);
        if (cliente.getPais() == null || cliente.getPais().isBlank())
            cliente.setPais("Colombia");

        return clienteGateway.guardarCliente(cliente);
    }

    public Cliente actualizarCliente(Long clienteId, Cliente cliente, String empresaId) {
        Cliente existente = clienteGateway.buscarClientePorId(clienteId, empresaId);
        if (existente == null)
            throw new NoSuchElementException("Cliente no encontrado");

        cliente.setEmpresaId(empresaId);
        validar(cliente);

        if (!existente.getNumeroDocumento().equalsIgnoreCase(cliente.getNumeroDocumento()))
            if (clienteGateway.existePorDocumento(cliente.getNumeroDocumento(), empresaId))
                throw new RuntimeException("Ya existe un cliente con el documento: " + cliente.getNumeroDocumento());

        cliente.setClienteId(clienteId);
        return clienteGateway.guardarCliente(cliente);
    }

    public void eliminarCliente(Long clienteId, String empresaId) {
        Cliente existente = clienteGateway.buscarClientePorId(clienteId, empresaId);
        if (existente == null)
            throw new NoSuchElementException("Cliente no encontrado");
        clienteGateway.eliminarCliente(clienteId, empresaId);
    }

    private void validar(Cliente cliente) {
        if (cliente.getTipoPersona() == null || cliente.getTipoPersona().isBlank())
            throw new RuntimeException("El tipo de persona es obligatorio");
        if (!cliente.getTipoPersona().equals("NATURAL") && !cliente.getTipoPersona().equals("JURIDICA"))
            throw new RuntimeException("El tipo de persona debe ser NATURAL o JURIDICA");

        if (cliente.getTipoDocumento() == null || cliente.getTipoDocumento().isBlank())
            throw new RuntimeException("El tipo de documento es obligatorio");

        if (cliente.getNumeroDocumento() == null || cliente.getNumeroDocumento().isBlank())
            throw new RuntimeException("El número de documento es obligatorio");

        if (cliente.getTipoPersona().equals("NATURAL")) {
            if (cliente.getNombres() == null || cliente.getNombres().isBlank())
                throw new RuntimeException("Los nombres son obligatorios para persona natural");
            if (cliente.getApellidos() == null || cliente.getApellidos().isBlank())
                throw new RuntimeException("Los apellidos son obligatorios para persona natural");
        } else {
            if (cliente.getRazonSocial() == null || cliente.getRazonSocial().isBlank())
                throw new RuntimeException("La razón social es obligatoria para persona jurídica");
        }

        if (cliente.getTipoDocumento().equals("NIT") && (cliente.getDv() == null || cliente.getDv().isBlank()))
            throw new RuntimeException("El dígito de verificación (DV) es obligatorio para NIT");

        if (cliente.getCorreo() == null || cliente.getCorreo().isBlank())
            throw new RuntimeException("El correo del cliente es obligatorio");
        if (cliente.getTelefono() == null || cliente.getTelefono().isBlank())
            throw new RuntimeException("El teléfono del cliente es obligatorio");
    }
}
