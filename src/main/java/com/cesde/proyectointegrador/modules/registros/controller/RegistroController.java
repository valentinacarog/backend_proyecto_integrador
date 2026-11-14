package com.cesde.proyectointegrador.modules.registros.controller;

import com.cesde.proyectointegrador.modules.registros.entity.Registro;
import com.cesde.proyectointegrador.modules.registros.service.RegistroService;
import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import com.cesde.proyectointegrador.modules.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/registros")
public class RegistroController {

    @Autowired
    private RegistroService registroService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping
    public ResponseEntity<List<Registro>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) Integer usuarioId,
            @RequestParam(required = false) Integer retoId,
            @RequestParam(required = false) Integer empresaId,
            Authentication authentication) {

        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        boolean esAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))
                || "admin@sistema.com".equalsIgnoreCase(email);

        Integer filterUsuarioId = usuarioId;
        Integer filterEmpresaId = empresaId;

        if (!esAdmin) {
            Optional<Usuario> userOpt = usuarioRepository.findByEmailIgnoreCase(email);
            if (userOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            Usuario u = userOpt.get();
            if ("EMPRESA".equalsIgnoreCase(u.getRol())) {
                // Empresa solo ve los registros de sus propios retos. Nunca ve los de otras empresas.
                if (u.getEmpresa() != null) {
                    filterEmpresaId = u.getEmpresa().getId();
                } else {
                    return ResponseEntity.ok(Collections.emptyList());
                }
            } else if ("USUARIO".equalsIgnoreCase(u.getRol())) {
                // Estudiante solo ve sus propias inscripciones/publicaciones. Nunca ve las de otros estudiantes.
                filterUsuarioId = u.getId();
            } else {
                return ResponseEntity.ok(Collections.emptyList());
            }
        }

        return ResponseEntity.ok(registroService.getAll(search, tipo, estado, filterUsuarioId, retoId, filterEmpresaId));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<Map<String, Object>> getEstadisticas(Authentication authentication) {
        List<Registro> todos = getAll(null, null, null, null, null, null, authentication).getBody();
        if (todos == null) todos = Collections.emptyList();

        long total = todos.size();
        long pendientes = todos.stream().filter(r -> "PENDIENTE".equalsIgnoreCase(r.getEstado())).count();
        long enProgreso = todos.stream().filter(r -> "INSCRIPCION".equalsIgnoreCase(r.getTipo()) && !"COMPLETADO".equalsIgnoreCase(r.getEstado()) && !"APROBADO".equalsIgnoreCase(r.getEstado())).count();
        long entregados = todos.stream().filter(r -> "ENTREGA".equalsIgnoreCase(r.getTipo()) || "COMPLETADO".equalsIgnoreCase(r.getEstado())).count();
        long aprobados = todos.stream().filter(r -> "APROBADO".equalsIgnoreCase(r.getEstado())).count();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalInscripciones", total);
        stats.put("pendientes", pendientes);
        stats.put("enProgreso", enProgreso);
        stats.put("entregados", entregados);
        stats.put("aprobados", aprobados);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Integer id, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Registro> opt = registroService.getById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Registro reg = opt.get();
        String email = authentication.getName();
        boolean esAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))
                || "admin@sistema.com".equalsIgnoreCase(email);

        if (esAdmin) {
            return ResponseEntity.ok(reg);
        }

        Optional<Usuario> userOpt = usuarioRepository.findByEmailIgnoreCase(email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Usuario u = userOpt.get();
        if ("USUARIO".equalsIgnoreCase(u.getRol())) {
            // Estudiante solo puede ver su propia publicación y la respuesta recibida
            if (reg.getUsuario() != null && reg.getUsuario().getId().equals(u.getId())) {
                return ResponseEntity.ok(reg);
            }
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "No tienes permisos para visualizar esta publicación.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }

        if ("EMPRESA".equalsIgnoreCase(u.getRol())) {
            // Empresa solo puede ver publicaciones de retos propios
            if (u.getEmpresa() != null && reg.getEmpresa() != null && u.getEmpresa().getId().equals(reg.getEmpresa().getId())) {
                return ResponseEntity.ok(reg);
            }
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "No tienes permisos para visualizar publicaciones de retos de otra empresa.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }

        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
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

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Map<String, Object> payload, Authentication authentication) {
        try {
            Registro registro = new Registro();
            registro.setCodigo((String) payload.get("codigo"));
            registro.setTipo(payload.get("tipo") != null ? (String) payload.get("tipo") : "INSCRIPCION");
            registro.setDescripcion((String) payload.get("descripcion"));
            registro.setEstado(payload.get("estado") != null ? (String) payload.get("estado") : "PENDIENTE");

            String repoUrl = extractString(payload, "repositorioUrl", "repoUrl", "repositorio", "repo", "github", "githubUrl");
            String demoUrl = extractString(payload, "demoUrl", "demo", "despliegue", "despliegueUrl", "urlDemo");
            String archivoUrl = extractString(payload, "archivoUrl", "archivo", "fileUrl", "file", "documentoUrl");
            String notas = extractString(payload, "descripcionSolucion", "solucion", "notas", "comentarios");

            StringBuilder obs = new StringBuilder();
            if (payload.get("observaciones") != null && !payload.get("observaciones").toString().trim().isEmpty()) {
                obs.append(payload.get("observaciones").toString().trim());
            }

            if (repoUrl != null || demoUrl != null || archivoUrl != null || notas != null) {
                if (obs.length() > 0) obs.append("\n\n");
                obs.append("--- [ENTREGA DE SOLUCIÓN] ---");
                if (repoUrl != null) obs.append("\n• Repositorio: ").append(repoUrl);
                if (demoUrl != null) obs.append("\n• Demo/Despliegue: ").append(demoUrl);
                if (archivoUrl != null) obs.append("\n• Archivo: ").append(archivoUrl);
                if (notas != null) obs.append("\n• Descripción: ").append(notas);
                registro.setTipo("ENTREGA");
                registro.setEstado("COMPLETADO");
            }

            if (obs.length() > 0) {
                registro.setObservaciones(obs.toString());
            } else if (payload.get("observaciones") != null) {
                registro.setObservaciones((String) payload.get("observaciones"));
            }

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
        } catch (IllegalArgumentException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al procesar la inscripción: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
        }
    }

    @RequestMapping(value = {"/{id}/entregar", "/{id}/entrega"}, method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> entregarSolucion(@PathVariable Integer id, @RequestBody Map<String, Object> payload, Authentication authentication) {
        try {
            String repoUrl = payload.get("repositorioUrl") != null ? payload.get("repositorioUrl").toString() : (String) payload.get("repo");
            String demoUrl = payload.get("demoUrl") != null ? payload.get("demoUrl").toString() : (String) payload.get("demo");
            String notas = payload.get("descripcionSolucion") != null ? payload.get("descripcionSolucion").toString() : (String) payload.get("descripcion");
            if (notas == null && payload.get("observaciones") != null) notas = payload.get("observaciones").toString();

            String userEmail = (authentication != null) ? authentication.getName() : null;
            boolean esAdmin = authentication != null && (
                    authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))
                    || "admin@sistema.com".equalsIgnoreCase(userEmail)
            );

            Optional<Registro> entregado = registroService.entregarSolucion(id, repoUrl, demoUrl, notas, userEmail, esAdmin);
            if (entregado.isPresent()) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "¡Solución entregada con éxito! La empresa aliada evaluará tu proyecto.");
                res.put("registro", entregado.get());
                return ResponseEntity.ok(res);
            } else {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Registro no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
            }
        } catch (SecurityException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al registrar la entrega: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @RequestMapping(value = {"/{id}/evaluar", "/{id}/evaluacion"}, method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> evaluarReto(@PathVariable Integer id, @RequestBody Map<String, Object> payload, Authentication authentication) {
        try {
            String estado = payload.get("estado") != null ? payload.get("estado").toString() : "APROBADO";
            String retroalimentacion = payload.get("retroalimentacion") != null ? payload.get("retroalimentacion").toString() : (String) payload.get("observaciones");
            Integer puntos = payload.get("puntos") != null ? Integer.valueOf(payload.get("puntos").toString()) : null;

            String evaluatorEmail = (authentication != null) ? authentication.getName() : null;
            boolean esAdmin = authentication != null && (
                    authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))
                    || "admin@sistema.com".equalsIgnoreCase(evaluatorEmail)
            );

            Optional<Registro> evaluado = registroService.evaluarReto(id, estado, retroalimentacion, puntos, evaluatorEmail, esAdmin);
            if (evaluado.isPresent()) {
                Map<String, Object> res = new HashMap<>();
                res.put("success", true);
                res.put("message", "Evaluación registrada exitosamente.");
                res.put("registro", evaluado.get());
                return ResponseEntity.ok(res);
            } else {
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("message", "Registro no encontrado");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(err);
            }
        } catch (SecurityException e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Error al evaluar la entrega: " + e.getMessage());
            return ResponseEntity.badRequest().body(err);
        }
    }

    @RequestMapping(value = "/{id}", method = {RequestMethod.PUT, RequestMethod.PATCH})
    public ResponseEntity<?> update(@PathVariable Integer id, @RequestBody Map<String, Object> payload, Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String email = authentication.getName();
        boolean esAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))
                || "admin@sistema.com".equalsIgnoreCase(email);

        if (!esAdmin) {
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("message", "Solo los administradores pueden modificar directamente los registros de inscripción.");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(err);
        }

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
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Integer id, Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        if (authentication == null || authentication.getName() == null) {
            response.put("success", false);
            response.put("message", "No autenticado");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }

        Optional<Registro> opt = registroService.getById(id);
        if (opt.isEmpty()) {
            response.put("success", false);
            response.put("message", "Inscripción o publicación no encontrada");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        Registro reg = opt.get();
        String email = authentication.getName();
        boolean esAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().contains("ADMIN"))
                || "admin@sistema.com".equalsIgnoreCase(email);

        if (!esAdmin) {
            Optional<Usuario> userOpt = usuarioRepository.findByEmailIgnoreCase(email);
            if (userOpt.isEmpty()) {
                response.put("success", false);
                response.put("message", "Usuario no autorizado");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
            Usuario u = userOpt.get();
            boolean esDueno = reg.getUsuario() != null && reg.getUsuario().getId().equals(u.getId());
            if (!esDueno) {
                response.put("success", false);
                response.put("message", "Solo el administrador o el estudiante titular pueden eliminar o desvincular esta publicación.");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
            }
        }

        boolean deleted = registroService.delete(id);
        if (deleted) {
            response.put("success", true);
            response.put("message", "Publicación o inscripción eliminada exitosamente");
            return ResponseEntity.ok(response);
        } else {
            response.put("success", false);
            response.put("message", "No se pudo eliminar el registro");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}
