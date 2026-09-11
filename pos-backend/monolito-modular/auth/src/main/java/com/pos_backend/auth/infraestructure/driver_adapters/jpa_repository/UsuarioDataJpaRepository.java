package com.pos_backend.auth.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface UsuarioDataJpaRepository extends JpaRepository<UsuarioData, String> {

    Optional<UsuarioData> findByCorreo(String correo);

    Optional<UsuarioData> findByResetPasswordToken(String resetPasswordToken);

    Optional<UsuarioData> findByRefreshToken(String refreshToken);

    int countByEmpresaId(String empresaId);

    java.util.List<UsuarioData> findByEmpresaId(String empresaId);

}
