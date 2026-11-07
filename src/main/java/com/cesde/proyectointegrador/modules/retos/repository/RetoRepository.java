package com.cesde.proyectointegrador.modules.retos.repository;

import com.cesde.proyectointegrador.modules.retos.entity.Reto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RetoRepository extends JpaRepository<Reto, Integer> {

    @Query("SELECT r FROM Reto r WHERE " +
           "(:search IS NULL OR LOWER(r.titulo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.descripcion) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(r.empresa.nombre) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:dificultad IS NULL OR r.dificultad = :dificultad) AND " +
           "(:estado IS NULL OR r.estado = :estado) AND " +
           "(:empresaId IS NULL OR r.empresa.id = :empresaId) AND " +
           "(:categoriaId IS NULL OR r.categoria.id = :categoriaId) " +
           "ORDER BY r.id DESC")
    List<Reto> findByFilters(@Param("search") String search,
                             @Param("dificultad") String dificultad,
                             @Param("estado") String estado,
                             @Param("empresaId") Integer empresaId,
                             @Param("categoriaId") Integer categoriaId);
}
