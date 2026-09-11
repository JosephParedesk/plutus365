package com.pos_backend.inventario.domain.usecase;

import com.pos_backend.inventario.domain.model.*;
import com.pos_backend.inventario.domain.model.gateway.CategoriaConsultaGateway;
import com.pos_backend.inventario.domain.model.gateway.ContabilidadGateway;
import com.pos_backend.inventario.domain.model.gateway.LectorExcelGateway;
import com.pos_backend.inventario.domain.model.gateway.MovimientoInventarioGateway;
import com.pos_backend.inventario.domain.model.gateway.ProductoGateway;
import com.pos_backend.inventario.domain.model.gateway.ProveedorConsultaGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@RequiredArgsConstructor
public class ProductoUseCase {

    private final ProductoGateway productoGateway;
    private final MovimientoInventarioGateway movimientoGateway;
    private final LectorExcelGateway lectorExcelGateway;
    private final CategoriaConsultaGateway categoriaConsultaGateway;
    private final ProveedorConsultaGateway proveedorConsultaGateway;
    private final ContabilidadGateway contabilidadGateway;

    public List<Producto> listarProductos(String empresaId) {
        return productoGateway.listarProductos(empresaId);
    }

    public Producto buscarProductoPorSku(String sku, String empresaId) {
        Producto producto = productoGateway.buscarProductoPorSku(sku, empresaId);
        if (producto == null)
            throw new NoSuchElementException("Producto no encontrado");
        return producto;
    }

    public Producto guardarProducto(Producto producto, String empresaId) {
        validar(producto);
        producto.setEmpresaId(empresaId);
        if (producto.getActivo() == null) producto.setActivo(true);
        return productoGateway.guardarProducto(producto);
    }

    public Producto actualizarProducto(String sku, Producto producto, String empresaId) {
        Producto existente = productoGateway.buscarProductoPorSku(sku, empresaId);
        if (existente == null)
            throw new NoSuchElementException("Producto no encontrado");

        validar(producto);
        producto.setSku(sku);
        producto.setEmpresaId(empresaId);
        return productoGateway.guardarProducto(producto);
    }

    public void eliminarProducto(String sku, String empresaId) {
        Producto existente = productoGateway.buscarProductoPorSku(sku, empresaId);
        if (existente == null)
            throw new NoSuchElementException("Producto no encontrado");
        productoGateway.eliminarProductoPorSku(sku, empresaId);
    }

    /**
     * Importa el catálogo (sku, nombre, categoría, proveedor, precios, unidad) desde
     * un Excel. NUNCA toca el stock — ni al crear ni al actualizar — porque las
     * cantidades se cargan aparte con importarSaldosIniciales, igual que en Siigo
     * (Crear productos vs. Saldos iniciales de inventario son cosas separadas).
     */
    public ResultadoImportacion importarCatalogo(byte[] excel, String empresaId) {
        List<FilaProductoExcel> filas = lectorExcelGateway.leerCatalogo(excel);
        ResultadoImportacion resultado = new ResultadoImportacion();

        for (FilaProductoExcel fila : filas) {
            try {
                if (fila.getNombre() == null || fila.getNombre().isBlank())
                    throw new RuntimeException("El nombre es obligatorio");
                if (fila.getPrecioVenta() == null || fila.getPrecioVenta() <= 0)
                    throw new RuntimeException("El precio de venta debe ser mayor a 0");

                Producto existente = productoGateway.buscarProductoPorSku(fila.getSku(), empresaId);
                Producto producto = existente != null ? existente : new Producto();

                producto.setSku(fila.getSku());
                producto.setEmpresaId(empresaId);
                producto.setNombre(fila.getNombre());
                producto.setDescripcion(fila.getDescripcion());
                producto.setCategoriaNombre(fila.getCategoriaNombre());
                producto.setCategoriaId(categoriaConsultaGateway.buscarIdPorNombre(fila.getCategoriaNombre(), empresaId));
                producto.setProveedorNombre(fila.getProveedorNombre());
                producto.setProveedorId(proveedorConsultaGateway.buscarIdPorNombre(fila.getProveedorNombre(), empresaId));
                if (fila.getPrecioCompra() != null) producto.setPrecioCompra(fila.getPrecioCompra());
                else if (producto.getPrecioCompra() == null) producto.setPrecioCompra(0.0);
                producto.setPrecioVenta(fila.getPrecioVenta());
                producto.setStockMinimo(fila.getStockMinimo() != null ? fila.getStockMinimo()
                        : (producto.getStockMinimo() != null ? producto.getStockMinimo() : 5));
                producto.setUnidad(fila.getUnidad() != null && !fila.getUnidad().isBlank() ? fila.getUnidad()
                        : (producto.getUnidad() != null ? producto.getUnidad() : "UND"));
                if (fila.getTipoIva() != null && !fila.getTipoIva().isBlank()) {
                    String tipoIva = fila.getTipoIva().trim().toUpperCase();
                    if (!TIPOS_IVA_VALIDOS.contains(tipoIva))
                        throw new RuntimeException("Tipo de IVA inválido: " + fila.getTipoIva() + " (debe ser GENERAL_19, REDUCIDO_5, EXENTO o EXCLUIDO)");
                    producto.setTipoIva(tipoIva);
                } else if (producto.getTipoIva() == null) {
                    producto.setTipoIva("GENERAL_19");
                }
                if (producto.getActivo() == null) producto.setActivo(true);
                if (existente == null) producto.setStock(0); // recién creado: arranca en 0, se carga con saldos iniciales

                productoGateway.guardarProducto(producto);
                if (existente == null) resultado.setCreados(resultado.getCreados() + 1);
                else resultado.setActualizados(resultado.getActualizados() + 1);
            } catch (Exception e) {
                resultado.getErrores().add(new ResultadoImportacion.ErrorFila(fila.getNumeroFila(), fila.getSku(), e.getMessage()));
            }
        }
        return resultado;
    }

    /**
     * Carga el saldo inicial de inventario (cantidades + costo) desde un Excel y
     * genera UN solo asiento contable por el total: Debe Inventario, Haber la
     * cuenta contrapartida que indique el usuario/contador. Es una operación de
     * una sola vez por empresa — contabilidad-service la rechaza si ya se cargó
     * antes (ver AsientoContableUseCase.generarDesdeSaldoInicialInventario).
     *
     * Primero valida y calcula el total, registra el asiento, y SOLO SI eso
     * funciona aplica los cambios de stock — así, si algo falla (cuenta inválida,
     * ya se había cargado), no queda stock movido sin su respaldo contable.
     */
    public ResultadoSaldoInicial importarSaldosIniciales(byte[] excel, String empresaId, String cuentaContrapartida,
                                                          LocalDate fechaCorte, String creadoPor) {
        List<FilaSaldoInicialExcel> filas = lectorExcelGateway.leerSaldosIniciales(excel);
        ResultadoSaldoInicial resultado = new ResultadoSaldoInicial();

        record FilaValida(Producto producto, int cantidad, double costoUnitario) {}
        List<FilaValida> validas = new ArrayList<>();
        double valorTotal = 0;

        for (FilaSaldoInicialExcel fila : filas) {
            try {
                if (fila.getCantidad() == null || fila.getCantidad() < 0)
                    throw new RuntimeException("La cantidad debe ser un número válido (0 o más)");
                if (fila.getCostoUnitario() == null || fila.getCostoUnitario() < 0)
                    throw new RuntimeException("El costo unitario debe ser un número válido");

                Producto producto = productoGateway.buscarProductoPorSku(fila.getSku(), empresaId);
                if (producto == null)
                    throw new NoSuchElementException("El SKU no existe en el catálogo — importa primero el catálogo de productos");

                validas.add(new FilaValida(producto, fila.getCantidad(), fila.getCostoUnitario()));
                valorTotal += fila.getCantidad() * fila.getCostoUnitario();
            } catch (Exception e) {
                resultado.getErrores().add(new ResultadoImportacion.ErrorFila(fila.getNumeroFila(), fila.getSku(), e.getMessage()));
            }
        }

        if (validas.isEmpty()) return resultado;

        String numeroAsiento = contabilidadGateway.registrarSaldoInicialInventario(
                valorTotal, cuentaContrapartida, fechaCorte, empresaId, creadoPor);

        for (FilaValida fv : validas) {
            int anterior = fv.producto().getStock() != null ? fv.producto().getStock() : 0;
            fv.producto().setStock(fv.cantidad());
            fv.producto().setPrecioCompra(fv.costoUnitario());
            Producto actualizado = productoGateway.guardarProducto(fv.producto());
            registrarMovimiento(actualizado, "SALDO_INICIAL", fv.cantidad(), anterior,
                    actualizado.getStock(), fv.costoUnitario(), "Carga inicial de inventario", empresaId);
            resultado.setProductosActualizados(resultado.getProductosActualizados() + 1);
        }

        resultado.setValorTotal(valorTotal);
        resultado.setNumeroAsiento(numeroAsiento);
        return resultado;
    }

    public List<Producto> listarPorCategoria(String categoriaId, String empresaId) {
        return productoGateway.listarPorCategoria(categoriaId, empresaId);
    }

    public List<Producto> listarPorProveedor(String proveedorId, String empresaId) {
        return productoGateway.listarPorProveedor(proveedorId, empresaId);
    }

    public List<Producto> listarStockBajo(String empresaId) {
        return productoGateway.listarStockBajo(empresaId);
    }

    public Producto descontarStock(String sku, String empresaId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0)
            throw new RuntimeException("La cantidad a descontar debe ser mayor a 0");

        Producto producto = productoGateway.buscarProductoPorSku(sku, empresaId);
        if (producto == null)
            throw new NoSuchElementException("Producto no encontrado: " + sku);

        int stockActual = producto.getStock() != null ? producto.getStock() : 0;
        if (stockActual < cantidad)
            throw new RuntimeException("Stock insuficiente para " + sku + " (disponible: " + stockActual + ", solicitado: " + cantidad + ")");

        Producto actualizado = productoGateway.descontarStock(sku, empresaId, cantidad);
        registrarMovimiento(actualizado, "SALIDA_VENTA", cantidad, stockActual,
                actualizado.getStock(), producto.getPrecioCompra(), null, empresaId);
        return actualizado;
    }

    public Producto incrementarStock(String sku, String empresaId, Integer cantidad) {
        if (cantidad == null || cantidad <= 0)
            throw new RuntimeException("La cantidad a incrementar debe ser mayor a 0");

        Producto producto = productoGateway.buscarProductoPorSku(sku, empresaId);
        if (producto == null)
            throw new NoSuchElementException("Producto no encontrado: " + sku);

        int anterior = producto.getStock() != null ? producto.getStock() : 0;
        Producto actualizado = productoGateway.incrementarStock(sku, empresaId, cantidad);
        registrarMovimiento(actualizado, "AJUSTE_ENTRADA", cantidad, anterior,
                actualizado.getStock(), producto.getPrecioCompra(), null, empresaId);
        return actualizado;
    }

    /**
     * Lo usa compra-service al registrar una compra (manual o importada): si el
     * producto ya existe, suma la cantidad comprada al stock y actualiza el costo
     * al más reciente. Si no existe, lo crea con valores razonables por defecto
     * (el usuario debe revisar precio de venta, categoría y stock mínimo después).
     */
    public ResultadoRegistroStock registrarCompra(RegistroCompraStock datos, String empresaId) {
        if (datos.getSku() == null || datos.getSku().isBlank())
            throw new RuntimeException("El SKU es obligatorio");
        if (datos.getCantidad() == null || datos.getCantidad() <= 0)
            throw new RuntimeException("La cantidad debe ser mayor a 0");

        Producto existente = productoGateway.buscarProductoPorSku(datos.getSku(), empresaId);

        if (existente != null) {
            int anterior = existente.getStock() != null ? existente.getStock() : 0;
            Producto actualizado = productoGateway.incrementarStock(datos.getSku(), empresaId, datos.getCantidad());
            if (datos.getPrecioCompra() != null) {
                actualizado.setPrecioCompra(datos.getPrecioCompra());
                actualizado = productoGateway.guardarProducto(actualizado);
            }
            registrarMovimiento(actualizado, "ENTRADA_COMPRA", datos.getCantidad(), anterior,
                    actualizado.getStock(), datos.getPrecioCompra(), datos.getDocumentoOrigen(), empresaId);
            return new ResultadoRegistroStock(actualizado, false);
        }

        Producto nuevo = new Producto();
        nuevo.setSku(datos.getSku());
        nuevo.setNombre(datos.getNombre() != null && !datos.getNombre().isBlank() ? datos.getNombre() : datos.getSku());
        nuevo.setCategoriaId(datos.getCategoriaId());
        nuevo.setProveedorId(datos.getProveedorId());
        nuevo.setProveedorNombre(datos.getProveedorNombre());
        nuevo.setPrecioCompra(datos.getPrecioCompra() != null ? datos.getPrecioCompra() : 0.0);
        // Precio de venta sugerido con margen del 30% — el usuario debe revisarlo, no se puede adivinar su estrategia de precios.
        nuevo.setPrecioVenta(Math.round((nuevo.getPrecioCompra() != null ? nuevo.getPrecioCompra() : 0.0) * 1.3 * 100) / 100.0);
        nuevo.setStock(datos.getCantidad());
        nuevo.setStockMinimo(5);
        nuevo.setUnidad(datos.getUnidad() != null && !datos.getUnidad().isBlank() ? datos.getUnidad() : "UND");
        nuevo.setActivo(true);
        nuevo.setEmpresaId(empresaId);

        Producto creado = productoGateway.guardarProducto(nuevo);
        registrarMovimiento(creado, "ENTRADA_COMPRA", datos.getCantidad(), 0,
                creado.getStock(), datos.getPrecioCompra(), datos.getDocumentoOrigen(), empresaId);
        return new ResultadoRegistroStock(creado, true);
    }

    public record ResultadoRegistroStock(Producto producto, boolean creado) {}

    /** Deja rastro de cada cambio de stock. Si esto falla, NO se tumba la operación
     *  principal: es preferible perder una línea del Kárdex que perder una venta. */
    private void registrarMovimiento(Producto p, String tipo, Integer cantidad, Integer saldoAnterior,
                                     Integer saldoNuevo, Double costo, String documento, String empresaId) {
        try {
            MovimientoInventario m = new MovimientoInventario();
            m.setEmpresaId(empresaId);
            m.setSku(p.getSku());
            m.setNombreProducto(p.getNombre());
            m.setFecha(LocalDateTime.now());
            m.setTipo(tipo);
            m.setDocumentoOrigen(documento);
            m.setCantidad(cantidad);
            m.setSaldoAnterior(saldoAnterior);
            m.setSaldoNuevo(saldoNuevo);
            m.setCostoUnitario(costo != null ? costo : p.getPrecioCompra());
            m.setValorMovimiento((costo != null ? costo : (p.getPrecioCompra() != null ? p.getPrecioCompra() : 0.0)) * cantidad);
            movimientoGateway.guardar(m);
        } catch (Exception ignored) { }
    }

    public List<MovimientoInventario> kardexPorSku(String sku, String empresaId) {
        return movimientoGateway.listarPorSku(sku, empresaId);
    }

    public List<MovimientoInventario> movimientosPorRango(String empresaId, LocalDateTime desde, LocalDateTime hasta) {
        return movimientoGateway.listarPorRango(empresaId, desde, hasta);
    }

    private static final java.util.Set<String> TIPOS_IVA_VALIDOS =
            java.util.Set.of("GENERAL_19", "REDUCIDO_5", "EXENTO", "EXCLUIDO");

    private void validar(Producto producto) {
        if (producto.getSku() == null || producto.getSku().isBlank())
            throw new RuntimeException("El SKU es obligatorio");
        if (producto.getNombre() == null || producto.getNombre().isBlank())
            throw new RuntimeException("El nombre del producto es obligatorio");
        if (producto.getPrecioVenta() == null || producto.getPrecioVenta() <= 0)
            throw new RuntimeException("El precio de venta debe ser mayor a 0");
        if (producto.getTipoIva() == null || producto.getTipoIva().isBlank())
            producto.setTipoIva("GENERAL_19");
        else if (!TIPOS_IVA_VALIDOS.contains(producto.getTipoIva().toUpperCase()))
            throw new RuntimeException("El tipo de IVA debe ser uno de: " + TIPOS_IVA_VALIDOS);
        else
            producto.setTipoIva(producto.getTipoIva().toUpperCase());
    }
}