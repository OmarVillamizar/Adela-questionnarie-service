package com.example.adela.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class ProfesorDTO extends UsuarioDTO {
    private String carrera;
    private RolDTO rol; // Objeto completo con id y descripcion
    private String estadoProfesor; // "ACTIVA", "INACTIVA"
    
    public ProfesorDTO(String email, String nombre, String codigo, String estado,
                       String carrera, RolDTO rol, String estadoProfesor) {
        super(email, nombre, codigo, estado);
        this.carrera = carrera;
        this.rol = rol;
        this.estadoProfesor = estadoProfesor;
    }
}