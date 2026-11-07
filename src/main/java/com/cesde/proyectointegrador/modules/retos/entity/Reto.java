package com.cesde.proyectointegrador.modules.retos.entity;

import com.cesde.proyectointegrador.modules.categorias.entity.Categoria;
import com.cesde.proyectointegrador.modules.empresas.entity.Empresa;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "retos")
public class Reto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @JdbcTypeCode(Types.CHAR)
    @Column(nullable = false, columnDefinition = "ENUM('FACIL', 'MEDIA', 'DIFICIL')")
    private String dificultad = "MEDIA";

    @Column(nullable = false)
    private Integer puntos;

    @JsonProperty("fechaLimite")
    @Column(name = "fecha_limite", nullable = false)
    private LocalDate fechaLimite;

    @JdbcTypeCode(Types.CHAR)
    @Column(nullable = false, columnDefinition = "ENUM('ABIERTO', 'EN_PROGRESO', 'CERRADO', 'FINALIZADO')")
    private String estado = "ABIERTO";

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @JsonProperty("createdAt")
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Reto() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public String getDificultad() { return dificultad; }
    public void setDificultad(String dificultad) { this.dificultad = dificultad; }

    public Integer getPuntos() { return puntos; }
    public void setPuntos(Integer puntos) { this.puntos = puntos; }

    public LocalDate getFechaLimite() { return fechaLimite; }
    public void setFechaLimite(LocalDate fechaLimite) { this.fechaLimite = fechaLimite; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    @JsonProperty("empresaId")
    public Integer getEmpresaId() {
        return empresa != null ? empresa.getId() : null;
    }

    @JsonProperty("empresaNombre")
    public String getEmpresaNombre() {
        return empresa != null ? empresa.getNombre() : null;
    }

    @JsonProperty("categoriaId")
    public Integer getCategoriaId() {
        return categoria != null ? categoria.getId() : null;
    }

    @JsonProperty("categoriaNombre")
    public String getCategoriaNombre() {
        return categoria != null ? categoria.getNombre() : null;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
