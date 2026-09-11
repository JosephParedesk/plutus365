package com.pos_backend.auth.domain.model.gateway;

public interface EncrypterGateway {

    String encrypt(String contrasena);
    boolean matches(String rawPassword, String encryptedPassword);

}
