package com.example.adela.repositories;

import com.example.adela.entities.Cuestionario;
import com.example.adela.entities.ResultadoCuestionario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResultadoCuestionarioRepository extends JpaRepository<ResultadoCuestionario, Long> {

    // Buscar por cuestionario y email de estudiante (sin resolver)
    Optional<ResultadoCuestionario> findByCuestionarioAndEstudianteEmailAndFechaResolucionIsNull(
            Cuestionario cuestionario, String estudianteEmail);

    // Buscar por cuestionario, email de estudiante y grupo específico
    Optional<ResultadoCuestionario> findByCuestionarioAndEstudianteEmailAndGrupoId(
            Cuestionario cuestionario, String estudianteEmail, Long grupoId);

    // Buscar por email de estudiante no bloqueados
    List<ResultadoCuestionario> findByEstudianteEmailAndBloqueadoFalse(String estudianteEmail);

    // Buscar por grupo ID
    List<ResultadoCuestionario> findByGrupoId(Long grupoId);

    // Buscar por grupo ID y cuestionario
    List<ResultadoCuestionario> findByGrupoIdAndCuestionario(Long grupoId, Cuestionario cuestionario);
    
    @Transactional
    @Modifying
    @Query("DELETE FROM ResultadoCuestionario rc WHERE rc.cuestionario = :cuestionario")
    void deleteByCuestionario(Cuestionario cuestionario);
}