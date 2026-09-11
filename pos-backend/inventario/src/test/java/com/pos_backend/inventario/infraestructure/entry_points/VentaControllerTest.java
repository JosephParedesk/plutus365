package com.pos_backend.inventario.infraestructure.entry_points;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VentaControllerTest {

    private VentaController ventaController;
    private VentaUseCase ventaUseCase;
    private VentaMapper ventaMapper;

    @BeforeEach
    void setUp() {
        ventaUseCase = mock(VentaUseCase.class);
        ventaMapper = mock(VentaMapper.class);
        ventaController = new VentaController(ventaUseCase, ventaMapper);
    }

    @Test
    void realizarVenta_Exitoso_RetornaRecibo() {
        // Arrange
        VentaRequest request = mock(VentaRequest.class);
        Venta venta = new Venta();
        Recibo reciboEsperado = new Recibo();

        when(ventaMapper.toVenta(request)).thenReturn(venta);
        when(ventaUseCase.realizarVenta(venta)).thenReturn(reciboEsperado);

        // Act
        ResponseEntity<Recibo> response = ventaController.realizarVenta(request);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reciboEsperado, response.getBody());
        verify(ventaMapper, times(1)).toVenta(request);
        verify(ventaUseCase, times(1)).realizarVenta(venta);
    }

    @Test
    void obtenerReciboPorVenta_Exitoso_RetornaRecibo() {
        // Arrange
        String ventaId = "venta-123";
        Recibo reciboEsperado = new Recibo();
        when(ventaUseCase.obtenerReciboPorVenta(ventaId)).thenReturn(reciboEsperado);

        // Act
        ResponseEntity<Recibo> response = ventaController.obtenerReciboPorVenta(ventaId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reciboEsperado, response.getBody());
        verify(ventaUseCase, times(1)).obtenerReciboPorVenta(ventaId);
    }

    @Test
    void obtenerReciboPorId_Exitoso_RetornaRecibo() {
        // Arrange
        String reciboId = "recibo-123";
        Recibo reciboEsperado = new Recibo();
        when(ventaUseCase.obtenerReciboPorId(reciboId)).thenReturn(reciboEsperado);

        // Act
        ResponseEntity<Recibo> response = ventaController.obtenerReciboPorId(reciboId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(reciboEsperado, response.getBody());
        verify(ventaUseCase, times(1)).obtenerReciboPorId(reciboId);
    }

    @Test
    void obtenerVentasPorCliente_Exitoso_RetornaListaVentas() {
        // Arrange
        String clienteId = "cliente-123";
        List<Venta> ventasEsperadas = List.of(new Venta(), new Venta());
        when(ventaUseCase.obtenerVentasPorCliente(clienteId)).thenReturn(ventasEsperadas);

        // Act
        ResponseEntity<List<Venta>> response = ventaController.obtenerVentasPorCliente(clienteId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(ventasEsperadas, response.getBody());
        assertEquals(2, response.getBody().size());
        verify(ventaUseCase, times(1)).obtenerVentasPorCliente(clienteId);
    }
}