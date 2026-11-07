package com.cesde.proyectointegrador.modules.categorias.service;

import com.cesde.proyectointegrador.modules.categorias.entity.Categoria;
import com.cesde.proyectointegrador.modules.categorias.repository.CategoriaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoriaService {

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Categoria> getAll(String search, String estado) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanEstado = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;
        return categoriaRepository.findByFilters(cleanSearch, cleanEstado);
    }

    public Optional<Categoria> getById(Integer id) {
        return categoriaRepository.findById(id);
    }

    public Categoria create(Categoria categoria) {
        if (categoria.getCodigo() == null || categoria.getCodigo().trim().isEmpty()) {
            categoria.setCodigo("CAT-" + (System.currentTimeMillis() % 100000));
        } else {
            categoria.setCodigo(categoria.getCodigo().trim().toUpperCase());
        }
        if (categoria.getDescripcion() == null || categoria.getDescripcion().trim().isEmpty()) {
            categoria.setDescripcion(categoria.getNombre() != null ? categoria.getNombre() : "Categoría de retos");
        }
        if (categoria.getColor() == null || categoria.getColor().trim().isEmpty()) {
            categoria.setColor("#6366f1");
        }
        if (categoria.getEstado() == null || categoria.getEstado().trim().isEmpty()) {
            categoria.setEstado("ACTIVO");
        }
        return categoriaRepository.save(categoria);
    }

    public Optional<Categoria> update(Integer id, Categoria data) {
        return categoriaRepository.findById(id).map(cat -> {
            if (data.getNombre() != null) cat.setNombre(data.getNombre());
            if (data.getCodigo() != null) cat.setCodigo(data.getCodigo().trim().toUpperCase());
            if (data.getDescripcion() != null) cat.setDescripcion(data.getDescripcion());
            if (data.getColor() != null) cat.setColor(data.getColor());
            if (data.getEstado() != null) cat.setEstado(data.getEstado());
            return categoriaRepository.save(cat);
        });
    }

    public boolean delete(Integer id) {
        if (categoriaRepository.existsById(id)) {
            categoriaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
