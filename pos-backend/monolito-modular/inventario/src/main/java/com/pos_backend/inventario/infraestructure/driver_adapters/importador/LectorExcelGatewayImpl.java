package com.pos_backend.inventario.infraestructure.driver_adapters.importador;

import com.pos_backend.inventario.domain.model.FilaProductoExcel;
import com.pos_backend.inventario.domain.model.FilaSaldoInicialExcel;
import com.pos_backend.inventario.domain.model.gateway.LectorExcelGateway;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Lee las plantillas de Excel (.xlsx/.xls) de importación de inventario. Columnas
 * fijas por posición (la primera fila es el encabezado, se ignora su contenido):
 *
 *  Catálogo:        sku | nombre | descripcion | categoria | proveedor | precioCompra | precioVenta | stockMinimo | unidad | tipoIva
 *  Saldos iniciales: sku | cantidad | costoUnitario
 */
@Component
public class LectorExcelGatewayImpl implements LectorExcelGateway {

    @Override
    public List<FilaProductoExcel> leerCatalogo(byte[] contenido) {
        List<FilaProductoExcel> filas = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(contenido))) {
            Sheet hoja = wb.getSheetAt(0);
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila == null) continue;
                String sku = texto(fila.getCell(0));
                if (sku == null || sku.isBlank()) continue; // fila vacía, se salta sin contarla como error

                FilaProductoExcel f = new FilaProductoExcel();
                f.setNumeroFila(i + 1);
                f.setSku(sku.trim());
                f.setNombre(texto(fila.getCell(1)));
                f.setDescripcion(texto(fila.getCell(2)));
                f.setCategoriaNombre(texto(fila.getCell(3)));
                f.setProveedorNombre(texto(fila.getCell(4)));
                f.setPrecioCompra(numero(fila.getCell(5)));
                f.setPrecioVenta(numero(fila.getCell(6)));
                Double stockMinimo = numero(fila.getCell(7));
                f.setStockMinimo(stockMinimo != null ? stockMinimo.intValue() : null);
                f.setUnidad(texto(fila.getCell(8)));
                f.setTipoIva(texto(fila.getCell(9)));
                filas.add(f);
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer el archivo de Excel: " + e.getMessage());
        }
        return filas;
    }

    @Override
    public List<FilaSaldoInicialExcel> leerSaldosIniciales(byte[] contenido) {
        List<FilaSaldoInicialExcel> filas = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(new ByteArrayInputStream(contenido))) {
            Sheet hoja = wb.getSheetAt(0);
            for (int i = 1; i <= hoja.getLastRowNum(); i++) {
                Row fila = hoja.getRow(i);
                if (fila == null) continue;
                String sku = texto(fila.getCell(0));
                if (sku == null || sku.isBlank()) continue;

                FilaSaldoInicialExcel f = new FilaSaldoInicialExcel();
                f.setNumeroFila(i + 1);
                f.setSku(sku.trim());
                Double cantidad = numero(fila.getCell(1));
                f.setCantidad(cantidad != null ? cantidad.intValue() : null);
                f.setCostoUnitario(numero(fila.getCell(2)));
                filas.add(f);
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo leer el archivo de Excel: " + e.getMessage());
        }
        return filas;
    }

    private static final DataFormatter FORMATO = new DataFormatter(Locale.of("es", "CO"));

    // DataFormatter no muta la celda (a diferencia de forzar setCellType), y lee
    // cualquier tipo (texto, número, fórmula) como el usuario lo vería en Excel.
    private String texto(Cell celda) {
        if (celda == null) return null;
        String valor = FORMATO.formatCellValue(celda);
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private Double numero(Cell celda) {
        if (celda == null) return null;
        try {
            if (celda.getCellType() == CellType.NUMERIC || celda.getCellType() == CellType.FORMULA)
                return celda.getNumericCellValue();
            String valor = FORMATO.formatCellValue(celda);
            return valor == null || valor.isBlank() ? null : Double.parseDouble(valor.trim().replace(",", "."));
        } catch (Exception e) {
            return null;
        }
    }
}
