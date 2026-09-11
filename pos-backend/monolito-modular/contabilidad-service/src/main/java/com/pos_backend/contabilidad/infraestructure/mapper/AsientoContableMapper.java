package com.pos_backend.contabilidad.infraestructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.contabilidad.domain.model.AsientoContable;
import com.pos_backend.contabilidad.domain.model.MovimientoContable;
import com.pos_backend.contabilidad.infraestructure.driver_adapters.jpa_repository.AsientoContableData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AsientoContableMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AsientoContableData toData(AsientoContable a) {
        AsientoContableData data = new AsientoContableData();
        data.setAsientoId(a.getAsientoId());
        data.setEmpresaId(a.getEmpresaId());
        data.setNumero(a.getNumero());
        data.setFecha(a.getFecha());
        data.setDescripcion(a.getDescripcion());
        data.setOrigen(a.getOrigen());
        data.setReferenciaId(a.getReferenciaId());
        data.setEstado(a.getEstado());
        data.setTotalDebe(a.getTotalDebe());
        data.setTotalHaber(a.getTotalHaber());
        data.setCreadoPor(a.getCreadoPor());
        try {
            data.setMovimientosJson(objectMapper.writeValueAsString(a.getMovimientos()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar los movimientos del asiento");
        }
        return data;
    }

    public AsientoContable toDomain(AsientoContableData data) {
        if (data == null) return null;
        AsientoContable a = new AsientoContable();
        a.setAsientoId(data.getAsientoId());
        a.setEmpresaId(data.getEmpresaId());
        a.setNumero(data.getNumero());
        a.setFecha(data.getFecha());
        a.setDescripcion(data.getDescripcion());
        a.setOrigen(data.getOrigen());
        a.setReferenciaId(data.getReferenciaId());
        a.setEstado(data.getEstado());
        a.setTotalDebe(data.getTotalDebe());
        a.setTotalHaber(data.getTotalHaber());
        a.setCreadoPor(data.getCreadoPor());
        try {
            if (data.getMovimientosJson() != null) {
                List<MovimientoContable> movimientos = objectMapper.readValue(
                        data.getMovimientosJson(), new TypeReference<List<MovimientoContable>>() {});
                a.setMovimientos(movimientos);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al deserializar los movimientos del asiento");
        }
        return a;
    }
}
