package com.example.adela.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CategoriaDTO {
    private String nombre;
    private int id; // No es el id de la bd, es su posición en la lista
}