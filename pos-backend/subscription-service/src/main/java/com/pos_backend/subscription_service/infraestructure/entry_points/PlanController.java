package com.pos_backend.subscription_service.infraestructure.entry_points;

import com.pos_backend.subscription_service.domain.model.Plan;
import com.pos_backend.subscription_service.domain.usecase.PlanUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/pos/planes")
@RequiredArgsConstructor
public class PlanController {

    private final PlanUseCase planUseCase;

    @GetMapping
    public ResponseEntity<List<Plan>> listarPlanes() {
        return ResponseEntity.ok(planUseCase.listarPlanes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Plan> buscarPlan(@PathVariable Long id) {
        return ResponseEntity.ok(planUseCase.buscarPlanPorId(id));
    }

    @PostMapping("/save")
    public ResponseEntity<Plan> guardarPlan(@RequestBody Plan plan) {
        return ResponseEntity.ok(planUseCase.guardarPlan(plan));
    }

    @GetMapping("/{id}/modulos")
    public ResponseEntity<List<String>> obtenerModulos(@PathVariable Long id) {
        return ResponseEntity.ok(planUseCase.obtenerModulosPorPlan(id));
    }

    @GetMapping("/{id}/tiene-acceso/{modulo}")
    public ResponseEntity<Boolean> tieneAcceso(
            @PathVariable Long id,
            @PathVariable String modulo) {
        return ResponseEntity.ok(planUseCase.planTieneAccesoAModulo(id, modulo));
    }
}