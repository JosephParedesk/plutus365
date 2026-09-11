package com.pos_backend.venta.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.venta.domain.model.Venta;
import com.pos_backend.venta.domain.model.gateway.VentaGateway;
import com.pos_backend.venta.infraestructure.mapper.VentaMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class VentaDataGatewayImpl implements VentaGateway {

    private final VentaDataJpaRepository ventaDataJpaRepository;
    private final VentaMapper ventaMapper;

    @Override
    public Venta guardarVenta(Venta venta) {
        try {
            VentaData saved = ventaDataJpaRepository.save(ventaMapper.toVentaData(venta));
            return ventaMapper.toVenta(saved);
        } catch (DataIntegrityViolationException e) {
            // Choca la restricción única (empresa_id, numero_venta): dos ventas
            // concurrentes generaron el mismo número. VentaUseCase reconoce este
            // prefijo en el mensaje y reintenta con un número nuevo.
            throw new RuntimeException("NUMERO_DUPLICADO: ya existe una venta con el número " + venta.getNumeroVenta());
        }
    }

    @Override
    public Venta buscarVentaPorId(Long ventaId, String empresaId) {
        return ventaDataJpaRepository.findByVentaIdAndEmpresaId(ventaId, empresaId)
                .map(ventaMapper::toVenta)
                .orElse(null);
    }

    @Override
    public List<Venta> listarVentas(String empresaId) {
        return ventaDataJpaRepository.findByEmpresaId(empresaId)
                .stream().map(ventaMapper::toVenta).toList();
    }

    @Override
    public List<Venta> buscarConFiltros(String empresaId, Long clienteId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<VentaData> base;

        if (fechaInicio != null && fechaFin != null) {
            base = ventaDataJpaRepository.findByEmpresaIdAndFechaBetween(
                    empresaId, fechaInicio.atStartOfDay(), LocalDateTime.of(fechaFin, LocalTime.MAX));
        } else {
            base = ventaDataJpaRepository.findByEmpresaId(empresaId);
        }

        return base.stream()
                .filter(v -> clienteId == null || clienteId.equals(v.getClienteId()))
                .map(ventaMapper::toVenta)
                .toList();
    }

    @Override
    public List<Venta> listarPorRangoExacto(String empresaId, LocalDateTime desde, LocalDateTime hasta) {
        return ventaDataJpaRepository.findByEmpresaIdAndFechaBetween(empresaId, desde, hasta)
                .stream().map(ventaMapper::toVenta).toList();
    }

    @Override
    public void anularVenta(Long ventaId, String empresaId) {
        ventaDataJpaRepository.findByVentaIdAndEmpresaId(ventaId, empresaId)
                .ifPresent(data -> {
                    data.setEstado("ANULADA");
                    ventaDataJpaRepository.save(data);
                });
    }

    @Override
    public String generarSiguienteNumero(String empresaId) {
        long total = ventaDataJpaRepository.countByEmpresaId(empresaId) + 1;
        return "VT-" + String.format("%05d", total);
    }
}
