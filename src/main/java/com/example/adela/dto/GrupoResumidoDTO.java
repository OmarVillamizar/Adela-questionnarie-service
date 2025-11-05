package com.example.adela.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class GrupoResumidoDTO {
    private Long id;
    private String nombre;
    private String profesorNombre;
    private String profesorEmail;
    private Integer numEstudiantes;
    
    /**
     * Convierte desde GrupoDTO (el que viene de ms-grupos)
     */
    public static GrupoResumidoDTO from(GrupoDTO grupoDTO) {
        if (grupoDTO == null) {
            return null;
        }
        
        GrupoResumidoDTO gt = new GrupoResumidoDTO();
        gt.setId(grupoDTO.getId());
        gt.setNombre(grupoDTO.getNombre());
        gt.setProfesorEmail(grupoDTO.getProfesorEmail());
        gt.setProfesorNombre(grupoDTO.getProfesorNombre());
        gt.setNumEstudiantes(grupoDTO.getNumEstudiantes());
        return gt;
    }
}