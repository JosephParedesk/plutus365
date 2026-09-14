package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import com.pos_backend.facturacion.domain.model.gateway.ConfiguracionDianGateway;
import com.pos_backend.facturacion.domain.model.gateway.FacturaElectronicaGateway;
import lombok.RequiredArgsConstructor;

import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class ConfiguracionDianUseCase {

    private final ConfiguracionDianGateway configuracionDianGateway;
    private final FacturaElectronicaGateway facturaElectronicaGateway;

    public ConfiguracionDian obtener(String empresaId) {
        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null)
            throw new NoSuchElementException("Aún no has configurado Factus para tu empresa");
        return ocultarSecretos(config);
    }

    public java.util.List<FacturaElectronicaGateway.RangoNumeracion> listarRangosNumeracion(String empresaId, String codigoDocumento) {
        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null)
            throw new NoSuchElementException("Configura primero tus credenciales de Factus en Configuración");
        return facturaElectronicaGateway.listarRangosNumeracion(config, codigoDocumento);
    }

    // TEMPORAL — ver nota en FacturaElectronicaGateway.consultarSuscripcionRaw.
    public String consultarSuscripcionRaw(String empresaId) {
        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null)
            throw new NoSuchElementException("Configura primero tus credenciales de Factus en Configuración");
        return facturaElectronicaGateway.consultarSuscripcionRaw(config);
    }

    public ConfiguracionDian guardar(ConfiguracionDian config, String empresaId) {
        config.setEmpresaId(empresaId);

        // El GET nunca devuelve los secretos (ver ocultarSecretos) — si el formulario
        // los manda vacíos es porque el usuario no los tocó, no porque los quiera borrar.
        ConfiguracionDian existente = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (existente != null) {
            if (esVacio(config.getFactusClientSecret())) config.setFactusClientSecret(existente.getFactusClientSecret());
            if (esVacio(config.getFactusPassword())) config.setFactusPassword(existente.getFactusPassword());
        }

        validar(config);

        // Prueba las credenciales contra Factus ANTES de guardar — evita dejar
        // guardadas credenciales rotas que solo se descubren al intentar facturar.
        try {
            facturaElectronicaGateway.verificarCredenciales(config);
        } catch (RuntimeException e) {
            throw new RuntimeException("No se pudieron validar las credenciales con Factus: " + e.getMessage());
        }

        config.setActivo(true);
        return ocultarSecretos(configuracionDianGateway.guardar(config));
    }

    // El gateway devuelve los secretos descifrados (los necesita el propio use case
    // para volver a guardarlos/usarlos) — pero nunca deben salir del backend en una
    // respuesta HTTP. Mismo cuidado que falta hoy en auth-service con el password
    // hasheado del Usuario (ver CLAUDE.md).
    private ConfiguracionDian ocultarSecretos(ConfiguracionDian config) {
        config.setFactusClientSecret(null);
        config.setFactusPassword(null);
        return config;
    }

    private boolean esVacio(String s) { return s == null || s.isBlank(); }

    private void validar(ConfiguracionDian config) {
        if (config.getFactusClientId() == null || config.getFactusClientId().isBlank())
            throw new RuntimeException("El Client ID de Factus es obligatorio");
        if (config.getFactusClientSecret() == null || config.getFactusClientSecret().isBlank())
            throw new RuntimeException("El Client Secret de Factus es obligatorio");
        if (config.getFactusUsername() == null || config.getFactusUsername().isBlank())
            throw new RuntimeException("El usuario de Factus es obligatorio");
        if (config.getFactusPassword() == null || config.getFactusPassword().isBlank())
            throw new RuntimeException("La contraseña de Factus es obligatoria");
    }
}
