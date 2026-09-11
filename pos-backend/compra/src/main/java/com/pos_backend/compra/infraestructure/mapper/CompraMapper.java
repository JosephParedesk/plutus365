package com.pos_backend.compra.infraestructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pos_backend.compra.domain.model.AplicacionPago;
import com.pos_backend.compra.domain.model.Compra;
import com.pos_backend.compra.domain.model.CompraItem;
import com.pos_backend.compra.domain.model.FormaPago;
import com.pos_backend.compra.infraestructure.driver_adapters.jpa_repository.CompraData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompraMapper {

    // FormaPago.fechaVencimiento es LocalDate y se serializa dentro de formasPagoJson,
    // por eso hace falta JavaTimeModule.
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompraData toCompraData(Compra compra) {
        CompraData data = new CompraData();
        data.setCompraId(compra.getCompraId());
        data.setEmpresaId(compra.getEmpresaId());
        data.setTipoTransaccion(compra.getTipoTransaccion());
        data.setNumeroComprobante(compra.getNumeroComprobante());
        data.setFacturaProveedor(compra.getFacturaProveedor());
        data.setCufeProveedor(compra.getCufeProveedor());
        data.setCentroCostoId(compra.getCentroCostoId());
        data.setCentroCostoNombre(compra.getCentroCostoNombre());
        data.setProveedorId(compra.getProveedorId());
        data.setProveedorNombre(compra.getProveedorNombre());
        data.setFechaElaboracion(compra.getFechaElaboracion());
        data.setCreadoPor(compra.getCreadoPor());
        data.setSucursal(compra.getSucursal());
        data.setEstado(compra.getEstado());
        data.setTotalBruto(compra.getTotalBruto());
        data.setTotalDescuentos(compra.getTotalDescuentos());
        data.setSubtotal(compra.getSubtotal());
        data.setTotalIva(compra.getTotalIva());
        data.setTotalRetencion(compra.getTotalRetencion());
        data.setTotalPagar(compra.getTotalPagar());
        data.setTieneCreditoProveedor(compra.getTieneCreditoProveedor());
        data.setFechaVencimientoCredito(compra.getFechaVencimientoCredito());
        data.setSaldoPendiente(compra.getSaldoPendiente());
        data.setCompraReferenciaId(compra.getCompraReferenciaId());
        data.setTipoRecibo(compra.getTipoRecibo());
        data.setOrigenDinero(compra.getOrigenDinero());
        data.setObservaciones(compra.getObservaciones());

        try {
            data.setItemsJson(objectMapper.writeValueAsString(compra.getItems()));
            data.setFormasPagoJson(objectMapper.writeValueAsString(compra.getFormasPago()));
            data.setAplicacionesJson(objectMapper.writeValueAsString(compra.getAplicaciones()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar items, formas de pago o aplicaciones");
        }

        return data;
    }

    public Compra toCompra(CompraData data) {
        Compra compra = new Compra();
        compra.setCompraId(data.getCompraId());
        compra.setEmpresaId(data.getEmpresaId());
        compra.setTipoTransaccion(data.getTipoTransaccion());
        compra.setNumeroComprobante(data.getNumeroComprobante());
        compra.setFacturaProveedor(data.getFacturaProveedor());
        compra.setCufeProveedor(data.getCufeProveedor());
        compra.setCentroCostoId(data.getCentroCostoId());
        compra.setCentroCostoNombre(data.getCentroCostoNombre());
        compra.setProveedorId(data.getProveedorId());
        compra.setProveedorNombre(data.getProveedorNombre());
        compra.setFechaElaboracion(data.getFechaElaboracion());
        compra.setCreadoPor(data.getCreadoPor());
        compra.setSucursal(data.getSucursal());
        compra.setEstado(data.getEstado());
        compra.setTotalBruto(data.getTotalBruto());
        compra.setTotalDescuentos(data.getTotalDescuentos());
        compra.setSubtotal(data.getSubtotal());
        compra.setTotalIva(data.getTotalIva());
        compra.setTotalRetencion(data.getTotalRetencion());
        compra.setTotalPagar(data.getTotalPagar());
        compra.setTieneCreditoProveedor(data.getTieneCreditoProveedor());
        compra.setFechaVencimientoCredito(data.getFechaVencimientoCredito());
        compra.setSaldoPendiente(data.getSaldoPendiente());
        compra.setCompraReferenciaId(data.getCompraReferenciaId());
        compra.setTipoRecibo(data.getTipoRecibo());
        compra.setOrigenDinero(data.getOrigenDinero());
        compra.setObservaciones(data.getObservaciones());

        try {
            if (data.getItemsJson() != null) {
                List<CompraItem> items = objectMapper.readValue(
                        data.getItemsJson(), new TypeReference<List<CompraItem>>() {});
                compra.setItems(items);
            }
            if (data.getFormasPagoJson() != null) {
                List<FormaPago> formasPago = objectMapper.readValue(
                        data.getFormasPagoJson(), new TypeReference<List<FormaPago>>() {});
                compra.setFormasPago(formasPago);
            }
            if (data.getAplicacionesJson() != null) {
                List<AplicacionPago> aplicaciones = objectMapper.readValue(
                        data.getAplicacionesJson(), new TypeReference<List<AplicacionPago>>() {});
                compra.setAplicaciones(aplicaciones);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al deserializar items, formas de pago o aplicaciones");
        }

        return compra;
    }
}