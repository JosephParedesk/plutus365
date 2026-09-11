package com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository.ventas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ReciboDataGatewayImplTest {

    private ReciboDataGatewayImpl reciboDataGateway;
    private ReciboDataJpaRepository reciboDataJpaRepository;
    private VentaDataJpaRepository ventaDataJpaRepository;
    private ReciboMapper reciboMapper;
    private VentaMapper ventaMapper;

    @BeforeEach
    void setUp() {
        reciboDataJpaRepository = mock(ReciboDataJpaRepository.class);
        ventaDataJpaRepository = mock(VentaDataJpaRepository.class);
        reciboMapper = mock(ReciboMapper.class);
        ventaMapper = mock(VentaMapper.class);
        reciboDataGateway = new ReciboDataGatewayImpl(
                reciboDataJpaRepository,
                ventaDataJpaRepository,
                reciboMapper,
                ventaMapper
        );
    }

    @Test
    void guardarRecibo_ConVentaExistente_RetornaReciboConDetalles() {
        // Arrange
        Recibo reciboInput = mock(Recibo.class);
        ReciboData reciboData = mock(ReciboData.class);
        VentaData ventaData = mock(VentaData.class);
        Venta venta = mock(Venta.class);
        List<DetalleVenta> detallesEsperados = List.of(mock(DetalleVenta.class));
        Recibo reciboEsperado = mock(Recibo.class);

        when(reciboMapper.toReciboData(reciboInput)).thenReturn(reciboData);
        when(reciboDataJpaRepository.save(reciboData)).thenReturn(reciboData);
        when(reciboData.getVentaId()).thenReturn("venta-123");

        when(ventaDataJpaRepository.findById("venta-123")).thenReturn(Optional.of(ventaData));
        when(ventaMapper.toVenta(ventaData)).thenReturn(venta);
        when(venta.getDetalles()).thenReturn(detallesEsperados);

        when(reciboMapper.toRecibo(reciboData, detallesEsperados)).thenReturn(reciboEsperado);

        // Act
        Recibo resultado = reciboDataGateway.guardarRecibo(reciboInput);

        // Assert
        assertNotNull(resultado);
        assertEquals(reciboEsperado, resultado);
        verify(reciboDataJpaRepository, times(1)).save(reciboData);
    }

    @Test
    void guardarRecibo_ConVentaInexistente_RetornaReciboSinDetalles() {
        // Arrange
        Recibo reciboInput = mock(Recibo.class);
        ReciboData reciboData = mock(ReciboData.class);
        Recibo reciboEsperado = mock(Recibo.class);

        when(reciboMapper.toReciboData(reciboInput)).thenReturn(reciboData);
        when(reciboDataJpaRepository.save(reciboData)).thenReturn(reciboData);
        when(reciboData.getVentaId()).thenReturn("venta-123");

        when(ventaDataJpaRepository.findById("venta-123")).thenReturn(Optional.empty());
        when(reciboMapper.toRecibo(reciboData, null)).thenReturn(reciboEsperado);

        // Act
        Recibo resultado = reciboDataGateway.guardarRecibo(reciboInput);

        // Assert
        assertNotNull(resultado);
        assertEquals(reciboEsperado, resultado);
    }

    @Test
    void buscarReciboPorVentaId_Existe_RetornaRecibo() {
        // Arrange
        String ventaId = "venta-123";
        ReciboData reciboData = mock(ReciboData.class);
        VentaData ventaData = mock(VentaData.class);
        Venta venta = mock(Venta.class);
        List<DetalleVenta> detallesEsperados = List.of(mock(DetalleVenta.class));
        Recibo reciboEsperado = mock(Recibo.class);

        when(reciboDataJpaRepository.findByVentaId(ventaId)).thenReturn(Optional.of(reciboData));
        when(reciboData.getVentaId()).thenReturn(ventaId);
        when(ventaDataJpaRepository.findById(ventaId)).thenReturn(Optional.of(ventaData));
        when(ventaMapper.toVenta(ventaData)).thenReturn(venta);
        when(venta.getDetalles()).thenReturn(detallesEsperados);
        when(reciboMapper.toRecibo(reciboData, detallesEsperados)).thenReturn(reciboEsperado);

        // Act
        Recibo resultado = reciboDataGateway.buscarReciboPorVentaId(ventaId);

        // Assert
        assertNotNull(resultado);
        assertEquals(reciboEsperado, resultado);
    }

    @Test
    void buscarReciboPorVentaId_NoExiste_RetornaNull() {
        // Arrange
        String ventaId = "venta-123";
        when(reciboDataJpaRepository.findByVentaId(ventaId)).thenReturn(Optional.empty());

        // Act
        Recibo resultado = reciboDataGateway.buscarReciboPorVentaId(ventaId);

        // Assert
        assertNull(resultado);
        verify(ventaDataJpaRepository, never()).findById(anyString());
    }

    @Test
    void buscarReciboPorId_Existe_RetornaRecibo() {
        // Arrange
        String reciboId = "recibo-123";
        String ventaId = "venta-123";
        ReciboData reciboData = mock(ReciboData.class);
        VentaData ventaData = mock(VentaData.class);
        Venta venta = mock(Venta.class);
        List<DetalleVenta> detallesEsperados = List.of(mock(DetalleVenta.class));
        Recibo reciboEsperado = mock(Recibo.class);

        when(reciboDataJpaRepository.findById(reciboId)).thenReturn(Optional.of(reciboData));
        when(reciboData.getVentaId()).thenReturn(ventaId);
        when(ventaDataJpaRepository.findById(ventaId)).thenReturn(Optional.of(ventaData));
        when(ventaMapper.toVenta(ventaData)).thenReturn(venta);
        when(venta.getDetalles()).thenReturn(detallesEsperados);
        when(reciboMapper.toRecibo(reciboData, detallesEsperados)).thenReturn(reciboEsperado);

        // Act
        Recibo resultado = reciboDataGateway.buscarReciboPorId(reciboId);

        // Assert
        assertNotNull(resultado);
        assertEquals(reciboEsperado, resultado);
    }

    @Test
    void buscarReciboPorId_NoExiste_RetornaNull() {
        // Arrange
        String reciboId = "recibo-123";
        when(reciboDataJpaRepository.findById(reciboId)).thenReturn(Optional.empty());

        // Act
        Recibo resultado = reciboDataGateway.buscarReciboPorId(reciboId);

        // Assert
        assertNull(resultado);
        verify(ventaDataJpaRepository, never()).findById(anyString());
    }
}