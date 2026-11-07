package com.cesde.proyectointegrador.modules.categorias.repository;

import com.cesde.proyectointegrador.modules.categorias.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Integer> {

    Optional<Categoria> findByCodigo(String codigo);

    @Query("SELECT c FROM Categoria c WHERE " +
           "(:search IS NULL OR LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.codigo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:estado IS NULL OR c.estado = :estado) " +
           "ORDER BY c.id DESC")
    List<Categoria> findByFilters(@Param("search") String search, @Param("estado") String estado);
}
