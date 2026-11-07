package com.cesde.proyectointegrador.modules.usuarios.repository;

import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    @Query("SELECT u FROM Usuario u WHERE " +
           "(:search IS NULL OR LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.apellido) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:rol IS NULL OR u.rol = :rol) AND " +
           "(:estado IS NULL OR u.estado = :estado) " +
           "ORDER BY u.id DESC")
    List<Usuario> findByFilters(@Param("search") String search,
                                @Param("rol") String rol,
                                @Param("estado") String estado);
}
