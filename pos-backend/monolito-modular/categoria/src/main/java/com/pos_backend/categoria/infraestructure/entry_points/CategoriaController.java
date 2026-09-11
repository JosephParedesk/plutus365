package com.pos_backend.categoria.infraestructure.entry_points;

import com.pos_backend.categoria.domain.model.Categoria;
import com.pos_backend.categoria.domain.usecase.CategoriaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pos/categorias")
@RequiredArgsConstructor
public class CategoriaController {

    private final CategoriaUseCase categoriaUseCase;

    @GetMapping("/listar")
    public ResponseEntity<List<Categoria>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(categoriaUseCase.listarCategorias(empresaId));
    }

    @GetMapping("/buscar/{categoriaId}")
    public ResponseEntity<Categoria> buscar(
            @PathVariable Long categoriaId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(categoriaUseCase.buscarCategoriaPorId(categoriaId, empresaId));
    }

    @PostMapping("/save")
    public ResponseEntity<Categoria> guardar(
            @RequestBody Categoria categoria,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(categoriaUseCase.guardarCategoria(categoria, empresaId));
    }

    @PutMapping("/actualizar/{categoriaId}")
    public ResponseEntity<Categoria> actualizar(
            @PathVariable Long categoriaId,
            @RequestBody Categoria categoria,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(categoriaUseCase.actualizarCategoria(categoriaId, categoria, empresaId));
    }

    @DeleteMapping("/eliminar/{categoriaId}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long categoriaId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        categoriaUseCase.eliminarCategoria(categoriaId, empresaId);
        return ResponseEntity.noContent().build();
    }
}
