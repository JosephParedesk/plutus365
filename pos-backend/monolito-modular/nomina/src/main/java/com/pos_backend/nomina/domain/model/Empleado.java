package com.pos_backend.nomina.domain.model;

import lombok.*;
import java.time.LocalDate;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class Empleado {
    private Long empleadoId;
    private String empresaId;

    private String tipoDocumento;      // CC, CE, PA, PEP
    private String numeroDocumento;
    private String nombres;
    private String apellidos;
    private String correo;
    private String telefono;
    private String direccion;
    private String ciudad;

    private String cargo;
    private String tipoContrato;       // INDEFINIDO, FIJO, OBRA_LABOR, APRENDIZAJE
    private LocalDate fechaIngreso;
    private LocalDate fechaRetiro;

    private Double salarioBase;
    private Boolean salarioIntegral;   // sobre 13 SMMLV; no causa cesantías/prima aparte
    private Boolean auxilioTransporte; // el sistema lo valida contra el tope legal

    private String eps;
    private String fondoPension;
    private String fondoCesantias;
    private String cajaCompensacion;
    private String arl;
    private String nivelRiesgoArl;     // I..V

    private String bancoPago;
    private String tipoCuenta;         // AHORROS, CORRIENTE
    private String numeroCuenta;

    private Boolean activo;
}
