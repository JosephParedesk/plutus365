package com.pos_backend.subscription_service.infraestructure.mapper;

import com.pos_backend.subscription_service.domain.model.Suscripcion;
import com.pos_backend.subscription_service.infraestructure.driver_adapters.jpa_repository.SuscripcionData;
import org.springframework.stereotype.Component;

@Component
public class SuscripcionMapper {

    public Suscripcion toSuscripcion(SuscripcionData data) {
        Suscripcion s = new Suscripcion();
        s.setId(data.getId());
        s.setUsuarioCedula(data.getUsuarioCedula());
        s.setPlanId(data.getPlanId());
        s.setEstado(data.getEstado());
        s.setFechaInicio(data.getFechaInicio());
        s.setFechaFin(data.getFechaFin());
        s.setFechaCreacion(data.getFechaCreacion());
        s.setMetodoPago(data.getMetodoPago());
        return s;
    }

    public SuscripcionData toSuscripcionData(Suscripcion s) {
        SuscripcionData data = new SuscripcionData();
        data.setId(s.getId());
        data.setUsuarioCedula(s.getUsuarioCedula());
        data.setPlanId(s.getPlanId());
        data.setEstado(s.getEstado());
        data.setFechaInicio(s.getFechaInicio());
        data.setFechaFin(s.getFechaFin());
        data.setFechaCreacion(s.getFechaCreacion());
        data.setMetodoPago(s.getMetodoPago());
        return data;
    }
}