package com.pos_backend.inventario.infraestructure.mapper;

import com.pos_backend.inventario.domain.model.Producto;
import com.pos_backend.inventario.infraestructure.driver_adapters.jpa_repository.ProductoData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ProductoMapperTest {

    private ProductoMapper productoMapper;

    @BeforeEach
    void setUp() {

        productoMapper = new ProductoMapper();
    }

    @Test
    void topruductoData_ShouldMapCorrectly() {
        // 1. Arrange:
        Producto producto = new Producto("1", "Leche", "Bolsa de 1 Litro", 4500.0, 20);

        // 2. Act:
        ProductoData resultado = productoMapper.topruductoData(producto);

        // 3. Assert:
        assertNotNull(resultado);
        assertEquals(producto.getProductoId(), resultado.getProductoId());
        assertEquals(producto.getNombre(), resultado.getNombre());
        assertEquals(producto.getDescripcion(), resultado.getDescripcion());
        assertEquals(producto.getPrecio(), resultado.getPrecio());
        assertEquals(producto.getStock(), resultado.getStock());
    }

    @Test
    void topruducto_ShouldMapCorrectly() {
        // Arrange:
        ProductoData productoData = new ProductoData("2", "Arroz", "Arroz Diana 1kg", 3800.0, 50);

        // Act:
        Producto resultado = productoMapper.topruducto(productoData);

        // Assert
        assertNotNull(resultado);
        assertEquals(productoData.getProductoId(), resultado.getProductoId());
        assertEquals(productoData.getNombre(), resultado.getNombre());
        assertEquals(productoData.getDescripcion(), resultado.getDescripcion());
        assertEquals(productoData.getPrecio(), resultado.getPrecio());
        assertEquals(productoData.getStock(), resultado.getStock());
    }
}