package com.pos_backend.auth.infraestructure.entry_points;


import com.pos_backend.auth.domain.model.TokenResponse;
import com.pos_backend.auth.domain.model.Usuario;
import com.pos_backend.auth.domain.model.gateway.JwtGateway;
import com.pos_backend.auth.domain.usecase.UsuarioUseCase;
import com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository.UsuarioData;
import com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository.dto.ForgotPasswordRequest;
import com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository.dto.LoginRequest;
import com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository.dto.RefreshTokenRequest;
import com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository.dto.ResetPasswordRequest;
import com.pos_backend.auth.infraestructure.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/pos/usuario")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioUseCase usuarioUseCase;
    private final UsuarioMapper usuarioMapper;
    private final JwtGateway jwtGateway;

    // Registro público deshabilitado temporalmente (2026-09-11, pedido del
    // usuario) — por ahora solo se permite iniciar sesión con cuentas ya
    // existentes. Mismo guard que en el monolito (ver
    // monolito-modular/auth/.../UsuarioController.java), replicado acá por si
    // se hace rollback a este standalone durante el período de quemado.
    @PostMapping("/save")
    public ResponseEntity<Usuario> saveUsauraio(@RequestBody UsuarioData usuarioData){
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @GetMapping("/buscar/{cedula}")
    public ResponseEntity<Usuario> buscarUsuario(@PathVariable String cedula){

        return ResponseEntity.ok(
                usuarioUseCase.buscarUsuarioPorCc(cedula)
        );
    }

    @DeleteMapping("/eliminar/{cedula}")
    public ResponseEntity<Void> eliminarUsuario(@PathVariable String cedula, @RequestHeader("Authorization") String authHeader) {
        usuarioUseCase.eliminarEmpleado(cedula, authHeader);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request){
        boolean recordarme = request.getRecordarme() != null && request.getRecordarme();
        return ResponseEntity.ok(usuarioUseCase.login(request.getCorreo(), request.getContrasena(), recordarme));
    }

    // Renueva el access token (JWT, 1h) sin pedir contraseña, mientras el
    // refreshToken siga vigente — ver UsuarioUseCase.refrescarToken.
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@RequestBody RefreshTokenRequest request){
        return ResponseEntity.ok(usuarioUseCase.refrescarToken(request.getRefreshToken()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody RefreshTokenRequest request){
        usuarioUseCase.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }


    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestBody ForgotPasswordRequest request){

        usuarioUseCase.forgotPassword(request.getEmail());

        return ResponseEntity.ok("Se ha enviado un token de recuperación al correo registrado");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request){

        usuarioUseCase.resetPassword(request.getToken(), request.getNuevaContrasena());

        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }
    @PostMapping("/crear-empleado")
    public ResponseEntity<Usuario> crearEmpleado(
            @RequestBody Usuario usuario,
            @RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(usuarioUseCase.crearEmpleado(usuario, token));
    }

    @GetMapping("/empleados")
    public ResponseEntity<java.util.List<Usuario>> listarEmpleados(@RequestHeader("Authorization") String token) {
        return ResponseEntity.ok(usuarioUseCase.listarEmpleados(token));
    }

}
