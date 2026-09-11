package com.pos_backend.venta.infraestructure.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pos_backend.venta.domain.model.FormaPago;
import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.VentaItem;
import com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository.VentaData;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VentaMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public VentaData toVentaData(Venta venta) {
        VentaData data = new VentaData();
        data.setVentaId(venta.getVentaId());
        data.setEmpresaId(venta.getEmpresaId());
        data.setNumeroVenta(venta.getNumeroVenta());
        data.setClienteId(venta.getClienteId());
        data.setClienteNombre(venta.getClienteNombre());
        data.setFecha(venta.getFecha());
        data.setEstado(venta.getEstado());
        data.setCreadoPor(venta.getCreadoPor());
        data.setTieneCreditoCliente(venta.getTieneCreditoCliente());
        data.setFechaVencimientoCredito(venta.getFechaVencimientoCredito());
        data.setSaldoPendiente(venta.getSaldoPendiente());
        data.setCentroCostoId(venta.getCentroCostoId());
        data.setTieneCreditoCliente(venta.getTieneCreditoCliente());
        data.setFechaVencimientoCredito(venta.getFechaVencimientoCredito());
        data.setSaldoPendiente(venta.getSaldoPendiente());
        data.setCentroCostoNombre(venta.getCentroCostoNombre());
        data.setEsObsequio(venta.getEsObsequio());
        data.setSubtotal(venta.getSubtotal());
        data.setDescuentoTotal(venta.getDescuentoTotal());
        data.setTotalIva(venta.getTotalIva());
        data.setTotal(venta.getTotal());

        try {
            data.setItemsJson(objectMapper.writeValueAsString(venta.getItems()));
            data.setFormasPagoJson(objectMapper.writeValueAsString(venta.getFormasPago()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar items o formas de pago");
        }

        return data;
    }

    public Venta toVenta(VentaData data) {
        Venta venta = new Venta();
        venta.setVentaId(data.getVentaId());
        venta.setEmpresaId(data.getEmpresaId());
        venta.setNumeroVenta(data.getNumeroVenta());
        venta.setClienteId(data.getClienteId());
        venta.setClienteNombre(data.getClienteNombre());
        venta.setFecha(data.getFecha());
        venta.setEstado(data.getEstado());
        venta.setCreadoPor(data.getCreadoPor());
        venta.setTieneCreditoCliente(data.getTieneCreditoCliente());
        venta.setFechaVencimientoCredito(data.getFechaVencimientoCredito());
        venta.setSaldoPendiente(data.getSaldoPendiente());
        venta.setCentroCostoId(data.getCentroCostoId());
        venta.setTieneCreditoCliente(data.getTieneCreditoCliente());
        venta.setFechaVencimientoCredito(data.getFechaVencimientoCredito());
        venta.setSaldoPendiente(data.getSaldoPendiente());
        venta.setCentroCostoNombre(data.getCentroCostoNombre());
        venta.setEsObsequio(data.getEsObsequio());
        venta.setSubtotal(data.getSubtotal());
        venta.setDescuentoTotal(data.getDescuentoTotal());
        venta.setTotalIva(data.getTotalIva());
        venta.setTotal(data.getTotal());

        try {
            if (data.getItemsJson() != null) {
                List<VentaItem> items = objectMapper.readValue(
                        data.getItemsJson(), new TypeReference<List<VentaItem>>() {});
                venta.setItems(items);
            }
            if (data.getFormasPagoJson() != null) {
                List<FormaPago> formasPago = objectMapper.readValue(
                        data.getFormasPagoJson(), new TypeReference<List<FormaPago>>() {});
                venta.setFormasPago(formasPago);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error al deserializar items o formas de pago");
        }

        return venta;
    }
}
