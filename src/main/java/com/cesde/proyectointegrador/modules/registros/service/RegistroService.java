package com.cesde.proyectointegrador.modules.registros.service;

import com.cesde.proyectointegrador.modules.empresas.repository.EmpresaRepository;
import com.cesde.proyectointegrador.modules.registros.entity.Registro;
import com.cesde.proyectointegrador.modules.registros.repository.RegistroRepository;
import com.cesde.proyectointegrador.modules.retos.repository.RetoRepository;
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

    public Registro create(Registro registro, Integer usuarioId, Integer empresaId, Integer retoId) {
        if (registro.getCodigo() == null || registro.getCodigo().trim().isEmpty()) {
            int year = Year.now().getValue();
            int randomSuffix = 100 + new Random().nextInt(900);
            registro.setCodigo("REG-" + year + "-" + randomSuffix);
        } else {
            registro.setCodigo(registro.getCodigo().trim().toUpperCase());
        }

        if (usuarioId != null) {
            usuarioRepository.findById(usuarioId).ifPresent(registro::setUsuario);
        }
        if (empresaId != null) {
            empresaRepository.findById(empresaId).ifPresent(registro::setEmpresa);
        }
        if (retoId != null) {
            retoRepository.findById(retoId).ifPresent(registro::setReto);
        }

        return registroRepository.save(registro);
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
