package com.pos_backend.nomina.domain.model.gateway;

import com.pos_backend.nomina.domain.model.Nomina;
import java.util.List;

public interface NominaGateway {
    Nomina guardar(Nomina nomina);
    Nomina buscarPorId(Long nominaId, String empresaId);
    List<Nomina> listar(String empresaId);
    boolean existePeriodo(Integer anio, Integer mes, String empresaId);
    String generarSiguienteNumero(String empresaId);
}
