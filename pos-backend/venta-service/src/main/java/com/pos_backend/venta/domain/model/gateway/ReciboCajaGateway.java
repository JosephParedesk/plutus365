package com.pos_backend.venta.domain.model.gateway;

import com.pos_backend.venta.domain.model.ReciboCaja;
import java.time.LocalDate;
import java.util.List;

public interface ReciboCajaGateway {
    ReciboCaja guardar(ReciboCaja recibo);
    ReciboCaja buscarPorId(Long reciboId, String empresaId);
    List<ReciboCaja> listar(String empresaId);
    List<ReciboCaja> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta);
    String generarSiguienteNumero(String empresaId);
}
