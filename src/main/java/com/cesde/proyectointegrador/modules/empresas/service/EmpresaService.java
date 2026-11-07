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
