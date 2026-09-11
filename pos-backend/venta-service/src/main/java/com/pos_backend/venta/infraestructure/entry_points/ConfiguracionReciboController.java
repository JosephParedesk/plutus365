package com.pos_backend.venta.infraestructure.entry_points;

import com.pos_backend.venta.domain.model.ConfiguracionRecibo;
import com.pos_backend.venta.domain.usecase.ConfiguracionReciboUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pos/ventas/configuracion-recibo")
@RequiredArgsConstructor
public class ConfiguracionReciboController {

    private final ConfiguracionReciboUseCase configuracionReciboUseCase;

    // Siempre devuelve algo (valores por defecto si la empresa nunca la configuró).
    @GetMapping
    public ResponseEntity<ConfiguracionRecibo> obtener(@RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(configuracionReciboUseCase.obtener(empresaId));
    }

    @PutMapping
    public ResponseEntity<ConfiguracionRecibo> guardar(
            @RequestBody ConfiguracionRecibo configuracion,
            @RequestHeader("X-Empresa-Id") String empresaId) {
        return ResponseEntity.ok(configuracionReciboUseCase.guardar(configuracion, empresaId));
    }
}
