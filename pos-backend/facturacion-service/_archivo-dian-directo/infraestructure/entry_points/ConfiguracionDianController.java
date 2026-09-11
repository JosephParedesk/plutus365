package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.ConfiguracionDian;
import com.pos_backend.facturacion.domain.usecase.ConfiguracionDianUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pos/facturacion/configuracion")
@RequiredArgsConstructor
public class ConfiguracionDianController {

    private final ConfiguracionDianUseCase configuracionDianUseCase;

    @GetMapping
    public ResponseEntity<ConfiguracionDian> obtener(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(configuracionDianUseCase.obtener(empresaId));
    }

    @PutMapping
    public ResponseEntity<ConfiguracionDian> guardar(
            @RequestBody ConfiguracionDian configuracion,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(configuracionDianUseCase.guardar(configuracion, empresaId));
    }

    // Sube el certificado digital (.p12/.pfx). Se guarda como firmadigital_{empresaId}.p12
    @PostMapping(value = "/certificado", consumes = "multipart/form-data")
    public ResponseEntity<Void> subirCertificado(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("password") String password,
            @RequestHeader("X-Empresa-Id") String empresaId) throws IOException {
        configuracionDianUseCase.subirCertificado(empresaId, archivo.getBytes(), password);
        return ResponseEntity.noContent().build();
    }
}
