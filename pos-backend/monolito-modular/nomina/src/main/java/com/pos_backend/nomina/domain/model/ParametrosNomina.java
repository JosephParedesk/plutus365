package com.pos_backend.nomina.domain.model;

/**
 * Parámetros legales de nómina en Colombia.
 *
 * IMPORTANTE: estos valores CAMBIAN CADA AÑO. Los de aquí corresponden a 2026
 * (Decretos 1469 y 1470 de dic/2025, ratificados por el Decreto 0159 de 2026;
 * el Consejo de Estado levantó la suspensión el 17 de julio de 2026).
 * Al cambiar de año hay que actualizar SMMLV y AUXILIO_TRANSPORTE aquí.
 *
 * Los porcentajes de aportes vienen de la Ley 100 de 1993, Ley 1607 de 2012
 * (exoneración de aportes) y Ley 21 de 1982 (parafiscales).
 */
public final class ParametrosNomina {

    private ParametrosNomina() {}

    public static final int ANIO_VIGENCIA = 2026;

    // ── Valores base 2026 ───────────────────────────────────────────────
    public static final double SMMLV = 1_750_905;
    public static final double AUXILIO_TRANSPORTE = 249_095;
    /** El auxilio se paga solo a quien devenga hasta 2 SMMLV. */
    public static final double TOPE_AUXILIO_TRANSPORTE = SMMLV * 2;
    /** Por debajo de 13 SMMLV no se puede pactar salario integral. */
    public static final double MINIMO_SALARIO_INTEGRAL = SMMLV * 13;
    /** Tope máximo de cotización a seguridad social. */
    public static final double TOPE_COTIZACION = SMMLV * 25;

    // ── Aportes del EMPLEADO (se descuentan del sueldo) ─────────────────
    public static final double SALUD_EMPLEADO = 0.04;
    public static final double PENSION_EMPLEADO = 0.04;

    // ── Aportes del EMPLEADOR (costo adicional, no se descuenta) ────────
    public static final double SALUD_EMPLEADOR = 0.085;
    public static final double PENSION_EMPLEADOR = 0.12;

    // ── Parafiscales (empleador) ────────────────────────────────────────
    public static final double SENA = 0.02;
    public static final double ICBF = 0.03;
    public static final double CAJA_COMPENSACION = 0.04;

    /**
     * Ley 1607 de 2012, art. 25: los empleadores están EXONERADOS de aportar
     * salud (8.5%), SENA e ICBF por los empleados que devengan menos de
     * 10 SMMLV. La caja de compensación (4%) NUNCA se exonera.
     */
    public static final double TOPE_EXONERACION_APORTES = SMMLV * 10;

    // ── Provisiones de prestaciones sociales (empleador) ────────────────
    public static final double CESANTIAS = 0.0833;          // 1 mes de salario por año
    public static final double INTERESES_CESANTIAS = 0.01;  // 12% anual sobre cesantías
    public static final double PRIMA_SERVICIOS = 0.0833;    // 1 mes de salario por año
    public static final double VACACIONES = 0.0417;         // 15 días hábiles por año

    // ── Fondo de Solidaridad Pensional (empleado, desde 4 SMMLV) ────────
    public static final double TOPE_FSP = SMMLV * 4;

    /** Porcentaje de FSP según el número de SMMLV que devenga el empleado. */
    public static double porcentajeFsp(double salarioBase) {
        double enSmmlv = salarioBase / SMMLV;
        if (enSmmlv < 4) return 0.0;
        if (enSmmlv < 16) return 0.01;
        if (enSmmlv < 17) return 0.012;
        if (enSmmlv < 18) return 0.014;
        if (enSmmlv < 19) return 0.016;
        if (enSmmlv < 20) return 0.018;
        return 0.02;
    }

    /** El empleador queda exonerado de salud, SENA e ICBF bajo 10 SMMLV. */
    public static boolean aplicaExoneracion(double salarioBase) {
        return salarioBase < TOPE_EXONERACION_APORTES;
    }

    public static boolean tieneDerechoAuxilioTransporte(double salarioBase) {
        return salarioBase <= TOPE_AUXILIO_TRANSPORTE;
    }
}
