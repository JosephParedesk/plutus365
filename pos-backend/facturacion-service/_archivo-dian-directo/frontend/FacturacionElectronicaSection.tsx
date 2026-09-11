import { useState, useEffect } from 'react'
import { colors } from '../../shared/theme/colors'
import { Form, Input, InputNumber, Select, Button, Upload, message, Alert, Tag, Divider } from 'antd'
import { FileProtectOutlined, UploadOutlined, SaveOutlined, SafetyCertificateOutlined, LockOutlined } from '@ant-design/icons'
import type { UploadProps } from 'antd'
import {
    configuracionDianService,
    type ConfiguracionDian,
} from '../../shared/services/facturacionService'

export default function FacturacionElectronicaSection() {
    const [form] = Form.useForm()
    const [loading, setLoading] = useState(true)
    const [guardando, setGuardando] = useState(false)
    const [subiendoCertificado, setSubiendoCertificado] = useState(false)
    const [config, setConfig] = useState<ConfiguracionDian | null>(null)
    const [passwordCertificado, setPasswordCertificado] = useState('')

    const ambiente = Form.useWatch('ambiente', form)

    const cargar = () => {
        setLoading(true)
        configuracionDianService.obtener()
            .then(({ data }) => {
                form.setFieldsValue(data)
                setConfig(data)
            })
            .catch((error) => {
                if (error.response?.status === 404) {
                    form.setFieldsValue({ ambiente: 'HABILITACION' })
                    setConfig(null)
                } else {
                    message.error('Error al cargar la configuración de facturación electrónica')
                }
            })
            .finally(() => setLoading(false))
    }

    useEffect(cargar, [])

    const guardar = async (values: any) => {
        setGuardando(true)
        try {
            const { data } = await configuracionDianService.guardar(values)
            setConfig(data)
            message.success('Configuración de facturación electrónica guardada')
        } catch (error: any) {
            message.error(error.response?.data?.message || 'Error al guardar la configuración')
        } finally {
            setGuardando(false)
        }
    }

    const propsCertificado: UploadProps = {
        showUploadList: false,
        accept: '.p12,.pfx',
        beforeUpload: (file) => {
            if (!config) {
                message.warning('Guarda primero la resolución de numeración antes de subir el certificado')
                return Upload.LIST_IGNORE
            }
            if (!passwordCertificado) {
                message.warning('Escribe el password del certificado antes de seleccionarlo')
                return Upload.LIST_IGNORE
            }
            return true
        },
        customRequest: async (options) => {
            const { file, onSuccess, onError } = options
            setSubiendoCertificado(true)
            try {
                await configuracionDianService.subirCertificado(file as File, passwordCertificado)
                message.success('Certificado cargado correctamente')
                setPasswordCertificado('')
                cargar()
                onSuccess?.({})
            } catch (error: any) {
                message.error(error.response?.data?.message || 'No se pudo cargar el certificado')
                onError?.(error)
            } finally {
                setSubiendoCertificado(false)
            }
        },
    }

    if (loading) return null

    return (
        <div style={{ maxWidth: 720, marginTop: 40 }}>
            <Divider />
            <h2 style={{ fontSize: 20, fontWeight: 700, color: colors.heading, margin: 0 }}>
                <FileProtectOutlined style={{ marginRight: 8 }} />
                Facturación electrónica
            </h2>
            <p style={{ color: colors.textSecondary, margin: '4px 0 20px', fontSize: 13 }}>
                Datos de tu habilitación ante la DIAN y tu certificado digital de firma.
            </p>

            <Alert
                type="info"
                showIcon
                message="Empieza siempre en ambiente de HABILITACIÓN"
                description="La DIAN exige probar tu software en su ambiente de pruebas antes de facturar en producción de verdad. No cambies a PRODUCCIÓN hasta que la DIAN te confirme que pasaste las pruebas de habilitación."
                style={{ marginBottom: 20, borderRadius: 16 }}
            />

            {config?.alertaRangoBajo && (
                <Alert
                    type="warning"
                    showIcon
                    message="La numeración autorizada se está agotando"
                    description={`Te quedan ${config.numerosRestantes} número${config.numerosRestantes === 1 ? '' : 's'} disponibles en el rango autorizado por la DIAN (hasta ${config.rangoHasta}). Solicita una nueva resolución de numeración antes de que se agote — si se acaba, el sistema deja de poder facturar electrónicamente.`}
                    style={{ marginBottom: 20, borderRadius: 16 }}
                />
            )}

            <Form form={form} layout="vertical" onFinish={guardar}>
                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                    <Form.Item name="ambiente" label="Ambiente" rules={[{ required: true, message: 'Requerido' }]}>
                        <Select
                            options={[
                                { value: 'HABILITACION', label: 'Habilitación (pruebas)' },
                                { value: 'PRODUCCION', label: 'Producción' },
                            ]}
                        />
                    </Form.Item>
                    {ambiente === 'HABILITACION' && (
                        <Form.Item name="testSetId" label="TestSetId (te lo entrega la DIAN)" rules={[{ required: true, message: 'Requerido' }]}>
                            <Input placeholder="ej: 5d84c3b4-..." />
                        </Form.Item>
                    )}
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                    <Form.Item name="prefijo" label="Prefijo de facturación" rules={[{ required: true, message: 'Requerido' }]}>
                        <Input placeholder="SETP" />
                    </Form.Item>
                    <Form.Item name="resolucionNumero" label="Número de resolución" rules={[{ required: true, message: 'Requerido' }]}>
                        <Input placeholder="18760000001" />
                    </Form.Item>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                    <Form.Item name="resolucionFechaInicio" label="Vigente desde" rules={[{ required: true, message: 'Requerido' }]}>
                        <Input type="date" />
                    </Form.Item>
                    <Form.Item name="resolucionFechaFin" label="Vigente hasta" rules={[{ required: true, message: 'Requerido' }]}>
                        <Input type="date" />
                    </Form.Item>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: 16 }}>
                    <Form.Item name="rangoDesde" label="Numeración autorizada — desde" rules={[{ required: true, message: 'Requerido' }]}>
                        <InputNumber style={{ width: '100%' }} placeholder="990000000" />
                    </Form.Item>
                    <Form.Item name="rangoHasta" label="Numeración autorizada — hasta" rules={[{ required: true, message: 'Requerido' }]}>
                        <InputNumber style={{ width: '100%' }} placeholder="995000000" />
                    </Form.Item>
                </div>

                <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr 1fr', gap: 16 }}>
                    <Form.Item name="softwareId" label="Software ID" rules={[{ required: true, message: 'Requerido' }]}>
                        <Input placeholder="Entregado por la DIAN" />
                    </Form.Item>
                    <Form.Item name="softwarePin" label="PIN del software" rules={[{ required: true, message: 'Requerido' }]}>
                        <Input.Password placeholder="••••" />
                    </Form.Item>
                    <Form.Item name="claveTecnica" label="Clave técnica" rules={[{ required: true, message: 'Requerido' }]}>
                        <Input.Password placeholder="Entregada por la DIAN" />
                    </Form.Item>
                </div>

                <Button
                    type="primary"
                    htmlType="submit"
                    icon={<SaveOutlined />}
                    loading={guardando}
                    style={{ background: colors.primary, borderColor: colors.primary, borderRadius: 14, fontWeight: 600, marginBottom: 28 }}
                >
                    Guardar configuración DIAN
                </Button>
            </Form>

            <div style={{
                background: config?.certificadoActivo ? colors.primaryLight : '#FFF8E8',
                border: `1px solid ${config?.certificadoActivo ? colors.primary : colors.orange}`,
                borderRadius: 18, padding: 16,
            }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: 8, marginBottom: 10 }}>
                    <SafetyCertificateOutlined style={{ fontSize: 18, color: config?.certificadoActivo ? colors.primary : colors.orange }} />
                    <span style={{ fontWeight: 600 }}>Certificado digital</span>
                    <Tag color={config?.certificadoActivo ? 'green' : 'orange'}>
                        {config?.certificadoActivo ? 'Cargado' : 'No cargado'}
                    </Tag>
                </div>

                <p style={{ fontSize: 12.5, color: colors.textSecondary, margin: '0 0 12px' }}>
                    Sube tu certificado de firma digital (.p12 / .pfx). Se guarda cifrado en el
                    servidor y solo se usa para firmar tus facturas — nunca se muestra de nuevo.
                </p>

                <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap' }}>
                    <Input.Password
                        placeholder="Password del certificado"
                        prefix={<LockOutlined style={{ color: colors.textMuted }} />}
                        value={passwordCertificado}
                        onChange={(e) => setPasswordCertificado(e.target.value)}
                        style={{ maxWidth: 260 }}
                    />
                    <Upload {...propsCertificado}>
                        <Button icon={<UploadOutlined />} loading={subiendoCertificado} disabled={!config}>
                            {config?.certificadoActivo ? 'Reemplazar certificado' : 'Subir certificado'}
                        </Button>
                    </Upload>
                </div>
                {!config && (
                    <div style={{ fontSize: 11.5, color: colors.textMuted, marginTop: 8 }}>
                        Guarda primero la resolución de numeración de arriba.
                    </div>
                )}
            </div>
        </div>
    )
}
