package com.example.adela.dto;

import java.sql.Date;
import com.example.adela.entities.ResultadoCuestionario;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResultadoCuestionarioDTO {
    private CuestionarioResumidoDTO cuestionario;
    private EstudianteDTO estudiante;
    private Date fechaAplicacion;
    private Date fechaResolucion;
    private GrupoResumidoDTO grupo;
    private Long id;

    /**
     * Convierte desde entidad usando DTOs ya cargados desde otros microservicios
     * Este es el método principal que debe usarse en el Service
     */
    public static ResultadoCuestionarioDTO from(ResultadoCuestionario rc, 
                                                 EstudianteDTO estudiante, 
                                                 GrupoResumidoDTO grupo) {
        ResultadoCuestionarioDTO rcd = new ResultadoCuestionarioDTO();
        rcd.setCuestionario(CuestionarioResumidoDTO.from(rc.getCuestionario()));
        rcd.setEstudiante(estudiante);
        rcd.setFechaAplicacion(rc.getFechaAplicacion());
        rcd.setFechaResolucion(rc.getFechaResolucion());
        rcd.setGrupo(grupo);
        rcd.setId(rc.getId());
        return rcd;
    }
}