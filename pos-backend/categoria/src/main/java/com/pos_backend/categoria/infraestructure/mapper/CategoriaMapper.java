package com.pos_backend.categoria.infraestructure.mapper;

import com.pos_backend.categoria.domain.model.Categoria;
import com.pos_backend.categoria.infraestructure.driver_adapters.jpa_repository.CategoriaData;
import org.springframework.stereotype.Component;

@Component
public class CategoriaMapper {

    public Categoria toCategoria(CategoriaData data) {
        Categoria c = new Categoria();
        c.setCategoriaId(data.getCategoriaId());
        c.setNombre(data.getNombre());
        c.setDescripcion(data.getDescripcion());
        c.setIcono(data.getIcono());
        c.setActivo(data.getActivo());
        c.setEmpresaId(data.getEmpresaId());
        return c;
    }

    public CategoriaData toCategoriaData(Categoria c) {
        CategoriaData data = new CategoriaData();
        data.setCategoriaId(c.getCategoriaId());
        data.setNombre(c.getNombre());
        data.setDescripcion(c.getDescripcion());
        data.setIcono(c.getIcono());
        data.setActivo(c.getActivo());
        data.setEmpresaId(c.getEmpresaId());
        return data;
    }
}
