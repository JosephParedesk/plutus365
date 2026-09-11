package com.pos_backend.auth.domain.model.gateway;


import com.pos_backend.auth.domain.model.Usuario;

public interface UsuarioGateway {

Usuario guardarUsuario(Usuario usuario);

Usuario buscarUsuarioPorCc (String cedula);

void eliminarUsuarioPorCc (String cedula);

Usuario buscarPorCorreo(String correo);

Usuario buscarPorResetToken(String token);

Usuario buscarPorRefreshToken(String refreshToken);

int contarPorEmpresa(String empresaId);

java.util.List<Usuario> listarPorEmpresa(String empresaId);

}
