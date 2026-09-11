package com.pos_backend.inventario.infraestructure.entry_points;

import com.pos_backend.inventario.domain.model.Producto;
import com.pos_backend.inventario.domain.model.ResultadoImportacion;
import com.pos_backend.inventario.domain.model.ResultadoSaldoInicial;
import com.pos_backend.inventario.domain.usecase.ProductoUseCase;
import com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository.ProductoData;
import com.pos_backend.inventario.infraestructure.mapper.ProductoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/surtiana/inventario")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoUseCase productoUseCase;
    private final ProductoMapper productoMapper;

    // ── Listar todos ──────────────────────────────────────────────
    @GetMapping("/productos")
    public ResponseEntity<List<Producto>> listarProductos(
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(productoUseCase.listarProductos(empresaId));
    }

    // ── Buscar por SKU ────────────────────────────────────────────
    @GetMapping("/buscar/{sku}")
    public ResponseEntity<Producto> buscarProducto(
            @PathVariable String sku,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        Producto encontrado = productoUseCase.buscarProductoPorSku(sku, empresaId);
        if (encontrado.getSku() != null) {
            return ResponseEntity.ok(encontrado);
        }
        return ResponseEntity.notFound().build();
    }

    // ── Crear ─────────────────────────────────────────────────────
    @PostMapping("/save")
    public ResponseEntity<Producto> saveProducto(
            @RequestBody ProductoData productoData,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        Producto producto = productoMapper.toProducto(productoData);
        producto.setEmpresaId(empresaId);
        Producto guardado = productoUseCase.guardarProducto(producto, empresaId);
        if (guardado.getSku() != null) {
            return ResponseEntity.ok(guardado);
        }
        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    // ── Actualizar ────────────────────────────────────────────────
    @PutMapping("/actualizar/{sku}")
    public ResponseEntity<Producto> actualizarProducto(
            @PathVariable String sku,
            @RequestBody ProductoData productoData,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        productoData.setSku(sku);
        Producto producto = productoMapper.toProducto(productoData);
        Producto actualizado = productoUseCase.actualizarProducto(sku, producto, empresaId);
        return ResponseEntity.ok(actualizado);
    }

    // ── Eliminar ──────────────────────────────────────────────────
    @DeleteMapping("/eliminar/{sku}")
    public ResponseEntity<Void> eliminarProducto(
            @PathVariable String sku,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        productoUseCase.eliminarProducto(sku, empresaId);
        return ResponseEntity.noContent().build();
    }

    // ── Por categoría ─────────────────────────────────────────────
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<Producto>> porCategoria(
            @PathVariable String categoriaId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(productoUseCase.listarPorCategoria(categoriaId, empresaId));
    }

    // ── Por proveedor ──────────────────────────────────────────────
    @GetMapping("/proveedor/{proveedorId}")
    public ResponseEntity<List<Producto>> porProveedor(
            @PathVariable String proveedorId,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(productoUseCase.listarPorProveedor(proveedorId, empresaId));
    }

    // ── Stock bajo ────────────────────────────────────────────────
    @GetMapping("/stock-bajo")
    public ResponseEntity<List<Producto>> stockBajo(
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(productoUseCase.listarStockBajo(empresaId));
    }

    // ── Descontar stock (usado por venta-service al confirmar una venta) ──
    @PatchMapping("/stock/{sku}/descontar")
    public ResponseEntity<Producto> descontarStock(
            @PathVariable String sku,
            @RequestBody java.util.Map<String, Integer> body,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(productoUseCase.descontarStock(sku, empresaId, body.get("cantidad")));
    }

    // ── Incrementar stock (usado al anular una venta o compensar un error) ──
    @PatchMapping("/stock/{sku}/incrementar")
    public ResponseEntity<Producto> incrementarStock(
            @PathVariable String sku,
            @RequestBody java.util.Map<String, Integer> body,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(productoUseCase.incrementarStock(sku, empresaId, body.get("cantidad")));
    }

    // ── Registrar compra (usado por compra-service): suma stock si el producto ya
    // existe, o lo crea automáticamente si es la primera vez que se compra ──
    // ── Kárdex: histórico de movimientos de un producto ──
    @GetMapping("/kardex/{sku}")
    public ResponseEntity<java.util.List<com.pos_backend.inventario.domain.model.MovimientoInventario>> kardex(
            @PathVariable String sku,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(productoUseCase.kardexPorSku(sku, empresaId));
    }

    // ── Todos los movimientos en un rango de fechas ──
    @GetMapping("/movimientos")
    public ResponseEntity<java.util.List<com.pos_backend.inventario.domain.model.MovimientoInventario>> movimientos(
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate desde,
            @RequestParam @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) java.time.LocalDate hasta,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(productoUseCase.movimientosPorRango(
                empresaId, desde.atStartOfDay(), hasta.atTime(23, 59, 59)));
    }

    @PostMapping("/stock/registrar-compra")
    public ResponseEntity<ProductoUseCase.ResultadoRegistroStock> registrarCompra(
            @RequestBody com.pos_backend.inventario.domain.model.RegistroCompraStock datos,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(productoUseCase.registrarCompra(datos, empresaId));
    }

    // ── Importar catálogo desde Excel (sku, nombre, categoría, proveedor, precios,
    // stockMinimo, unidad) — NO toca cantidades, eso es aparte con saldos iniciales ──
    @PostMapping(value = "/importar-catalogo", consumes = "multipart/form-data")
    public ResponseEntity<ResultadoImportacion> importarCatalogo(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestHeader("X-Empresa-Id") String empresaId) throws IOException {
        return ResponseEntity.ok(productoUseCase.importarCatalogo(archivo.getBytes(), empresaId));
    }

    // ── Cargar saldos iniciales de inventario (cantidad + costo por SKU) desde
    // Excel. Genera un asiento contable único: Debe Inventario, Haber la cuenta
    // contrapartida que se indique. Es de una sola vez por empresa. ──
    @PostMapping(value = "/importar-saldos-iniciales", consumes = "multipart/form-data")
    public ResponseEntity<ResultadoSaldoInicial> importarSaldosIniciales(
            @RequestParam("archivo") MultipartFile archivo,
            @RequestParam("cuentaContrapartida") String cuentaContrapartida,
            @RequestParam(value = "fechaCorte", required = false)
            @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE) LocalDate fechaCorte,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) throws IOException {
        return ResponseEntity.ok(productoUseCase.importarSaldosIniciales(
                archivo.getBytes(), empresaId, cuentaContrapartida, fechaCorte, creadoPor));
    }
}
