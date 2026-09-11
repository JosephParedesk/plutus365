package com.pos_backend.facturacion.domain.model;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class EmpleadoRemota {
    private Long empleadoId;
    private String tipoDocumento;      // CC, CE, PA, PEP
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String direccion;
    private String ciudad;
    private String tipoContrato;       // INDEFINIDO, FIJO, OBRA_LABOR, APRENDIZAJE
    private LocalDate fechaIngreso;
    private Double salarioBase;
    private Boolean salarioIntegral;
    private String bancoPago;
    private String tipoCuenta;         // AHORROS, CORRIENTE
    private String numeroCuenta;
}
