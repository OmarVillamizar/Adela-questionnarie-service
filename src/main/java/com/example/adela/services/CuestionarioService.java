package com.example.adela.services;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.adela.dto.CategoriaDTO;
import com.example.adela.dto.CuestionarioDTO;
import com.example.adela.dto.CuestionarioResumidoDTO;
import com.example.adela.dto.PreguntaDTO;
import com.example.adela.entities.Categoria;
import com.example.adela.entities.Cuestionario;
import com.example.adela.entities.Pregunta;
import com.example.adela.repositories.CuestionarioRepository;
//import com.example.adela.repositories.ResultadoCuestionarioRepository;
import com.example.adela.repositories.ResultadoCuestionarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CuestionarioService {
    
    @Autowired
    private CuestionarioRepository cuestionarioRepository;
    
    @Autowired
    private ResultadoCuestionarioRepository resultadoCuestionarioRepository;
   
    @Autowired
    private CategoriaService categoriaService;
    
    @Autowired
    private PreguntaService preguntaService;
    
    public Cuestionario crearCuestionario(String nombre, String descripcion, String autor, String version,
            String siglas) {
        Cuestionario cuestionario = new Cuestionario();
        cuestionario.setNombre(nombre);
        cuestionario.setDescripcion(descripcion);
        cuestionario.setAutor(autor);
        cuestionario.setVersion(version);
        cuestionario.setSiglas(siglas);
        
        return cuestionarioRepository.save(cuestionario);
    }
    
    @Transactional
    public Cuestionario crearCuestionario(CuestionarioDTO cuestionarioDTO) {
        Cuestionario cuestionarioSave = new Cuestionario();
        cuestionarioSave.setNombre(cuestionarioDTO.getNombre());
        cuestionarioSave.setDescripcion(cuestionarioDTO.getDescripcion());
        cuestionarioSave.setAutor(cuestionarioDTO.getAutor());
        cuestionarioSave.setSiglas(cuestionarioDTO.getSiglas());
        cuestionarioSave.setVersion(cuestionarioDTO.getVersion());
        
        Cuestionario cuestionario = cuestionarioRepository.save(cuestionarioSave);
        
        Map<Integer, Categoria> idMap = new HashMap<>();
        
        Set<Categoria> categorias = new HashSet<>();
        Set<Pregunta> preguntas = new HashSet<>();
        
        for (CategoriaDTO categoriaDTO : cuestionarioDTO.getCategorias()) {
            int otherId = categoriaDTO.getId();
            Categoria categoria = categoriaService.crearCategoria(cuestionario, categoriaDTO);
            idMap.put(otherId, categoria);
            categorias.add(categoria);
        }
        
        for (PreguntaDTO preguntaDTO : cuestionarioDTO.getPreguntas()) {
            Pregunta pregunta = preguntaService.crearPregunta(cuestionario, idMap, preguntaDTO);
            preguntas.add(pregunta);
        }
        
        categoriaService.guardarCategorias(categorias);
        
        cuestionario.setCategorias(categorias);
        cuestionario.setPreguntas(preguntas);
        
        return cuestionarioRepository.save(cuestionario);
    }
    
 
    public void eliminarCuestionario(Long id) {
        Cuestionario cuestionario = cuestionarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cuestionario no encontrado con el ID: " + id));
        
        eliminarReferenciasAsociadas(cuestionario);
        
        for (Pregunta pregunta : cuestionario.getPreguntas()) {
            preguntaService.eliminarPregunta(pregunta);
        }
        cuestionario.getPreguntas().clear();
        
        for (Categoria categoria : cuestionario.getCategorias()) {
            categoriaService.eliminarCategoria(categoria);
        }
        cuestionario.getCategorias().clear();
        
        cuestionarioRepository.delete(cuestionario);
    }
    
    
    private void eliminarReferenciasAsociadas(Cuestionario cuestionario) {
        resultadoCuestionarioRepository.deleteByCuestionario(cuestionario);
    }
  
    public Cuestionario getCuestionarioPorId(Long id) {
        return cuestionarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cuestionario no encontrado con el ID: " + id));
    }
    
    public List<CuestionarioResumidoDTO> getCuestionarios() {
        List<Cuestionario> cuestionarios = cuestionarioRepository.findAll();
        List<CuestionarioResumidoDTO> cdtos = new LinkedList<>();
        
        for (Cuestionario cuestionario : cuestionarios) {
            CuestionarioResumidoDTO cdto = new CuestionarioResumidoDTO();
            cdto.setId(cuestionario.getId());
            cdto.setAutor(cuestionario.getAutor());
            cdto.setDescripcion(cuestionario.getDescripcion());
            cdto.setNombre(cuestionario.getNombre());
            cdto.setSiglas(cuestionario.getSiglas());
            cdto.setVersion(cuestionario.getVersion());
            cdto.setNumPreguntas(cuestionario.getPreguntas().size());
            cdtos.add(cdto);
        }
        
        return cdtos;
    }
    
}