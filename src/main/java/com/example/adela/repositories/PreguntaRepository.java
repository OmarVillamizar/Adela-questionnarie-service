package com.example.adela.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.adela.entities.Cuestionario;
import com.example.adela.entities.Pregunta;

public interface PreguntaRepository extends JpaRepository<Pregunta, Long> {
    List<Pregunta> findByCuestionario(Cuestionario c);
}
