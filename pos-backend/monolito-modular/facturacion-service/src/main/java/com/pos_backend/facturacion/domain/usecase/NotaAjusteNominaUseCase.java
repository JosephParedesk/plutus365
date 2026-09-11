package com.pos_backend.facturacion.domain.usecase;

import com.pos_backend.facturacion.domain.model.*;
import com.pos_backend.facturacion.domain.model.gateway.*;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Anula ante la DIAN una nómina electrónica de un empleado ya ACEPTADA
 * (`/v2/adjustment-payrolls` de Factus). Sin ítems ni concepto: es una
 * eliminación pura por número de nómina electrónica.
 */
@RequiredArgsConstructor
public class NotaAjusteNominaUseCase {

    private final NotaAjusteNominaGateway notaAjusteNominaGateway;
    private final NominaElectronicaGateway nominaElectronicaGateway;
    private final ConfiguracionDianGateway configuracionDianGateway;
    private final FacturaElectronicaGateway facturaElectronicaGateway;

    public List<NotaAjusteNomina> listar(String empresaId) {
        return notaAjusteNominaGateway.listar(empresaId);
    }

    public NotaAjusteNomina emitir(Long nominaElectronicaId, String empresaId, String creadoPor) {
        NominaElectronica nominaElectronica = nominaElectronicaGateway.buscarPorId(nominaElectronicaId, empresaId);
        if (nominaElectronica == null)
            throw new NoSuchElementException("La nómina electrónica que quieres anular no existe");
        if (!"ACEPTADA".equals(nominaElectronica.getEstado()))
            throw new RuntimeException("Solo se puede anular una nómina electrónica ACEPTADA por la DIAN. " +
                    "Esta está en estado " + nominaElectronica.getEstado() + ".");

        NotaAjusteNomina existente = notaAjusteNominaGateway.buscarPorNominaElectronicaId(nominaElectronicaId, empresaId);
        if (existente != null && "ACEPTADA".equals(existente.getEstado()))
            throw new RuntimeException("Esta nómina electrónica ya tiene una nota de ajuste aceptada: " + existente.getNumeroAjuste());

        ConfiguracionDian config = configuracionDianGateway.buscarPorEmpresaId(empresaId);
        if (config == null || !Boolean.TRUE.equals(config.getActivo()))
            throw new RuntimeException("Configura primero tus credenciales de Factus en Configuración");

        NotaAjusteNomina nota = new NotaAjusteNomina();
        nota.setEmpresaId(empresaId);
        nota.setNominaElectronicaId(nominaElectronicaId);
        nota.setNumeroNominaElectronica(nominaElectronica.getNumeroDocumento());
        nota.setFechaEmision(LocalDateTime.now());
        nota.setCreadoPor(creadoPor);
        nota.setEstado("ERROR");

        String referenceCode = "AN-" + nominaElectronica.getNumeroDocumento() + "-" + System.currentTimeMillis();
        nota.setReferenceCode(referenceCode);

        try {
            nota.setFechaEnvio(LocalDateTime.now());
            FacturaElectronicaGateway.ResultadoEmision resultado = facturaElectronicaGateway.emitirNotaAjusteNomina(
                    config, nota, referenceCode);

            nota.setNumeroAjuste(resultado.numeroDocumento());
            nota.setAmbiente(resultado.ambiente());
            nota.setRespuestaDian(resultado.mensaje());
            nota.setEstado(resultado.aceptada() ? "ACEPTADA" : "RECHAZADA");
        } catch (RuntimeException e) {
            nota.setRespuestaDian(e.getMessage());
            notaAjusteNominaGateway.guardar(nota);
            throw new RuntimeException("Factus rechazó la nota de ajuste de nómina: " + e.getMessage());
        }

        if (!"ACEPTADA".equals(nota.getEstado())) {
            notaAjusteNominaGateway.guardar(nota);
            throw new RuntimeException("Factus no validó la nota de ajuste de nómina: " + nota.getRespuestaDian());
        }

        return notaAjusteNominaGateway.guardar(nota);
    }
}
