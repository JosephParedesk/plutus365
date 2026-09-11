package com.pos_backend.contabilidad.domain.usecase;

import com.pos_backend.contabilidad.domain.model.*;
import com.pos_backend.contabilidad.domain.model.gateway.AsientoContableGateway;
import com.pos_backend.contabilidad.domain.model.gateway.CuentaContableGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.*;

/**
 * IMPORTANTE: el Balance General acá no aplica un "cierre contable" formal
 * (asientos de cierre que trasladen Ingresos/Gastos/Costos a una cuenta de
 * utilidades retenidas). En su lugar, muestra la utilidad acumulada como una
 * línea dentro de Patrimonio ("Utilidad del ejercicio, sin cerrar") para que
 * la ecuación Activo = Pasivo + Patrimonio siga cuadrando. Es el mismo criterio
 * que usan la mayoría de software contable de pymes para reportes intermedios
 * (antes del cierre anual formal). Para cierre fiscal real, consulta a tu contador.
 */
@RequiredArgsConstructor
public class EstadosFinancierosUseCase {

    private final AsientoContableGateway asientoContableGateway;
    private final CuentaContableGateway cuentaContableGateway;

    public BalanceGeneral balanceGeneral(String empresaId, LocalDate fechaCorte) {
        List<AsientoContable> asientos = asientosHasta(empresaId, fechaCorte);
        Map<String, Double> netosPorCuenta = calcularNetos(asientos);
        Map<String, CuentaContable> cuentas = mapaCuentas(empresaId);

        List<SaldoCuenta> activos = new ArrayList<>();
        List<SaldoCuenta> pasivos = new ArrayList<>();
        List<SaldoCuenta> patrimonio = new ArrayList<>();

        for (Map.Entry<String, Double> e : netosPorCuenta.entrySet()) {
            CuentaContable cuenta = cuentas.get(e.getKey());
            if (cuenta == null) continue;

            double saldoNatural = saldoNatural(cuenta, e.getValue());
            if (Math.abs(saldoNatural) < 1.0) continue; // omite cuentas en cero

            SaldoCuenta sc = new SaldoCuenta(cuenta.getCodigo(), cuenta.getNombre(), cuenta.getNivel(), saldoNatural);
            switch (cuenta.getCodigo().substring(0, 1)) {
                case "1" -> activos.add(sc);
                case "2" -> pasivos.add(sc);
                case "3" -> patrimonio.add(sc);
                default -> { /* clases 4,5,6 no van directo al balance; se resumen abajo */ }
            }
        }

        // Utilidad acumulada (Ingresos - Costos - Gastos) como línea de Patrimonio,
        // para que el balance cuadre sin necesitar un cierre contable formal todavía.
        double totalIngresos = sumarPorClase(netosPorCuenta, cuentas, "4");
        double totalCostos = sumarPorClase(netosPorCuenta, cuentas, "6");
        double totalGastos = sumarPorClase(netosPorCuenta, cuentas, "5");
        double utilidadAcumulada = totalIngresos - totalCostos - totalGastos;
        if (Math.abs(utilidadAcumulada) >= 1.0) {
            patrimonio.add(new SaldoCuenta("RESULTADO", "Utilidad del ejercicio (acumulada, sin cerrar)", "AUXILIAR", utilidadAcumulada));
        }

        double totalActivo = activos.stream().mapToDouble(SaldoCuenta::getSaldo).sum();
        double totalPasivo = pasivos.stream().mapToDouble(SaldoCuenta::getSaldo).sum();
        double totalPatrimonio = patrimonio.stream().mapToDouble(SaldoCuenta::getSaldo).sum();

        ordenar(activos); ordenar(pasivos); ordenar(patrimonio);

        boolean cuadra = Math.abs(totalActivo - (totalPasivo + totalPatrimonio)) < 5.0;

        return new BalanceGeneral(fechaCorte, activos, pasivos, patrimonio, totalActivo, totalPasivo, totalPatrimonio, cuadra);
    }

    public EstadoResultados estadoResultados(String empresaId, LocalDate fechaInicio, LocalDate fechaFin) {
        List<AsientoContable> asientos = asientoContableGateway.listar(empresaId).stream()
                .filter(a -> "CONTABILIZADO".equals(a.getEstado()))
                .filter(a -> !a.getFecha().isBefore(fechaInicio) && !a.getFecha().isAfter(fechaFin))
                .toList();

        Map<String, Double> netosPorCuenta = calcularNetos(asientos);
        Map<String, CuentaContable> cuentas = mapaCuentas(empresaId);

        List<SaldoCuenta> ingresos = new ArrayList<>();
        List<SaldoCuenta> costos = new ArrayList<>();
        List<SaldoCuenta> gastos = new ArrayList<>();

        for (Map.Entry<String, Double> e : netosPorCuenta.entrySet()) {
            CuentaContable cuenta = cuentas.get(e.getKey());
            if (cuenta == null) continue;

            double saldoNatural = saldoNatural(cuenta, e.getValue());
            if (Math.abs(saldoNatural) < 1.0) continue;

            SaldoCuenta sc = new SaldoCuenta(cuenta.getCodigo(), cuenta.getNombre(), cuenta.getNivel(), saldoNatural);
            switch (cuenta.getCodigo().substring(0, 1)) {
                case "4" -> ingresos.add(sc);
                case "6" -> costos.add(sc);
                case "5" -> gastos.add(sc);
                default -> { /* otras clases no aplican al estado de resultados */ }
            }
        }

        ordenar(ingresos); ordenar(costos); ordenar(gastos);

        double totalIngresos = ingresos.stream().mapToDouble(SaldoCuenta::getSaldo).sum();
        double totalCostos = costos.stream().mapToDouble(SaldoCuenta::getSaldo).sum();
        double totalGastos = gastos.stream().mapToDouble(SaldoCuenta::getSaldo).sum();
        double utilidad = totalIngresos - totalCostos - totalGastos;

        return new EstadoResultados(fechaInicio, fechaFin, ingresos, costos, gastos, totalIngresos, totalCostos, totalGastos, utilidad);
    }


    // ─── Estado de flujos de efectivo (método indirecto) ────────────────────

    /**
     * Clasifica cada cuenta como operación / inversión / financiación según su
     * grupo del PUC. Es una aproximación estándar para pymes:
     *   - Deudores (13), inventarios (14), proveedores (22), cuentas por pagar (23),
     *     impuestos (24) y obligaciones laborales (25) → OPERACIÓN
     *   - Propiedad planta y equipo (15), intangibles (16), inversiones (12) → INVERSIÓN
     *   - Obligaciones financieras (21) y patrimonio (3) → FINANCIACIÓN
     * Si tu operación tiene casos especiales, tu contador debe reclasificarlos.
     */
    private String actividadDeLaCuenta(String codigo) {
        if (codigo.startsWith("11")) return "EFECTIVO";
        String grupo = codigo.length() >= 2 ? codigo.substring(0, 2) : codigo;
        return switch (grupo) {
            case "12", "15", "16", "17", "18", "19" -> "INVERSION";
            case "21" -> "FINANCIACION";
            case "13", "14", "22", "23", "24", "25", "26", "27", "28" -> "OPERACION";
            default -> codigo.startsWith("3") ? "FINANCIACION" : "OPERACION";
        };
    }

    public FlujoEfectivo flujoEfectivo(String empresaId, LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, CuentaContable> cuentas = mapaCuentas(empresaId);

        // Saldos al inicio (día anterior) y al final del período
        Map<String, Double> netosInicio = calcularNetos(asientosHasta(empresaId, fechaInicio.minusDays(1)));
        Map<String, Double> netosFin = calcularNetos(asientosHasta(empresaId, fechaFin));

        double efectivoInicial = 0, efectivoFinal = 0;
        List<FlujoEfectivo.LineaFlujo> operacion = new ArrayList<>();
        List<FlujoEfectivo.LineaFlujo> inversion = new ArrayList<>();
        List<FlujoEfectivo.LineaFlujo> financiacion = new ArrayList<>();

        Set<String> todas = new HashSet<>();
        todas.addAll(netosInicio.keySet());
        todas.addAll(netosFin.keySet());

        for (String codigo : todas) {
            CuentaContable cuenta = cuentas.get(codigo);
            if (cuenta == null) continue;
            // Las cuentas de resultado (4,5,6) ya están dentro de la utilidad del período.
            if (codigo.startsWith("4") || codigo.startsWith("5") || codigo.startsWith("6")) continue;

            double saldoIni = saldoNatural(cuenta, netosInicio.getOrDefault(codigo, 0.0));
            double saldoFin = saldoNatural(cuenta, netosFin.getOrDefault(codigo, 0.0));
            String actividad = actividadDeLaCuenta(codigo);

            if ("EFECTIVO".equals(actividad)) {
                efectivoInicial += saldoIni;
                efectivoFinal += saldoFin;
                continue;
            }

            double variacion = saldoFin - saldoIni;
            if (Math.abs(variacion) < 1.0) continue;

            // Un activo que sube CONSUME efectivo; un pasivo/patrimonio que sube lo GENERA.
            double efecto = "DEBITO".equals(cuenta.getNaturaleza()) ? -variacion : variacion;
            FlujoEfectivo.LineaFlujo linea = new FlujoEfectivo.LineaFlujo(
                    cuenta.getCodigo(), cuenta.getNombre(), redondear(efecto));

            switch (actividad) {
                case "INVERSION" -> inversion.add(linea);
                case "FINANCIACION" -> financiacion.add(linea);
                default -> operacion.add(linea);
            }
        }

        // La utilidad del período encabeza el flujo de operación (método indirecto).
        EstadoResultados resultados = estadoResultados(empresaId, fechaInicio, fechaFin);
        double utilidad = resultados.getUtilidad();
        operacion.add(0, new FlujoEfectivo.LineaFlujo("", "Utilidad del período", redondear(utilidad)));

        ordenarFlujo(operacion); ordenarFlujo(inversion); ordenarFlujo(financiacion);

        double flujoOp = operacion.stream().mapToDouble(FlujoEfectivo.LineaFlujo::getValor).sum();
        double flujoInv = inversion.stream().mapToDouble(FlujoEfectivo.LineaFlujo::getValor).sum();
        double flujoFin = financiacion.stream().mapToDouble(FlujoEfectivo.LineaFlujo::getValor).sum();
        double variacionNeta = flujoOp + flujoInv + flujoFin;

        boolean cuadra = Math.abs((efectivoInicial + variacionNeta) - efectivoFinal) < 5.0;

        return new FlujoEfectivo(fechaInicio, fechaFin, redondear(utilidad),
                operacion, inversion, financiacion,
                redondear(flujoOp), redondear(flujoInv), redondear(flujoFin), redondear(variacionNeta),
                redondear(efectivoInicial), redondear(efectivoFinal), cuadra);
    }

    // ─── Estado de cambios en el patrimonio ─────────────────────────────────

    public CambiosPatrimonio cambiosPatrimonio(String empresaId, LocalDate fechaInicio, LocalDate fechaFin) {
        Map<String, CuentaContable> cuentas = mapaCuentas(empresaId);
        Map<String, Double> netosInicio = calcularNetos(asientosHasta(empresaId, fechaInicio.minusDays(1)));
        Map<String, Double> netosFin = calcularNetos(asientosHasta(empresaId, fechaFin));

        List<CambiosPatrimonio.LineaPatrimonio> lineas = new ArrayList<>();
        Set<String> todas = new HashSet<>();
        todas.addAll(netosInicio.keySet());
        todas.addAll(netosFin.keySet());

        for (String codigo : todas) {
            if (!codigo.startsWith("3")) continue;
            CuentaContable cuenta = cuentas.get(codigo);
            if (cuenta == null) continue;

            double ini = saldoNatural(cuenta, netosInicio.getOrDefault(codigo, 0.0));
            double fin = saldoNatural(cuenta, netosFin.getOrDefault(codigo, 0.0));
            if (Math.abs(ini) < 1.0 && Math.abs(fin) < 1.0) continue;

            lineas.add(new CambiosPatrimonio.LineaPatrimonio(
                    cuenta.getCodigo(), cuenta.getNombre(),
                    redondear(ini), redondear(fin - ini), redondear(fin)));
        }

        // Utilidad acumulada de cada corte (no está en cuentas de patrimonio hasta el cierre)
        double utilidadIni = utilidadAcumuladaHasta(empresaId, fechaInicio.minusDays(1), cuentas);
        double utilidadFin = utilidadAcumuladaHasta(empresaId, fechaFin, cuentas);
        if (Math.abs(utilidadIni) >= 1.0 || Math.abs(utilidadFin) >= 1.0) {
            lineas.add(new CambiosPatrimonio.LineaPatrimonio("RESULTADO",
                    "Utilidad del ejercicio (acumulada, sin cerrar)",
                    redondear(utilidadIni), redondear(utilidadFin - utilidadIni), redondear(utilidadFin)));
        }

        lineas.sort(Comparator.comparing(CambiosPatrimonio.LineaPatrimonio::getCodigo));

        double totalIni = lineas.stream().mapToDouble(CambiosPatrimonio.LineaPatrimonio::getSaldoInicial).sum();
        double totalVar = lineas.stream().mapToDouble(CambiosPatrimonio.LineaPatrimonio::getVariacion).sum();
        double totalFin = lineas.stream().mapToDouble(CambiosPatrimonio.LineaPatrimonio::getSaldoFinal).sum();

        return new CambiosPatrimonio(fechaInicio, fechaFin, lineas,
                redondear(totalIni), redondear(totalVar), redondear(totalFin));
    }

    private double utilidadAcumuladaHasta(String empresaId, LocalDate corte, Map<String, CuentaContable> cuentas) {
        Map<String, Double> netos = calcularNetos(asientosHasta(empresaId, corte));
        return sumarPorClase(netos, cuentas, "4") - sumarPorClase(netos, cuentas, "6") - sumarPorClase(netos, cuentas, "5");
    }

    private void ordenarFlujo(List<FlujoEfectivo.LineaFlujo> l) {
        l.sort(Comparator.comparing(FlujoEfectivo.LineaFlujo::getCodigo));
    }

    private double redondear(double v) { return Math.round(v); }

    // ─── Helpers ─────────────────────────────────────────────────────────


    private List<AsientoContable> asientosHasta(String empresaId, LocalDate fechaCorte) {
        return asientoContableGateway.listar(empresaId).stream()
                .filter(a -> "CONTABILIZADO".equals(a.getEstado()))
                .filter(a -> !a.getFecha().isAfter(fechaCorte))
                .toList();
    }

    private Map<String, CuentaContable> mapaCuentas(String empresaId) {
        Map<String, CuentaContable> mapa = new HashMap<>();
        cuentaContableGateway.listar(empresaId).forEach(c -> mapa.put(c.getCodigo(), c));
        return mapa;
    }

    /** Suma de Debe - Haber por cuenta, en bruto (sin ajustar todavía por naturaleza). */
    private Map<String, Double> calcularNetos(List<AsientoContable> asientos) {
        Map<String, Double> netos = new HashMap<>();
        for (AsientoContable asiento : asientos) {
            if (asiento.getMovimientos() == null) continue;
            for (MovimientoContable m : asiento.getMovimientos()) {
                double neto = safe(m.getDebe()) - safe(m.getHaber());
                netos.merge(m.getCuentaCodigo(), neto, Double::sum);
            }
        }
        return netos;
    }

    private double saldoNatural(CuentaContable cuenta, double netoDebeMenosHaber) {
        return "DEBITO".equals(cuenta.getNaturaleza()) ? netoDebeMenosHaber : -netoDebeMenosHaber;
    }

    private double sumarPorClase(Map<String, Double> netos, Map<String, CuentaContable> cuentas, String clase) {
        double total = 0;
        for (Map.Entry<String, Double> e : netos.entrySet()) {
            CuentaContable cuenta = cuentas.get(e.getKey());
            if (cuenta != null && cuenta.getCodigo().substring(0, 1).equals(clase)) {
                total += saldoNatural(cuenta, e.getValue());
            }
        }
        return total;
    }

    private void ordenar(List<SaldoCuenta> lista) {
        lista.sort(Comparator.comparing(SaldoCuenta::getCodigo));
    }

    private double safe(Double valor) {
        return valor != null ? valor : 0.0;
    }
}
