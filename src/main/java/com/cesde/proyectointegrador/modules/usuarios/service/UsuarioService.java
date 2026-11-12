package com.cesde.proyectointegrador.modules.usuarios.service;

import com.cesde.proyectointegrador.modules.empresas.repository.EmpresaRepository;
import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import com.cesde.proyectointegrador.modules.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<Usuario> getAll(String search, String rol, String estado) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanRol = (rol != null && !rol.trim().isEmpty()) ? rol.trim() : null;
        String cleanEstado = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;
        return usuarioRepository.findByFilters(cleanSearch, cleanRol, cleanEstado);
    }

    public Optional<Usuario> getById(Integer id) {
        return usuarioRepository.findById(id);
    }

    public Optional<Usuario> getByEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email);
    }

    public Usuario create(Usuario usuario, Integer empresaId) {
        if (usuario.getNombre() == null || usuario.getNombre().trim().isEmpty()) {
            usuario.setNombre("Usuario");
        }
        if (usuario.getApellido() == null) {
            usuario.setApellido("");
        }
        if (usuario.getRol() == null || usuario.getRol().trim().isEmpty()) {
            usuario.setRol("USUARIO");
        }
        if (usuario.getEstado() == null || usuario.getEstado().trim().isEmpty()) {
            usuario.setEstado("ACTIVO");
        }

        String rawPassword = (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty())
                ? usuario.getPassword().trim()
                : "Admin123!";
        usuario.setPasswordHash(passwordEncoder.encode(rawPassword));

        if (empresaId != null) {
            empresaRepository.findById(empresaId).ifPresent(usuario::setEmpresa);
        }

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> update(Integer id, Usuario data, Integer empresaId, boolean esAdmin) {
        return usuarioRepository.findById(id).map(usr -> {
            if (data.getNombre() != null) usr.setNombre(data.getNombre());
            if (data.getApellido() != null) usr.setApellido(data.getApellido());
            if (data.getEmail() != null) usr.setEmail(data.getEmail());
            if (data.getTelefono() != null) usr.setTelefono(data.getTelefono());
            
            // Solo un Administrador puede alterar roles o estados del sistema
            if (esAdmin) {
                if (data.getRol() != null && !data.getRol().trim().isEmpty()) {
                    // Protección: El Administrador raíz (ID 1) no puede ser degradado
                    if (id == 1 && !"ADMIN".equalsIgnoreCase(data.getRol())) {
                        usr.setRol("ADMIN");
                    } else {
                        usr.setRol(data.getRol().trim().toUpperCase());
                    }
                }
                if (data.getEstado() != null && !data.getEstado().trim().isEmpty()) {
                    usr.setEstado(data.getEstado().trim().toUpperCase());
                }
            }

            if (data.getAvatarUrl() != null) usr.setAvatarUrl(data.getAvatarUrl());

            if (data.getPassword() != null && !data.getPassword().trim().isEmpty()) {
                usr.setPasswordHash(passwordEncoder.encode(data.getPassword().trim()));
            }

            if (empresaId != null) {
                empresaRepository.findById(empresaId).ifPresent(usr::setEmpresa);
            }

            return usuarioRepository.save(usr);
        });
    }

    public Optional<Usuario> update(Integer id, Usuario data, Integer empresaId) {
        return update(id, data, empresaId, false);
    }

    public void updatePasswordHash(Integer id, String newPasswordHash) {
        usuarioRepository.findById(id).ifPresent(usr -> {
            usr.setPasswordHash(newPasswordHash);
            usuarioRepository.save(usr);
        });
    }

    public boolean delete(Integer id) {
        // Protección crítica: El Administrador principal (ID 1) nunca puede ser eliminado
        if (id != null && id == 1) {
            return false;
        }
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
