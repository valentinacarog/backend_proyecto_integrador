package com.cesde.proyectointegrador.modules.empresas.controller;

import com.cesde.proyectointegrador.modules.empresas.entity.Empresa;
import com.cesde.proyectointegrador.modules.empresas.service.EmpresaService;
import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import com.cesde.proyectointegrador.modules.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<Empresa>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sector,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(empresaService.getAll(search, sector, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return empresaService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Empresa) null));
    }

    @GetMapping("/{id}/usuarios")
    public ResponseEntity<List<Usuario>> getUsuariosDeEmpresa(@PathVariable Integer id) {
        return ResponseEntity.ok(usuarioRepository.findByEmpresaId(id));
    }

    private String extractPassword(Map<String, Object> payload) {
        if (payload == null) return null;
        String[] keys = {"password", "newPassword", "new_password", "contrasena", "nuevaContrasena", "nueva_contrasena", "clave"};
        for (String k : keys) {
            Object val = payload.get(k);
            if (val != null && !val.toString().trim().isEmpty()) {
                return val.toString().trim();
            }
        }
        return null;
    }

    private void syncCompanyUserPassword(Empresa empresa, String rawPassword) {
        if (empresa == null || empresa.getId() == null || rawPassword == null || rawPassword.trim().isEmpty()) {
            return;
        }
        List<Usuario> usuarios = usuarioRepository.findByEmpresaId(empresa.getId());
        if (!usuarios.isEmpty()) {
            for (Usuario u : usuarios) {
                u.setPasswordHash(passwordEncoder.encode(rawPassword.trim()));
                usuarioRepository.save(u);
            }
        } else {
            Usuario nuevo = new Usuario();
            nuevo.setNombre(empresa.getNombre());
            nuevo.setApellido("(Empresa)");
            nuevo.setEmail(empresa.getEmail() != null ? empresa.getEmail() : "empresa" + empresa.getId() + "@cesde.edu.co");
            nuevo.setPasswordHash(passwordEncoder.encode(rawPassword.trim()));
            nuevo.setRol("EMPRESA");
            nuevo.setEstado("ACTIVO");
            nuevo.setEmpresa(empresa);
            usuarioRepository.save(nuevo);
        }
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> payload) {
        try {
            Empresa empresa = new Empresa();
            if (payload.get("nombre") != null) empresa.setNombre(payload.get("nombre").toString());
            if (payload.get("nit") != null) empresa.setNit(payload.get("nit").toString());
            if (payload.get("sector") != null) empresa.setSector(payload.get("sector").toString());
            if (payload.get("email") != null) empresa.setEmail(payload.get("email").toString());
            if (payload.get("telefono") != null) empresa.setTelefono(payload.get("telefono").toString());
            if (payload.get("direccion") != null) empresa.setDireccion(payload.get("direccion").toString());
            if (payload.get("ciudad") != null) empresa.setCiudad(payload.get("ciudad").toString());
            if (payload.get("estado") != null) empresa.setEstado(payload.get("estado").toString());

            Empresa created = empresaService.create(empresa);

            String pwd = extractPassword(payload);
            if (pwd != null) {
                syncCompanyUserPassword(created, pwd);
            }

            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al registrar la empresa: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        try {
            Empresa empresa = new Empresa();
            if (payload.containsKey("nombre") && payload.get("nombre") != null) empresa.setNombre(payload.get("nombre").toString());
            if (payload.containsKey("nit") && payload.get("nit") != null) empresa.setNit(payload.get("nit").toString());
            if (payload.containsKey("sector") && payload.get("sector") != null) empresa.setSector(payload.get("sector").toString());
            if (payload.containsKey("email") && payload.get("email") != null) empresa.setEmail(payload.get("email").toString());
            if (payload.containsKey("telefono") && payload.get("telefono") != null) empresa.setTelefono(payload.get("telefono").toString());
            if (payload.containsKey("direccion") && payload.get("direccion") != null) empresa.setDireccion(payload.get("direccion").toString());
            if (payload.containsKey("ciudad") && payload.get("ciudad") != null) empresa.setCiudad(payload.get("ciudad").toString());
            if (payload.containsKey("estado") && payload.get("estado") != null) empresa.setEstado(payload.get("estado").toString());

            Optional<Empresa> updatedOpt = empresaService.update(id, empresa);
            if (updatedOpt.isPresent()) {
                String pwd = extractPassword(payload);
                if (pwd != null) {
                    syncCompanyUserPassword(updatedOpt.get(), pwd);
                }
                return ResponseEntity.ok(updatedOpt.get());
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body((Empresa) null);
            }
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al actualizar la empresa: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @RequestMapping(value = {"/{id}/password", "/{id}/contrasena", "/{id}/cambiar-password"}, method = {RequestMethod.PUT, RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<?> updatePassword(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        String pwd = extractPassword(payload);
        if (pwd == null || pwd.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "La contraseña es requerida");
            return ResponseEntity.badRequest().body(err);
        }

        Optional<Empresa> empresaOpt = empresaService.getById(id);
        if (empresaOpt.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Empresa aliada no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        syncCompanyUserPassword(empresaOpt.get(), pwd);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contraseña de acceso para " + empresaOpt.get().getNombre() + " actualizada exitosamente");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = empresaService.delete(id);
        if (deleted) {
            response.put("success", true);
            response.put("message", "Empresa eliminada exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "No se pudo eliminar la empresa");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
