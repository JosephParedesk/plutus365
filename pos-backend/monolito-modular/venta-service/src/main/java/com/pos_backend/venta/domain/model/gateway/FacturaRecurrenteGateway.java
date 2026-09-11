package com.pos_backend.venta.domain.model.gateway;

import com.pos_backend.venta.domain.model.FacturaRecurrente;
import java.util.List;

public interface FacturaRecurrenteGateway {
    FacturaRecurrente guardar(FacturaRecurrente recurrente);
    FacturaRecurrente buscarPorId(Long recurrenteId, String empresaId);
    List<FacturaRecurrente> listar(String empresaId);
    void eliminar(Long recurrenteId, String empresaId);
}
