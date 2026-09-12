package com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.auth.domain.model.Usuario;
import com.pos_backend.auth.domain.model.gateway.UsuarioGateway;
import com.pos_backend.auth.infraestructure.mapper.UsuarioMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UsuarioDataGatewayImpl implements UsuarioGateway {

    private final UsuarioDataJpaRepository usuarioDataJpaRepository;
    private final UsuarioMapper usuarioMapper;

    @Override
    public Usuario guardarUsuario(Usuario usuario) {
        UsuarioData usuarioDataGuardar = usuarioMapper.tousuarioData(usuario);

        return usuarioMapper.toUsuario(usuarioDataJpaRepository.save(usuarioDataGuardar));
    }

    @Override
    public Usuario buscarUsuarioPorCc(String cedula) {

        UsuarioData usarioEncontrado = usuarioDataJpaRepository.findById(cedula).orElse(null);

        if(usarioEncontrado == null){
            return null;
        }

        return usuarioMapper.toUsuario(usarioEncontrado);
    }


    @Override
    public void eliminarUsuarioPorCc(String cedula) {
        usuarioDataJpaRepository.deleteById(cedula);
    }

    @Override
    public Usuario buscarPorCorreo(String correo) {
        return usuarioDataJpaRepository.findByCorreo(correo).map(usuarioMapper::toUsuario).orElse(null);
    }

    @Override
    public Usuario buscarPorResetToken(String token) {
        return usuarioDataJpaRepository.findByResetPasswordToken(token).map(usuarioMapper::toUsuario).orElse(null);
    }

    @Override
    public Usuario buscarPorRefreshToken(String refreshToken) {
        return usuarioDataJpaRepository.findByRefreshToken(refreshToken).map(usuarioMapper::toUsuario).orElse(null);
    }

    @Override
    public int contarPorEmpresa(String empresaId) {
        return usuarioDataJpaRepository.countByEmpresaId(empresaId);
    }

    @Override
    public java.util.List<Usuario> listarPorEmpresa(String empresaId) {
        return usuarioDataJpaRepository.findByEmpresaId(empresaId).stream()
                .map(usuarioMapper::toUsuario)
                .toList();
    }

    @Override
    public java.util.List<Usuario> listarTodos() {
        return usuarioDataJpaRepository.findAll().stream()
                .map(usuarioMapper::toUsuario)
                .toList();
    }
}
