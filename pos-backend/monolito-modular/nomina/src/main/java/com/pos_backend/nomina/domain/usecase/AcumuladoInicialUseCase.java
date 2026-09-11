package com.pos_backend.nomina.domain.usecase;

import com.pos_backend.nomina.domain.model.AcumuladoInicial;
import com.pos_backend.nomina.domain.model.Empleado;
import com.pos_backend.nomina.domain.model.gateway.AcumuladoInicialGateway;
import com.pos_backend.nomina.domain.model.gateway.EmpleadoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class AcumuladoInicialUseCase {

    private final AcumuladoInicialGateway acumuladoInicialGateway;
    private final EmpleadoGateway empleadoGateway;

    public List<AcumuladoInicial> listar(String empresaId, Integer anio) {
        return acumuladoInicialGateway.listar(empresaId, anio);
    }

    public AcumuladoInicial guardar(AcumuladoInicial acumulado, String empresaId) {
        if (acumulado.getEmpleadoId() == null)
            throw new RuntimeException("Debes indicar el empleado");
        if (acumulado.getAnio() == null)
            throw new RuntimeException("Debes indicar el año al que corresponden los saldos");

        Empleado empleado = empleadoGateway.buscarPorId(acumulado.getEmpleadoId(), empresaId);
        if (empleado == null)
            throw new NoSuchElementException("Empleado no encontrado");

        // Un empleado solo puede tener un registro de acumulados por año:
        // si ya existe, se actualiza en vez de duplicar.
        AcumuladoInicial existente = acumuladoInicialGateway.buscarPorEmpleadoYAnio(
                acumulado.getEmpleadoId(), acumulado.getAnio(), empresaId);
        if (existente != null) acumulado.setAcumuladoId(existente.getAcumuladoId());

        acumulado.setEmpresaId(empresaId);
        acumulado.setNombreEmpleado((empleado.getNombres() + " " + empleado.getApellidos()).trim());
        return acumuladoInicialGateway.guardar(acumulado);
    }

    public void eliminar(Long acumuladoId, String empresaId) {
        acumuladoInicialGateway.eliminar(acumuladoId, empresaId);
    }
}
