package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.pos_backend.venta.domain.model.Cotizacion;
import com.pos_backend.venta.domain.model.VentaItem;
import com.pos_backend.venta.domain.model.gateway.CotizacionGateway;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository @RequiredArgsConstructor
public class CotizacionDataGatewayImpl implements CotizacionGateway {

    private final CotizacionDataJpaRepository repository;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public Cotizacion guardar(Cotizacion c) {
        CotizacionData d = new CotizacionData();
        BeanUtils.copyProperties(c, d, "items");
        try {
            d.setItemsJson(objectMapper.writeValueAsString(c.getItems()));
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar los ítems de la cotización");
        }
        return toDomain(repository.save(d));
    }

    @Override
    public Cotizacion buscarPorId(Long id, String empresaId) {
        return repository.findByCotizacionIdAndEmpresaId(id, empresaId).map(this::toDomain).orElse(null);
    }

    @Override
    public List<Cotizacion> listar(String empresaId) {
        return repository.findByEmpresaIdOrderByCotizacionIdDesc(empresaId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Cotizacion> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta) {
        return repository.findByEmpresaIdAndFechaBetween(empresaId, desde.atStartOfDay(), hasta.atTime(23, 59, 59))
                .stream().map(this::toDomain).toList();
    }

    @Override
    public String generarSiguienteNumero(String empresaId) {
        return "COT-" + String.format("%05d", repository.countByEmpresaId(empresaId) + 1);
    }

    private Cotizacion toDomain(CotizacionData d) {
        Cotizacion c = new Cotizacion();
        BeanUtils.copyProperties(d, c, "itemsJson");
        try {
            if (d.getItemsJson() != null)
                c.setItems(objectMapper.readValue(d.getItemsJson(), new TypeReference<List<VentaItem>>() {}));
        } catch (Exception e) {
            throw new RuntimeException("Error al leer los ítems de la cotización");
        }
        return c;
    }
}
