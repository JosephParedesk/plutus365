import api from './api'
import type { ConfiguracionDian, ConfiguracionDianRequest, RangoNumeracion } from './facturacionService'

// Panel de super administrador (rol SUPERADMIN) — Plutus365 es quien tramita
// las credenciales de Factus con cada empresa, así que las carga acá en vez
// de que cada empresa las autogestione. Solo llega acá un usuario con ese
// rol: el gateway (PermisosInterceptor) rechaza a cualquier otro con 403,
// incluido un ADMIN normal de una empresa. Base: /api/pos/admin
// (AdminController.java, facturacion-service).

export interface EmpresaAdminResumen {
    empresaId: string
    nombre: string
    tipoDocumento: string
    numeroDocumento: string
    correo: string
    factusConfigurado: boolean
    facturasEmitidas: number
}

export interface UsuarioAdminResumen {
    cedula: string
    nombre: string
    correo: string
    rol: string
    empresaId: string
}

export const adminService = {
    listarEmpresas: () =>
        api.get<EmpresaAdminResumen[]>('/api/pos/admin/empresas'),

    listarUsuarios: () =>
        api.get<UsuarioAdminResumen[]>('/api/pos/admin/usuarios'),

    // 404 si esa empresa todavía no tiene Factus configurado
    obtenerFactus: (empresaId: string) =>
        api.get<ConfiguracionDian>(`/api/pos/admin/empresas/${empresaId}/factus`),

    guardarFactus: (empresaId: string, data: ConfiguracionDianRequest) =>
        api.put<ConfiguracionDian>(`/api/pos/admin/empresas/${empresaId}/factus`, data),

    // codigoDocumento: 21 factura, 22 nota crédito, 23 nota débito, 24 documento
    // soporte, 26 nómina — los rangos que Factus le asignó a esa empresa.
    rangosNumeracion: (empresaId: string, codigoDocumento: string) =>
        api.get<RangoNumeracion[]>(`/api/pos/admin/empresas/${empresaId}/factus/rangos-numeracion`, { params: { codigoDocumento } }),
}
