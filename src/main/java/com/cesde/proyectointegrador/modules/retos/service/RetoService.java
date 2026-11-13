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
        if (reto.getTitulo() == null || reto.getTitulo().trim().isEmpty()) {
            reto.setTitulo("Reto de Innovación");
        }
        if (reto.getDescripcion() == null || reto.getDescripcion().trim().isEmpty()) {
            reto.setDescripcion(reto.getTitulo());
        }
        if (reto.getDificultad() == null || reto.getDificultad().trim().isEmpty()) {
            reto.setDificultad("MEDIA");
        }
        if (reto.getPuntos() == null) {
            reto.setPuntos(100);
        }
        if (reto.getFechaLimite() == null) {
            reto.setFechaLimite(LocalDate.now().plusMonths(1));
        }
        if (reto.getEstado() == null || reto.getEstado().trim().isEmpty()) {
            reto.setEstado("ABIERTO");
        }

        if (empresaId != null) {
            empresaRepository.findById(empresaId).ifPresent(reto::setEmpresa);
        }
        if (reto.getEmpresa() == null) {
            empresaRepository.findAll().stream().findFirst().ifPresent(reto::setEmpresa);
        }

        if (categoriaId != null) {
            categoriaRepository.findById(categoriaId).ifPresent(reto::setCategoria);
        }
        if (reto.getCategoria() == null) {
            categoriaRepository.findAll().stream().findFirst().ifPresent(reto::setCategoria);
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
            try {
                retoRepository.deleteById(id);
                return true;
            } catch (Exception e) {
                // Si el reto tiene registros asociados (FK RESTRICT), soft-delete
                retoRepository.findById(id).ifPresent(r -> {
                    r.setEstado("CERRADO");
                    retoRepository.save(r);
                });
                return true;
            }
        }
        return false;
    }
}
