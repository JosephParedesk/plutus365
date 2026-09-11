package com.pos_backend.inventario.domain.model.gateway;

import com.pos_backend.inventario.domain.model.FilaProductoExcel;
import com.pos_backend.inventario.domain.model.FilaSaldoInicialExcel;

import java.util.List;

public interface LectorExcelGateway {
    List<FilaProductoExcel> leerCatalogo(byte[] contenido);
    List<FilaSaldoInicialExcel> leerSaldosIniciales(byte[] contenido);
}
