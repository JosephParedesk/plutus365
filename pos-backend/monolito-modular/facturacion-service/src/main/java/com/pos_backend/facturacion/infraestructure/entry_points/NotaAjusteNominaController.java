package com.pos_backend.facturacion.infraestructure.entry_points;

import com.pos_backend.facturacion.domain.model.NotaAjusteNomina;
import com.pos_backend.facturacion.domain.usecase.NotaAjusteNominaUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pos/facturacion/notas-ajuste-nomina")
@RequiredArgsConstructor
public class NotaAjusteNominaController {

    private final NotaAjusteNominaUseCase notaAjusteNominaUseCase;

    @GetMapping
    public ResponseEntity<List<NotaAjusteNomina>> listar(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(notaAjusteNominaUseCase.listar(empresaId));
    }

    // Anula ante la DIAN una nómina electrónica de un empleado ya ACEPTADA.
    @PostMapping("/{nominaElectronicaId}")
    public ResponseEntity<NotaAjusteNomina> emitir(
            @PathVariable Long nominaElectronicaId,
            @RequestHeader("X-Empresa-Id") String empresaId,
            @RequestHeader(value = "X-Usuario-Nombre", required = false, defaultValue = "Sistema") String creadoPor) {
        return ResponseEntity.ok(notaAjusteNominaUseCase.emitir(nominaElectronicaId, empresaId, creadoPor));
    }
}
