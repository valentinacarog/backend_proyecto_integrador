package com.cesde.proyectointegrador.modules.registros.repository;

import com.cesde.proyectointegrador.modules.registros.entity.Registro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegistroRepository extends JpaRepository<Registro, Integer> {

    Optional<Registro> findByCodigo(String codigo);

    @Query("SELECT reg FROM Registro reg WHERE " +
           "(:search IS NULL OR LOWER(reg.codigo) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(reg.descripcion) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(reg.usuario.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(reg.usuario.apellido) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:tipo IS NULL OR reg.tipo = :tipo) AND " +
           "(:estado IS NULL OR reg.estado = :estado) AND " +
           "(:usuarioId IS NULL OR reg.usuario.id = :usuarioId) AND " +
           "(:retoId IS NULL OR reg.reto.id = :retoId) AND " +
           "(:empresaId IS NULL OR reg.empresa.id = :empresaId) " +
           "ORDER BY reg.id DESC")
    List<Registro> findByFilters(@Param("search") String search,
                                 @Param("tipo") String tipo,
                                 @Param("estado") String estado,
                                 @Param("usuarioId") Integer usuarioId,
                                 @Param("retoId") Integer retoId,
                                 @Param("empresaId") Integer empresaId);
}
