package com.pos_backend.venta.infraestructure.driver_adapters.mail;

import com.pos_backend.venta.domain.model.gateway.EmailGateway;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JavaMailEmailGatewayImpl implements EmailGateway {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String remitente;

    @Override
    public void enviarCorreo(String destinatario, String asunto, String cuerpoHtml) {
        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
            helper.setFrom(remitente);
            helper.setTo(destinatario);
            helper.setSubject(asunto);
            helper.setText(cuerpoHtml, true);
            mailSender.send(mensaje);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo enviar el correo: " + e.getMessage());
        }
    }
}
