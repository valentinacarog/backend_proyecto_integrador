package com.cesde.proyectointegrador.modules.empresas.controller;

import com.cesde.proyectointegrador.modules.empresas.entity.Empresa;
import com.cesde.proyectointegrador.modules.empresas.service.EmpresaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/empresas")
public class EmpresaController {

    @Autowired
    private EmpresaService empresaService;

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

            return empresaService.update(id, empresa)
                    .map(ResponseEntity::ok)
                    .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                            .body((Empresa) null));
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al actualizar la empresa: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
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
