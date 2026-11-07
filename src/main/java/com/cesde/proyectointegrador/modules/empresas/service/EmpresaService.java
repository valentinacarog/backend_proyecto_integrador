package com.cesde.proyectointegrador.modules.empresas.service;

import com.cesde.proyectointegrador.modules.empresas.entity.Empresa;
import com.cesde.proyectointegrador.modules.empresas.repository.EmpresaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EmpresaService {

    @Autowired
    private EmpresaRepository empresaRepository;

    public List<Empresa> getAll(String search, String sector, String estado) {
        String cleanSearch = (search != null && !search.trim().isEmpty()) ? search.trim() : null;
        String cleanSector = (sector != null && !sector.trim().isEmpty()) ? sector.trim() : null;
        String cleanEstado = (estado != null && !estado.trim().isEmpty()) ? estado.trim() : null;
        return empresaRepository.findByFilters(cleanSearch, cleanSector, cleanEstado);
    }

    public Optional<Empresa> getById(Integer id) {
        return empresaRepository.findById(id);
    }

    public Empresa create(Empresa empresa) {
        if (empresa.getNit() == null || empresa.getNit().trim().isEmpty()) {
            empresa.setNit("NIT-" + System.currentTimeMillis());
        }
        if (empresa.getSector() == null || empresa.getSector().trim().isEmpty()) {
            empresa.setSector("Tecnología");
        }
        if (empresa.getEmail() == null || empresa.getEmail().trim().isEmpty()) {
            empresa.setEmail("contacto" + System.currentTimeMillis() + "@empresa.co");
        }
        if (empresa.getTelefono() == null || empresa.getTelefono().trim().isEmpty()) {
            empresa.setTelefono("+57 300 000 0000");
        }
        if (empresa.getCiudad() == null || empresa.getCiudad().trim().isEmpty()) {
            empresa.setCiudad("Medellín");
        }
        if (empresa.getEstado() == null || empresa.getEstado().trim().isEmpty()) {
            empresa.setEstado("ACTIVO");
        }
        return empresaRepository.save(empresa);
    }

    public Optional<Empresa> update(Integer id, Empresa data) {
        return empresaRepository.findById(id).map(emp -> {
            if (data.getNombre() != null) emp.setNombre(data.getNombre());
            if (data.getNit() != null) emp.setNit(data.getNit());
            if (data.getSector() != null) emp.setSector(data.getSector());
            if (data.getEmail() != null) emp.setEmail(data.getEmail());
            if (data.getTelefono() != null) emp.setTelefono(data.getTelefono());
            if (data.getDireccion() != null) emp.setDireccion(data.getDireccion());
            if (data.getCiudad() != null) emp.setCiudad(data.getCiudad());
            if (data.getEstado() != null) emp.setEstado(data.getEstado());
            return empresaRepository.save(emp);
        });
    }

    public boolean delete(Integer id) {
        if (empresaRepository.existsById(id)) {
            empresaRepository.deleteById(id);
            return true;
        }
        return false;
    }
}
