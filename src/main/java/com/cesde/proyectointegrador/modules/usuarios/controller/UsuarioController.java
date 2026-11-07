package com.cesde.proyectointegrador.modules.usuarios.controller;

import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import com.cesde.proyectointegrador.modules.usuarios.service.UsuarioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @PostMapping
    public ResponseEntity<Usuario> create(@RequestBody Map<String, Object> payload) {
        Usuario usuario = new Usuario();
        usuario.setNombre((String) payload.get("nombre"));
        usuario.setApellido((String) payload.get("apellido"));
        usuario.setEmail((String) payload.get("email"));
        usuario.setPassword((String) payload.get("password"));
        usuario.setTelefono((String) payload.get("telefono"));
        usuario.setRol(payload.get("rol") != null ? (String) payload.get("rol") : "USUARIO");
        usuario.setEstado(payload.get("estado") != null ? (String) payload.get("estado") : "ACTIVO");

        Integer empresaId = extractId(payload.get("empresaId"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa_id"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa"));

        Usuario created = usuarioService.create(usuario, empresaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        Usuario usuario = new Usuario();
        if (payload.containsKey("nombre")) usuario.setNombre((String) payload.get("nombre"));
        if (payload.containsKey("apellido")) usuario.setApellido((String) payload.get("apellido"));
        if (payload.containsKey("email")) usuario.setEmail((String) payload.get("email"));
        if (payload.containsKey("password")) usuario.setPassword((String) payload.get("password"));
        if (payload.containsKey("telefono")) usuario.setTelefono((String) payload.get("telefono"));
        if (payload.containsKey("rol")) usuario.setRol((String) payload.get("rol"));
        if (payload.containsKey("estado")) usuario.setEstado((String) payload.get("estado"));

        Integer empresaId = extractId(payload.get("empresaId"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa_id"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa"));

        return usuarioService.update(id, usuario, empresaId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Usuario) null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = usuarioService.delete(id);
        if (deleted) {
            response.put("success", true);
            response.put("message", "Usuario eliminado exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Usuario no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
