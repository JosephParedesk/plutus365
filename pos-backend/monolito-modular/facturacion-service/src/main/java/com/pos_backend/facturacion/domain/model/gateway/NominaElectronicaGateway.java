package com.pos_backend.facturacion.domain.model.gateway;

import com.pos_backend.facturacion.domain.model.NominaElectronica;
import java.util.List;

public interface NominaElectronicaGateway {
    NominaElectronica guardar(NominaElectronica n);
    NominaElectronica buscarPorNominaYEmpleado(Long nominaId, Long empleadoId, String empresaId);
    NominaElectronica buscarPorId(Long nominaElectronicaId, String empresaId);
    List<NominaElectronica> listar(String empresaId);
    List<NominaElectronica> listarPorNomina(Long nominaId, String empresaId);
    void eliminar(Long nominaElectronicaId, String empresaId);
}
