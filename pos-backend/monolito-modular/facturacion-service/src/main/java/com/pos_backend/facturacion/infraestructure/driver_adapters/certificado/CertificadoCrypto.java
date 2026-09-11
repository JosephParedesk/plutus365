package com.pos_backend.facturacion.infraestructure.driver_adapters.certificado;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Cifra secretos (password del certificado, credenciales de Factus) con
 * AES-256-GCM antes de guardarlos. La llave sale de la variable de entorno
 * CERT_ENCRYPTION_KEY (base64, 32 bytes). Genera una con: openssl rand -base64 32
 */
@Component
public class CertificadoCrypto {

    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final SecretKeySpec key;

    public CertificadoCrypto(@Value("${cert.encryption.key}") String encryptionKeyBase64) {
        if (encryptionKeyBase64 == null || encryptionKeyBase64.isBlank())
            throw new IllegalStateException(
                    "Falta configurar CERT_ENCRYPTION_KEY. Genera una con: openssl rand -base64 32");
        byte[] decoded = Base64.getDecoder().decode(encryptionKeyBase64);
        this.key = new SecretKeySpec(decoded, "AES");
    }

    public String encriptar(String textoPlano) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            byte[] cifrado = cipher.doFinal(textoPlano.getBytes());

            ByteBuffer buffer = ByteBuffer.allocate(iv.length + cifrado.length);
            buffer.put(iv).put(cifrado);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception e) {
            throw new RuntimeException("Error al cifrar el secreto");
        }
    }

    public String desencriptar(String textoCifrado) {
        try {
            byte[] data = Base64.getDecoder().decode(textoCifrado);
            byte[] iv = new byte[IV_LENGTH];
            byte[] cifrado = new byte[data.length - IV_LENGTH];
            System.arraycopy(data, 0, iv, 0, IV_LENGTH);
            System.arraycopy(data, IV_LENGTH, cifrado, 0, cifrado.length);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(TAG_LENGTH_BITS, iv));
            return new String(cipher.doFinal(cifrado));
        } catch (Exception e) {
            throw new RuntimeException("Error al descifrar el secreto");
        }
    }
}
