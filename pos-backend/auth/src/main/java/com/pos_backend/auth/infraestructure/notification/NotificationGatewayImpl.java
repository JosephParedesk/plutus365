package com.pos_backend.auth.infraestructure.notification;

import com.pos_backend.auth.domain.model.Usuario;
import com.pos_backend.auth.domain.model.gateway.NotificationGateway;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

// Antes esto llamaba a un "notification-service" en el puerto 9092 que nunca
// existió en este proyecto — la recuperación de contraseña fallaba siempre.
// Ahora manda el correo de verdad, con el mismo mecanismo SMTP (Brevo) que ya
// usan venta-service y facturacion-service.
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationGatewayImpl implements NotificationGateway {

    private final JavaMailSender mailSender;

    @Value("${mail.from}")
    private String remitente;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Override
    public void enviarNotificacion(Usuario usuario) {
        try {
            enviar(usuario.getCorreo(), "Bienvenido a Plutus365", correoBienvenida(usuario));
            log.info("Correo de bienvenida enviado a: {}", usuario.getCorreo());
        } catch (Exception e) {
            // El registro no debe fallar solo porque el correo de bienvenida no salió.
            log.error("Error enviando correo de bienvenida a {}: {}", usuario.getCorreo(), e.getMessage());
        }
    }

    @Override
    public void enviarNotificacionRecuperacion(Usuario usuario) {
        try {
            enviar(usuario.getCorreo(), "Recupera tu contraseña — Plutus365", correoRecuperacion(usuario));
            log.info("Correo de recuperación enviado a: {}", usuario.getCorreo());
        } catch (Exception e) {
            log.error("Error enviando correo de recuperación a {}: {}", usuario.getCorreo(), e.getMessage());
            // Acá sí se propaga: si el usuario pidió recuperar la contraseña y el correo
            // no salió, tiene que enterarse — no puede quedarse esperando algo que nunca llega.
            throw new RuntimeException("No fue posible enviar el correo de recuperación");
        }
    }

    private void enviar(String destinatario, String asunto, String cuerpoHtml) throws Exception {
        MimeMessage mensaje = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mensaje, "UTF-8");
        helper.setFrom(remitente);
        helper.setTo(destinatario);
        helper.setSubject(asunto);
        helper.setText(cuerpoHtml, true);
        mailSender.send(mensaje);
    }

    private String correoBienvenida(Usuario usuario) {
        return "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto;color:#333\">"
                + "<h2 style=\"color:#4E6F3A;margin-bottom:2px\">¡Bienvenido a Plutus365!</h2>"
                + "<p style=\"font-size:14px;line-height:1.5\">Hola " + usuario.getNombre() + ", tu cuenta ya está lista. "
                + "Ya podés entrar y empezar a configurar tu empresa.</p>"
                + "<p style=\"color:#aaa;font-size:11px;margin-top:24px\">Si no creaste esta cuenta, podés ignorar este correo.</p>"
                + "</div>";
    }

    private String correoRecuperacion(Usuario usuario) {
        String link = frontendUrl + "/restablecer-contrasena?token=" + usuario.getResetPasswordToken();
        return "<div style=\"font-family:Arial,sans-serif;max-width:480px;margin:auto;color:#333\">"
                + "<h2 style=\"color:#4E6F3A;margin-bottom:2px\">Recupera tu contraseña</h2>"
                + "<p style=\"font-size:14px;line-height:1.5\">Hola " + usuario.getNombre() + ", pediste restablecer tu contraseña. "
                + "Hacé clic en el botón de abajo — el enlace vence en 30 minutos.</p>"
                + "<p style=\"text-align:center;margin:24px 0\">"
                + "<a href=\"" + link + "\" style=\"background:#4E6F3A;color:#fff;padding:12px 28px;border-radius:14px;"
                + "text-decoration:none;font-weight:600;display:inline-block\">Restablecer contraseña</a>"
                + "</p>"
                + "<p style=\"font-size:12px;color:#888\">Si el botón no funciona, copiá y pegá este enlace en tu navegador:<br>"
                + "<span style=\"word-break:break-all\">" + link + "</span></p>"
                + "<p style=\"color:#aaa;font-size:11px;margin-top:24px\">Si no pediste este cambio, podés ignorar este correo — "
                + "tu contraseña actual sigue funcionando.</p>"
                + "</div>";
    }
}
