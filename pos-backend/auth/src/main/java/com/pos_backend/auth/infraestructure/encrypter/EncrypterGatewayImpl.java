package com.pos_backend.auth.infraestructure.encrypter;


import com.pos_backend.auth.domain.model.gateway.EncrypterGateway;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class EncrypterGatewayImpl implements EncrypterGateway {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
    @Override
    public String encrypt(String contrasena) {
        return encoder.encode(contrasena);
    }

    @Override
    public boolean matches(String rawPassword, String encryptedPassword) {
        return encoder.matches(rawPassword, encryptedPassword);
    }
}
