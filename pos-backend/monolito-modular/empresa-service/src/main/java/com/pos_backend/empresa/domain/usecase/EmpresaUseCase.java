package com.pos_backend.empresa.domain.usecase;

import com.pos_backend.empresa.domain.model.Empresa;
import com.pos_backend.empresa.domain.model.gateway.ContabilidadGateway;
import com.pos_backend.empresa.domain.model.gateway.EmpresaGateway;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class EmpresaUseCase {

    private final EmpresaGateway empresaGateway;
    private final ContabilidadGateway contabilidadGateway;

    public Empresa obtenerConfiguracion(String empresaId) {
        Empresa empresa = empresaGateway.buscarPorEmpresaId(empresaId);
        if (empresa == null)
            throw new NoSuchElementException("La empresa aún no ha configurado sus datos");
        return empresa;
    }

    public Empresa guardarConfiguracion(Empresa empresa, String empresaId) {
        empresa.setEmpresaId(empresaId);
        validar(empresa);

        if (empresa.getPais() == null || empresa.getPais().isBlank())
            empresa.setPais("Colombia");
        if (empresa.getMoneda() == null || empresa.getMoneda().isBlank())
            empresa.setMoneda("COP");
        if (empresa.getColorPrincipal() == null || empresa.getColorPrincipal().isBlank())
            empresa.setColorPrincipal("#4E6F3A"); // verde de marca de Plutus365, por defecto

        // Es un upsert: si ya existe configuración para esta empresa, conserva el logo
        // que ya tenía (el logo se administra por separado, vía /logo).
        Empresa existente = empresaGateway.buscarPorEmpresaId(empresaId);
        if (existente != null && (empresa.getLogoUrl() == null || empresa.getLogoUrl().isBlank()))
            empresa.setLogoUrl(existente.getLogoUrl());

        Empresa guardada = empresaGateway.guardarEmpresa(empresa);

        // Primera vez que esta empresa configura sus datos: siembra su plan de
        // cuentas ya mismo, para que no le falle la primera venta/compra solo
        // porque nadie entró todavía a la pantalla de Contabilidad.
        if (existente == null)
            contabilidadGateway.sembrarPlanDeCuentas(empresaId);

        return guardada;
    }

    // Uso exclusivo del panel de super administrador (ver AdminUseCase en
    // facturacion-service) — nunca se expone a una empresa normal.
    public java.util.List<Empresa> listarTodas() {
        return empresaGateway.listarTodas();
    }

    public Empresa actualizarLogo(String empresaId, String logoUrl) {
        Empresa existente = empresaGateway.buscarPorEmpresaId(empresaId);
        if (existente == null)
            throw new RuntimeException("Configura primero los datos de tu empresa antes de subir el logo");

        existente.setLogoUrl(logoUrl);
        return empresaGateway.guardarEmpresa(existente);
    }

    private void validar(Empresa empresa) {
        if (empresa.getTipoPersona() == null || empresa.getTipoPersona().isBlank())
            throw new RuntimeException("El tipo de persona es obligatorio");
        if (!empresa.getTipoPersona().equals("NATURAL") && !empresa.getTipoPersona().equals("JURIDICA"))
            throw new RuntimeException("El tipo de persona debe ser NATURAL o JURIDICA");

        if (empresa.getTipoDocumento() == null || empresa.getTipoDocumento().isBlank())
            throw new RuntimeException("El tipo de documento es obligatorio");
        if (empresa.getNumeroDocumento() == null || empresa.getNumeroDocumento().isBlank())
            throw new RuntimeException("El número de documento es obligatorio");

        if (empresa.getTipoPersona().equals("JURIDICA")) {
            if (empresa.getRazonSocial() == null || empresa.getRazonSocial().isBlank())
                throw new RuntimeException("La razón social es obligatoria para persona jurídica");
        } else {
            if (empresa.getNombres() == null || empresa.getNombres().isBlank())
                throw new RuntimeException("Los nombres son obligatorios para persona natural");
            if (empresa.getApellidos() == null || empresa.getApellidos().isBlank())
                throw new RuntimeException("Los apellidos son obligatorios para persona natural");
        }

        if (empresa.getTipoDocumento().equals("NIT") && (empresa.getDv() == null || empresa.getDv().isBlank()))
            throw new RuntimeException("El dígito de verificación (DV) es obligatorio para NIT");

        if (empresa.getRegimenFiscal() == null || empresa.getRegimenFiscal().isBlank())
            throw new RuntimeException("El régimen fiscal es obligatorio");

        if (empresa.getPeriodicidadIva() != null && !empresa.getPeriodicidadIva().isBlank()
                && !empresa.getPeriodicidadIva().equals("BIMESTRAL") && !empresa.getPeriodicidadIva().equals("CUATRIMESTRAL"))
            throw new RuntimeException("La periodicidad de IVA debe ser BIMESTRAL o CUATRIMESTRAL");

        if (empresa.getCorreo() == null || empresa.getCorreo().isBlank())
            throw new RuntimeException("El correo de la empresa es obligatorio");
        if (empresa.getTelefono() == null || empresa.getTelefono().isBlank())
            throw new RuntimeException("El teléfono de la empresa es obligatorio");
    }
}
