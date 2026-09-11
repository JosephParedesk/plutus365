package com.pos_backend.inventario.domain.usecase;

import com.pos_backend.inventario.domain.model.Producto;
import com.pos_backend.inventario.domain.model.gateway.ProductoGateway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoUseCaseTest {

    private ProductoGateway productoGateway;
    private ProductoUseCase productoUseCase;

    @BeforeEach
    void setUp() {
        productoGateway = mock(ProductoGateway.class);
        productoUseCase = new ProductoUseCase(productoGateway);
    }

    @Test
    void guardarProducto_Success_ShouldSaveAndReturnProducto() {
        // Arrange
        Producto productoInput = new Producto("PROD123", "Arroz", "1kg", 3500.0, 10);
        when(productoGateway.guardarProducto(productoInput)).thenReturn(productoInput);

        // Act
        Producto resultado = productoUseCase.guardarProducto(productoInput);

        // Assert
        assertNotNull(resultado);
        assertEquals("PROD123", resultado.getProductoId());
        verify(productoGateway, times(1)).guardarProducto(productoInput);
    }

    @Test
    void guardarProducto_WhenStockIsNull_ShouldSetStockToZeroAndSave() {
        // Arrange
        Producto productoInput = new Producto("PROD123", "Arroz", "1kg", 3500.0, null);
        when(productoGateway.guardarProducto(productoInput)).thenReturn(productoInput);

        // Act
        Producto resultado = productoUseCase.guardarProducto(productoInput);

        // Assert
        assertNotNull(resultado);
        assertEquals(0, resultado.getStock());
    }

    @Test
    void guardarProducto_WhenIdIsNull_ShouldThrowException() {
        // Arrange
        Producto productoInput = new Producto(null, "Arroz", "1kg", 3500.0, 10);

        // Act
        Executable action = () -> productoUseCase.guardarProducto(productoInput);

        // Assert
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertEquals("El ID del producto es obligatorio", exception.getMessage());
    }

    @Test
    void guardarProducto_WhenIdIsBlank_ShouldThrowException() {
        // Arrange
        Producto productoInput = new Producto("   ", "Arroz", "1kg", 3500.0, 10);

        // Act
        Executable action = () -> productoUseCase.guardarProducto(productoInput);

        // Assert
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertEquals("El ID del producto es obligatorio", exception.getMessage());
    }

    @Test
    void guardarProducto_WhenPrecioIsNull_ShouldThrowException() {
        // Arrange
        Producto productoInput = new Producto("PROD123", "Arroz", "1kg", null, 10);

        // Act
        Executable action = () -> productoUseCase.guardarProducto(productoInput);

        // Assert
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertEquals("El precio debe ser mayor a 0", exception.getMessage());
    }

    @Test
    void guardarProducto_WhenPrecioIsZeroOrLess_ShouldThrowException() {
        // Arrange
        Producto productoInput = new Producto("PROD123", "Arroz", "1kg", 0.0, 10);

        // Act
        Executable action = () -> productoUseCase.guardarProducto(productoInput);

        // Assert
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertEquals("El precio debe ser mayor a 0", exception.getMessage());
    }

    @Test
    void guardarProducto_WhenStockIsNegative_ShouldThrowException() {
        // Arrange
        Producto productoInput = new Producto("PROD123", "Arroz", "1kg", 3500.0, -5);

        // Act
        Executable action = () -> productoUseCase.guardarProducto(productoInput);

        // Assert
        RuntimeException exception = assertThrows(RuntimeException.class, action);
        assertEquals("El stock no puede ser negativo", exception.getMessage());
    }

    @Test
    void buscarProductoPorId_Success_ShouldReturnProducto() {
        // Arrange
        Producto productoEsperado = new Producto("PROD123", "Arroz", "1kg", 3500.0, 10);
        when(productoGateway.buscarProductoPorId("PROD123")).thenReturn(productoEsperado);

        // Act
        Producto resultado = productoUseCase.buscarProductoPorId("PROD123");

        // Assert
        assertNotNull(resultado);
        assertEquals("PROD123", resultado.getProductoId());
    }

    @Test
    void buscarProductoPorId_Exception_ShouldReturnEmptyProducto() {
        // Arrange
        when(productoGateway.buscarProductoPorId("PROD123")).thenThrow(new RuntimeException("DB Error"));

        // Act
        Producto resultado = productoUseCase.buscarProductoPorId("PROD123");

        // Assert
        assertNotNull(resultado);
        assertNull(resultado.getProductoId());
    }

    @Test
    void eliminarProductoPorId_Success_ShouldCallGateway() {
        // Arrange
        String productoId = "PROD123";

        // Act
        productoUseCase.eliminarProductoPorId(productoId);

        // Assert
        verify(productoGateway, times(1)).eliminarProductoPorId(productoId);
    }

    @Test
    void eliminarProductoPorId_Exception_ShouldCatchException() {
        // Arrange
        doThrow(new RuntimeException("Delete Error")).when(productoGateway).eliminarProductoPorId("PROD123");

        // Act
        Executable action = () -> productoUseCase.eliminarProductoPorId("PROD123");

        // Assert
        assertDoesNotThrow(action);
    }
}