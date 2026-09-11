package com.pos_backend.nomina.infraestructure.entry_points;

import com.pos_backend.nomina.domain.model.Empleado;
import com.pos_backend.nomina.domain.usecase.EmpleadoUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pos/nomina/empleados")
@RequiredArgsConstructor
public class EmpleadoController {

    private final EmpleadoUseCase empleadoUseCase;

    @GetMapping
    public ResponseEntity<List<Empleado>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(empleadoUseCase.listar(empresaId));
    }

    @GetMapping("/{empleadoId}")
    public ResponseEntity<Empleado> buscar(@PathVariable Long empleadoId,
                                           @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(empleadoUseCase.buscarPorId(empleadoId, empresaId));
    }

    @PostMapping
    public ResponseEntity<Empleado> crear(@RequestBody Empleado empleado,
                                          @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(empleadoUseCase.crear(empleado, empresaId));
    }

    @PutMapping("/{empleadoId}")
    public ResponseEntity<Empleado> actualizar(@PathVariable Long empleadoId, @RequestBody Empleado cambios,
                                               @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(empleadoUseCase.actualizar(empleadoId, cambios, empresaId));
    }

    @DeleteMapping("/{empleadoId}")
    public ResponseEntity<Void> eliminar(@PathVariable Long empleadoId,
                                         @RequestHeader("X-Empresa-Id") String empresaId) {
        empleadoUseCase.eliminar(empleadoId, empresaId);
        return ResponseEntity.noContent().build();
    }
}
