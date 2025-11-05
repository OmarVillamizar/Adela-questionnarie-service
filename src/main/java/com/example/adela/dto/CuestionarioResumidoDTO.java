package com.example.adela.dto;

import com.example.adela.entities.Cuestionario;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CuestionarioResumidoDTO {
    private Long id;
    private String nombre;
    private String siglas;
    private String descripcion;
    private String autor;
    private String version;
    private int numPreguntas;
    
    public static CuestionarioResumidoDTO from(Cuestionario cuestionario) {
        CuestionarioResumidoDTO res = new CuestionarioResumidoDTO();
        res.setAutor(cuestionario.getAutor());
        res.setDescripcion(cuestionario.getDescripcion());
        res.setId(cuestionario.getId());
        res.setNombre(cuestionario.getNombre());
        res.setNumPreguntas(cuestionario.getPreguntas().size());
        res.setSiglas(cuestionario.getSiglas());
        res.setVersion(cuestionario.getVersion());
        return res;
    }
}