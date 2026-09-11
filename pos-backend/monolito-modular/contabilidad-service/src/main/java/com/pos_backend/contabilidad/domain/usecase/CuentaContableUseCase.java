package com.pos_backend.contabilidad.domain.usecase;

import com.pos_backend.contabilidad.domain.model.CuentaContable;
import com.pos_backend.contabilidad.domain.model.gateway.CuentaContableGateway;
import com.pos_backend.contabilidad.domain.model.gateway.PucBaseGateway;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;

@RequiredArgsConstructor
public class CuentaContableUseCase {

    private final CuentaContableGateway cuentaContableGateway;
    private final PucBaseGateway pucBaseGateway;

    private static final Set<String> NIVELES_VALIDOS = Set.of("CLASE", "GRUPO", "CUENTA", "SUBCUENTA", "AUXILIAR");

    public List<CuentaContable> listar(String empresaId) {
        sembrarBaseSiEsNecesario(empresaId);
        return cuentaContableGateway.listar(empresaId);
    }

    public CuentaContable buscarPorCodigo(String codigo, String empresaId) {
        sembrarBaseSiEsNecesario(empresaId);
        CuentaContable cuenta = cuentaContableGateway.buscarPorCodigo(codigo, empresaId);
        if (cuenta == null)
            throw new NoSuchElementException("Cuenta contable no encontrada: " + codigo);
        return cuenta;
    }

    public List<CuentaContable> listarHijas(String codigoPadre, String empresaId) {
        return cuentaContableGateway.listarHijas(codigoPadre, empresaId);
    }

    public CuentaContable crear(CuentaContable cuenta, String empresaId) {
        cuenta.setEmpresaId(empresaId);
        cuenta.setPersonalizada(true);
        if (cuenta.getActiva() == null) cuenta.setActiva(true);
        if (cuenta.getDetalleSaldos() == null) cuenta.setDetalleSaldos("SIN_DETALLE");

        validarCodigo(cuenta.getCodigo());

        if (cuentaContableGateway.existeCodigo(cuenta.getCodigo(), empresaId))
            throw new RuntimeException("Ya existe una cuenta con el código " + cuenta.getCodigo());

        String nivel = inferirNivel(cuenta.getCodigo());
        cuenta.setNivel(nivel);
        cuenta.setEsTransaccional(nivel.equals("AUXILIAR"));

        if (!nivel.equals("CLASE")) {
            String codigoPadre = inferirCodigoPadre(cuenta.getCodigo(), nivel);
            CuentaContable padre = cuentaContableGateway.buscarPorCodigo(codigoPadre, empresaId);
            if (padre == null)
                throw new NoSuchElementException(
                        "No existe la cuenta padre " + codigoPadre + ". Créala primero (o crea los niveles intermedios).");
            cuenta.setCodigoPadre(codigoPadre);
            cuenta.setNaturaleza(padre.getNaturaleza());
        }

        return cuentaContableGateway.guardar(cuenta);
    }

    public CuentaContable actualizar(String codigo, CuentaContable cambios, String empresaId) {
        CuentaContable existente = buscarPorCodigo(codigo, empresaId);

        // Solo se pueden editar estos campos; el código, nivel, jerarquía y naturaleza
        // no se tocan una vez creada la cuenta, para no romper la estructura del PUC.
        if (cambios.getNombre() != null) existente.setNombre(cambios.getNombre());
        if (cambios.getCategoria() != null) existente.setCategoria(cambios.getCategoria());
        if (cambios.getDetalleSaldos() != null) existente.setDetalleSaldos(cambios.getDetalleSaldos());
        if (cambios.getActiva() != null) existente.setActiva(cambios.getActiva());

        return cuentaContableGateway.guardar(existente);
    }

    public void eliminar(String codigo, String empresaId) {
        CuentaContable existente = buscarPorCodigo(codigo, empresaId);

        if (!Boolean.TRUE.equals(existente.getPersonalizada()))
            throw new RuntimeException("No puedes eliminar una cuenta del PUC base. Si no la usas, puedes desactivarla en su lugar.");

        List<CuentaContable> hijas = cuentaContableGateway.listarHijas(codigo, empresaId);
        if (!hijas.isEmpty())
            throw new RuntimeException("No puedes eliminar esta cuenta porque tiene subcuentas creadas debajo de ella");

        cuentaContableGateway.eliminar(codigo, empresaId);
    }

    // ─── Siembra automática del PUC base (una sola vez por empresa) ────────

    private void sembrarBaseSiEsNecesario(String empresaId) {
        if (cuentaContableGateway.existeAlgunaCuenta(empresaId)) return;

        List<CuentaContable> base = pucBaseGateway.cargarBase();
        for (CuentaContable cuenta : base) {
            cuenta.setEmpresaId(empresaId);
            cuenta.setActiva(true);
            cuenta.setPersonalizada(false);
        }
        cuentaContableGateway.guardarTodas(base);
    }

    // ─── Validaciones de estructura del código PUC ──────────────────────────

    private void validarCodigo(String codigo) {
        if (codigo == null || !codigo.matches("\\d+"))
            throw new RuntimeException("El código debe contener solo números");
        if (codigo.length() < 1 || codigo.length() > 12)
            throw new RuntimeException("El código debe tener entre 1 y 12 dígitos");
    }

    private String inferirNivel(String codigo) {
        return switch (codigo.length()) {
            case 1 -> "CLASE";
            case 2 -> "GRUPO";
            case 4 -> "CUENTA";
            case 6 -> "SUBCUENTA";
            default -> "AUXILIAR"; // 7+ dígitos
        };
    }

    private String inferirCodigoPadre(String codigo, String nivel) {
        return switch (nivel) {
            case "GRUPO" -> codigo.substring(0, 1);
            case "CUENTA" -> codigo.substring(0, 2);
            case "SUBCUENTA" -> codigo.substring(0, 4);
            default -> codigo.substring(0, 6); // AUXILIAR
        };
    }
}
