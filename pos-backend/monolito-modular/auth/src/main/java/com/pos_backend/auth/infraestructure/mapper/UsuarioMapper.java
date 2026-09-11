package com.pos_backend.auth.infraestructure.mapper;

import com.pos_backend.auth.domain.model.Usuario;

import com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository.UsuarioData;
import org.springframework.stereotype.Component;

@Component
public class UsuarioMapper {

    public UsuarioData tousuarioData(Usuario usuario){
        return new UsuarioData(
                usuario.getCedula(),
                usuario.getTipoDocumento(),
                usuario.getNombre(),
                usuario.getCorreo(),
                usuario.getContrasena(),
                usuario.getTelefono(),
                usuario.getRol(),
                usuario.getPlanId(),
                usuario.getEmpresaId(),
                usuario.getResetPasswordToken(),
                usuario.getResetPasswordTokenExpiry(),
                usuario.getRefreshToken(),
                usuario.getRefreshTokenExpiry(),
                usuario.getRefreshTokenRecordarme()
        );
    }

    public Usuario toUsuario(UsuarioData usuarioData){
        return new Usuario(
                usuarioData.getCedula(),
                usuarioData.getTipoDocumento(),
                usuarioData.getNombre(),
                usuarioData.getCorreo(),
                usuarioData.getContrasena(),
                usuarioData.getTelefono(),
                usuarioData.getRol(),
                usuarioData.getPlanId(),
                usuarioData.getEmpresaId(),
                usuarioData.getResetPasswordToken(),
                usuarioData.getResetPasswordTokenExpiry(),
                usuarioData.getRefreshToken(),
                usuarioData.getRefreshTokenExpiry(),
                usuarioData.getRefreshTokenRecordarme()
        );
    }
}
