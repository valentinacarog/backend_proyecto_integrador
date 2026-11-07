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
            @RequestParam(required = false) Integer categoriaId) {
        return ResponseEntity.ok(retoService.getAll(search, dificultad, estado, empresaId, categoriaId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return retoService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Reto) null));
    }

    @PostMapping
    public ResponseEntity<Reto> create(@RequestBody Map<String, Object> payload) {
        Reto reto = new Reto();
        reto.setTitulo((String) payload.get("titulo"));
        reto.setDescripcion((String) payload.get("descripcion"));
        reto.setDificultad(payload.get("dificultad") != null ? (String) payload.get("dificultad") : "MEDIA");
        reto.setEstado(payload.get("estado") != null ? (String) payload.get("estado") : "ABIERTO");

        if (payload.get("puntos") != null) {
            reto.setPuntos(Integer.valueOf(payload.get("puntos").toString()));
        }

        if (payload.get("fechaLimite") != null) {
            reto.setFechaLimite(LocalDate.parse(payload.get("fechaLimite").toString().substring(0, 10)));
        } else if (payload.get("fecha_limite") != null) {
            reto.setFechaLimite(LocalDate.parse(payload.get("fecha_limite").toString().substring(0, 10)));
        }

        Integer empresaId = null;
        if (payload.get("empresaId") != null) {
            empresaId = Integer.valueOf(payload.get("empresaId").toString());
        } else if (payload.get("empresa_id") != null) {
            empresaId = Integer.valueOf(payload.get("empresa_id").toString());
        }

        Integer categoriaId = null;
        if (payload.get("categoriaId") != null) {
            categoriaId = Integer.valueOf(payload.get("categoriaId").toString());
        } else if (payload.get("categoria_id") != null) {
            categoriaId = Integer.valueOf(payload.get("categoria_id").toString());
        }

        Reto created = retoService.create(reto, empresaId, categoriaId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        Reto reto = new Reto();
        if (payload.containsKey("titulo")) reto.setTitulo((String) payload.get("titulo"));
        if (payload.containsKey("descripcion")) reto.setDescripcion((String) payload.get("descripcion"));
        if (payload.containsKey("dificultad")) reto.setDificultad((String) payload.get("dificultad"));
        if (payload.containsKey("estado")) reto.setEstado((String) payload.get("estado"));

        if (payload.containsKey("puntos") && payload.get("puntos") != null) {
            reto.setPuntos(Integer.valueOf(payload.get("puntos").toString()));
        }

        if (payload.containsKey("fechaLimite") && payload.get("fechaLimite") != null) {
            reto.setFechaLimite(LocalDate.parse(payload.get("fechaLimite").toString().substring(0, 10)));
        } else if (payload.containsKey("fecha_limite") && payload.get("fecha_limite") != null) {
            reto.setFechaLimite(LocalDate.parse(payload.get("fecha_limite").toString().substring(0, 10)));
        }

        Integer empresaId = null;
        if (payload.containsKey("empresaId") && payload.get("empresaId") != null) {
            empresaId = Integer.valueOf(payload.get("empresaId").toString());
        } else if (payload.containsKey("empresa_id") && payload.get("empresa_id") != null) {
            empresaId = Integer.valueOf(payload.get("empresa_id").toString());
        }

        Integer categoriaId = null;
        if (payload.containsKey("categoriaId") && payload.get("categoriaId") != null) {
            categoriaId = Integer.valueOf(payload.get("categoriaId").toString());
        } else if (payload.containsKey("categoria_id") && payload.get("categoria_id") != null) {
            categoriaId = Integer.valueOf(payload.get("categoria_id").toString());
        }

        return retoService.update(id, reto, empresaId, categoriaId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Reto) null));
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
