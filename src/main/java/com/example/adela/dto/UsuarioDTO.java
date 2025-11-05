package com.example.adela.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {
    private String email;
    private String nombre;
    private String codigo;
    private String estado; // "ACTIVA", "INCOMPLETA", "INACTIVA"
}