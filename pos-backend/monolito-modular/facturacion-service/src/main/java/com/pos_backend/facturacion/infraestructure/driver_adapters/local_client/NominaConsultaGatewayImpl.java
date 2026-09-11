package com.pos_backend.facturacion.infraestructure.driver_adapters.local_client;

import com.pos_backend.facturacion.domain.model.NominaRemota;
import com.pos_backend.facturacion.domain.model.gateway.NominaConsultaGateway;
import com.pos_backend.nomina.domain.model.Nomina;
import com.pos_backend.nomina.domain.model.NominaDetalle;
import com.pos_backend.nomina.domain.usecase.NominaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

// Reemplaza el http_client original (RestClient a nomina-service, GET
// /periodos/{id}): ahora que nomina ya está migrado, es una llamada directa
// al UseCase.
@Component("facturacionNominaConsultaGatewayImpl")
@RequiredArgsConstructor
public class NominaConsultaGatewayImpl implements NominaConsultaGateway {

    private final NominaUseCase nominaUseCase;

    @Override
    public NominaRemota buscarNomina(Long nominaId, String empresaId) {
        Nomina n;
        try {
            n = nominaUseCase.buscarPorId(nominaId, empresaId);
        } catch (NoSuchElementException e) {
            return null;
        }
        return new NominaRemota(
                n.getNominaId(), n.getAnio(), n.getMes(), n.getPeriodicidad(), n.getFechaPago(), n.getEstado(),
                detallesRemotos(n.getDetalles()));
    }

    private List<NominaRemota.DetalleRemoto> detallesRemotos(List<NominaDetalle> detalles) {
        if (detalles == null) return List.of();
        List<NominaRemota.DetalleRemoto> remotos = new ArrayList<>();
        for (NominaDetalle d : detalles) {
            List<NominaRemota.HoraExtraRemota> horas = new ArrayList<>();
            if (d.getHorasExtra() != null) {
                for (NominaDetalle.HoraExtraItem h : d.getHorasExtra())
                    horas.add(new NominaRemota.HoraExtraRemota(h.getTipoCode(), h.getCantidadHoras(), h.getPorcentaje(), h.getValor()));
            }
            remotos.add(new NominaRemota.DetalleRemoto(
                    d.getEmpleadoId(), d.getDiasTrabajados(), d.getSueldo(), d.getAuxilioTransporte(), horas,
                    d.getComisiones(), d.getBonificaciones(), d.getOtrosDevengados(), d.getIbc(),
                    d.getSaludEmpleado(), d.getPensionEmpleado(), d.getFondoSolidaridad(), d.getRetencionFuente(),
                    d.getPrestamos(), d.getOtrasDeducciones(), d.getNetoPagar()));
        }
        return remotos;
    }
}
