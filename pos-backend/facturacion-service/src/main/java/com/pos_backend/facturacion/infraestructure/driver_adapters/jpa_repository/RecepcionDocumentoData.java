package com.pos_backend.facturacion.infraestructure.driver_adapters.jpa_repository;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity @Table(name = "recepciones_documentos")
@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class RecepcionDocumentoData {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recepcionId;

    @Column(nullable = false, length = 50) private String empresaId;
    @Column(nullable = false) private Long compraId;
    @Column(length = 120) private String cufe;
    @Column(length = 60) private String billId;

    @Column(length = 20) private String estado;
    @Column(length = 1000) private String respuestaDian;

    @Lob @Column(columnDefinition = "TEXT") private String eventosJson;

    private LocalDateTime fechaCarga;
    @Column(length = 100) private String creadoPor;
}
