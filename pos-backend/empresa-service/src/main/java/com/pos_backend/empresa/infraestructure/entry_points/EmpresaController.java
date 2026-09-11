package com.pos_backend.empresa.infraestructure.entry_points;

import com.pos_backend.empresa.domain.model.Empresa;
import com.pos_backend.empresa.domain.usecase.EmpresaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pos/empresa")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaUseCase empresaUseCase;

    @Value("${app.upload.dir:uploads/logos}")
    private String uploadDir;

    @Value("${app.public-url:http://localhost:8088}")
    private String publicUrl;

    private static final List<String> TIPOS_PERMITIDOS = List.of("image/png", "image/jpeg", "image/webp");

    // ── Obtener configuración de la empresa ──────────────────────
    @GetMapping
    public ResponseEntity<Empresa> obtener(
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(empresaUseCase.obtenerConfiguracion(empresaId));
    }

    // ── Crear o actualizar configuración (upsert) ────────────────
    @PutMapping
    public ResponseEntity<Empresa> guardar(
            @RequestBody Empresa empresa,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(empresaUseCase.guardarConfiguracion(empresa, empresaId));
    }

    // ── Subir/reemplazar el logo ──────────────────────────────────
    @PostMapping(value = "/logo", consumes = "multipart/form-data")
    public ResponseEntity<Empresa> subirLogo(
            @RequestParam("logo") MultipartFile logo,
            @RequestHeader("X-Empresa-Id") String empresaId) throws IOException {

        if (logo.isEmpty())
            throw new RuntimeException("El archivo del logo está vacío");
        if (!TIPOS_PERMITIDOS.contains(logo.getContentType()))
            throw new RuntimeException("El logo debe ser PNG, JPG o WEBP");

        Path directorio = Paths.get(uploadDir);
        Files.createDirectories(directorio);

        String extension = switch (logo.getContentType()) {
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> ".jpg";
        };
        String nombreArchivo = empresaId + "_" + UUID.randomUUID() + extension;
        Path destino = directorio.resolve(nombreArchivo);
        Files.copy(logo.getInputStream(), destino);

        String logoUrl = publicUrl + "/uploads/logos/" + nombreArchivo;
        return ResponseEntity.ok(empresaUseCase.actualizarLogo(empresaId, logoUrl));
    }
}
