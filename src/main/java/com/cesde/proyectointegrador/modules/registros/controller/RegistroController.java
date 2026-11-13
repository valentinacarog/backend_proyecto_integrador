package com.cesde.proyectointegrador.modules.registros.controller;

import com.cesde.proyectointegrador.modules.registros.entity.Registro;
import com.cesde.proyectointegrador.modules.registros.service.RegistroService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/registros")
public class RegistroController {

    @Autowired
    private RegistroService registroService;

    @GetMapping
    public ResponseEntity<List<Registro>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) Integer retoId,
            @RequestParam(required = false) Integer empresaId) {
        return ResponseEntity.ok(registroService.getAll(search, tipo, estado, usuarioId, retoId, empresaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return registroService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Registro) null));
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
    public ResponseEntity<Registro> create(@RequestBody Map<String, Object> payload, Authentication authentication) {
        Registro registro = new Registro();
        registro.setCodigo((String) payload.get("codigo"));
        registro.setTipo(payload.get("tipo") != null ? (String) payload.get("tipo") : "INSCRIPCION");
        registro.setDescripcion((String) payload.get("descripcion"));
        registro.setEstado(payload.get("estado") != null ? (String) payload.get("estado") : "PENDIENTE");
        registro.setObservaciones((String) payload.get("observaciones"));

        Integer usuarioId = extractId(payload.get("usuarioId"));
        if (usuarioId == null) usuarioId = extractId(payload.get("usuario_id"));
        if (usuarioId == null) usuarioId = extractId(payload.get("usuario"));
        if (usuarioId == null) usuarioId = extractId(payload.get("user"));

        Integer empresaId = extractId(payload.get("empresaId"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa_id"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa"));

        Integer retoId = extractId(payload.get("retoId"));
        if (retoId == null) retoId = extractId(payload.get("reto_id"));
        if (retoId == null) retoId = extractId(payload.get("reto"));

        String userEmail = (authentication != null) ? authentication.getName() : null;
        Registro created = registroService.create(registro, usuarioId, empresaId, retoId, userEmail);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        Registro registro = new Registro();
        if (payload.containsKey("codigo")) registro.setCodigo((String) payload.get("codigo"));
        if (payload.containsKey("tipo")) registro.setTipo((String) payload.get("tipo"));
        if (payload.containsKey("descripcion")) registro.setDescripcion((String) payload.get("descripcion"));
        if (payload.containsKey("estado")) registro.setEstado((String) payload.get("estado"));
        if (payload.containsKey("observaciones")) registro.setObservaciones((String) payload.get("observaciones"));

        Integer usuarioId = extractId(payload.get("usuarioId"));
        if (usuarioId == null) usuarioId = extractId(payload.get("usuario_id"));
        if (usuarioId == null) usuarioId = extractId(payload.get("usuario"));
        if (usuarioId == null) usuarioId = extractId(payload.get("user"));

        Integer empresaId = extractId(payload.get("empresaId"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa_id"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa"));

        Integer retoId = extractId(payload.get("retoId"));
        if (retoId == null) retoId = extractId(payload.get("reto_id"));
        if (retoId == null) retoId = extractId(payload.get("reto"));

        return registroService.update(id, registro, usuarioId, empresaId, retoId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Registro) null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = registroService.delete(id);
        if (deleted) {
            response.put("success", true);
            response.put("message", "Registro eliminado exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Registro no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
