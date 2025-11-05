package com.example.adela.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.adela.entities.Opcion;

public interface OpcionRepository extends JpaRepository<Opcion, Long> {
    
}
