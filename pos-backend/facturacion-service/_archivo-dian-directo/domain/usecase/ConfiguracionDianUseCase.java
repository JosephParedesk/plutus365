package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import com.pos_backend.facturacion.domain.model.gateway.CertificadoGateway;
import com.pos_backend.facturacion.domain.model.gateway.ConfiguracionDianGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class ConfiguracionDianUseCase {

    private final ConfiguracionDianGateway configuracionDianGateway;
    private final CertificadoGateway certificadoGateway;

    public ConfiguracionDian obtener(String empresaId) {
        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null)
            throw new NoSuchElementException("Aún no has configurado la facturación electrónica de tu empresa");
        config.setCertificadoActivo(certificadoGateway.existeCertificado(empresaId));
        calcularAlertaRango(config);
        return config;
    }

    public ConfiguracionDian guardar(ConfiguracionDian config, String empresaId) {
        config.setEmpresaId(empresaId);
        validar(config);

        ConfiguracionDian existente = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (existente != null && config.getConsecutivoActual() == null)
            config.setConsecutivoActual(existente.getConsecutivoActual());
        else if (config.getConsecutivoActual() == null)
            config.setConsecutivoActual(config.getRangoDesde());

        ConfiguracionDian guardada = configuracionDianGateway.guardar(config);
        calcularAlertaRango(guardada);
        return guardada;
    }

    // Cuántos números le quedan a la empresa en su rango autorizado y si ya
    // está por debajo del umbral de aviso. Se calcula al vuelo, no se persiste.
    private void calcularAlertaRango(ConfiguracionDian config) {
        if (config.getRangoHasta() == null || config.getConsecutivoActual() == null) return;
        long restantes = config.getRangoHasta() - config.getConsecutivoActual() + 1;
        config.setNumerosRestantes(Math.max(0, restantes));
        config.setAlertaRangoBajo(restantes <= ConfiguracionDian.UMBRAL_ALERTA_RANGO);
    }

    public void subirCertificado(String empresaId, byte[] contenidoPfx, String password) {
        if (contenidoPfx == null || contenidoPfx.length == 0)
            throw new RuntimeException("El archivo del certificado está vacío");
        if (password == null || password.isBlank())
            throw new RuntimeException("Debes indicar el password del certificado");

        // Valida que el .pfx y el password realmente abran antes de guardarlo,
        // para no dejar guardado un certificado inservible.
        certificadoGateway.guardarCertificado(empresaId, contenidoPfx, password);
    }

    private void validar(ConfiguracionDian config) {
        if (config.getPrefijo() == null || config.getPrefijo().isBlank())
            throw new RuntimeException("El prefijo de facturación es obligatorio");
        if (config.getResolucionNumero() == null || config.getResolucionNumero().isBlank())
            throw new RuntimeException("El número de resolución de la DIAN es obligatorio");
        if (config.getRangoDesde() == null || config.getRangoHasta() == null)
            throw new RuntimeException("El rango de numeración autorizado es obligatorio");
        if (config.getRangoDesde() >= config.getRangoHasta())
            throw new RuntimeException("El rango autorizado no es válido (desde debe ser menor que hasta)");
        if (config.getSoftwareId() == null || config.getSoftwareId().isBlank())
            throw new RuntimeException("El Software ID entregado por la DIAN es obligatorio");
        if (config.getSoftwarePin() == null || config.getSoftwarePin().isBlank())
            throw new RuntimeException("El PIN del software es obligatorio");
        if (config.getClaveTecnica() == null || config.getClaveTecnica().isBlank())
            throw new RuntimeException("La clave técnica entregada por la DIAN es obligatoria");
        if (config.getAmbiente() == null ||
                !(config.getAmbiente().equals("HABILITACION") || config.getAmbiente().equals("PRODUCCION")))
            throw new RuntimeException("El ambiente debe ser HABILITACION o PRODUCCION");
        if (config.getAmbiente().equals("HABILITACION") &&
                (config.getTestSetId() == null || config.getTestSetId().isBlank()))
            throw new RuntimeException("El TestSetId es obligatorio en ambiente de HABILITACION (te lo entrega la DIAN)");

        LocalDate inicio, fin;
        try {
            inicio = LocalDate.parse(config.getResolucionFechaInicio());
            fin = LocalDate.parse(config.getResolucionFechaFin());
        } catch (DateTimeParseException | NullPointerException e) {
            throw new RuntimeException("Las fechas de la resolución deben tener formato yyyy-MM-dd");
        }

        if (!fin.isAfter(inicio))
            throw new RuntimeException("La fecha de fin de la resolución debe ser posterior a la fecha de inicio");

        // Art. 44, Resolución DIAN 0044 de 2020: la autorización de numeración
        // consecutiva tiene una vigencia máxima de 2 años desde la asignación.
        LocalDate vigenciaMaxima = inicio.plusYears(2);
        if (fin.isAfter(vigenciaMaxima))
            throw new RuntimeException("La vigencia de la resolución no puede superar los 2 años desde la fecha de inicio " +
                    "(Art. 44, Resolución DIAN 0044 de 2020). Con inicio " + inicio +
                    ", la fecha de fin no puede pasar de " + vigenciaMaxima);
    }
}
