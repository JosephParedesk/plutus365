package com.pos_backend.inventario.infraestructure.mapper;

import com.surtiana.catalogo.infraestructure.driver_adapters.jpa_repository.ventas.VentaData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VentaMapperTest {

    private VentaMapper ventaMapper;

    @BeforeEach
    void setUp() {
        ventaMapper = new VentaMapper();
    }

    @Test
    void toVenta_RequestNull_RetornaNull() {
        Venta resultado = ventaMapper.toVenta((VentaRequest) null);
        assertNull(resultado);
    }

    @Test
    void toVenta_ConRequestYDetalles_RetornaVentaConDetalles() {
        VentaRequest request = mock(VentaRequest.class);
        when(request.getClienteId()).thenReturn("cliente-123");

        Class<?> detalleClass = Object.class;
        if (VentaRequest.class.getDeclaredClasses().length > 0) {
            detalleClass = VentaRequest.class.getDeclaredClasses()[0];
        }

        Object mockDetalleReq = mock(detalleClass, invocation -> {
            String methodName = invocation.getMethod().getName();
            if (methodName.equals("getProductoId")) {
                return "prod-abc";
            } else if (methodName.equals("getCantidad")) {
                return 3;
            }
            return null;
        });

        List<Object> listaDetallesMock = List.of(mockDetalleReq);
        when(request.getDetalles()).thenAnswer(invocation -> listaDetallesMock);

        Venta resultado = ventaMapper.toVenta(request);

        assertNotNull(resultado);
        assertEquals("cliente-123", resultado.getClienteId());
        assertNotNull(resultado.getDetalles());
        assertEquals(1, resultado.getDetalles().size());
        assertEquals("prod-abc", resultado.getDetalles().get(0).getProductoId());
        assertEquals(3, resultado.getDetalles().get(0).getCantidad());
    }

    @Test
    void toVenta_ConRequestSinDetalles_RetornaVentaConDetallesNull() {
        VentaRequest request = mock(VentaRequest.class);
        when(request.getClienteId()).thenReturn("cliente-456");
        when(request.getDetalles()).thenReturn(null);

        Venta resultado = ventaMapper.toVenta(request);

        assertNotNull(resultado);
        assertEquals("cliente-456", resultado.getClienteId());
        assertNull(resultado.getDetalles());
    }

    @Test
    void toVentaData_ConVentaYDetalles_RetornaVentaDataConDetalles() {
        Venta venta = mock(Venta.class);
        LocalDateTime fecha = LocalDateTime.now();

        when(venta.getVentaId()).thenReturn("venta-123");
        when(venta.getClienteId()).thenReturn("cliente-789");
        when(venta.getFecha()).thenReturn(fecha);
        when(venta.getTotal()).thenReturn(35000.0);
        when(venta.getEstado()).thenReturn("PAGADA");

        DetalleVenta detalle = mock(DetalleVenta.class);
        when(detalle.getDetalleId()).thenReturn("det-1");
        when(detalle.getProductoId()).thenReturn("prod-1");
        when(detalle.getNombreProducto()).thenReturn("Leche");
        when(detalle.getCantidad()).thenReturn(2);
        when(detalle.getPrecioUnitario()).thenReturn(4500.0);
        when(detalle.getSubtotal()).thenReturn(9000.0);

        when(venta.getDetalles()).thenReturn(List.of(detalle));

        VentaData resultado = ventaMapper.toVentaData(venta);

        assertNotNull(resultado);
        assertEquals("venta-123", resultado.getVentaId());
        assertEquals("cliente-789", resultado.getClienteId());
        assertEquals(fecha, resultado.getFecha());
        assertEquals(35000.0, resultado.getTotal());
        assertEquals("PAGADA", resultado.getEstado());

        assertNotNull(resultado.getDetalles());
        assertEquals(1, resultado.getDetalles().size());

        DetalleVentaData resultadoDetalle = resultado.getDetalles().get(0);
        assertEquals("det-1", resultadoDetalle.getDetalleId());
        assertEquals("prod-1", resultadoDetalle.getProductoId());
        assertEquals("Leche", resultadoDetalle.getNombreProducto());
        assertEquals(2, resultadoDetalle.getCantidad());
        assertEquals(4500.0, resultadoDetalle.getPrecioUnitario());
        assertEquals(9000.0, resultadoDetalle.getSubtotal());
        assertEquals(resultado, resultadoDetalle.getVenta());
    }

    @Test
    void toVentaData_ConVentaSinDetalles_RetornaVentaDataConDetallesNull() {
        Venta venta = mock(Venta.class);
        when(venta.getVentaId()).thenReturn("venta-000");
        when(venta.getDetalles()).thenReturn(null);

        VentaData resultado = ventaMapper.toVentaData(venta);

        assertNotNull(resultado);
        assertEquals("venta-000", resultado.getVentaId());
        assertNull(resultado.getDetalles());
    }

    @Test
    void toVenta_VentaDataNull_RetornaNull() {
        Venta resultado = ventaMapper.toVenta((VentaData) null);
        assertNull(resultado);
    }

    @Test
    void toVenta_ConVentaDataYDetalles_RetornaVentaConDetalles() {
        VentaData ventaData = mock(VentaData.class);
        LocalDateTime fecha = LocalDateTime.now();

        when(ventaData.getVentaId()).thenReturn("venta-777");
        when(ventaData.getClienteId()).thenReturn("cliente-777");
        when(ventaData.getFecha()).thenReturn(fecha);
        when(ventaData.getTotal()).thenReturn(15000.0);
        when(ventaData.getEstado()).thenReturn("DESPACHADA");

        DetalleVentaData dd = mock(DetalleVentaData.class);
        when(dd.getDetalleId()).thenReturn("det-7");
        when(dd.getProductoId()).thenReturn("prod-7");
        when(dd.getNombreProducto()).thenReturn("Arroz");
        when(dd.getCantidad()).thenReturn(5);
        when(dd.getPrecioUnitario()).thenReturn(3000.0);
        when(dd.getSubtotal()).thenReturn(15000.0);

        when(ventaData.getDetalles()).thenReturn(List.of(dd));

        Venta resultado = ventaMapper.toVenta(ventaData);

        assertNotNull(resultado);
        assertEquals("venta-777", resultado.getVentaId());
        assertEquals("cliente-777", resultado.getClienteId());
        assertEquals(fecha, resultado.getFecha());
        assertEquals(15000.0, resultado.getTotal());
        assertEquals("DESPACHADA", resultado.getEstado());

        assertNotNull(resultado.getDetalles());
        assertEquals(1, resultado.getDetalles().size());

        DetalleVenta resultadoDetalle = resultado.getDetalles().get(0);
        assertEquals("det-7", resultadoDetalle.getDetalleId());
        assertEquals("prod-7", resultadoDetalle.getProductoId());
        assertEquals("Arroz", resultadoDetalle.getNombreProducto());
        assertEquals(5, resultadoDetalle.getCantidad());
        assertEquals(3000.0, resultadoDetalle.getPrecioUnitario());
        assertEquals(15000.0, resultadoDetalle.getSubtotal());
    }

    @Test
    void toVenta_ConVentaDataSinDetalles_RetornaVentaConDetallesNull() {
        VentaData ventaData = mock(VentaData.class);
        when(ventaData.getVentaId()).thenReturn("venta-888");
        when(ventaData.getDetalles()).thenReturn(null);

        Venta resultado = ventaMapper.toVenta(ventaData);

        assertNotNull(resultado);
        assertEquals("venta-888", resultado.getVentaId());
        assertNull(resultado.getDetalles());
    }
}