package com.cesde.proyectointegrador.modules.empresas.repository;

import com.cesde.proyectointegrador.modules.empresas.entity.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Integer> {

    Optional<Empresa> findByNit(String nit);
    Optional<Empresa> findByEmail(String email);

    @Query("SELECT e FROM Empresa e WHERE " +
           "(:search IS NULL OR LOWER(e.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.nit) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.email) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(e.sector) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "(:sector IS NULL OR e.sector = :sector) AND " +
           "(:estado IS NULL OR e.estado = :estado) " +
           "ORDER BY e.id DESC")
    List<Empresa> findByFilters(@Param("search") String search,
                                @Param("sector") String sector,
                                @Param("estado") String estado);
}
