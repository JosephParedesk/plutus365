package com.pos_backend.facturacion.infraestructure.driver_adapters.local_client;

import com.pos_backend.facturacion.domain.model.EmpleadoRemota;
import com.pos_backend.facturacion.domain.model.gateway.EmpleadoConsultaGateway;
import com.pos_backend.nomina.domain.model.Empleado;
import com.pos_backend.nomina.domain.usecase.EmpleadoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

// Reemplaza el http_client original (RestClient a nomina-service, GET
// /empleados/{id}): ahora que nomina ya está migrado, es una llamada directa
// al UseCase.
@Component("facturacionEmpleadoConsultaGatewayImpl")
@RequiredArgsConstructor
public class EmpleadoConsultaGatewayImpl implements EmpleadoConsultaGateway {

    private final EmpleadoUseCase empleadoUseCase;

    @Override
    public EmpleadoRemota buscarEmpleado(Long empleadoId, String empresaId) {
        Empleado e;
        try {
            e = empleadoUseCase.buscarPorId(empleadoId, empresaId);
        } catch (NoSuchElementException ex) {
            return null;
        }
        return new EmpleadoRemota(
                e.getEmpleadoId(), e.getTipoDocumento(), e.getNumeroDocumento(), e.getNombres(), e.getApellidos(),
                e.getDireccion(), e.getCiudad(), e.getTipoContrato(), e.getFechaIngreso(), e.getSalarioBase(),
                e.getSalarioIntegral(), e.getBancoPago(), e.getTipoCuenta(), e.getNumeroCuenta());
    }
}
