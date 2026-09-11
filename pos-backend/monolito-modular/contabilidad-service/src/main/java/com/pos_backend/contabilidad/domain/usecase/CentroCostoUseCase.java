package com.pos_backend.contabilidad.domain.usecase;

import com.pos_backend.contabilidad.domain.model.CentroCosto;
import com.pos_backend.contabilidad.domain.model.gateway.CentroCostoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class CentroCostoUseCase {

    private final CentroCostoGateway centroCostoGateway;

    public List<CentroCosto> listar(String empresaId) {
        return centroCostoGateway.listar(empresaId);
    }

    public CentroCosto buscarPorId(Long id, String empresaId) {
        CentroCosto c = centroCostoGateway.buscarPorId(id, empresaId);
        if (c == null) throw new NoSuchElementException("Centro de costo no encontrado");
        return c;
    }

    public CentroCosto crear(CentroCosto c, String empresaId) {
        c.setEmpresaId(empresaId);
        if (c.getActivo() == null) c.setActivo(true);
        validar(c);
        if (centroCostoGateway.existeCodigo(c.getCodigo(), empresaId))
            throw new RuntimeException("Ya existe un centro de costo con el código " + c.getCodigo());
        return centroCostoGateway.guardar(c);
    }

    public CentroCosto actualizar(Long id, CentroCosto cambios, String empresaId) {
        CentroCosto existente = buscarPorId(id, empresaId);
        if (cambios.getNombre() != null) existente.setNombre(cambios.getNombre());
        if (cambios.getDescripcion() != null) existente.setDescripcion(cambios.getDescripcion());
        if (cambios.getResponsable() != null) existente.setResponsable(cambios.getResponsable());
        if (cambios.getActivo() != null) existente.setActivo(cambios.getActivo());
        // El código no se edita: ya quedó estampado en documentos anteriores.
        return centroCostoGateway.guardar(existente);
    }

    public void eliminar(Long id, String empresaId) {
        buscarPorId(id, empresaId);
        // No se borra físicamente si ya se usó en documentos; se desactiva.
        centroCostoGateway.eliminar(id, empresaId);
    }

    private void validar(CentroCosto c) {
        if (c.getCodigo() == null || c.getCodigo().isBlank())
            throw new RuntimeException("El código es obligatorio");
        if (c.getNombre() == null || c.getNombre().isBlank())
            throw new RuntimeException("El nombre es obligatorio");
    }
}
