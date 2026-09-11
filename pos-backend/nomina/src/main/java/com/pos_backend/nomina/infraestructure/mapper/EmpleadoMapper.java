package com.pos_backend.nomina.infraestructure.mapper;

import com.pos_backend.nomina.domain.model.Empleado;
import com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository.EmpleadoData;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class EmpleadoMapper {
    public Empleado toDomain(EmpleadoData d) {
        if (d == null) return null;
        Empleado e = new Empleado();
        BeanUtils.copyProperties(d, e);
        return e;
    }
    public EmpleadoData toData(Empleado e) {
        EmpleadoData d = new EmpleadoData();
        BeanUtils.copyProperties(e, d);
        return d;
    }
}
