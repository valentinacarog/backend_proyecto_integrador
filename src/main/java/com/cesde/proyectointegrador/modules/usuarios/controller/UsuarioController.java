package com.cesde.proyectointegrador.modules.usuarios.controller;

import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import com.cesde.proyectointegrador.modules.usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @GetMapping
    public ResponseEntity<List<Usuario>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String rol,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(usuarioService.getAll(search, rol, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return usuarioService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Usuario) null));
    }

    private Integer extractId(Object val) {
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).intValue();
        if (val instanceof Map) {
            Object idVal = ((Map<?, ?>) val).get("id");
            if (idVal instanceof Number) return ((Number) idVal).intValue();
            if (idVal != null) {
                try { return Integer.parseInt(idVal.toString()); } catch (Exception ignored) {}
            }
        }
        try {
            return Integer.parseInt(val.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private String extractPassword(Map<String, Object> payload) {
        if (payload == null) return null;
        String[] keys = {"password", "newPassword", "new_password", "contrasena", "nuevaContrasena", "nueva_contrasena", "clave"};
        for (String key : keys) {
            Object val = payload.get(key);
            if (val != null && !val.toString().trim().isEmpty()) {
                return val.toString().trim();
            }
        }
        return null;
    }

    @PostMapping
    public ResponseEntity<Usuario> create(@RequestBody Map<String, Object> payload) {
        Usuario usuario = new Usuario();
        usuario.setNombre((String) payload.get("nombre"));
        usuario.setApellido((String) payload.get("apellido"));
        usuario.setEmail((String) payload.get("email"));
        usuario.setPassword(extractPassword(payload));
        usuario.setTelefono((String) payload.get("telefono"));
        usuario.setRol(payload.get("rol") != null ? (String) payload.get("rol") : "USUARIO");
        usuario.setEstado(payload.get("estado") != null ? (String) payload.get("estado") : "ACTIVO");

        Integer empresaId = extractId(payload.get("empresaId"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa_id"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa"));

        Usuario created = usuarioService.create(usuario, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> payload, Authentication authentication) {
        Usuario usuario = new Usuario();
        if (payload.containsKey("nombre")) usuario.setNombre((String) payload.get("nombre"));
        if (payload.containsKey("apellido")) usuario.setApellido((String) payload.get("apellido"));
        if (payload.containsKey("email")) usuario.setEmail((String) payload.get("email"));

        String newPwd = extractPassword(payload);
        if (newPwd != null) {
            usuario.setPassword(newPwd);
        }

        if (payload.containsKey("telefono")) usuario.setTelefono((String) payload.get("telefono"));
        if (payload.containsKey("rol")) usuario.setRol((String) payload.get("rol"));
        if (payload.containsKey("estado")) usuario.setEstado((String) payload.get("estado"));

        Integer empresaId = extractId(payload.get("empresaId"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa_id"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa"));

        boolean esAdmin = authentication != null && (
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))
                || "admin@sistema.com".equalsIgnoreCase(authentication.getName())
        );

        return usuarioService.update(id, usuario, empresaId, esAdmin)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Usuario) null));
    }

    @RequestMapping(value = {"/{id}/password", "/{id}/contrasena", "/{id}/cambiar-password", "/{id}/reset-password"}, method = {RequestMethod.PUT, RequestMethod.POST, RequestMethod.PATCH})
    public ResponseEntity<?> changePassword(@PathVariable Integer id, @RequestBody Map<String, Object> payload, Authentication authentication) {
        String newPassword = extractPassword(payload);
        if (newPassword == null || newPassword.trim().isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "La nueva contraseña es requerida");
            return ResponseEntity.badRequest().body(err);
        }

        boolean esAdmin = authentication != null && (
                authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))
                || "admin@sistema.com".equalsIgnoreCase(authentication.getName())
        );

        Optional<Usuario> targetUserOpt = usuarioService.getById(id);
        if (targetUserOpt.isEmpty()) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
        }

        Usuario targetUser = targetUserOpt.get();

        boolean esPropioUsuario = authentication != null && targetUser.getEmail().equalsIgnoreCase(authentication.getName());
        if (!esAdmin && !esPropioUsuario) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "No tienes permisos para modificar la contraseña de este usuario");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }

        usuarioService.updatePassword(id, newPassword.trim());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "Contraseña actualizada exitosamente");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();

        if (id != null && id == 1) {
            response.put("success", false);
            response.put("message", "Acción denegada: No se permite eliminar la cuenta del Administrador principal");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        boolean deleted = usuarioService.delete(id);
        if (deleted) {
            response.put("success", true);
            response.put("message", "Usuario eliminado exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Usuario no encontrado o no se pudo eliminar");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
