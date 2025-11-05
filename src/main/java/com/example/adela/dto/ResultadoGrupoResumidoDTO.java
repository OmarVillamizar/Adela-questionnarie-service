package com.example.adela.dto;

import java.sql.Date;
import com.example.adela.entities.ResultadoCuestionario;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ResultadoGrupoResumidoDTO {
    public CuestionarioResumidoDTO cuestionario;
    public Date fechaAplicacion;
    public Date fechaResolucion;
    public GrupoResumidoDTO grupo;
    public boolean bloqueado;
    
    /**
     * Convierte desde entidad usando DTO de grupo ya cargado
     */
    public static ResultadoGrupoResumidoDTO from(ResultadoCuestionario rc, GrupoResumidoDTO grupo) {
        ResultadoGrupoResumidoDTO rg = new ResultadoGrupoResumidoDTO();
        rg.setFechaAplicacion(rc.getFechaAplicacion());
        rg.setFechaResolucion(rc.getFechaResolucion());
        rg.setCuestionario(CuestionarioResumidoDTO.from(rc.getCuestionario()));
        rg.setGrupo(grupo);
        rg.setBloqueado(rc.isBloqueado());
        return rg;
    }
}