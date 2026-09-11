package com.pos_backend.inventario.infraestructure.mapper;

import com.surtiana.catalogo.infraestructure.driver_adapters.jpa_repository.ventas.ReciboData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReciboMapperTest {

    private ReciboMapper reciboMapper;

    @BeforeEach
    void setUp() {
        reciboMapper = new ReciboMapper();
    }

    @Test
    void toReciboData_Exitoso_RetornaReciboData() {
        // Arrange
        Recibo recibo = mock(Recibo.class);
        LocalDateTime fecha = LocalDateTime.now();

        when(recibo.getReciboId()).thenReturn("recibo-123");
        when(recibo.getVentaId()).thenReturn("venta-123");
        when(recibo.getClienteId()).thenReturn("cliente-123");
        when(recibo.getFechaEmision()).thenReturn(fecha);
        when(recibo.getSubtotal()).thenReturn(15000.0);
        when(recibo.getImpuesto()).thenReturn(2850.0);
        when(recibo.getTotal()).thenReturn(17850.0);

        // Act
        ReciboData resultado = reciboMapper.toReciboData(recibo);

        // Assert
        assertNotNull(resultado);
        assertEquals("recibo-123", resultado.getReciboId());
        assertEquals("venta-123", resultado.getVentaId());
        assertEquals("cliente-123", resultado.getClienteId());
        assertEquals(fecha, resultado.getFechaEmision());
        assertEquals(15000.0, resultado.getSubtotal());
        assertEquals(2850.0, resultado.getImpuesto());
        assertEquals(17850.0, resultado.getTotal());
    }

    @Test
    void toRecibo_Exitoso_RetornaRecibo() {
        // Arrange
        ReciboData reciboData = mock(ReciboData.class);
        List<DetalleVenta> detalles = List.of(mock(DetalleVenta.class));
        LocalDateTime fecha = LocalDateTime.now();

        when(reciboData.getReciboId()).thenReturn("recibo-123");
        when(reciboData.getVentaId()).thenReturn("venta-123");
        when(reciboData.getClienteId()).thenReturn("cliente-123");
        when(reciboData.getFechaEmision()).thenReturn(fecha);
        when(reciboData.getSubtotal()).thenReturn(20000.0);
        when(reciboData.getImpuesto()).thenReturn(3800.0);
        when(reciboData.getTotal()).thenReturn(23800.0);

        // Act
        Recibo resultado = reciboMapper.toRecibo(reciboData, detalles);

        // Assert
        assertNotNull(resultado);
        assertEquals("recibo-123", resultado.getReciboId());
        assertEquals("venta-123", resultado.getVentaId());
        assertEquals("cliente-123", resultado.getClienteId());
        assertEquals(fecha, resultado.getFechaEmision());
        assertEquals(detalles, resultado.getDetalles());
        assertEquals(20000.0, resultado.getSubtotal());
        assertEquals(3800.0, resultado.getImpuesto());
        assertEquals(23800.0, resultado.getTotal());
    }

    @Test
    void toRecibo_ReciboDataNull_RetornaNull() {
        // Arrange
        List<DetalleVenta> detalles = new ArrayList<>();

        // Act
        Recibo resultado = reciboMapper.toRecibo(null, detalles);

        // Assert
        assertNull(resultado);
    }
}