package com.example.adela.dto;

import com.example.adela.dto.OpcionDTO;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OpcionDTO {
    private int orden;
    private String respuesta;
    private Double valor;
    private int categoriaId; // no es el id de la bd
}