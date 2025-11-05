package com.example.adela.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.adela.entities.Cuestionario;

public interface CuestionarioRepository extends JpaRepository<Cuestionario, Long> {
    
}
