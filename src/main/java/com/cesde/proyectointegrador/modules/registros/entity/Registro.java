package com.cesde.proyectointegrador.modules.registros.entity;

import com.cesde.proyectointegrador.modules.empresas.entity.Empresa;
import com.cesde.proyectointegrador.modules.retos.entity.Reto;
import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;
import java.time.LocalDateTime;

@Entity
@Table(name = "registros")
public class Registro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 40)
    private String codigo;

    @JdbcTypeCode(Types.CHAR)
    @Column(nullable = false, columnDefinition = "ENUM('INSCRIPCION', 'EVALUACION', 'AUDITORIA', 'ENTREGA')")
    private String tipo = "INSCRIPCION";

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "empresa_id")
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reto_id")
    private Reto reto;

    @JdbcTypeCode(Types.CHAR)
    @Column(nullable = false, columnDefinition = "ENUM('PENDIENTE', 'APROBADO', 'COMPLETADO', 'RECHAZADO')")
    private String estado = "PENDIENTE";

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @JsonProperty("createdAt")
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @JsonProperty("updatedAt")
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Registro() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public Empresa getEmpresa() { return empresa; }
    public void setEmpresa(Empresa empresa) { this.empresa = empresa; }

    public Reto getReto() { return reto; }
    public void setReto(Reto reto) { this.reto = reto; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }

    @JsonProperty("usuarioId")
    public Integer getUsuarioId() {
        return usuario != null ? usuario.getId() : null;
    }

    @JsonProperty("usuarioNombre")
    public String getUsuarioNombre() {
        return usuario != null ? (usuario.getNombre() + " " + usuario.getApellido()).trim() : null;
    }

    @JsonProperty("empresaId")
    public Integer getEmpresaId() {
        return empresa != null ? empresa.getId() : null;
    }

    @JsonProperty("empresaNombre")
    public String getEmpresaNombre() {
        return empresa != null ? empresa.getNombre() : null;
    }

    @JsonProperty("retoId")
    public Integer getRetoId() {
        return reto != null ? reto.getId() : null;
    }

    @JsonProperty("retoTitulo")
    public String getRetoTitulo() {
        return reto != null ? reto.getTitulo() : null;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
