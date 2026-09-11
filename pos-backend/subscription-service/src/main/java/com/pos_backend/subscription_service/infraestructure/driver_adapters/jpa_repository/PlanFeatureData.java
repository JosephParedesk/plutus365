package com.pos_backend.subscription_service.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "plan_features")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class PlanFeatureData {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long planId;
    private String modulo; // INVENTARIO, VENTAS, CONTABILIDAD, NOMINA, FACTURACION, CLIENTES, PROVEEDORES
}