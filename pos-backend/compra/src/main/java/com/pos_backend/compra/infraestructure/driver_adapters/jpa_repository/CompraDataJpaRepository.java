package com.pos_backend.compra.infraestructure.driver_adapters.jpa_repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CompraDataJpaRepository extends JpaRepository<CompraData, Long> {

    List<CompraData> findByEmpresaId(String empresaId);

    Optional<CompraData> findByCompraIdAndEmpresaId(Long compraId, String empresaId);

    List<CompraData> findByEmpresaIdAndProveedorId(String empresaId, Long proveedorId);

    List<CompraData> findByEmpresaIdAndTipoTransaccion(String empresaId, String tipoTransaccion);

    List<CompraData> findByEmpresaIdAndFechaElaboracionBetween(
            String empresaId, LocalDate fechaInicio, LocalDate fechaFin);

    long countByEmpresaIdAndTipoTransaccion(String empresaId, String tipoTransaccion);

    // Cuentas por pagar próximas a vencer (para la notificación del crédito a proveedores)
    List<CompraData> findByEmpresaIdAndTieneCreditoProveedorTrueAndSaldoPendienteGreaterThanAndFechaVencimientoCreditoBetweenAndEstadoNot(
            String empresaId, Double saldoMinimo, LocalDate desde, LocalDate hasta, String estadoExcluido);
}