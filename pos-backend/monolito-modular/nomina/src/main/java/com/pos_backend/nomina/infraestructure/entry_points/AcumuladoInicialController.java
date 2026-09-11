package com.pos_backend.nomina.infraestructure.entry_points;

import com.pos_backend.nomina.domain.model.AcumuladoInicial;
import com.pos_backend.nomina.domain.usecase.AcumuladoInicialUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pos/nomina/acumulados-iniciales")
@RequiredArgsConstructor
public class AcumuladoInicialController {

    private final AcumuladoInicialUseCase acumuladoInicialUseCase;

    @GetMapping
    public ResponseEntity<List<AcumuladoInicial>> listar(
            @RequestParam Integer anio,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(acumuladoInicialUseCase.listar(empresaId, anio));
    }

    @PostMapping
    public ResponseEntity<AcumuladoInicial> guardar(
            @RequestBody AcumuladoInicial acumulado,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(acumuladoInicialUseCase.guardar(acumulado, empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @RequestHeader("X-Empresa-Id") String empresaId) {
        acumuladoInicialUseCase.eliminar(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
