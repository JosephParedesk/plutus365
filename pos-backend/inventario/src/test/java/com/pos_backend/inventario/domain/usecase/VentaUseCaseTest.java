package com.pos_backend.inventario.domain.usecase;

import com.pos_backend.inventario.domain.model.Producto;
import com.pos_backend.inventario.domain.model.gateway.ProductoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class VentaUseCaseTest {

    private VentaUseCase ventaUseCase;
    private VentaGateway ventaGateway;
    private ProductoGateway productoGateway;
    private ReciboGateway reciboGateway;

    @BeforeEach
    void setUp() {
        ventaGateway = mock(VentaGateway.class);
        productoGateway = mock(ProductoGateway.class);
        reciboGateway = mock(ReciboGateway.class);
        ventaUseCase = new VentaUseCase(ventaGateway, productoGateway, reciboGateway);
    }

    @Test
    void realizarVenta_ClienteIdNull_LanzaIllegalArgumentException() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ventaUseCase.realizarVenta(venta));
    }

    @Test
    void realizarVenta_ClienteIdBlanco_LanzaIllegalArgumentException() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId("   ");

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ventaUseCase.realizarVenta(venta));
    }

    @Test
    void realizarVenta_DetallesNull_LanzaIllegalArgumentException() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId("cliente-123");
        venta.setDetalles(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ventaUseCase.realizarVenta(venta));
    }

    @Test
    void realizarVenta_DetallesVacios_LanzaIllegalArgumentException() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId("cliente-123");
        venta.setDetalles(new ArrayList<>());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ventaUseCase.realizarVenta(venta));
    }

    @Test
    void realizarVenta_DetalleProductoIdNull_LanzaIllegalArgumentException() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId("cliente-123");
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId(null);
        venta.setDetalles(List.of(detalle));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ventaUseCase.realizarVenta(venta));
    }

    @Test
    void realizarVenta_DetalleProductoIdBlanco_LanzaIllegalArgumentException() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId("cliente-123");
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId("");
        venta.setDetalles(List.of(detalle));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ventaUseCase.realizarVenta(venta));
    }

    @Test
    void realizarVenta_DetalleCantidadNull_LanzaIllegalArgumentException() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId("cliente-123");
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId("prod-123");
        detalle.setCantidad(null);
        venta.setDetalles(List.of(detalle));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ventaUseCase.realizarVenta(venta));
    }

    @Test
    void realizarVenta_DetalleCantidadCeroOMenos_LanzaIllegalArgumentException() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId("cliente-123");
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId("prod-123");
        detalle.setCantidad(0);
        venta.setDetalles(List.of(detalle));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ventaUseCase.realizarVenta(venta));
    }

    @Test
    void realizarVenta_ProductoNoExiste_LanzaNoSuchElementException() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId("cliente-123");
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId("prod-123");
        detalle.setCantidad(2);
        venta.setDetalles(List.of(detalle));

        when(productoGateway.buscarProductoPorId("prod-123")).thenReturn(null);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> ventaUseCase.realizarVenta(venta));
    }

    @Test
    void realizarVenta_StockInsuficiente_LanzaRuntimeException() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId("cliente-123");
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId("prod-123");
        detalle.setCantidad(10);
        venta.setDetalles(List.of(detalle));

        Producto producto = new Producto();
        producto.setProductoId("prod-123");
        producto.setNombre("Arroz");
        producto.setPrecio(2500.0);
        producto.setStock(5);

        when(productoGateway.buscarProductoPorId("prod-123")).thenReturn(producto);

        // Act & Assert
        assertThrows(RuntimeException.class, () -> ventaUseCase.realizarVenta(venta));
    }

    @Test
    void realizarVenta_Exitoso_RetornaRecibo() {
        // Arrange
        Venta venta = new Venta();
        venta.setClienteId("cliente-123");
        DetalleVenta detalle = new DetalleVenta();
        detalle.setProductoId("prod-123");
        detalle.setCantidad(2);
        venta.setDetalles(List.of(detalle));

        Producto producto = new Producto();
        producto.setProductoId("prod-123");
        producto.setNombre("Arroz");
        producto.setPrecio(2000.0);
        producto.setStock(10);

        when(productoGateway.buscarProductoPorId("prod-123")).thenReturn(producto);
        when(productoGateway.guardarProducto(any(Producto.class))).thenReturn(producto);
        when(ventaGateway.guardarVenta(any(Venta.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(reciboGateway.guardarRecibo(any(Recibo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Recibo recibo = ventaUseCase.realizarVenta(venta);

        // Assert
        assertNotNull(recibo);
        assertEquals("COMPLETADA", venta.getEstado());
        assertEquals(4000.0, venta.getTotal());
        assertEquals(8, producto.getStock()); // 10 - 2
        verify(productoGateway, times(1)).guardarProducto(producto);
        verify(ventaGateway, times(1)).guardarVenta(venta);
        verify(reciboGateway, times(1)).guardarRecibo(any(Recibo.class));
    }

    @Test
    void generarRecibo_Exitoso_CalculaValoresCorrectamente() {
        // Arrange
        Venta venta = new Venta();
        venta.setVentaId("venta-123");
        venta.setClienteId("cliente-123");
        venta.setTotal(100.0);
        venta.setDetalles(new ArrayList<>());

        when(reciboGateway.guardarRecibo(any(Recibo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Recibo recibo = ventaUseCase.generarRecibo(venta);

        // Assert
        assertNotNull(recibo);
        assertEquals(100.0, recibo.getSubtotal());
        assertEquals(19.0, recibo.getImpuesto()); // 100 * 0.19
        assertEquals(119.0, recibo.getTotal());
    }

    @Test
    void obtenerReciboPorVenta_Existe_RetornaRecibo() {
        // Arrange
        String ventaId = "venta-123";
        Recibo reciboEsperado = new Recibo();
        when(reciboGateway.buscarReciboPorVentaId(ventaId)).thenReturn(reciboEsperado);

        // Act
        Recibo resultado = ventaUseCase.obtenerReciboPorVenta(ventaId);

        // Assert
        assertNotNull(resultado);
        assertEquals(reciboEsperado, resultado);
    }

    @Test
    void obtenerReciboPorVenta_NoExiste_LanzaNoSuchElementException() {
        // Arrange
        String ventaId = "venta-123";
        when(reciboGateway.buscarReciboPorVentaId(ventaId)).thenReturn(null);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> ventaUseCase.obtenerReciboPorVenta(ventaId));
    }

    @Test
    void obtenerReciboPorId_Existe_RetornaRecibo() {
        // Arrange
        String reciboId = "recibo-123";
        Recibo reciboEsperado = new Recibo();
        when(reciboGateway.buscarReciboPorId(reciboId)).thenReturn(reciboEsperado);

        // Act
        Recibo resultado = ventaUseCase.obtenerReciboPorId(reciboId);

        // Assert
        assertNotNull(resultado);
        assertEquals(reciboEsperado, resultado);
    }

    @Test
    void obtenerReciboPorId_NoExiste_LanzaNoSuchElementException() {
        // Arrange
        String reciboId = "recibo-123";
        when(reciboGateway.buscarReciboPorId(reciboId)).thenReturn(null);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> ventaUseCase.obtenerReciboPorId(reciboId));
    }

    @Test
    void obtenerVentasPorCliente_IdNull_LanzaIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ventaUseCase.obtenerVentasPorCliente(null));
    }

    @Test
    void obtenerVentasPorCliente_IdBlanco_LanzaIllegalArgumentException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> ventaUseCase.obtenerVentasPorCliente("   "));
    }

    @Test
    void obtenerVentasPorCliente_ListaNull_LanzaNoSuchElementException() {
        // Arrange
        String clienteId = "cliente-123";
        when(ventaGateway.buscarVentasPorCliente(clienteId)).thenReturn(null);

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> ventaUseCase.obtenerVentasPorCliente(clienteId));
    }

    @Test
    void obtenerVentasPorCliente_ListaVacia_LanzaNoSuchElementException() {
        // Arrange
        String clienteId = "cliente-123";
        when(ventaGateway.buscarVentasPorCliente(clienteId)).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> ventaUseCase.obtenerVentasPorCliente(clienteId));
    }

    @Test
    void obtenerVentasPorCliente_ConVentas_RetornaLista() {
        // Arrange
        String clienteId = "cliente-123";
        List<Venta> ventasEsperadas = List.of(new Venta(), new Venta());
        when(ventaGateway.buscarVentasPorCliente(clienteId)).thenReturn(ventasEsperadas);

        // Act
        List<Venta> resultado = ventaUseCase.obtenerVentasPorCliente(clienteId);

        // Assert
        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(ventasEsperadas, resultado);
    }
}