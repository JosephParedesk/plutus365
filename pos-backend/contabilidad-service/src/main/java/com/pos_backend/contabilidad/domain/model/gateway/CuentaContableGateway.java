package com.pos_backend.contabilidad.domain.model.gateway;

import com.pos_backend.contabilidad.domain.model.CuentaContable;
import java.util.List;

public interface CuentaContableGateway {
    List<CuentaContable> listar(String empresaId);
    CuentaContable buscarPorCodigo(String codigo, String empresaId);
    CuentaContable guardar(CuentaContable cuenta);
    void guardarTodas(List<CuentaContable> cuentas);
    void eliminar(String codigo, String empresaId);
    boolean existeAlgunaCuenta(String empresaId);
    boolean existeCodigo(String codigo, String empresaId);
    List<CuentaContable> listarHijas(String codigoPadre, String empresaId);
}
