package com.pos_backend.compra.domain.model;

import lombok.*;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class FacturaImportada {
    private String fuente;                 // XML o PDF — el frontend usa esto para avisar qué tan confiable es
    private String nitProveedor;
    private String nombreProveedor;
    private String numeroFacturaProveedor;
    private LocalDate fecha;
    private String cufe;
    private Double subtotal;
    private Double totalIva;
    private Double total;
    private List<ItemImportado> items;     // solo se llena desde XML; desde PDF es demasiado poco confiable
    private List<String> camposNoExtraidos; // para que el frontend le avise al usuario qué debe llenar a mano

    @Getter @Setter @NoArgsConstructor @AllArgsConstructor
    public static class ItemImportado {
        private String descripcion;
        private Double cantidad;
        private Double valorUnitario;
        private Double valorTotal;
    }
}
