package com.example.adela.services;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.adela.dto.OpcionDTO;
import com.example.adela.dto.PreguntaDTO;
import com.example.adela.entities.Categoria;
import com.example.adela.entities.Cuestionario;
import com.example.adela.entities.Opcion;
import com.example.adela.entities.Pregunta;
import com.example.adela.repositories.CuestionarioRepository;
import com.example.adela.repositories.PreguntaRepository;
//import com.example.adela.repositories.ResultadoPreguntaRepository;
import com.example.adela.repositories.ResultadoPreguntaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PreguntaService {
    
    @Autowired
    private PreguntaRepository preguntaRepository;
    
    @Autowired
    private CuestionarioRepository cuestionarioRepository;
    
    @Autowired
    private ResultadoPreguntaRepository resultadoPreguntaRepository;
    
    @Autowired
    private OpcionService opcionService;
    
    public Pregunta crearPregunta(Long idCuestionario, String preguntaStr, int orden, boolean opcionMultple) {
        Cuestionario cuestionario = cuestionarioRepository.findById(idCuestionario)
                .orElseThrow(() -> new EntityNotFoundException("Cuestionario no encontrado con id " + idCuestionario));
        Pregunta pregunta = new Pregunta();
        pregunta.setCuestionario(cuestionario);
        pregunta.setPregunta(preguntaStr);
        pregunta.setOrden(orden);
        
        return preguntaRepository.save(pregunta);
    }
    
    public void eliminarPregunta(Pregunta pregunta) {
        for (Opcion opcion : pregunta.getOpciones()) {
            eliminarRespuestasAsociadas(opcion);
            opcionService.eliminarOpcion(opcion);
        }
        pregunta.getOpciones().clear();
        preguntaRepository.delete(pregunta);
    }
    
    
    private void eliminarRespuestasAsociadas(Opcion opcion) {
        resultadoPreguntaRepository.deleteByOpcion(opcion);
    }
    
    
    public Pregunta crearPregunta(Cuestionario cuestionario, Map<Integer, Categoria> mapId, PreguntaDTO preguntaDTO) {
        Pregunta preguntaSave = new Pregunta();
        preguntaSave.setCuestionario(cuestionario);
        preguntaSave.setPregunta(preguntaDTO.getPregunta());
        preguntaSave.setOrden(preguntaDTO.getOrden());
        preguntaSave.setOpcionMultiple(preguntaDTO.isOpcionMultiple());
        
        Pregunta pregunta = preguntaRepository.save(preguntaSave);
        
        Set<Opcion> opciones = new HashSet<>();
        Map<Integer, Double> max = new TreeMap<Integer, Double>();
        Map<Integer, Double> min = new TreeMap<Integer, Double>();
        
        for (OpcionDTO opcionDTO : preguntaDTO.getOpciones()) {
            int cateId = opcionDTO.getCategoriaId();
            Categoria categoria = mapId.get(opcionDTO.getCategoriaId());
            
            if (!mapId.containsKey(cateId)) {
                throw new RuntimeException(
                        "No existe una categoría con id " + opcionDTO.getCategoriaId() + " en la solicitud.");
            }
            Opcion opcion = opcionService.crearOpcion(pregunta, categoria, opcionDTO);
            opciones.add(opcion);
            if (pregunta.isOpcionMultiple()) {
                if (max.containsKey(cateId)) {
                    max.put(cateId, Math.max(max.get(cateId), max.get(cateId) + opcion.getValor()));
                    min.put(cateId, Math.min(min.get(cateId), min.get(cateId) + opcion.getValor()));
                } else {
                    max.put(cateId, opcion.getValor());
                    min.put(cateId, 0d);
                }
            } else {
                if (max.containsKey(cateId)) {
                    max.put(cateId, Math.max(max.get(cateId), opcion.getValor()));
                    min.put(cateId, Math.min(min.get(cateId), opcion.getValor()));
                } else {
                    max.put(cateId, opcion.getValor());
                    min.put(cateId, opcion.getValor());
                }
            }
            
        }
        
        for (Entry<Integer, Double> pair : max.entrySet()) {
            Categoria categoria = mapId.get(pair.getKey());
            categoria.setValorMaximo(categoria.getValorMaximo() + pair.getValue());
        }
        
        for (Entry<Integer, Double> pair : min.entrySet()) {
            Categoria categoria = mapId.get(pair.getKey());
            categoria.setValorMinimo(categoria.getValorMinimo() + pair.getValue());
        }
        
        pregunta.setOpciones(opciones);
        
        return preguntaRepository.save(pregunta);
    }
}