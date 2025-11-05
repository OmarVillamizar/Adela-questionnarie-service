package com.example.adela.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * DTO para recibir el email de un estudiante en requests
 * Usado en asignación individual de cuestionarios
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestEstudianteEmail {
    private String email;
}