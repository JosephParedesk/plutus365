package com.pos_backend.contabilidad.infraestructure.entry_points;

import com.pos_backend.contabilidad.domain.model.CuentaContable;
import com.pos_backend.contabilidad.domain.usecase.CuentaContableUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/contabilidad/cuentas")
@RequiredArgsConstructor
public class CuentaContableController {

    private final CuentaContableUseCase cuentaContableUseCase;

    // Primera vez que una empresa llama esto, se siembra automáticamente el PUC base.
    @GetMapping
    public ResponseEntity<List<CuentaContable>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cuentaContableUseCase.listar(empresaId));
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<CuentaContable> buscar(
            @PathVariable String codigo,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cuentaContableUseCase.buscarPorCodigo(codigo, empresaId));
    }

    @GetMapping("/{codigo}/hijas")
    public ResponseEntity<List<CuentaContable>> hijas(
            @PathVariable String codigo,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cuentaContableUseCase.listarHijas(codigo, empresaId));
    }

    @PostMapping
    public ResponseEntity<CuentaContable> crear(
            @RequestBody CuentaContable cuenta,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cuentaContableUseCase.crear(cuenta, empresaId));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<CuentaContable> actualizar(
            @PathVariable String codigo,
            @RequestBody CuentaContable cambios,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(cuentaContableUseCase.actualizar(codigo, cambios, empresaId));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> eliminar(
            @PathVariable String codigo,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        cuentaContableUseCase.eliminar(codigo, empresaId);
        return ResponseEntity.noContent().build();
    }
}
