package com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository;

import com.pos_backend.inventario.domain.model.Producto;
import com.pos_backend.inventario.infraestructure.mapper.ProductoMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ProductoDataGatewayImplTest {

    private ProductoDataJpaRepository productoDataJpaRepository;
    private ProductoMapper productoMapper;
    private ProductoDataGatewayImpl productoDataGateway;

    @BeforeEach
    void setUp() {
        productoDataJpaRepository = mock(ProductoDataJpaRepository.class);
        productoMapper = mock(ProductoMapper.class);
        productoDataGateway = new ProductoDataGatewayImpl(productoDataJpaRepository, productoMapper);
    }

    @Test
    void guardarProducto_ShouldSaveAndReturnProducto() {
        // Arrange
        Producto productoInput = new Producto("PROD1", "Leche", "1L", 4500.0, 10);
        ProductoData productoDataMock = new ProductoData("PROD1", "Leche", "1L", 4500.0, 10);
        Producto productoEsperado = new Producto("PROD1", "Leche", "1L", 4500.0, 10);

        when(productoMapper.topruductoData(productoInput)).thenReturn(productoDataMock);
        when(productoDataJpaRepository.save(productoDataMock)).thenReturn(productoDataMock);
        when(productoMapper.topruducto(productoDataMock)).thenReturn(productoEsperado);

        // Act
        Producto resultado = productoDataGateway.guardarProducto(productoInput);

        // Assert
        assertNotNull(resultado);
        assertEquals("PROD1", resultado.getProductoId());
        verify(productoMapper, times(1)).topruductoData(productoInput);
        verify(productoDataJpaRepository, times(1)).save(productoDataMock);
        verify(productoMapper, times(1)).topruducto(productoDataMock);
    }

    @Test
    void buscarProductoPorId_WhenProductExists_ShouldReturnProducto() {
        // Arrange
        String productoId = "PROD1";
        ProductoData productoDataMock = new ProductoData("PROD1", "Leche", "1L", 4500.0, 10);
        Producto productoEsperado = new Producto("PROD1", "Leche", "1L", 4500.0, 10);

        when(productoDataJpaRepository.findById(productoId)).thenReturn(Optional.of(productoDataMock));
        when(productoMapper.topruducto(productoDataMock)).thenReturn(productoEsperado);

        // Act
        Producto resultado = productoDataGateway.buscarProductoPorId(productoId);

        // Assert
        assertNotNull(resultado);
        assertEquals("PROD1", resultado.getProductoId());
        verify(productoDataJpaRepository, times(1)).findById(productoId);
        verify(productoMapper, times(1)).topruducto(productoDataMock);
    }

    @Test
    void buscarProductoPorId_WhenProductDoesNotExist_ShouldReturnNull() {
        // Arrange
        String productoId = "PROD_NO_EXISTE";

        when(productoDataJpaRepository.findById(productoId)).thenReturn(Optional.empty());
        when(productoMapper.topruducto(null)).thenReturn(null);

        // Act
        Producto resultado = productoDataGateway.buscarProductoPorId(productoId);

        // Assert
        assertNull(resultado);
        verify(productoDataJpaRepository, times(1)).findById(productoId);
        verify(productoMapper, times(1)).topruducto(null);
    }

    @Test
    void eliminarProductoPorId_ShouldCallRepositoryDelete() {
        // Arrange
        String productoId = "PROD1";
        doNothing().when(productoDataJpaRepository).deleteById(productoId);

        // Act
        productoDataGateway.eliminarProductoPorId(productoId);

        // Assert
        verify(productoDataJpaRepository, times(1)).deleteById(productoId);
    }
}