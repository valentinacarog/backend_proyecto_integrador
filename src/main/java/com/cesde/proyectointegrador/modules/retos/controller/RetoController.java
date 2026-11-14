package com.cesde.proyectointegrador.modules.retos.controller;

import com.cesde.proyectointegrador.modules.registros.entity.Registro;
import com.cesde.proyectointegrador.modules.registros.repository.RegistroRepository;
import com.cesde.proyectointegrador.modules.registros.service.RegistroService;
import com.cesde.proyectointegrador.modules.retos.entity.Reto;
import com.cesde.proyectointegrador.modules.retos.service.RetoService;
import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import com.cesde.proyectointegrador.modules.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/retos")
public class RetoController {

    @Autowired
    private RetoService retoService;

    @Autowired
    private RegistroRepository registroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RegistroService registroService;

    private Map<String, Object> buildRetoMap(Reto r, Authentication authentication) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", r.getId());
        map.put("titulo", r.getTitulo());
        map.put("descripcion", r.getDescripcion());
        map.put("dificultad", r.getDificultad());
        map.put("puntos", r.getPuntos());
        map.put("fechaLimite", r.getFechaLimite());
        map.put("estado", r.getEstado());
        map.put("empresa", r.getEmpresa());
        map.put("categoria", r.getCategoria());
        map.put("createdAt", r.getCreatedAt());
        map.put("updatedAt", r.getUpdatedAt());

        if (authentication != null && authentication.getName() != null) {
            String email = authentication.getName();
            boolean esAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))
                    || "admin@sistema.com".equalsIgnoreCase(email);
            Optional<Usuario> userOpt = usuarioRepository.findByEmailIgnoreCase(email);

            if (esAdmin) {
                List<Registro> entregas = registroRepository.findByFilters(null, null, null, null, r.getId(), null);
                map.put("postulaciones", entregas);
                map.put("entregas", entregas);
                map.put("totalPostulaciones", entregas.size());
            } else if (userOpt.isPresent()) {
                Usuario u = userOpt.get();
                if ("USUARIO".equalsIgnoreCase(u.getRol())) {
                    Optional<Registro> regOpt = registroRepository.findByUsuarioIdAndRetoId(u.getId(), r.getId());
                    if (regOpt.isPresent()) {
                        Registro reg = regOpt.get();
                        map.put("inscrito", true);
                        map.put("miRegistro", reg);
                        map.put("registroId", reg.getId());
                        map.put("estadoInscripcion", reg.getEstado());
                        map.put("tipoInscripcion", reg.getTipo());
                        map.put("datosEntrega", reg.getDatosEntrega());
                        map.put("repositorioUrl", reg.getRepositorioUrl());
                        map.put("demoUrl", reg.getDemoUrl());
                        map.put("archivoUrl", reg.getArchivoUrl());
                        map.put("descripcionSolucion", reg.getDescripcionSolucion());
                        map.put("retroalimentacion", reg.getRetroalimentacion());
                        map.put("observaciones", reg.getObservaciones());
                    } else {
                        map.put("inscrito", false);
                    }
                } else if ("EMPRESA".equalsIgnoreCase(u.getRol())) {
                    if (u.getEmpresa() != null && r.getEmpresa() != null && u.getEmpresa().getId().equals(r.getEmpresa().getId())) {
                        List<Registro> entregas = registroRepository.findByFilters(null, null, null, null, r.getId(), null);
                        map.put("postulaciones", entregas);
                        map.put("entregas", entregas);
                        map.put("totalPostulaciones", entregas.size());
                    }
                }
            }
        }
        return map;
    }

    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String dificultad,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer empresaId,
            @RequestParam(required = false) Integer categoriaId,
            @RequestParam(required = false, defaultValue = "false") Boolean incluirVencidos,
            Authentication authentication) {
        List<Reto> retos = retoService.getAll(search, dificultad, estado, empresaId, categoriaId, incluirVencidos);
        List<Map<String, Object>> mapped = retos.stream()
                .map(r -> buildRetoMap(r, authentication))
                .toList();
        return ResponseEntity.ok(mapped);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id, Authentication authentication) {
        return retoService.getById(id)
                .map(r -> ResponseEntity.ok(buildRetoMap(r, authentication)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
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

    private String extractString(Map<String, Object> payload, String... keys) {
        if (payload == null) return null;
        for (String k : keys) {
            Object v = payload.get(k);
            if (v != null && !v.toString().trim().isEmpty()) {
                return v.toString().trim();
            }
        }
        return null;
    }

    @PostMapping(value = {"/{id}/inscribir", "/{id}/inscribirse", "/{id}/aceptar"})
    public ResponseEntity<?> inscribirEnReto(@PathVariable Integer id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            Registro reg = new Registro();
            reg.setTipo("INSCRIPCION");
            reg.setEstado("PENDIENTE");
            Registro created = registroService.create(reg, null, null, id, authentication.getName());
            return ResponseEntity.ok(created);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al inscribirse en el reto: " + e.getMessage()));
        }
    }

    @PostMapping(value = {"/{id}/entregar", "/{id}/subir", "/{id}/solucion"})
    public ResponseEntity<?> entregarEnReto(@PathVariable Integer id, @RequestBody Map<String, Object> payload, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String userEmail = authentication.getName();
        Optional<Usuario> userOpt = usuarioRepository.findByEmailIgnoreCase(userEmail);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        try {
            Registro reg = new Registro();
            reg.setTipo("ENTREGA");
            reg.setEstado("COMPLETADO");

            String repoUrl = extractString(payload, "repositorioUrl", "repoUrl", "repositorio", "repo", "github", "githubUrl");
            String demoUrl = extractString(payload, "demoUrl", "demo", "despliegue", "despliegueUrl", "urlDemo");
            String archivoUrl = extractString(payload, "archivoUrl", "archivo", "fileUrl", "file", "documentoUrl");
            String notas = extractString(payload, "descripcionSolucion", "solucion", "notas", "comentarios", "descripcion");

            StringBuilder obs = new StringBuilder();
            if (payload.get("observaciones") != null && !payload.get("observaciones").toString().trim().isEmpty()) {
                obs.append(payload.get("observaciones").toString().trim());
            }
            if (obs.length() > 0) obs.append("\n\n");
            obs.append("--- [ENTREGA DE SOLUCIÓN] ---");
            if (repoUrl != null) obs.append("\n• Repositorio: ").append(repoUrl);
            if (demoUrl != null) obs.append("\n• Demo/Despliegue: ").append(demoUrl);
            if (archivoUrl != null) obs.append("\n• Archivo: ").append(archivoUrl);
            if (notas != null) obs.append("\n• Descripción: ").append(notas);

            reg.setObservaciones(obs.toString());
            Registro saved = registroService.create(reg, userOpt.get().getId(), null, id, userEmail);

            Map<String, Object> res = new HashMap<>();
            res.put("success", true);
            res.put("message", "¡Solución entregada con éxito! La empresa aliada evaluará tu proyecto.");
            res.put("registro", saved);
            res.put("datosEntrega", saved.getDatosEntrega());
            res.put("repositorioUrl", saved.getRepositorioUrl());
            res.put("demoUrl", saved.getDemoUrl());
            res.put("archivoUrl", saved.getArchivoUrl());
            res.put("descripcionSolucion", saved.getDescripcionSolucion());
            return ResponseEntity.ok(res);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("success", false, "message", "Error al registrar la entrega: " + e.getMessage()));
        }
    }

    @GetMapping(value = {"/{id}/registros", "/{id}/entregas", "/{id}/postulaciones"})
    public ResponseEntity<?> getRegistrosDeReto(@PathVariable Integer id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String email = authentication.getName();
        boolean esAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))
                || "admin@sistema.com".equalsIgnoreCase(email);
        Optional<Usuario> userOpt = usuarioRepository.findByEmailIgnoreCase(email);

        if (esAdmin) {
            return ResponseEntity.ok(registroRepository.findByFilters(null, null, null, null, id, null));
        }
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        Usuario u = userOpt.get();
        if ("USUARIO".equalsIgnoreCase(u.getRol())) {
            Optional<Registro> reg = registroRepository.findByUsuarioIdAndRetoId(u.getId(), id);
            return ResponseEntity.ok(reg.map(List::of).orElse(Collections.emptyList()));
        }
        if ("EMPRESA".equalsIgnoreCase(u.getRol())) {
            Optional<Reto> retoOpt = retoService.getById(id);
            if (retoOpt.isPresent() && u.getEmpresa() != null && retoOpt.get().getEmpresa() != null
                    && u.getEmpresa().getId().equals(retoOpt.get().getEmpresa().getId())) {
                return ResponseEntity.ok(registroRepository.findByFilters(null, null, null, null, id, null));
            }
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "No tienes permisos para ver entregas de retos de otra empresa."));
        }
        return ResponseEntity.ok(Collections.emptyList());
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
