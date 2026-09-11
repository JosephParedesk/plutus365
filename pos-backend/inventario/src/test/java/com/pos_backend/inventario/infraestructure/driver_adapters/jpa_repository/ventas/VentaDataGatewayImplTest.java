package com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository.ventas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VentaDataGatewayImplTest {

    private VentaDataGatewayImpl ventaDataGateway;
    private VentaDataJpaRepository ventaDataJpaRepository;
    private VentaMapper ventaMapper;

    @BeforeEach
    void setUp() {
        ventaDataJpaRepository = mock(VentaDataJpaRepository.class);
        ventaMapper = mock(VentaMapper.class);
        ventaDataGateway = new VentaDataGatewayImpl(ventaDataJpaRepository, ventaMapper);
    }

    @Test
    void guardarVenta_Exitoso_RetornaVenta() {
        // Arrange
        Venta venta = new Venta();
        VentaData ventaData = new VentaData();
        when(ventaMapper.toVentaData(venta)).thenReturn(ventaData);
        when(ventaDataJpaRepository.save(ventaData)).thenReturn(ventaData);
        when(ventaMapper.toVenta(ventaData)).thenReturn(venta);

        // Act
        Venta resultado = ventaDataGateway.guardarVenta(venta);

        // Assert
        assertNotNull(resultado);
        assertEquals(venta, resultado);
        verify(ventaDataJpaRepository, times(1)).save(ventaData);
    }

    @Test
    void buscarVentaPorId_Existe_RetornaVenta() {
        // Arrange
        String ventaId = "venta-123";
        VentaData ventaData = new VentaData();
        Venta venta = new Venta();
        when(ventaDataJpaRepository.findById(ventaId)).thenReturn(Optional.of(ventaData));
        when(ventaMapper.toVenta(ventaData)).thenReturn(venta);

        // Act
        Venta resultado = ventaDataGateway.buscarVentaPorId(ventaId);

        // Assert
        assertNotNull(resultado);
        assertEquals(venta, resultado);
    }

    @Test
    void buscarVentaPorId_NoExiste_RetornaNull() {
        // Arrange
        String ventaId = "venta-123";
        when(ventaDataJpaRepository.findById(ventaId)).thenReturn(Optional.empty());
        when(ventaMapper.toVenta((VentaData) null)).thenReturn(null);

        // Act
        Venta resultado = ventaDataGateway.buscarVentaPorId(ventaId);

        // Assert
        assertNull(resultado);
        verify(ventaMapper, times(1)).toVenta((VentaData) null);
    }


    @Test
    void buscarVentasPorCliente_ConVentas_RetornaLista() {
        // Arrange
        String clienteId = "cliente-123";
        VentaData ventaData1 = mock(VentaData.class);
        VentaData ventaData2 = mock(VentaData.class);
        Venta venta1 = new Venta();
        Venta venta2 = new Venta();

        when(ventaDataJpaRepository.findByClienteId(clienteId)).thenReturn(List.of(ventaData1, ventaData2));
        when(ventaMapper.toVenta(ventaData1)).thenReturn(venta1);
        when(ventaMapper.toVenta(ventaData2)).thenReturn(venta2);

        // Act
        List<Venta> resultado = ventaDataGateway.buscarVentasPorCliente(clienteId);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(venta1, resultado.get(0)); // Ahora sí será venta1
        assertEquals(venta2, resultado.get(1)); // Y este será venta2
    }
    @Test
    void buscarVentasPorCliente_Vacio_RetornaListaVacia() {
        // Arrange
        String clienteId = "cliente-123";
        when(ventaDataJpaRepository.findByClienteId(clienteId)).thenReturn(Collections.emptyList());

        // Act
        List<Venta> resultado = ventaDataGateway.buscarVentasPorCliente(clienteId);

        // Assert
        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verify(ventaMapper, never()).toVenta(any(VentaData.class));
    }
}