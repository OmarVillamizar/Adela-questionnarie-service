package com.example.adela.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO que representa la respuesta completa del microservicio ms-grupos
 * Este DTO se usa SOLO para recibir datos desde el GroupClient
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrupoDTO {
    private Long id;
    private String nombre;
    private String profesorEmail;
    private String profesorNombre;
    private Integer numEstudiantes;
    
    // Nota: Los estudiantes no se incluyen aquí porque se obtienen
    // mediante el endpoint separado: obtenerEstudiantesPorGrupo()
}