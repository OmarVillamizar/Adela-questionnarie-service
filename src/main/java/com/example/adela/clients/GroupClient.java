package com.example.adela.clients;

import com.example.adela.dto.EstudianteDTO;
import com.example.adela.dto.GrupoDTO;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.service.annotation.GetExchange;
import org.springframework.web.service.annotation.HttpExchange;

import java.util.List;

@HttpExchange("/api/grupos")
public interface GroupClient {

    // Obtener grupo por ID
    @GetExchange("/{id}")
    GrupoDTO obtenerGrupoPorId(@PathVariable("id") Long id);

    // Obtener estudiantes de un grupo (con DTO completo)
    @GetExchange("/{id}/estudiantes")
    List<EstudianteDTO> obtenerEstudiantesPorGrupo(@PathVariable("id") Long id);
}
