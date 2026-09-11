package com.pos_backend.proveedor.infraestructure.entry_points;

import com.pos_backend.proveedor.domain.model.Proveedor;
import com.pos_backend.proveedor.domain.usecase.ProveedorUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pos/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorUseCase proveedorUseCase;

    @GetMapping("/listar")
    public ResponseEntity<List<Proveedor>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(proveedorUseCase.listarProveedores(empresaId));
    }

    @GetMapping("/buscar/{proveedorId}")
    public ResponseEntity<Proveedor> buscarPorId(
            @PathVariable Long proveedorId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(proveedorUseCase.buscarProveedorPorId(proveedorId, empresaId));
    }

    @GetMapping("/nit/{nit}")
    public ResponseEntity<Proveedor> buscarPorNit(
            @PathVariable String nit,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(proveedorUseCase.buscarProveedorPorNit(nit, empresaId));
    }

    @PostMapping("/save")
    public ResponseEntity<Proveedor> guardar(
            @RequestBody Proveedor proveedor,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(proveedorUseCase.guardarProveedor(proveedor, empresaId));
    }

    @PutMapping("/actualizar/{proveedorId}")
    public ResponseEntity<Proveedor> actualizar(
            @PathVariable Long proveedorId,
            @RequestBody Proveedor proveedor,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(proveedorUseCase.actualizarProveedor(proveedorId, proveedor, empresaId));
    }

    @DeleteMapping("/eliminar/{proveedorId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long proveedorId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        proveedorUseCase.eliminarProveedor(proveedorId, empresaId);
        return ResponseEntity.noContent().build();
    }
}
