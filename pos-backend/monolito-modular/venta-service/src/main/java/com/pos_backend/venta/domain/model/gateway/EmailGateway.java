package com.pos_backend.venta.domain.model.gateway;

public interface EmailGateway {
    void enviarCorreo(String destinatario, String asunto, String cuerpoHtml);
}
