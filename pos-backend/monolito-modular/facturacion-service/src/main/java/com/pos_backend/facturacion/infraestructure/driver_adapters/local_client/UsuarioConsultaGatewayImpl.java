package com.pos_backend.facturacion.infraestructure.driver_adapters.local_client;

import com.pos_backend.auth.domain.usecase.UsuarioUseCase;
import com.pos_backend.facturacion.domain.model.UsuarioAdminResumen;
import com.pos_backend.facturacion.domain.model.gateway.UsuarioConsultaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

// Llama directo al UseCase de auth (mismo JVM) — nunca por HTTP. Solo lo usa
// AdminUseCase (panel de super admin), ver ese comentario para el porqué.
@Component("facturacionUsuarioConsultaGatewayImpl")
@RequiredArgsConstructor
public class UsuarioConsultaGatewayImpl implements UsuarioConsultaGateway {

    private final UsuarioUseCase usuarioUseCase;

    @Override
    public List<UsuarioAdminResumen> listarTodos() {
        return usuarioUseCase.listarTodos().stream()
                .map(u -> new UsuarioAdminResumen(u.getCedula(), u.getNombre(), u.getCorreo(), u.getRol(), u.getEmpresaId()))
                .toList();
    }
}
