package com.example.adela.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Objects;

@Entity
@Data
@NoArgsConstructor
public class ResultadoPregunta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Relación local con ResultadoCuestionario
    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "cuestionario_id", referencedColumnName = "id", nullable = false)
    private ResultadoCuestionario resultadoCuestionario;

    // Relación local con opción (pertenece al cuestionario)
    @ManyToOne
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    @JoinColumn(name = "opcion_id", referencedColumnName = "id", nullable = false)
    private Opcion opcion;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResultadoPregunta)) return false;
        ResultadoPregunta that = (ResultadoPregunta) o;
        return Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
