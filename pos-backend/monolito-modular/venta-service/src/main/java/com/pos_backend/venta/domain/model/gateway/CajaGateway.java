package com.pos_backend.venta.domain.model.gateway;

import com.pos_backend.venta.domain.model.CajaSesion;
import java.util.List;

public interface CajaGateway {
    CajaSesion guardar(CajaSesion sesion);
    CajaSesion buscarAbierta(String empresaId);
    CajaSesion buscarPorId(Long cajaId, String empresaId);
    List<CajaSesion> listar(String empresaId);
}
