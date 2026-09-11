package com.pos_backend.compra.infraestructure.entry_points;

import com.pos_backend.compra.domain.model.FacturaImportada;
import com.pos_backend.compra.domain.usecase.ImportarFacturaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/pos/compras/importar")
@RequiredArgsConstructor
public class ImportarFacturaController {

    private final ImportarFacturaUseCase importarFacturaUseCase;

    @PostMapping(value = "/xml", consumes = "multipart/form-data")
    public ResponseEntity<FacturaImportada> importarXml(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        return ResponseEntity.ok(importarFacturaUseCase.importarXml(archivo.getBytes()));
    }

    @PostMapping(value = "/pdf", consumes = "multipart/form-data")
    public ResponseEntity<FacturaImportada> importarPdf(@RequestParam("archivo") MultipartFile archivo) throws IOException {
        return ResponseEntity.ok(importarFacturaUseCase.importarPdf(archivo.getBytes()));
    }
}
