package com.cesde.proyectointegrador.modules.categorias.controller;

import com.cesde.proyectointegrador.modules.categorias.entity.Categoria;
import com.cesde.proyectointegrador.modules.categorias.service.CategoriaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @GetMapping
    public ResponseEntity<List<Categoria>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(categoriaService.getAll(search, estado));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id) {
        return categoriaService.getById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Categoria) null));
    }

    @PostMapping
    public ResponseEntity<Categoria> create(@RequestBody Map<String, Object> payload) {
        Categoria categoria = new Categoria();
        if (payload.get("nombre") != null) categoria.setNombre(payload.get("nombre").toString());
        if (payload.get("codigo") != null) categoria.setCodigo(payload.get("codigo").toString());
        if (payload.get("descripcion") != null) categoria.setDescripcion(payload.get("descripcion").toString());
        if (payload.get("color") != null) categoria.setColor(payload.get("color").toString());
        if (payload.get("estado") != null) categoria.setEstado(payload.get("estado").toString());

        Categoria created = categoriaService.create(categoria);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> payload) {
        Categoria categoria = new Categoria();
        if (payload.containsKey("nombre") && payload.get("nombre") != null) categoria.setNombre(payload.get("nombre").toString());
        if (payload.containsKey("codigo") && payload.get("codigo") != null) categoria.setCodigo(payload.get("codigo").toString());
        if (payload.containsKey("descripcion") && payload.get("descripcion") != null) categoria.setDescripcion(payload.get("descripcion").toString());
        if (payload.containsKey("color") && payload.get("color") != null) categoria.setColor(payload.get("color").toString());
        if (payload.containsKey("estado") && payload.get("estado") != null) categoria.setEstado(payload.get("estado").toString());

        return categoriaService.update(id, categoria)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body((Categoria) null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id) {
        Map<String, Object> response = new HashMap<>();
        boolean deleted = categoriaService.delete(id);
        if (deleted) {
            response.put("success", true);
            response.put("message", "Categoría eliminada exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "No se pudo eliminar la categoría");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
}
