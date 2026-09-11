package com.pos_backend.nomina.domain.usecase;

import com.pos_backend.nomina.domain.model.*;
import com.pos_backend.nomina.domain.model.gateway.EmpleadoGateway;
import com.pos_backend.nomina.domain.model.gateway.NominaGateway;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Liquidación de nómina según la legislación laboral colombiana.
 *
 * ADVERTENCIA IMPORTANTE: la retención en la fuente por salarios NO se calcula
 * aquí automáticamente — depende de la depuración de cada empleado (dependientes,
 * medicina prepagada, intereses de vivienda, AFC/AVC, renta exenta del 25% con
 * tope de 790 UVT anuales, procedimiento 1 vs 2). Se recibe como un valor que el
 * contador ingresa manualmente por empleado. Calcularla mal genera sanciones,
 * así que preferí no adivinarla.
 *
 * El sistema usa el mes comercial de 30 días, que es el estándar en nómina
 * colombiana aunque el mes calendario tenga 28 o 31 días.
 */
@RequiredArgsConstructor
public class NominaUseCase {

    private final NominaGateway nominaGateway;
    private final EmpleadoGateway empleadoGateway;

    private static final int DIAS_MES_COMERCIAL = 30;

    public List<Nomina> listar(String empresaId) {
        return nominaGateway.listar(empresaId);
    }

    public Nomina buscarPorId(Long nominaId, String empresaId) {
        Nomina n = nominaGateway.buscarPorId(nominaId, empresaId);
        if (n == null) throw new NoSuchElementException("Nómina no encontrada");
        return n;
    }

    /**
     * Liquida el período para todos los empleados activos.
     * novedades: empleadoId -> valores extra que el usuario capturó (horas extra,
     * comisiones, préstamos, retención, días trabajados si hubo ingreso/retiro).
     */
    public Nomina liquidar(Integer anio, Integer mes, String periodicidad,
                           Map<Long, NominaDetalle> novedades,
                           String empresaId, String creadoPor) {

        if (nominaGateway.existePeriodo(anio, mes, empresaId))
            throw new RuntimeException("Ya existe una nómina liquidada para " + mes + "/" + anio +
                    ". Anúlala primero si necesitas rehacerla.");

        List<Empleado> empleados = empleadoGateway.listarActivos(empresaId);
        if (empleados.isEmpty())
            throw new RuntimeException("No hay empleados activos para liquidar");

        YearMonth ym = YearMonth.of(anio, mes);
        LocalDate inicio = ym.atDay(1);
        LocalDate fin = ym.atEndOfMonth();

        List<NominaDetalle> detalles = new ArrayList<>();
        for (Empleado e : empleados) {
            NominaDetalle novedad = novedades != null ? novedades.get(e.getEmpleadoId()) : null;
            detalles.add(liquidarEmpleado(e, novedad, inicio, fin));
        }

        Nomina nomina = new Nomina();
        nomina.setEmpresaId(empresaId);
        nomina.setAnio(anio);
        nomina.setMes(mes);
        nomina.setPeriodicidad(periodicidad != null ? periodicidad : "MENSUAL");
        nomina.setFechaInicio(inicio);
        nomina.setFechaFin(fin);
        nomina.setFechaPago(fin);
        nomina.setEstado("LIQUIDADA");
        nomina.setDetalles(detalles);
        nomina.setCreadoPor(creadoPor);

        nomina.setTotalDevengado(sumar(detalles, NominaDetalle::getTotalDevengado));
        nomina.setTotalDeducciones(sumar(detalles, NominaDetalle::getTotalDeducciones));
        nomina.setTotalNeto(sumar(detalles, NominaDetalle::getNetoPagar));
        nomina.setTotalAportesEmpleador(sumar(detalles, NominaDetalle::getTotalAportesEmpleador));
        nomina.setTotalProvisiones(sumar(detalles, NominaDetalle::getTotalProvisiones));
        nomina.setCostoTotalEmpresa(sumar(detalles, NominaDetalle::getCostoTotal));

        nomina.setNumero(nominaGateway.generarSiguienteNumero(empresaId));
        return nominaGateway.guardar(nomina);
    }

    private NominaDetalle liquidarEmpleado(Empleado e, NominaDetalle nov, LocalDate inicio, LocalDate fin) {
        NominaDetalle d = new NominaDetalle();
        d.setEmpleadoId(e.getEmpleadoId());
        d.setNombreEmpleado((e.getNombres() + " " + e.getApellidos()).trim());
        d.setNumeroDocumento(e.getNumeroDocumento());
        d.setCargo(e.getCargo());
        d.setSalarioBase(e.getSalarioBase());

        int dias = nov != null && nov.getDiasTrabajados() != null
                ? nov.getDiasTrabajados()
                : diasTrabajadosDelPeriodo(e, inicio, fin);
        d.setDiasTrabajados(dias);

        double salario = safe(e.getSalarioBase());
        double sueldo = redondear(salario / DIAS_MES_COMERCIAL * dias);
        d.setSueldo(sueldo);

        // Auxilio de transporte: solo hasta 2 SMMLV, proporcional a días trabajados.
        // No es salario, así que NO entra al IBC de seguridad social.
        boolean tieneAux = Boolean.TRUE.equals(e.getAuxilioTransporte())
                && ParametrosNomina.tieneDerechoAuxilioTransporte(salario)
                && !Boolean.TRUE.equals(e.getSalarioIntegral());
        d.setAuxilioTransporte(tieneAux
                ? redondear(ParametrosNomina.AUXILIO_TRANSPORTE / DIAS_MES_COMERCIAL * dias)
                : 0.0);

        List<NominaDetalle.HoraExtraItem> horasExtra = nov != null && nov.getHorasExtra() != null
                ? nov.getHorasExtra() : List.of();
        d.setHorasExtra(horasExtra);
        double totalHorasExtra = sumarValor(horasExtra);
        d.setComisiones(nov != null ? safe(nov.getComisiones()) : 0.0);
        d.setBonificaciones(nov != null ? safe(nov.getBonificaciones()) : 0.0);
        d.setOtrosDevengados(nov != null ? safe(nov.getOtrosDevengados()) : 0.0);

        double devengadoSalarial = sueldo + totalHorasExtra + d.getComisiones();

        d.setTotalDevengado(redondear(devengadoSalarial + d.getAuxilioTransporte()
                + d.getBonificaciones() + d.getOtrosDevengados()));

        // ── IBC: base de cotización ──
        // Salario integral: solo el 70% cotiza (art. 132 CST).
        double ibc = Boolean.TRUE.equals(e.getSalarioIntegral())
                ? devengadoSalarial * 0.70
                : devengadoSalarial;
        ibc = Math.min(ibc, ParametrosNomina.TOPE_COTIZACION);
        // El IBC nunca puede ser inferior a un SMMLV proporcional a los días.
        double minimoIbc = ParametrosNomina.SMMLV / DIAS_MES_COMERCIAL * dias;
        ibc = Math.max(ibc, Math.min(minimoIbc, ParametrosNomina.SMMLV));
        d.setIbc(redondear(ibc));

        // ── Deducciones al empleado ──
        d.setSaludEmpleado(redondear(ibc * ParametrosNomina.SALUD_EMPLEADO));
        d.setPensionEmpleado(redondear(ibc * ParametrosNomina.PENSION_EMPLEADO));
        d.setFondoSolidaridad(redondear(ibc * ParametrosNomina.porcentajeFsp(salario)));
        d.setRetencionFuente(nov != null ? safe(nov.getRetencionFuente()) : 0.0);
        d.setPrestamos(nov != null ? safe(nov.getPrestamos()) : 0.0);
        d.setOtrasDeducciones(nov != null ? safe(nov.getOtrasDeducciones()) : 0.0);

        d.setTotalDeducciones(redondear(d.getSaludEmpleado() + d.getPensionEmpleado()
                + d.getFondoSolidaridad() + d.getRetencionFuente()
                + d.getPrestamos() + d.getOtrasDeducciones()));

        d.setNetoPagar(redondear(d.getTotalDevengado() - d.getTotalDeducciones()));

        // ── Aportes del empleador ──
        boolean exonerado = ParametrosNomina.aplicaExoneracion(salario);
        d.setExonerado(exonerado);
        d.setSaludEmpleador(exonerado ? 0.0 : redondear(ibc * ParametrosNomina.SALUD_EMPLEADOR));
        d.setPensionEmpleador(redondear(ibc * ParametrosNomina.PENSION_EMPLEADOR));
        d.setArl(redondear(ibc * tarifaArl(e.getNivelRiesgoArl())));
        d.setSena(exonerado ? 0.0 : redondear(ibc * ParametrosNomina.SENA));
        d.setIcbf(exonerado ? 0.0 : redondear(ibc * ParametrosNomina.ICBF));
        // La caja de compensación NUNCA se exonera.
        d.setCajaCompensacion(redondear(ibc * ParametrosNomina.CAJA_COMPENSACION));

        d.setTotalAportesEmpleador(redondear(d.getSaludEmpleador() + d.getPensionEmpleador()
                + d.getArl() + d.getSena() + d.getIcbf() + d.getCajaCompensacion()));

        // ── Provisiones ──
        // Cesantías y prima SÍ incluyen el auxilio de transporte en su base.
        // El salario integral no causa cesantías, prima ni intereses por separado.
        double basePrestaciones = Boolean.TRUE.equals(e.getSalarioIntegral())
                ? 0.0
                : devengadoSalarial + d.getAuxilioTransporte();

        d.setProvCesantias(redondear(basePrestaciones * ParametrosNomina.CESANTIAS));
        d.setProvInteresesCesantias(redondear(d.getProvCesantias() * 0.12));
        d.setProvPrima(redondear(basePrestaciones * ParametrosNomina.PRIMA_SERVICIOS));
        // Vacaciones se calculan sin auxilio de transporte (no es salario).
        d.setProvVacaciones(Boolean.TRUE.equals(e.getSalarioIntegral())
                ? 0.0
                : redondear(devengadoSalarial * ParametrosNomina.VACACIONES));

        d.setTotalProvisiones(redondear(d.getProvCesantias() + d.getProvInteresesCesantias()
                + d.getProvPrima() + d.getProvVacaciones()));

        d.setCostoTotal(redondear(d.getTotalDevengado() + d.getTotalAportesEmpleador() + d.getTotalProvisiones()));
        return d;
    }

    /** Tarifas ARL por nivel de riesgo (Decreto 1772 de 1994). */
    private double tarifaArl(String nivel) {
        if (nivel == null) return 0.00522;
        return switch (nivel.toUpperCase().trim()) {
            case "I", "1" -> 0.00522;
            case "II", "2" -> 0.01044;
            case "III", "3" -> 0.02436;
            case "IV", "4" -> 0.04350;
            case "V", "5" -> 0.06960;
            default -> 0.00522;
        };
    }

    /** Si el empleado ingresó o se retiró en mitad del período, se prorratea. */
    private int diasTrabajadosDelPeriodo(Empleado e, LocalDate inicio, LocalDate fin) {
        LocalDate desde = e.getFechaIngreso() != null && e.getFechaIngreso().isAfter(inicio)
                ? e.getFechaIngreso() : inicio;
        LocalDate hasta = e.getFechaRetiro() != null && e.getFechaRetiro().isBefore(fin)
                ? e.getFechaRetiro() : fin;
        if (hasta.isBefore(desde)) return 0;

        // Mes comercial: si trabajó el mes completo son 30 días, no 28 ni 31.
        if (!desde.isAfter(inicio) && !hasta.isBefore(fin)) return DIAS_MES_COMERCIAL;

        long dias = java.time.temporal.ChronoUnit.DAYS.between(desde, hasta) + 1;
        return (int) Math.min(dias, DIAS_MES_COMERCIAL);
    }

    public void anular(Long nominaId, String empresaId) {
        Nomina n = buscarPorId(nominaId, empresaId);
        if ("PAGADA".equals(n.getEstado()))
            throw new RuntimeException("No puedes anular una nómina ya marcada como pagada");
        n.setEstado("ANULADA");
        nominaGateway.guardar(n);
    }

    public Nomina marcarPagada(Long nominaId, String empresaId) {
        Nomina n = buscarPorId(nominaId, empresaId);
        if (!"LIQUIDADA".equals(n.getEstado()))
            throw new RuntimeException("Solo se puede marcar como pagada una nómina liquidada");
        n.setEstado("PAGADA");
        return nominaGateway.guardar(n);
    }

    private double sumar(List<NominaDetalle> ds, java.util.function.Function<NominaDetalle, Double> f) {
        return redondear(ds.stream().mapToDouble(d -> safe(f.apply(d))).sum());
    }

    private double sumarValor(List<NominaDetalle.HoraExtraItem> items) {
        return items.stream().mapToDouble(i -> safe(i.getValor())).sum();
    }

    private double safe(Double v) { return v != null ? v : 0.0; }

    private double redondear(double v) { return Math.round(v); }
}
