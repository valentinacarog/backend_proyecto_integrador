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
        String rawPassword = (usuario.getPassword() != null && !usuario.getPassword().trim().isEmpty())
                ? usuario.getPassword().trim()
                : "Admin123!";
        usuario.setPasswordHash(passwordEncoder.encode(rawPassword));

        if (empresaId != null) {
            empresaRepository.findById(empresaId).ifPresent(usuario::setEmpresa);
        }

        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> update(Integer id, Usuario data, Integer empresaId) {
        return usuarioRepository.findById(id).map(usr -> {
            if (data.getNombre() != null) usr.setNombre(data.getNombre());
            if (data.getApellido() != null) usr.setApellido(data.getApellido());
            if (data.getEmail() != null) usr.setEmail(data.getEmail());
            if (data.getTelefono() != null) usr.setTelefono(data.getTelefono());
            if (data.getRol() != null) usr.setRol(data.getRol());
            if (data.getEstado() != null) usr.setEstado(data.getEstado());
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

    public boolean delete(Integer id) {
        if (usuarioRepository.existsById(id)) {
            usuarioRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
