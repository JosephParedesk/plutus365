package com.pos_backend.nomina.domain.usecase;

import com.pos_backend.nomina.domain.model.Empleado;
import com.pos_backend.nomina.domain.model.ParametrosNomina;
import com.pos_backend.nomina.domain.model.gateway.EmpleadoGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class EmpleadoUseCase {

    private final EmpleadoGateway empleadoGateway;

    public List<Empleado> listar(String empresaId) {
        return empleadoGateway.listar(empresaId);
    }

    public Empleado buscarPorId(Long empleadoId, String empresaId) {
        Empleado e = empleadoGateway.buscarPorId(empleadoId, empresaId);
        if (e == null) throw new NoSuchElementException("Empleado no encontrado");
        return e;
    }

    public Empleado crear(Empleado empleado, String empresaId) {
        empleado.setEmpresaId(empresaId);
        if (empleado.getActivo() == null) empleado.setActivo(true);
        validar(empleado, empresaId, true);
        return empleadoGateway.guardar(empleado);
    }

    public Empleado actualizar(Long empleadoId, Empleado cambios, String empresaId) {
        Empleado existente = buscarPorId(empleadoId, empresaId);
        cambios.setEmpleadoId(empleadoId);
        cambios.setEmpresaId(empresaId);
        if (cambios.getActivo() == null) cambios.setActivo(existente.getActivo());
        validar(cambios, empresaId, false);
        return empleadoGateway.guardar(cambios);
    }

    public void eliminar(Long empleadoId, String empresaId) {
        buscarPorId(empleadoId, empresaId);
        empleadoGateway.eliminar(empleadoId, empresaId);
    }

    private void validar(Empleado e, String empresaId, boolean esNuevo) {
        if (e.getNombres() == null || e.getNombres().isBlank())
            throw new RuntimeException("El nombre es obligatorio");
        if (e.getNumeroDocumento() == null || e.getNumeroDocumento().isBlank())
            throw new RuntimeException("El número de documento es obligatorio");
        if (e.getSalarioBase() == null || e.getSalarioBase() <= 0)
            throw new RuntimeException("El salario debe ser mayor a 0");
        if (e.getFechaIngreso() == null)
            throw new RuntimeException("La fecha de ingreso es obligatoria");

        // El salario no puede estar por debajo del mínimo legal vigente.
        if (!Boolean.TRUE.equals(e.getSalarioIntegral()) && e.getSalarioBase() < ParametrosNomina.SMMLV)
            throw new RuntimeException("El salario no puede ser inferior al mínimo legal vigente ($"
                    + String.format("%,.0f", ParametrosNomina.SMMLV) + ")");

        // Salario integral: mínimo 13 SMMLV (10 de salario + 3 de factor prestacional).
        if (Boolean.TRUE.equals(e.getSalarioIntegral()) && e.getSalarioBase() < ParametrosNomina.MINIMO_SALARIO_INTEGRAL)
            throw new RuntimeException("El salario integral no puede ser inferior a 13 SMMLV ($"
                    + String.format("%,.0f", ParametrosNomina.MINIMO_SALARIO_INTEGRAL) + ")");

        if (esNuevo) {
            Empleado duplicado = empleadoGateway.buscarPorDocumento(e.getNumeroDocumento(), empresaId);
            if (duplicado != null)
                throw new RuntimeException("Ya existe un empleado con el documento " + e.getNumeroDocumento());
        }
    }
}
