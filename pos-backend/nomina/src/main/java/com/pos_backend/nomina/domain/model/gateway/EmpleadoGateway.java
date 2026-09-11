package com.pos_backend.nomina.domain.model.gateway;

import com.pos_backend.nomina.domain.model.Empleado;
import java.util.List;

public interface EmpleadoGateway {
    Empleado guardar(Empleado empleado);
    Empleado buscarPorId(Long empleadoId, String empresaId);
    Empleado buscarPorDocumento(String numeroDocumento, String empresaId);
    List<Empleado> listar(String empresaId);
    List<Empleado> listarActivos(String empresaId);
    void eliminar(Long empleadoId, String empresaId);
}
