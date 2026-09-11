package com.pos_backend.nomina.domain.model.gateway;

import com.pos_backend.nomina.domain.model.AcumuladoInicial;
import java.util.List;

public interface AcumuladoInicialGateway {
    AcumuladoInicial guardar(AcumuladoInicial acumulado);
    AcumuladoInicial buscarPorEmpleadoYAnio(Long empleadoId, Integer anio, String empresaId);
    List<AcumuladoInicial> listar(String empresaId, Integer anio);
    void eliminar(Long acumuladoId, String empresaId);
}
