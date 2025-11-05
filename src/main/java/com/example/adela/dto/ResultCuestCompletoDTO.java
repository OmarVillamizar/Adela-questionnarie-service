package com.example.adela.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Date;
import java.util.List;

@Data
@NoArgsConstructor
public class ResultCuestCompletoDTO {
    private Long id;
    private CuestionarioResumidoDTO cuestionario;
    private EstudianteDTO estudiante;
    private GrupoResumidoDTO grupo;
    private Date fechaAplicacion;
    private Date fechaResolucion;
    private List<CategoriaResultadoDTO> categorias;
    private List<PreguntaResueltaDTO> preguntas;
}