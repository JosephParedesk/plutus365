package com.pos_backend.contabilidad.infraestructure.entry_points;

import com.pos_backend.contabilidad.domain.model.BalanceGeneral;
import com.pos_backend.contabilidad.domain.model.CambiosPatrimonio;
import com.pos_backend.contabilidad.domain.model.EstadoResultados;
import com.pos_backend.contabilidad.domain.model.FlujoEfectivo;
import com.pos_backend.contabilidad.domain.usecase.EstadosFinancierosUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/pos/contabilidad/estados-financieros")
@RequiredArgsConstructor
public class EstadosFinancierosController {

    private final EstadosFinancierosUseCase estadosFinancierosUseCase;

    @GetMapping("/balance-general")
    public ResponseEntity<BalanceGeneral> balanceGeneral(
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaCorte) {
        LocalDate corte = fechaCorte != null ? fechaCorte : LocalDate.now();
        return ResponseEntity.ok(estadosFinancierosUseCase.balanceGeneral(empresaId, corte));
    }

    @GetMapping("/estado-resultados")
    public ResponseEntity<EstadoResultados> estadoResultados(
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(estadosFinancierosUseCase.estadoResultados(empresaId, fechaInicio, fechaFin));
    }

    @GetMapping("/flujo-efectivo")
    public ResponseEntity<FlujoEfectivo> flujoEfectivo(
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(estadosFinancierosUseCase.flujoEfectivo(empresaId, fechaInicio, fechaFin));
    }

    @GetMapping("/cambios-patrimonio")
    public ResponseEntity<CambiosPatrimonio> cambiosPatrimonio(
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(estadosFinancierosUseCase.cambiosPatrimonio(empresaId, fechaInicio, fechaFin));
    }
}
