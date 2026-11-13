package com.cesde.proyectointegrador.modules.registros.service;

import com.cesde.proyectointegrador.modules.empresas.repository.EmpresaRepository;
import com.cesde.proyectointegrador.modules.registros.entity.Registro;
import com.cesde.proyectointegrador.modules.registros.repository.RegistroRepository;
import com.cesde.proyectointegrador.modules.retos.repository.RetoRepository;
import com.cesde.proyectointegrador.modules.usuarios.entity.Usuario;
import com.cesde.proyectointegrador.modules.usuarios.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class RegistroService {

    @Autowired
    private RegistroRepository registroRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmpresaRepository empresaRepository;

    @Autowired
    private RetoRepository retoRepository;

    public List<Registro> getAll(String search, String tipo, String estado, Integer usuarioId, Integer retoId, Integer empresaId) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanTipo = (tipo != null && !tipo.trim().isEmpty()) ? tipo.trim() : null;
        String cleanEstado = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;
        return registroRepository.findByFilters(cleanSearch, cleanTipo, cleanEstado, usuarioId, retoId, empresaId);
    }

    public Optional<Registro> getById(Integer id) {
        return registroRepository.findById(id);
    }

    public Registro create(Registro registro, Integer usuarioId, Integer empresaId, Integer retoId, String userEmail) {
        // 1. Resolver el usuario a inscribir
        if (usuarioId != null) {
            usuarioRepository.findById(usuarioId).ifPresent(registro::setUsuario);
        } else if (userEmail != null && !userEmail.trim().isEmpty()) {
            usuarioRepository.findByEmailIgnoreCase(userEmail.trim()).ifPresent(registro::setUsuario);
        }

        if (registro.getUsuario() == null) {
            throw new IllegalArgumentException("No se especificó un usuario válido para la inscripción.");
        }

        // Restricción: Empresas no pueden participar en retos
        if ("EMPRESA".equalsIgnoreCase(registro.getUsuario().getRol())) {
            throw new IllegalArgumentException("Las cuentas de empresa no pueden inscribirse ni participar en retos.");
        }

        // Restricción: Admin no puede auto-inscribirse en retos para sí mismo
        if (usuarioId == null && ("ADMIN".equalsIgnoreCase(registro.getUsuario().getRol()) || "admin@sistema.com".equalsIgnoreCase(userEmail))) {
            throw new IllegalArgumentException("El administrador no participa en retos. Para asignar un reto, indique el 'usuarioId' del estudiante.");
        }

        if (retoId != null) {
            retoRepository.findById(retoId).ifPresent(registro::setReto);
        }

        // Evitar duplicados si el estudiante ya está inscrito en este reto
        if (registro.getUsuario() != null && registro.getReto() != null) {
            Optional<Registro> existente = registroRepository.findByUsuarioIdAndRetoId(registro.getUsuario().getId(), registro.getReto().getId());
            if (existente.isPresent()) {
                return existente.get();
            }
        }

        if (registro.getCodigo() == null || registro.getCodigo().trim().isEmpty()) {
            int year = Year.now().getValue();
            int randomSuffix = 100 + new Random().nextInt(900);
            registro.setCodigo("REG-" + year + "-" + randomSuffix);
        } else {
            registro.setCodigo(registro.getCodigo().trim().toUpperCase());
        }

        if (registro.getDescripcion() == null || registro.getDescripcion().trim().isEmpty()) {
            registro.setDescripcion("Inscripción en reto: " + (registro.getReto() != null ? registro.getReto().getTitulo() : "Desafío técnico"));
        }
        if (registro.getTipo() == null || registro.getTipo().trim().isEmpty()) {
            registro.setTipo("INSCRIPCION");
        }
        if (registro.getEstado() == null || registro.getEstado().trim().isEmpty()) {
            registro.setEstado("PENDIENTE");
        }

        if (empresaId != null) {
            empresaRepository.findById(empresaId).ifPresent(registro::setEmpresa);
        } else if (registro.getReto() != null && registro.getReto().getEmpresa() != null) {
            registro.setEmpresa(registro.getReto().getEmpresa());
        }

        return registroRepository.save(registro);
    }

    public Optional<Registro> entregarSolucion(Integer registroId, String repoUrl, String demoUrl, String notas, String userEmail, boolean esAdmin) {
        return registroRepository.findById(registroId).map(reg -> {
            boolean esDueno = reg.getUsuario() != null && reg.getUsuario().getEmail().equalsIgnoreCase(userEmail);
            if (!esAdmin && !esDueno) {
                throw new SecurityException("No tienes permisos para entregar la solución de este reto.");
            }

            StringBuilder sb = new StringBuilder();
            if (reg.getObservaciones() != null && !reg.getObservaciones().isEmpty()) {
                sb.append(reg.getObservaciones()).append("\n\n");
            }
            sb.append("--- [ENTREGA DE SOLUCIÓN] ---");
            if (repoUrl != null && !repoUrl.trim().isEmpty()) {
                sb.append("\n• Repositorio: ").append(repoUrl.trim());
            }
            if (demoUrl != null && !demoUrl.trim().isEmpty()) {
                sb.append("\n• Demo/Despliegue: ").append(demoUrl.trim());
            }
            if (notas != null && !notas.trim().isEmpty()) {
                sb.append("\n• Descripción: ").append(notas.trim());
            }

            reg.setObservaciones(sb.toString().trim());
            reg.setTipo("ENTREGA");
            reg.setEstado("COMPLETADO");
            return registroRepository.save(reg);
        });
    }

    public Optional<Registro> evaluarReto(Integer registroId, String nuevoEstado, String retroalimentacion, Integer puntos, String evaluatorEmail, boolean esAdmin) {
        return registroRepository.findById(registroId).map(reg -> {
            if (!esAdmin) {
                Optional<Usuario> evalUser = usuarioRepository.findByEmailIgnoreCase(evaluatorEmail);
                boolean esEmpresaDuenia = evalUser.isPresent() 
                        && evalUser.get().getEmpresa() != null 
                        && reg.getEmpresa() != null 
                        && evalUser.get().getEmpresa().getId().equals(reg.getEmpresa().getId());
                if (!esEmpresaDuenia) {
                    throw new SecurityException("Solo la empresa patrocinadora o el administrador pueden evaluar esta entrega.");
                }
            }

            StringBuilder sb = new StringBuilder();
            if (reg.getObservaciones() != null && !reg.getObservaciones().isEmpty()) {
                sb.append(reg.getObservaciones()).append("\n\n");
            }
            sb.append("--- [EVALUACIÓN DE RETO] ---");
            sb.append("\n• Resultado: ").append(nuevoEstado != null ? nuevoEstado : "APROBADO");
            if (retroalimentacion != null && !retroalimentacion.trim().isEmpty()) {
                sb.append("\n• Retroalimentación: ").append(retroalimentacion.trim());
            }
            if (puntos != null) {
                sb.append("\n• Puntos otorgados: ").append(puntos).append(" XP");
            }

            reg.setObservaciones(sb.toString().trim());
            reg.setTipo("EVALUACION");
            reg.setEstado(nuevoEstado != null ? nuevoEstado.trim().toUpperCase() : "APROBADO");
            return registroRepository.save(reg);
        });
    }

    public Registro create(Registro registro, Integer usuarioId, Integer empresaId, Integer retoId) {
        return create(registro, usuarioId, empresaId, retoId, null);
    }

    public Optional<Registro> update(Integer id, Registro data, Integer usuarioId, Integer empresaId, Integer retoId) {
        return registroRepository.findById(id).map(reg -> {
            if (data.getCodigo() != null) reg.setCodigo(data.getCodigo().trim().toUpperCase());
            if (data.getTipo() != null) reg.setTipo(data.getTipo());
            if (data.getDescripcion() != null) reg.setDescripcion(data.getDescripcion());
            if (data.getEstado() != null) reg.setEstado(data.getEstado());
            if (data.getObservaciones() != null) reg.setObservaciones(data.getObservaciones());

            if (usuarioId != null) {
                usuarioRepository.findById(usuarioId).ifPresent(reg::setUsuario);
            }
            if (empresaId != null) {
                empresaRepository.findById(empresaId).ifPresent(reg::setEmpresa);
            }
            if (retoId != null) {
                retoRepository.findById(retoId).ifPresent(reg::setReto);
            }

            return registroRepository.save(reg);
        });
    }

    public boolean delete(Integer id) {
        if (registroRepository.existsById(id)) {
            registroRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
