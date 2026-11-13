package com.cesde.proyectointegrador.config;

import com.cesde.proyectointegrador.modules.empresas.entity.Empresa;
import com.cesde.proyectointegrador.modules.empresas.repository.EmpresaRepository;
import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import com.cesde.proyectointegrador.modules.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        try {
            // Garantizar que toda empresa aliada tenga una cuenta de usuario con rol EMPRESA
            List<Empresa> empresas = empresaRepository.findAll();
            for (Empresa emp : empresas) {
                List<Usuario> usuarios = usuarioRepository.findByEmpresaId(emp.getId());
                if (usuarios.isEmpty()) {
                    String emailContacto = (emp.getEmail() != null && !emp.getEmail().trim().isEmpty()) 
                            ? emp.getEmail().trim().toLowerCase() 
                            : "empresa" + emp.getId() + "@cesde.edu.co";

                    if (usuarioRepository.findByEmailIgnoreCase(emailContacto).isEmpty()) {
                        Usuario user = new Usuario();
                        user.setNombre(emp.getNombre());
                        user.setApellido("(Representante)");
                        user.setEmail(emailContacto);
                        user.setPasswordHash(passwordEncoder.encode("admin123"));
                        user.setTelefono(emp.getTelefono());
                        user.setRol("EMPRESA");
                        user.setEstado("ACTIVO");
                        user.setEmpresa(emp);
                        usuarioRepository.save(user);
                        System.out.println("✅ Cuenta de usuario creada para la empresa: " + emp.getNombre() + " (" + emailContacto + ")");
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Aviso: Sincronización inicial de empresas: " + e.getMessage());
        }
    }
}

