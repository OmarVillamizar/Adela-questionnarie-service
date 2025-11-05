package com.example.adela.entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import jakarta.annotation.Nullable;
import java.sql.Date;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Data
@NoArgsConstructor
@Table(name = "resultados_cuestionario")
public class ResultadoCuestionario {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relación local con Cuestionario (ms-quest)
    @ManyToOne
    @JoinColumn(name = "cuestionario_id", referencedColumnName = "id", nullable = false)
    @JsonBackReference
    private Cuestionario cuestionario;
    
    // Referencia externa al estudiante (solo email, viene de ms-auth)
    // CORRECCIÓN: Esta debe ser una columna String, no @ManyToOne
    @Column(name = "estudiante_email", nullable = false)
    private String estudianteEmail;
    
    // Referencia externa al grupo (solo ID, viene de ms-grupos)
    @Column(name = "grupo_id")
    private Long grupoId;
    
    @Column(name = "fecha_aplicacion")
    private Date fechaAplicacion;
    
    @Column(name = "bloqueado")
    private boolean bloqueado = false;
    
    @Nullable
    @Column(name = "fecha_resolucion")
    private Date fechaResolucion;
    
    @OneToMany(mappedBy = "resultadoCuestionario", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    @JsonManagedReference
    private Set<ResultadoPregunta> preguntas = new HashSet<>();
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ResultadoCuestionario)) return false;
        ResultadoCuestionario that = (ResultadoCuestionario) o;
        return Objects.equals(getId(), that.getId());
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
