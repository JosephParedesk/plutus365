package com.pos_backend.venta.domain.usecase;

import com.pos_backend.venta.domain.model.ConfiguracionRecibo;
import com.pos_backend.venta.domain.model.gateway.ConfiguracionReciboGateway;
import lombok.RequiredArgsConstructor;

import java.util.Set;

/**
 * Configuración del recibo impreso del POS. A diferencia de ConfiguracionDian,
 * acá SIEMPRE hay algo que devolver: si la empresa nunca la personalizó,
 * obtener() entrega los valores por defecto sin pedirle que configure nada
 * primero — el recibo tiene que poder imprimirse desde el día uno.
 */
@RequiredArgsConstructor
public class ConfiguracionReciboUseCase {

    private static final Set<String> ANCHOS_VALIDOS = Set.of("58MM", "80MM");
    private static final Set<String> FUENTES_VALIDAS = Set.of("PEQUENA", "NORMAL", "GRANDE");
    private static final int MENSAJE_MAX_LARGO = 200;

    private final ConfiguracionReciboGateway configuracionReciboGateway;

    public ConfiguracionRecibo obtener(String empresaId) {
        ConfiguracionRecibo config = configuracionReciboGateway.buscarPorEmpresaId(empresaId);
        return config != null ? config : valoresPorDefecto(empresaId);
    }

    public ConfiguracionRecibo guardar(ConfiguracionRecibo config, String empresaId) {
        config.setEmpresaId(empresaId);

        if (config.getAnchoPapel() == null || !ANCHOS_VALIDOS.contains(config.getAnchoPapel().toUpperCase()))
            throw new RuntimeException("El ancho del papel debe ser 58MM o 80MM");
        config.setAnchoPapel(config.getAnchoPapel().toUpperCase());

        if (config.getTamanioFuente() == null || !FUENTES_VALIDAS.contains(config.getTamanioFuente().toUpperCase()))
            throw new RuntimeException("El tamaño de letra debe ser PEQUENA, NORMAL o GRANDE");
        config.setTamanioFuente(config.getTamanioFuente().toUpperCase());

        if (config.getMensajePie() != null && config.getMensajePie().length() > MENSAJE_MAX_LARGO)
            throw new RuntimeException("El mensaje del pie no puede superar los " + MENSAJE_MAX_LARGO + " caracteres");
        if (config.getMensajePie() == null || config.getMensajePie().isBlank())
            config.setMensajePie("¡Gracias por tu compra!");

        if (config.getMostrarLogo() == null) config.setMostrarLogo(true);
        if (config.getMostrarDireccionEmpresa() == null) config.setMostrarDireccionEmpresa(true);
        if (config.getMostrarAtendidoPor() == null) config.setMostrarAtendidoPor(true);

        return configuracionReciboGateway.guardar(config);
    }

    private ConfiguracionRecibo valoresPorDefecto(String empresaId) {
        ConfiguracionRecibo config = new ConfiguracionRecibo();
        config.setEmpresaId(empresaId);
        config.setAnchoPapel("80MM");
        config.setTamanioFuente("NORMAL");
        config.setMensajePie("¡Gracias por tu compra!");
        config.setMostrarLogo(true);
        config.setMostrarDireccionEmpresa(true);
        config.setMostrarAtendidoPor(true);
        return config;
    }
}
