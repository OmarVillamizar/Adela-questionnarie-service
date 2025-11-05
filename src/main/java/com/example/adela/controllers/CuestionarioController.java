package com.example.adela.controllers;

import com.example.adela.dto.*;
import com.example.adela.entities.Cuestionario;
import com.example.adela.services.CuestionarioService;
import com.example.adela.services.ResultadoCuestionarioService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/cuestionarios")
@RequiredArgsConstructor
public class CuestionarioController {

    private final CuestionarioService cuestionarioService;
    private final ResultadoCuestionarioService resultadoCuestionarioService;

    // =====================================================================================
    // GESTIÓN DE CUESTIONARIOS (ADMINISTRADOR)
    // =====================================================================================
    
    /**
     * Crear un nuevo cuestionario
     * Solo ADMINISTRADOR
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> crearCuestionario(@RequestBody CuestionarioDTO cuestionarioDTO) {
        try {
            Cuestionario cuestionario = cuestionarioService.crearCuestionario(cuestionarioDTO);
            return ResponseEntity.ok(cuestionario);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error creando cuestionario: " + e.getMessage());
        }
    }

    /**
     * Listar todos los cuestionarios
     * ADMINISTRADOR o PROFESOR
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR')")
    public ResponseEntity<?> listarCuestionarios() {
        try {
            return ResponseEntity.ok(cuestionarioService.getCuestionarios());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Error obteniendo cuestionarios: " + e.getMessage());
        }
    }

    /**
     * Obtener un cuestionario por ID
     * ADMINISTRADOR, PROFESOR o ESTUDIANTE
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR') or hasRole('ESTUDIANTE')")
    public ResponseEntity<?> obtenerCuestionario(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(cuestionarioService.getCuestionarioPorId(id));
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Eliminar un cuestionario
     * Solo ADMINISTRADOR
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> eliminarCuestionario(@PathVariable Long id) {
        try {
            cuestionarioService.eliminarCuestionario(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }

    // =====================================================================================
    // ASIGNACIÓN DE CUESTIONARIOS (PROFESOR/ADMINISTRADOR)
    // =====================================================================================

    /**
     * Asignar un cuestionario a un grupo
     * El cuestionario se asigna a todos los estudiantes del grupo
     * ADMINISTRADOR o PROFESOR
     */
    @PostMapping("/{idCuestionario}/asignargrupo/{idGrupo}")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR')")
    public ResponseEntity<?> asignarCuestionarioAGrupo(
            @PathVariable Long idCuestionario, 
            @PathVariable Long idGrupo) {
        try {
            resultadoCuestionarioService.asignarCuestionarioAGrupo(idCuestionario, idGrupo);
            return new ResponseEntity<>("Cuestionario asignado exitosamente al grupo", HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Asignar un cuestionario a un estudiante específico
     * ADMINISTRADOR o PROFESOR
     */
    @PostMapping("/{idCuestionario}/asignarestudiante")
    @PreAuthorize("hasRole('ADMINISTRADOR') or hasRole('PROFESOR')")
    public ResponseEntity<?> asignarCuestionarioAEstudiante(
            @PathVariable Long idCuestionario,
            @RequestBody RequestEstudianteEmail estudianteEmail) {
        try {
            resultadoCuestionarioService.asignarCuestionarioAEstudiante(
                idCuestionario, 
                estudianteEmail.getEmail()
            );
            return new ResponseEntity<>("Cuestionario asignado exitosamente al estudiante", HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // =====================================================================================
    // RESPONDER CUESTIONARIOS (ESTUDIANTE)
    // =====================================================================================

    /**
     * Responder un cuestionario asignado
     * Solo ESTUDIANTE puede responder sus propios cuestionarios
     */
    @PostMapping("/responder")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<?> responderCuestionario(@RequestBody RespuestaCuestionarioDTO respuesta) {
        try {
            // Obtener email del estudiante autenticado desde SecurityContext
            String estudianteEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
            
            resultadoCuestionarioService.responderCuestionario(respuesta, estudianteEmail);
            return new ResponseEntity<>("Cuestionario respondido exitosamente", HttpStatus.CREATED);
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtener cuestionarios del estudiante autenticado
     * Devuelve pendientes y resueltos
     * Solo ESTUDIANTE
     */
    @GetMapping("/mis-cuestionarios")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<?> obtenerMisCuestionarios() {
        try {
            String estudianteEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
            
            return new ResponseEntity<>(
                resultadoCuestionarioService.obtenerCuestionarios(estudianteEmail), 
                HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtener resultado completo de un cuestionario resuelto
     * El estudiante solo puede ver sus propios resultados
     * Solo ESTUDIANTE
     */
    @GetMapping("/mis-cuestionarios/resuelto/{idResultado}")
    @PreAuthorize("hasRole('ESTUDIANTE')")
    public ResponseEntity<?> obtenerResultadoCuestionario(@PathVariable Long idResultado) {
        try {
            String estudianteEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
            
            return new ResponseEntity<>(
                resultadoCuestionarioService.obtenerResultadoCuestionario(idResultado, estudianteEmail),
                HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // =====================================================================================
    // REPORTES Y RESULTADOS (PROFESOR/ADMINISTRADOR)
    // =====================================================================================

    /**
     * Obtener reporte grupal de un cuestionario
     * Incluye promedios, estudiantes que resolvieron y no resolvieron
     * PROFESOR o ADMINISTRADOR
     */
    @GetMapping("/reporte/{idCuestionario}/grupo/{idGrupo}")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> obtenerReporteGrupo(
            @PathVariable Long idCuestionario, 
            @PathVariable Long idGrupo) {
        try {
            String profesorEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
            
            return new ResponseEntity<>(
                resultadoCuestionarioService.obtenerResultadosGrupoCuestionario(
                    idCuestionario, 
                    idGrupo, 
                    profesorEmail
                ),
                HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtener resultado individual de un estudiante
     * El profesor puede ver cualquier resultado
     * PROFESOR o ADMINISTRADOR
     */
    @GetMapping("/reporte-estudiante/{idCuestionarioResuelto}")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> obtenerReporteEstudiante(@PathVariable Long idCuestionarioResuelto) {
        try {
            return new ResponseEntity<>(
                resultadoCuestionarioService.obtenerResultadoCuestionario(idCuestionarioResuelto),
                HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtener todos los cuestionarios asignados a un grupo
     * Con información resumida
     * PROFESOR o ADMINISTRADOR
     */
    @GetMapping("/reporte/grupo/{idGrupo}")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> obtenerCuestionariosGrupo(@PathVariable Long idGrupo) {
        try {
            return new ResponseEntity<>(
                resultadoCuestionarioService.obtenerPorGrupo(idGrupo),
                HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // =====================================================================================
    // BLOQUEO DE CUESTIONARIOS (PROFESOR/ADMINISTRADOR)
    // =====================================================================================

    /**
     * Bloquear/Desbloquear un cuestionario para un grupo
     * Impide que los estudiantes respondan o continúen respondiendo
     * PROFESOR o ADMINISTRADOR
     */
    @PatchMapping("/reporte/{idCuestionario}/grupo/{idGrupo}")
    @PreAuthorize("hasRole('PROFESOR') or hasRole('ADMINISTRADOR')")
    public ResponseEntity<?> toggleBloqueo(
            @PathVariable Long idCuestionario, 
            @PathVariable Long idGrupo) {
        try {
            String profesorEmail = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();
            
            resultadoCuestionarioService.toggleBloqueoCuestionario(
                idCuestionario, 
                idGrupo, 
                profesorEmail
            );
            
            return new ResponseEntity<>(
                "Estado de bloqueo del cuestionario actualizado exitosamente", 
                HttpStatus.OK
            );
        } catch (EntityNotFoundException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}