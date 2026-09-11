package com.pos_backend.contabilidad.infraestructure.entry_points;

import com.pos_backend.contabilidad.domain.model.CentroCosto;
import com.pos_backend.contabilidad.domain.usecase.CentroCostoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pos/contabilidad/centros-costo")
@RequiredArgsConstructor
public class CentroCostoController {

    private final CentroCostoUseCase centroCostoUseCase;

    @GetMapping
    public ResponseEntity<List<CentroCosto>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(centroCostoUseCase.listar(empresaId));
    }

    @PostMapping
    public ResponseEntity<CentroCosto> crear(@RequestBody CentroCosto c,
                                             @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(centroCostoUseCase.crear(c, empresaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CentroCosto> actualizar(@PathVariable Long id, @RequestBody CentroCosto cambios,
                                                  @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(centroCostoUseCase.actualizar(id, cambios, empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id,
                                         @RequestHeader("X-Empresa-Id") String empresaId) {
        centroCostoUseCase.eliminar(id, empresaId);
        return ResponseEntity.noContent().build();
    }
}
