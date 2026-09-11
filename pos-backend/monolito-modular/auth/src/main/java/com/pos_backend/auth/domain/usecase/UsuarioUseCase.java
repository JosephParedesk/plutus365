package com.pos_backend.auth.domain.usecase;


import com.pos_backend.auth.domain.model.Usuario;
import com.pos_backend.auth.domain.model.gateway.EncrypterGateway;
import com.pos_backend.auth.domain.model.gateway.JwtGateway;
import com.pos_backend.auth.domain.model.gateway.NotificationGateway;
import com.pos_backend.auth.domain.model.gateway.UsuarioGateway;
import com.pos_backend.auth.domain.model.TokenResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;
import java.util.UUID;

@RequiredArgsConstructor
public class UsuarioUseCase {

    private final UsuarioGateway usuarioGateway;
    private final EncrypterGateway encrypterGateway;
    private final JwtGateway jwtGateway;
    private final NotificationGateway notificationGateway;



    public Usuario guardarUsuario(Usuario usuario) {

        if (usuario.getCedula() == null || usuario.getCedula().trim().isEmpty() ||
                usuario.getNombre() == null || usuario.getNombre().trim().isEmpty() ||
                usuario.getCorreo() == null || usuario.getCorreo().trim().isEmpty() ||
                usuario.getContrasena() == null || usuario.getContrasena().trim().isEmpty()) {
            throw new RuntimeException("Faltan campos obligatorios o están vacíos");
        }

        if (usuario.getRol() == null || usuario.getRol().trim().isEmpty()) {
            usuario.setRol("ADMIN");
        }

        // ← genera el empresaId automáticamente para admins
        if ("ADMIN".equalsIgnoreCase(usuario.getRol()) || usuario.getEmpresaId() == null) {
            usuario.setEmpresaId("EMP-" + usuario.getCedula());
        }

        String passEncrypter = encrypterGateway.encrypt(usuario.getContrasena());
        usuario.setContrasena(passEncrypter);

        Usuario usuarioGuardado = usuarioGateway.guardarUsuario(usuario);
        notificationGateway.enviarNotificacion(usuarioGuardado);

        return usuarioGuardado;
    }

    public Usuario buscarUsuarioPorCc(String cedula){

        Usuario usuario = usuarioGateway.buscarUsuarioPorCc(cedula);

        if(usuario == null){
            throw new NoSuchElementException("Usuario no encontrado");
        }

        return usuario;
    }

    public void eliminarUsuarioPorCc (String cedula){

        Usuario usuario = usuarioGateway.buscarUsuarioPorCc(cedula);

        if(usuario == null){
            throw new NoSuchElementException("Usuario no encontrado");
        }

        usuarioGateway.eliminarUsuarioPorCc(cedula);
    }

    // Días que dura el refreshToken sin volver a pedir contraseña: 30 si el usuario
    // marcó "recordarme" en el login, 1 si no. El access token (JWT) sigue durando
    // 1h fijo siempre (JwtGatewayImpl) — recordarme solo afecta cuánto tiempo se
    // puede seguir renovando ese JWT sin volver a autenticarse.
    private static final int DIAS_REFRESH_RECORDADO = 30;
    private static final int DIAS_REFRESH_NORMAL = 1;

    public TokenResponse login(String email, String password, boolean recordarme) {

        if (email == null || password == null) {
            throw new RuntimeException("Email y contraseña son obligatorios");
        }

        if (!email.contains("@")) {
            throw new RuntimeException("Correo inválido");
        }

        Usuario usuario = usuarioGateway.buscarPorCorreo(email);

        if (usuario == null || usuario.getCedula() == null) {
            throw new RuntimeException("Usuario no encontrado");
        }

        if (usuario.getContrasena() == null) {
            throw new RuntimeException("Error en datos del usuario");
        }

        if (!encrypterGateway.matches(password, usuario.getContrasena())) {
            throw new RuntimeException("Contraseña incorrecta");
        }

        return emitirTokens(usuario, recordarme);
    }

    public TokenResponse refrescarToken(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank())
            throw new RuntimeException("El refresh token es obligatorio");

        Usuario usuario = usuarioGateway.buscarPorRefreshToken(refreshToken);
        if (usuario == null)
            throw new RuntimeException("Sesión inválida, inicia sesión de nuevo");

        if (usuario.getRefreshTokenExpiry() == null || LocalDateTime.now().isAfter(usuario.getRefreshTokenExpiry()))
            throw new RuntimeException("Sesión expirada, inicia sesión de nuevo");

        boolean recordarme = usuario.getRefreshTokenRecordarme() != null && usuario.getRefreshTokenRecordarme();
        // Rota el refreshToken en cada renovación (sliding session): mientras el
        // usuario siga usando la app dentro de la ventana de "recordarme", nunca
        // tiene que volver a loguearse; si la deja quieta más de esa ventana, sí.
        return emitirTokens(usuario, recordarme);
    }

    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        Usuario usuario = usuarioGateway.buscarPorRefreshToken(refreshToken);
        if (usuario == null) return;
        usuario.setRefreshToken(null);
        usuario.setRefreshTokenExpiry(null);
        usuarioGateway.guardarUsuario(usuario);
    }

    private TokenResponse emitirTokens(Usuario usuario, boolean recordarme) {
        String accessToken = jwtGateway.generarToken(usuario);

        String refreshToken = UUID.randomUUID().toString();
        LocalDateTime expiry = LocalDateTime.now().plusDays(recordarme ? DIAS_REFRESH_RECORDADO : DIAS_REFRESH_NORMAL);
        usuario.setRefreshToken(refreshToken);
        usuario.setRefreshTokenExpiry(expiry);
        usuario.setRefreshTokenRecordarme(recordarme);
        usuarioGateway.guardarUsuario(usuario);

        return new TokenResponse(accessToken, refreshToken, expiry);
    }
    public void forgotPassword(String email) {

        if (email == null || email.trim().isEmpty()) {
            throw new RuntimeException("El correo es obligatorio");
        }

        Usuario usuario = usuarioGateway.buscarPorCorreo(email);

        if (usuario == null) {
            throw new NoSuchElementException("No existe una cuenta con ese correo");
        }

        String token = UUID.randomUUID().toString();
        usuario.setResetPasswordToken(token);
        usuario.setResetPasswordTokenExpiry(LocalDateTime.now().plusMinutes(30));

        usuarioGateway.guardarUsuario(usuario);
        notificationGateway.enviarNotificacionRecuperacion(usuario);
    }

    public void resetPassword(String token, String nuevaContrasena) {

        if (token == null || token.trim().isEmpty()) {
            throw new RuntimeException("El token es obligatorio");
        }

        if (nuevaContrasena == null || nuevaContrasena.trim().isEmpty()) {
            throw new RuntimeException("La nueva contraseña es obligatoria");
        }

        Usuario usuario = usuarioGateway.buscarPorResetToken(token);

        if (usuario == null) {
            throw new RuntimeException("Token inválido o expirado");
        }

        if (usuario.getResetPasswordTokenExpiry() == null ||
                LocalDateTime.now().isAfter(usuario.getResetPasswordTokenExpiry())) {
            throw new RuntimeException("El token ha expirado");
        }

        usuario.setContrasena(encrypterGateway.encrypt(nuevaContrasena));
        usuario.setResetPasswordToken(null);
        usuario.setResetPasswordTokenExpiry(null);

        usuarioGateway.guardarUsuario(usuario);
    }
    private static final java.util.Set<String> ROLES_VALIDOS = java.util.Set.of("ADMIN", "CAJERO", "CONTADOR", "INVENTARIO");

    public Usuario crearEmpleado(Usuario usuario, String token) {
        String tokenLimpio = token.replace("Bearer ", "");

        // extrae el empresaId y el rol del admin que hace la petición
        String empresaId = jwtGateway.extraerEmpresaId(tokenLimpio);
        String rolSolicitante = jwtGateway.extraerRol(tokenLimpio);

        if (empresaId == null)
            throw new RuntimeException("No se pudo identificar la empresa");

        if (rolSolicitante != null && !rolSolicitante.isBlank() && !"ADMIN".equalsIgnoreCase(rolSolicitante))
            throw new RuntimeException("Solo un administrador puede crear usuarios");

        if (usuario.getRol() == null || usuario.getRol().isBlank())
            usuario.setRol("CAJERO");
        if (!ROLES_VALIDOS.contains(usuario.getRol().toUpperCase()))
            throw new RuntimeException("Rol inválido. Debe ser uno de: " + ROLES_VALIDOS);
        usuario.setRol(usuario.getRol().toUpperCase());

        // verifica límite de perfiles según el plan
        Long planId = jwtGateway.extraerPlanId(tokenLimpio);
        int totalUsuarios = usuarioGateway.contarPorEmpresa(empresaId);
        int maxPerfiles = obtenerMaxPerfiles(planId);

        if (totalUsuarios >= maxPerfiles)
            throw new RuntimeException("Has alcanzado el límite de perfiles de tu plan");

        // hereda el empresaId del admin
        usuario.setEmpresaId(empresaId);
        usuario.setContrasena(encrypterGateway.encrypt(usuario.getContrasena()));

        return usuarioGateway.guardarUsuario(usuario);
    }

    public java.util.List<Usuario> listarEmpleados(String token) {
        String empresaId = jwtGateway.extraerEmpresaId(token.replace("Bearer ", ""));
        if (empresaId == null)
            throw new RuntimeException("No se pudo identificar la empresa");
        return usuarioGateway.listarPorEmpresa(empresaId);
    }

    public void eliminarEmpleado(String cedula, String token) {
        String tokenLimpio = token.replace("Bearer ", "");
        String rolSolicitante = jwtGateway.extraerRol(tokenLimpio);
        if (rolSolicitante != null && !rolSolicitante.isBlank() && !"ADMIN".equalsIgnoreCase(rolSolicitante))
            throw new RuntimeException("Solo un administrador puede eliminar usuarios");

        String empresaId = jwtGateway.extraerEmpresaId(tokenLimpio);
        Usuario empleado = usuarioGateway.buscarUsuarioPorCc(cedula);
        if (empleado == null)
            throw new NoSuchElementException("Usuario no encontrado");
        if (empresaId != null && !empresaId.equals(empleado.getEmpresaId()))
            throw new RuntimeException("No puedes eliminar usuarios de otra empresa");

        usuarioGateway.eliminarUsuarioPorCc(cedula);
    }

    private int obtenerMaxPerfiles(Long planId) {
        if (planId == null) return 2;
        return switch (planId.intValue()) {
            case 1 -> 2;
            case 2 -> 10;
            case 3 -> 999;
            default -> 2;
        };
    }
}