package com.cesde.proyectointegrador.modules.retos.controller;

import com.cesde.proyectointegrador.modules.retos.entity.Reto;
import com.cesde.proyectointegrador.modules.retos.service.RetoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/retos")
public class RetoController {

    @Autowired
    private RetoService retoService;

    @GetMapping
    public ResponseEntity<List<Reto>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dificultad,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer empresaId,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false, defaultValue = "false") Boolean incluirVencidos) {
        return ResponseEntity.ok(retoService.getAll(search, dificultad, estado, empresaId, categoriaId, incluirVencidos));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return retoService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Reto) null));
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

    private LocalDate parseDate(Object val) {
        if (val == null) return null;
        try {
            String str = val.toString().trim();
            if (str.length() >= 10) {
                return LocalDate.parse(str.substring(0, 10));
            }
        } catch (Exception ignored) {}
        return null;
    }

    @PostMapping
    public ResponseEntity<Reto> create(@RequestBody Map<String, Object> payload) {
        Reto reto = new Reto();
        reto.setTitulo((String) payload.get("titulo"));
        reto.setDescripcion((String) payload.get("descripcion"));
        reto.setDificultad(payload.get("dificultad") != null ? (String) payload.get("dificultad") : "MEDIA");
        reto.setEstado(payload.get("estado") != null ? (String) payload.get("estado") : "ABIERTO");

        if (payload.get("puntos") != null) {
            try {
                reto.setPuntos(Integer.valueOf(payload.get("puntos").toString()));
            } catch (Exception ignored) {
                reto.setPuntos(100);
            }
        }

        LocalDate fecha = parseDate(payload.get("fechaLimite"));
        if (fecha == null) fecha = parseDate(payload.get("fecha_limite"));
        if (fecha != null) reto.setFechaLimite(fecha);

        Integer empresaId = extractId(payload.get("empresaId"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa_id"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa"));

        Integer categoriaId = extractId(payload.get("categoriaId"));
        if (categoriaId == null) categoriaId = extractId(payload.get("categoria_id"));
        if (categoriaId == null) categoriaId = extractId(payload.get("categoria"));

        Reto created = retoService.create(reto, empresaId, categoriaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        Reto reto = new Reto();
        if (payload.containsKey("titulo")) reto.setTitulo((String) payload.get("titulo"));
        if (payload.containsKey("descripcion")) reto.setDescripcion((String) payload.get("descripcion"));
        if (payload.containsKey("dificultad")) reto.setDificultad((String) payload.get("dificultad"));
        if (payload.containsKey("estado")) reto.setEstado((String) payload.get("estado"));

        if (payload.containsKey("puntos") && payload.get("puntos") != null) {
            try {
                reto.setPuntos(Integer.valueOf(payload.get("puntos").toString()));
            } catch (Exception ignored) {}
        }

        LocalDate fecha = parseDate(payload.get("fechaLimite"));
        if (fecha == null) fecha = parseDate(payload.get("fecha_limite"));
        if (fecha != null) reto.setFechaLimite(fecha);

        Integer empresaId = extractId(payload.get("empresaId"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa_id"));
        if (empresaId == null) empresaId = extractId(payload.get("empresa"));

        Integer categoriaId = extractId(payload.get("categoriaId"));
        if (categoriaId == null) categoriaId = extractId(payload.get("categoria_id"));
        if (categoriaId == null) categoriaId = extractId(payload.get("categoria"));

        return retoService.update(id, reto, empresaId, categoriaId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = retoService.delete(id);
        if (deleted) {
            response.put("success", true);
            response.put("message", "Reto eliminado exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "Reto no encontrado");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
