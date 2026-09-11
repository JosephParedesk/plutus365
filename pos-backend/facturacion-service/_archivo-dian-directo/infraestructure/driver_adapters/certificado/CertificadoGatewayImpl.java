package com.pos_backend.facturacion.infraestructure.driver_adapters.certificado;

import com.pos_backend.facturacion.domain.model.gateway.CertificadoGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.Enumeration;

@Component
@RequiredArgsConstructor
public class CertificadoGatewayImpl implements CertificadoGateway {

    private final CertificadoSecretoDataJpaRepository secretoRepository;
    private final CertificadoCrypto crypto;

    @Value("${cert.storage.dir}")
    private String storageDir;

    private String rutaArchivo(String empresaId) {
        // Exactamente el nombre pedido: firmadigital_{empresaId}.p12
        return storageDir + "/firmadigital_" + empresaId + ".p12";
    }

    @Override
    public void guardarCertificado(String empresaId, byte[] contenidoPfx, String password) {
        // 1. Valida que el .pfx y el password realmente abran, y que traiga
        //    la cadena completa (firmante + emisor + raíz), ANTES de guardar nada.
        cargarKeyStore(contenidoPfx, password);

        try {
            Path dir = Path.of(storageDir);
            Files.createDirectories(dir);
            Files.write(Path.of(rutaArchivo(empresaId)), contenidoPfx);
        } catch (Exception e) {
            throw new RuntimeException("No se pudo guardar el certificado en el servidor: " + e.getMessage());
        }

        CertificadoSecretoData secreto = secretoRepository.findById(empresaId).orElse(new CertificadoSecretoData());
        secreto.setEmpresaId(empresaId);
        secreto.setNombreArchivo("firmadigital_" + empresaId + ".p12");
        secreto.setPasswordEncriptado(crypto.encriptar(password));
        secreto.setCargadoEn(LocalDateTime.now().toString());
        secretoRepository.save(secreto);
    }

    @Override
    public boolean existeCertificado(String empresaId) {
        return Files.exists(Path.of(rutaArchivo(empresaId))) && secretoRepository.existsById(empresaId);
    }

    @Override
    public MaterialFirma cargarMaterialFirma(String empresaId) {
        CertificadoSecretoData secreto = secretoRepository.findById(empresaId)
                .orElseThrow(() -> new RuntimeException("No hay certificado digital cargado para esta empresa"));

        byte[] contenidoPfx;
        try {
            contenidoPfx = Files.readAllBytes(Path.of(rutaArchivo(empresaId)));
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer el certificado guardado. Vuelve a subirlo en Configuración.");
        }

        String password = crypto.desencriptar(secreto.getPasswordEncriptado());
        return cargarKeyStore(contenidoPfx, password);
    }

    private MaterialFirma cargarKeyStore(byte[] contenidoPfx, String password) {
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            keyStore.load(new ByteArrayInputStream(contenidoPfx), password.toCharArray());

            String alias = primerAliasConClave(keyStore);
            if (alias == null)
                throw new RuntimeException("El certificado no contiene una llave privada");

            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, password.toCharArray());
            Certificate[] cadena = keyStore.getCertificateChain(alias);

            if (cadena == null || cadena.length == 0)
                throw new RuntimeException("El certificado no contiene la cadena de certificación");

            X509Certificate firmante = (X509Certificate) cadena[0];

            if (cadena.length < 3) {
                throw new RuntimeException(
                        "El archivo .pfx no trae la cadena completa (firmante + emisor + raíz), solo " +
                        cadena.length + " certificado(s). La DIAN exige los 3 para la firma XAdES. " +
                        "Algunas certificadoras (ej. GSE) no embeben la cadena en el .pfx; en ese caso " +
                        "contáctanos para habilitar la carga manual de los certificados intermedio y raíz.");
            }

            X509Certificate emisor = (X509Certificate) cadena[1];
            X509Certificate caRaiz = (X509Certificate) cadena[2];

            return new MaterialFirma(privateKey, firmante, emisor, caRaiz);

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                    "No se pudo abrir el certificado: verifica que el archivo .pfx/.p12 y el password sean correctos. (" +
                    e.getMessage() + ")");
        }
    }

    private String primerAliasConClave(KeyStore keyStore) throws Exception {
        Enumeration<String> aliases = keyStore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keyStore.isKeyEntry(alias)) return alias;
        }
        return null;
    }
}
