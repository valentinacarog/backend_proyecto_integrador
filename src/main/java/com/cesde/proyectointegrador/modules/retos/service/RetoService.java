package com.cesde.proyectointegrador.modules.retos.service;

import com.cesde.proyectointegrador.modules.categorias.repository.CategoriaRepository;
import com.cesde.proyectointegrador.modules.empresas.repository.EmpresaRepository;
import com.cesde.proyectointegrador.modules.retos.entity.Reto;
import com.cesde.proyectointegrador.modules.retos.repository.RetoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class RetoService {

    @Autowired
    private RetoRepository retoRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    public List<Reto> getAll(String search, String dificultad, String estado, Integer empresaId, Integer categoriaId) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanDificultad = (dificultad != null && !dificultad.trim().isEmpty()) ? dificultad.trim() : null;
        String cleanEstado = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;
        return retoRepository.findByFilters(cleanSearch, cleanDificultad, cleanEstado, empresaId, categoriaId);
    }

    public Optional<Reto> getById(Integer id) {
        return retoRepository.findById(id);
    }

    public Reto create(Reto reto, Integer empresaId, Integer categoriaId) {
        if (empresaId != null) {
            empresaRepository.findById(empresaId).ifPresent(reto::setEmpresa);
        }
        if (categoriaId != null) {
            categoriaRepository.findById(categoriaId).ifPresent(reto::setCategoria);
        }
        return retoRepository.save(reto);
    }

    public Optional<Reto> update(Integer id, Reto data, Integer empresaId, Integer categoriaId) {
        return retoRepository.findById(id).map(r -> {
            if (data.getTitulo() != null) r.setTitulo(data.getTitulo());
            if (data.getDescripcion() != null) r.setDescripcion(data.getDescripcion());
            if (data.getDificultad() != null) r.setDificultad(data.getDificultad());
            if (data.getPuntos() != null) r.setPuntos(data.getPuntos());
            if (data.getFechaLimite() != null) r.setFechaLimite(data.getFechaLimite());
            if (data.getEstado() != null) r.setEstado(data.getEstado());

            if (empresaId != null) {
                empresaRepository.findById(empresaId).ifPresent(r::setEmpresa);
            }
            if (categoriaId != null) {
                categoriaRepository.findById(categoriaId).ifPresent(r::setCategoria);
            }

            return retoRepository.save(r);
        });
    }

    public boolean delete(Integer id) {
        if (retoRepository.existsById(id)) {
            retoRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
