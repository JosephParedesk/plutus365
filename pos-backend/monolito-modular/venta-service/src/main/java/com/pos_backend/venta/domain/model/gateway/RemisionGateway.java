package com.pos_backend.venta.domain.model.gateway;

import com.pos_backend.venta.domain.model.Remision;
import java.time.LocalDate;
import java.util.List;

public interface RemisionGateway {
    Remision guardar(Remision remision);
    Remision buscarPorId(Long remisionId, String empresaId);
    List<Remision> listar(String empresaId);
    List<Remision> buscarPorRango(String empresaId, LocalDate desde, LocalDate hasta);
    String generarSiguienteNumero(String empresaId);
}
