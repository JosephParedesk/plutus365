package com.pos_backend.facturacion.infraestructure.driver_adapters.certificado;

import org.springframework.data.jpa.repository.JpaRepository;

// OJO: este archivo se perdió al borrarlo (nunca se leyó su contenido en la sesión
// que hizo el cambio a Factus) — esto es una RECONSTRUCCIÓN por uso, no el original
// recuperado. Se dedujo de cómo lo usaba CertificadoGatewayImpl.java (solo
// findById/save/existsById, heredados de JpaRepository, sin queries propias), así
// que lo más probable es que fuera exactamente esto. Verifícalo si lo vas a reusar.
public interface CertificadoSecretoDataJpaRepository extends JpaRepository<CertificadoSecretoData, String> {
}
