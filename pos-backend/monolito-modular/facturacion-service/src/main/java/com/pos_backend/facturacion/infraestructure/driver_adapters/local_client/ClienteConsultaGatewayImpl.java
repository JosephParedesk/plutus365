package com.pos_backend.facturacion.infraestructure.driver_adapters.local_client;

import com.pos_backend.cliente.domain.model.Cliente;
import com.pos_backend.cliente.domain.usecase.ClienteUseCase;
import com.pos_backend.facturacion.domain.model.ClienteRemoto;
import com.pos_backend.facturacion.domain.model.gateway.ClienteConsultaGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

// Reemplaza el http_client original (RestClient a cliente-service): ahora que
// cliente-service ya está migrado, es una llamada directa al UseCase. El 404
// HTTP que antes se traducía a null ahora es NoSuchElementException.
@Component("facturacionClienteConsultaGatewayImpl")
@RequiredArgsConstructor
public class ClienteConsultaGatewayImpl implements ClienteConsultaGateway {

    private final ClienteUseCase clienteUseCase;

    @Override
    public ClienteRemoto buscarCliente(Long clienteId, String empresaId) {
        Cliente c;
        try {
            c = clienteUseCase.buscarClientePorId(clienteId, empresaId);
        } catch (NoSuchElementException e) {
            return null;
        }
        return new ClienteRemoto(
                c.getClienteId(), c.getTipoPersona(), c.getTipoDocumento(), c.getNumeroDocumento(), c.getDv(),
                c.getRegimenFiscal(), c.getRazonSocial(), c.getNombres(), c.getApellidos(), c.getCorreo(),
                c.getTelefono(), c.getDireccion(), c.getCiudad(), c.getDepartamento(), c.getPais());
    }
}
