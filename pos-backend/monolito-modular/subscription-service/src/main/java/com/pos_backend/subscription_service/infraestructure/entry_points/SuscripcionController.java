package com.pos_backend.subscription_service.infraestructure.entry_points;

import com.pos_backend.subscription_service.domain.model.Suscripcion;
import com.pos_backend.subscription_service.domain.usecase.SuscripcionUseCase;
import com.pos_backend.subscription_service.infraestructure.entry_points.DTO.SuscripcionRequest;
import com.pos_backend.subscription_service.infraestructure.mapper.SuscripcionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pos/suscripciones")
@RequiredArgsConstructor
public class SuscripcionController {

    private final SuscripcionUseCase suscripcionUseCase;
    private final SuscripcionMapper suscripcionMapper;

    @PostMapping("/crear")
    public ResponseEntity<Suscripcion> crear(@RequestBody SuscripcionRequest request) {
        Suscripcion suscripcion = new Suscripcion();
        suscripcion.setUsuarioCedula(request.getUsuarioCedula());
        suscripcion.setPlanId(request.getPlanId());
        suscripcion.setMetodoPago(request.getMetodoPago());
        return ResponseEntity.ok(suscripcionUseCase.crearSuscripcion(suscripcion));
    }

    @PutMapping("/activar/{id}")
    public ResponseEntity<Suscripcion> activar(@PathVariable Long id) {
        return ResponseEntity.ok(suscripcionUseCase.activarSuscripcion(id));
    }

    @GetMapping("/usuario/{cedula}")
    public ResponseEntity<Suscripcion> buscarPorUsuario(@PathVariable String cedula) {
        return ResponseEntity.ok(suscripcionUseCase.buscarPorUsuario(cedula));
    }

    @DeleteMapping("/cancelar/{id}")
    public ResponseEntity<Void> cancelar(@PathVariable Long id) {
        suscripcionUseCase.cancelarSuscripcion(id);
        return ResponseEntity.noContent().build();
    }
}