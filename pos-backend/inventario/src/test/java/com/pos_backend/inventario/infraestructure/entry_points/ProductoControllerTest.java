package com.pos_backend.inventario.infraestructure.entry_points;

import com.pos_backend.inventario.domain.model.Producto;
import com.pos_backend.inventario.domain.usecase.ProductoUseCase;
import com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository.ProductoData;
import com.pos_backend.inventario.infraestructure.mapper.ProductoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoControllerTest {

    private ProductoUseCase productoUseCase;
    private ProductoMapper productoMapper;
    private ProductoController productoController;

    @BeforeEach
    void setUp() {
        productoUseCase = mock(ProductoUseCase.class);
        productoMapper = mock(ProductoMapper.class);
        productoController = new ProductoController(productoUseCase, productoMapper);
    }

    @Test
    void saveProducto_Success_ShouldReturnOk() {
        // Arrange
        ProductoData productoDataInput = new ProductoData("PROD1", "Leche", "1L", 4500.0, 10);
        Producto productoMapped = new Producto("PROD1", "Leche", "1L", 4500.0, 10);
        Producto productoSaved = new Producto("PROD1", "Leche", "1L", 4500.0, 10);

        when(productoMapper.topruducto(productoDataInput)).thenReturn(productoMapped);
        when(productoUseCase.guardarProducto(productoMapped)).thenReturn(productoSaved);

        // Act
        ResponseEntity<Producto> response = productoController.saveProducto(productoDataInput);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productoSaved, response.getBody());
        verify(productoMapper, times(1)).topruducto(productoDataInput);
        verify(productoUseCase, times(1)).guardarProducto(productoMapped);
    }

    @Test
    void saveProducto_Conflict_ShouldReturnConflict() {
        // Arrange
        ProductoData productoDataInput = new ProductoData(null, "Leche", "1L", 4500.0, 10);
        Producto productoMapped = new Producto(null, "Leche", "1L", 4500.0, 10);
        Producto productoSavedWithNullId = new Producto(null, "Leche", "1L", 4500.0, 10);

        when(productoMapper.topruducto(productoDataInput)).thenReturn(productoMapped);
        when(productoUseCase.guardarProducto(productoMapped)).thenReturn(productoSavedWithNullId);

        // Act
        ResponseEntity<Producto> response = productoController.saveProducto(productoDataInput);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void buscarProducto_Found_ShouldReturnOk() {
        // Arrange
        String productoId = "PROD1";
        Producto productoEncontrado = new Producto("PROD1", "Leche", "1L", 4500.0, 10);

        when(productoUseCase.buscarProductoPorId(productoId)).thenReturn(productoEncontrado);

        // Act
        ResponseEntity<Producto> response = productoController.buscarProducto(productoId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(productoEncontrado, response.getBody());
        verify(productoUseCase, times(1)).buscarProductoPorId(productoId);
    }

    @Test
    void buscarProducto_NotFound_ShouldReturnNotFound() {
        // Arrange
        String productoId = "PROD_NO_EXISTE";
        Producto productoVacio = new Producto(null, null, null, null, null);

        when(productoUseCase.buscarProductoPorId(productoId)).thenReturn(productoVacio);

        // Act
        ResponseEntity<Producto> response = productoController.buscarProducto(productoId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void eliminarProducto_ShouldReturnNoContent() {
        // Arrange
        String productoId = "PROD1";
        doNothing().when(productoUseCase).eliminarProductoPorId(productoId);

        // Act
        ResponseEntity<Void> response = productoController.eliminarProducto(productoId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(productoUseCase, times(1)).eliminarProductoPorId(productoId);
    }
}