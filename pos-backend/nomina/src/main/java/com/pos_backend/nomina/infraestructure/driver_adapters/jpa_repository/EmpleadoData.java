package com.pos_backend.nomina.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity @Table(name = "empleados")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class EmpleadoData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empleadoId;

    @Column(nullable = false, length = 50) private String empresaId;
    @Column(length = 10) private String tipoDocumento;
    @Column(length = 30) private String numeroDocumento;
    @Column(length = 100) private String nombres;
    @Column(length = 100) private String apellidos;
    @Column(length = 120) private String correo;
    @Column(length = 30) private String telefono;
    @Column(length = 200) private String direccion;
    @Column(length = 80) private String ciudad;
    @Column(length = 100) private String cargo;
    @Column(length = 30) private String tipoContrato;
    private LocalDate fechaIngreso;
    private LocalDate fechaRetiro;
    private Double salarioBase;
    private Boolean salarioIntegral;
    private Boolean auxilioTransporte;
    @Column(length = 100) private String eps;
    @Column(length = 100) private String fondoPension;
    @Column(length = 100) private String fondoCesantias;
    @Column(length = 100) private String cajaCompensacion;
    @Column(length = 100) private String arl;
    @Column(length = 10) private String nivelRiesgoArl;
    @Column(length = 80) private String bancoPago;
    @Column(length = 20) private String tipoCuenta;
    @Column(length = 40) private String numeroCuenta;
    private Boolean activo;
}
