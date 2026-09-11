package com.pos_backend.proveedor.domain.usecase;

import com.pos_backend.proveedor.domain.model.Proveedor;
import com.pos_backend.proveedor.domain.model.gateway.ProveedorGateway;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class ProveedorUseCase {

    private final ProveedorGateway proveedorGateway;

    public List<Proveedor> listarProveedores(String empresaId) {
        return proveedorGateway.listarProveedoresActivos(empresaId);
    }

    public Proveedor buscarProveedorPorId(Long proveedorId, String empresaId) {
        if (proveedorId == null)
            throw new RuntimeException("El id del proveedor es obligatorio");
        Proveedor proveedor = proveedorGateway.buscarProveedorPorId(proveedorId, empresaId);
        if (proveedor == null)
            throw new NoSuchElementException("Proveedor no encontrado");
        return proveedor;
    }

    public Proveedor buscarProveedorPorNit(String nit, String empresaId) {
        if (nit == null || nit.isBlank())
            throw new RuntimeException("El NIT es obligatorio");
        Proveedor proveedor = proveedorGateway.buscarProveedorPorNit(nit, empresaId);
        if (proveedor == null)
            throw new NoSuchElementException("Proveedor no encontrado con NIT: " + nit);
        return proveedor;
    }

    public Proveedor guardarProveedor(Proveedor proveedor, String empresaId) {
        validar(proveedor);

        if (proveedorGateway.existePorNit(proveedor.getNit(), empresaId))
            throw new RuntimeException("Ya existe un proveedor con el NIT: " + proveedor.getNit());

        if (proveedorGateway.existePorNombre(proveedor.getNombre(), empresaId))
            throw new RuntimeException("Ya existe un proveedor con el nombre: " + proveedor.getNombre());

        if (proveedor.getActivo() == null)
            proveedor.setActivo(true);
        proveedor.setEmpresaId(empresaId);

        return proveedorGateway.guardarProveedor(proveedor);
    }

    public Proveedor actualizarProveedor(Long proveedorId, Proveedor proveedor, String empresaId) {
        Proveedor existente = buscarProveedorPorId(proveedorId, empresaId);
        validarEsPropio(existente);

        validar(proveedor);

        if (!existente.getNit().equalsIgnoreCase(proveedor.getNit()))
            if (proveedorGateway.existePorNit(proveedor.getNit(), empresaId))
                throw new RuntimeException("Ya existe un proveedor con el NIT: " + proveedor.getNit());

        if (!existente.getNombre().equalsIgnoreCase(proveedor.getNombre()))
            if (proveedorGateway.existePorNombre(proveedor.getNombre(), empresaId))
                throw new RuntimeException("Ya existe un proveedor con el nombre: " + proveedor.getNombre());

        proveedor.setProveedorId(proveedorId);
        proveedor.setEmpresaId(empresaId);
        return proveedorGateway.guardarProveedor(proveedor);
    }

    public void eliminarProveedor(Long proveedorId, String empresaId) {
        Proveedor existente = buscarProveedorPorId(proveedorId, empresaId);
        validarEsPropio(existente);
        proveedorGateway.eliminarProveedor(proveedorId);
    }

    // Los proveedores base compartidos (empresaId null, legado) se pueden ver y usar,
    // pero no editar ni eliminar — afectarían a las demás empresas que también los ven.
    private void validarEsPropio(Proveedor proveedor) {
        if (proveedor.getEmpresaId() == null)
            throw new RuntimeException("Este es un proveedor base compartido, no se puede editar ni eliminar. Creá tu propio proveedor en su lugar.");
    }

    private void validar(Proveedor proveedor) {
        if (proveedor.getNit() == null || proveedor.getNit().isBlank())
            throw new RuntimeException("El NIT del proveedor es obligatorio");
        if (proveedor.getNombre() == null || proveedor.getNombre().isBlank())
            throw new RuntimeException("El nombre del proveedor es obligatorio");
        if (proveedor.getTelefono() == null || proveedor.getTelefono().isBlank())
            throw new RuntimeException("El teléfono del proveedor es obligatorio");
        if (proveedor.getCorreo() == null || proveedor.getCorreo().isBlank())
            throw new RuntimeException("El correo del proveedor es obligatorio");
    }
}
