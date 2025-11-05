package com.example.adela.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
public class EstudianteDTO extends UsuarioDTO {
    private String genero; // "MASCULINO", "FEMENINO", etc.
    private String fechaNacimiento; // Puede venir como String desde ms-auth
    
    public EstudianteDTO(String email, String nombre, String codigo, String estado, 
                         String genero, String fechaNacimiento) {
        super(email, nombre, codigo, estado);
        this.genero = genero;
        this.fechaNacimiento = fechaNacimiento;
    }
}