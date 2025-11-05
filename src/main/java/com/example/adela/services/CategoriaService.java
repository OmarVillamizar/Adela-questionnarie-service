package com.example.adela.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.adela.dto.CategoriaDTO;
import com.example.adela.entities.Categoria;
import com.example.adela.entities.Cuestionario;
import com.example.adela.repositories.CategoriaRepository;
import com.example.adela.repositories.CuestionarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CategoriaService {
    
    @Autowired
    private CategoriaRepository categoriaRepository;
    
    @Autowired
    private CuestionarioRepository cuestionarioRepository;
    
    public Categoria crearCategoria(Long idCuestionario, String nombre, Double valorMinimo, Double valorMaximo) {
        Cuestionario cuestionario = cuestionarioRepository.findById(idCuestionario)
                .orElseThrow(() -> new EntityNotFoundException("Cuestionario no encontrado con id " + idCuestionario));
        
        Categoria categoria = new Categoria();
        
        categoria.setCuestionario(cuestionario);
        categoria.setNombre(nombre);
        categoria.setValorMaximo(valorMaximo);
        categoria.setValorMinimo(valorMinimo);
        
        return categoriaRepository.save(categoria);
    }
    
    public void eliminarCategoria(Categoria categoria) {
        categoriaRepository.delete(categoria);
    }
    
    public void guardarCategorias(Iterable<Categoria> categorias) {
        categoriaRepository.saveAll(categorias);
    }
    
    public Categoria crearCategoria(Cuestionario cuestionario, CategoriaDTO categoriaDTO) {
        
        Categoria categoria = new Categoria();
        
        categoria.setCuestionario(cuestionario);
        categoria.setNombre(categoriaDTO.getNombre());
        categoria.setValorMaximo(0d);
        categoria.setValorMinimo(0d);
        
        return categoriaRepository.save(categoria);
    }
}