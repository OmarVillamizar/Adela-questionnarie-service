package com.example.adela.dto;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RespuestaCuestionarioDTO {
    private Long cuestionarioId;
    private List<Long> opcionesSeleccionadasId;
}