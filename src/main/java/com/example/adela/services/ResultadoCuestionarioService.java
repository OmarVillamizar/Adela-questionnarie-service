package com.example.adela.services;

import com.example.adela.dto.*;
import com.example.adela.entities.*;
import com.example.adela.repositories.*;
import com.example.adela.clients.GroupClient;
import com.example.adela.clients.UsuarioClient;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ResultadoCuestionarioService {

    private final ResultadoCuestionarioRepository resultadoCuestionarioRepository;
    private final ResultadoPreguntaRepository resultadoPreguntaRepository;
    private final CuestionarioRepository cuestionarioRepository;
    private final PreguntaRepository preguntaRepository;
    private final OpcionRepository opcionRepository;
    private final UsuarioClient usuarioClient;
    private final GroupClient groupClient;

    // =====================================================================================
    // RESPONDER CUESTIONARIO
    // =====================================================================================
    @Transactional
    public ResultadoCuestionario responderCuestionario(RespuestaCuestionarioDTO info, String estudianteEmail) {
        Long cuestionarioId = info.getCuestionarioId();
        Cuestionario cuestionario = cuestionarioRepository.findById(cuestionarioId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el cuestionario con id " + cuestionarioId));

        ResultadoCuestionario resC = resultadoCuestionarioRepository
                .findByCuestionarioAndEstudianteEmailAndFechaResolucionIsNull(cuestionario, estudianteEmail)
                .orElseThrow(() -> new EntityNotFoundException("Al estudiante " + estudianteEmail
                        + " no se le fue asignado el cuestionario " + cuestionario.getId()));

        if (resC.isBloqueado()) {
            throw new RuntimeException("Este cuestionario está bloqueado y no se puede responder.");
        }

        resC.setFechaResolucion(Date.valueOf(LocalDate.now()));
        resC = resultadoCuestionarioRepository.save(resC);

        List<ResultadoPregunta> resultadoPreguntas = new LinkedList<>();
        List<Pregunta> preguntas = preguntaRepository.findByCuestionario(cuestionario);
        Map<Long, Pregunta> answered = new TreeMap<>();
        Map<Long, Pregunta> unAnswered = new TreeMap<>();

        for (Pregunta pregunta : preguntas) {
            if (!pregunta.isOpcionMultiple()) {
                unAnswered.put(pregunta.getId(), pregunta);
            }
        }

        for (Long opcionId : info.getOpcionesSeleccionadasId()) {
            ResultadoPregunta rp = responderPregunta(opcionId, resC);
            Long preguntaId = rp.getOpcion().getPregunta().getId();
            if (answered.containsKey(preguntaId) && !rp.getOpcion().getPregunta().isOpcionMultiple()) {
                throw new RuntimeException("La pregunta " + answered.get(preguntaId).getOrden()
                        + " tuvo mas de una opcion seleccionada(" + rp.getOpcion().getOrden() + ").");
            }
            answered.put(preguntaId, rp.getOpcion().getPregunta());
            unAnswered.remove(preguntaId);
            resultadoPreguntas.add(rp);
        }

        if (!unAnswered.isEmpty()) {
            StringBuilder result = new StringBuilder();
            for (Pregunta value : unAnswered.values()) {
                result.append(value.getOrden());
                result.append(", ");
            }
            if (result.length() > 0) {
                result.setLength(result.length() - 2);
            }
            throw new RuntimeException("Las preguntas " + result + " no fueron respondidas");
        }

        resultadoPreguntaRepository.saveAll(resultadoPreguntas);
        return resultadoCuestionarioRepository.save(resC);
    }

    // =====================================================================================
    // RESPONDER PREGUNTA
    // =====================================================================================
    public ResultadoPregunta responderPregunta(Long opcionId, ResultadoCuestionario resC) {
        Opcion opcion = opcionRepository.findById(opcionId)
                .orElseThrow(() -> new EntityNotFoundException("No existe la opción " + opcionId));
        Pregunta pregunta = opcion.getPregunta();
        Cuestionario cuestionario = resC.getCuestionario();
        if (pregunta.getCuestionario().getId() != cuestionario.getId()) {
            throw new RuntimeException("Inconsistencia: la opcion de id " + opcionId + " no pertenece al cuestionario "
                    + cuestionario.getId());
        }
        ResultadoPregunta rp = new ResultadoPregunta();
        rp.setResultadoCuestionario(resC);
        rp.setOpcion(opcion);
        return rp;
    }

    // =====================================================================================
    // OBTENER CUESTIONARIOS POR GRUPO
    // =====================================================================================
    public List<Cuestionario> obtenerCuestionariosPorGrupo(Long grupoId) {
        // Verificar que el grupo existe
        GrupoDTO grupo = groupClient.obtenerGrupoPorId(grupoId);
        
        List<ResultadoCuestionario> resultados = resultadoCuestionarioRepository.findByGrupoId(grupoId);
        
        Set<Cuestionario> cuestionarios = new TreeSet<>((c1, c2) -> c1.getId().compareTo(c2.getId()));
        for (ResultadoCuestionario rc : resultados) {
            cuestionarios.add(rc.getCuestionario());
        }
        
        return new LinkedList<>(cuestionarios);
    }

    // =====================================================================================
    // VERIFICAR EXISTENCIA DE ASIGNACIÓN
    // =====================================================================================
    public boolean existeAsignacion(String estudianteEmail, Cuestionario cuestionario) {
        return resultadoCuestionarioRepository
            .findByCuestionarioAndEstudianteEmailAndFechaResolucionIsNull(cuestionario, estudianteEmail)
            .isPresent();
    }

    // =====================================================================================
    // ASIGNAR CUESTIONARIOS NUEVOS ESTUDIANTES
    // =====================================================================================
    @Transactional
    public void asignarCuestionariosAsignadosAlGrupoAEstudiantesNuevos(Long grupoId, Set<String> nuevosEstudiantesEmails) {
        List<ResultadoCuestionario> asignacionesExistentes = resultadoCuestionarioRepository.findByGrupoId(grupoId);
        
        if (asignacionesExistentes.isEmpty()) {
            return;
        }
        
        Map<Cuestionario, Date> cuestionariosConFecha = asignacionesExistentes.stream()
            .collect(Collectors.toMap(
                ResultadoCuestionario::getCuestionario,
                ResultadoCuestionario::getFechaAplicacion,
                (existing, replacement) -> existing
            ));
        
        for (String estudianteEmail : nuevosEstudiantesEmails) {
            for (Map.Entry<Cuestionario, Date> entry : cuestionariosConFecha.entrySet()) {
                Cuestionario cuestionario = entry.getKey();
                Date fechaAplicacion = entry.getValue();
                
                Optional<ResultadoCuestionario> existente = resultadoCuestionarioRepository
                    .findByCuestionarioAndEstudianteEmailAndGrupoId(cuestionario, estudianteEmail, grupoId);
                    
                if (existente.isEmpty()) {
                    ResultadoCuestionario nuevo = new ResultadoCuestionario();
                    nuevo.setEstudianteEmail(estudianteEmail);
                    nuevo.setCuestionario(cuestionario);
                    nuevo.setGrupoId(grupoId);
                    nuevo.setFechaResolucion(null);
                    nuevo.setBloqueado(false);
                    nuevo.setFechaAplicacion(fechaAplicacion);
                    
                    resultadoCuestionarioRepository.save(nuevo);
                }
            }
        }
    }

    // =====================================================================================
    // ASIGNAR CUESTIONARIO A GRUPO
    // =====================================================================================
    @Transactional
    public void asignarCuestionarioAGrupo(Long cuestionarioId, Long grupoId) {
        Cuestionario cuestionario = cuestionarioRepository.findById(cuestionarioId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el cuestionario con id " + cuestionarioId));
        
        GrupoDTO grupo = groupClient.obtenerGrupoPorId(grupoId);
        List<EstudianteDTO> estudiantes = groupClient.obtenerEstudiantesPorGrupo(grupoId);
        
        List<ResultadoCuestionario> asignaciones = new LinkedList<>();
        for (EstudianteDTO estudiante : estudiantes) {
            ResultadoCuestionario rc = new ResultadoCuestionario();
            rc.setCuestionario(cuestionario);
            rc.setEstudianteEmail(estudiante.getEmail());
            rc.setFechaAplicacion(Date.valueOf(LocalDate.now()));
            rc.setGrupoId(grupoId);
            rc.setBloqueado(false);
            asignaciones.add(rc);
        }
        resultadoCuestionarioRepository.saveAll(asignaciones);
    }

    // =====================================================================================
    // ASIGNAR CUESTIONARIO A ESTUDIANTE
    // =====================================================================================
    @Transactional
    public void asignarCuestionarioAEstudiante(Long cuestionarioId, String estudianteEmail) {
        Cuestionario cuestionario = cuestionarioRepository.findById(cuestionarioId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el cuestionario con id " + cuestionarioId));
        
        EstudianteDTO estudiante = usuarioClient.obtenerEstudiante(estudianteEmail);
        
        if (resultadoCuestionarioRepository
                .findByCuestionarioAndEstudianteEmailAndFechaResolucionIsNull(cuestionario, estudianteEmail).isEmpty()) {
            ResultadoCuestionario rc = new ResultadoCuestionario();
            rc.setCuestionario(cuestionario);
            rc.setEstudianteEmail(estudianteEmail);
            rc.setFechaAplicacion(Date.valueOf(LocalDate.now()));
            rc.setBloqueado(false);
            resultadoCuestionarioRepository.save(rc);
        }
    }

    // =====================================================================================
    // OBTENER CUESTIONARIOS DEL ESTUDIANTE
    // =====================================================================================
    public ListasCuestionariosDTO obtenerCuestionarios(String estudianteEmail) {
        List<ResultadoCuestionario> info = resultadoCuestionarioRepository
                .findByEstudianteEmailAndBloqueadoFalse(estudianteEmail);
        
        List<ResultadoCuestionarioDTO> pendientes = new LinkedList<>();
        List<ResultadoCuestionarioDTO> resueltos = new LinkedList<>();
        
        // Cargar datos del estudiante una vez
        EstudianteDTO estudiante = usuarioClient.obtenerEstudiante(estudianteEmail);
        
        // Cargar grupos únicos para evitar llamadas repetidas
        Map<Long, GrupoResumidoDTO> gruposCache = new HashMap<>();
        
        for (ResultadoCuestionario rc : info) {
            GrupoResumidoDTO grupo = null;
            if (rc.getGrupoId() != null) {
                if (!gruposCache.containsKey(rc.getGrupoId())) {
                    try {
                        GrupoDTO grupoDTO = groupClient.obtenerGrupoPorId(rc.getGrupoId());
                        gruposCache.put(rc.getGrupoId(), GrupoResumidoDTO.from(grupoDTO));
                    } catch (Exception e) {
                        // Si falla, crear básico
                        GrupoResumidoDTO grupoBasico = new GrupoResumidoDTO();
                        grupoBasico.setId(rc.getGrupoId());
                        gruposCache.put(rc.getGrupoId(), grupoBasico);
                    }
                }
                grupo = gruposCache.get(rc.getGrupoId());
            }
            
            ResultadoCuestionarioDTO rcdto = ResultadoCuestionarioDTO.from(rc, estudiante, grupo);
            
            if (rc.getFechaResolucion() == null) {
                pendientes.add(rcdto);
            } else {
                resueltos.add(rcdto);
            }
        }
        
        ListasCuestionariosDTO lcdto = new ListasCuestionariosDTO();
        lcdto.setPendientes(pendientes);
        lcdto.setResueltos(resueltos);
        
        return lcdto;
    }

    // =====================================================================================
    // OBTENER RESULTADO CUESTIONARIO
    // =====================================================================================
    public ResultCuestCompletoDTO obtenerResultadoCuestionario(Long cuestionarioResueltoId) {
        ResultadoCuestionario resC = resultadoCuestionarioRepository.findById(cuestionarioResueltoId).orElseThrow(
                () -> new EntityNotFoundException("El resultado de id " + cuestionarioResueltoId + " no existe"));
        return obtenerResultadoCuestionario(cuestionarioResueltoId, resC.getEstudianteEmail());
    }

    public ResultCuestCompletoDTO obtenerResultadoCuestionario(Long cuestionarioResueltoId, String estudianteEmail) {
        ResultCuestCompletoDTO res = new ResultCuestCompletoDTO();
        
        ResultadoCuestionario resC = resultadoCuestionarioRepository.findById(cuestionarioResueltoId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "El resultado de id " + cuestionarioResueltoId + " no pertenece al estudiante o no existe"));
        
        if (resC.getFechaResolucion() == null) {
            throw new EntityNotFoundException("El id " + cuestionarioResueltoId
                    + " corresponde a una aplicación de un cuestionario que no se ha completado");
        }
        
        Cuestionario c = resC.getCuestionario();
        res.setCuestionario(CuestionarioResumidoDTO.from(c));
        
        // Cargar estudiante desde ms-auth
        EstudianteDTO estudiante = usuarioClient.obtenerEstudiante(resC.getEstudianteEmail());
        res.setEstudiante(estudiante);
        
        // Cargar grupo desde ms-grupos si existe
        if (resC.getGrupoId() != null) {
            try {
                GrupoDTO grupo = groupClient.obtenerGrupoPorId(resC.getGrupoId());
                res.setGrupo(GrupoResumidoDTO.from(grupo));
            } catch (Exception e) {
                // Grupo opcional, continuar sin él
            }
        }
        
        res.setFechaAplicacion(resC.getFechaAplicacion());
        res.setFechaResolucion(resC.getFechaResolucion());
        res.setId(resC.getId());
        
        Map<Long, CategoriaResultadoDTO> mp = new TreeMap<>();
        Map<Long, PreguntaResueltaDTO> preg = new TreeMap<>();
        List<CategoriaResultadoDTO> categorias = new LinkedList<>();
        
        for (Categoria categoria : c.getCategorias()) {
            CategoriaResultadoDTO cr = new CategoriaResultadoDTO();
            cr.setNombre(categoria.getNombre());
            cr.setValor(0d);
            cr.setValorMaximo(categoria.getValorMaximo());
            cr.setValorMinimo(categoria.getValorMinimo());
            mp.put(categoria.getId(), cr);
            categorias.add(cr);
        }
        
        for (ResultadoPregunta rep : resC.getPreguntas()) {
            Opcion o = rep.getOpcion();
            Pregunta p = o.getPregunta();
            PreguntaResueltaDTO pr = new PreguntaResueltaDTO();
            if (preg.containsKey(p.getId())) {
                pr = preg.get(p.getId());
            } else {
                pr.setPregunta(p.getPregunta());
                pr.setRespuestas(new LinkedList<String>());
                pr.setOrden(p.getOrden());
            }
            pr.getRespuestas().add(o.getRespuesta());
            CategoriaResultadoDTO cr = mp.get(o.getCategoria().getId());
            cr.setValor(cr.getValor() + o.getValor());
            preg.put(p.getId(), pr);
        }
        
        for (Pregunta p : c.getPreguntas()) {
            if (!preg.containsKey(p.getId())) {
                PreguntaResueltaDTO pr = new PreguntaResueltaDTO();
                pr.setOrden(p.getOrden());
                pr.setPregunta(p.getPregunta());
                pr.setRespuestas(new LinkedList<>());
                preg.put(p.getId(), pr);
            }
        }
        
        res.setCategorias(categorias);
        res.setPreguntas(new LinkedList<>(preg.values()));
        
        return res;
    }

    // =====================================================================================
    // TOGGLE BLOQUEO CUESTIONARIO
    // =====================================================================================
    @Transactional
    public void toggleBloqueoCuestionario(Long cuestionarioId, Long grupoId, String profesorEmail) {
        Cuestionario cuestionario = cuestionarioRepository.findById(cuestionarioId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el cuestionario con id " + cuestionarioId));
        
        GrupoDTO grupo = groupClient.obtenerGrupoPorId(grupoId);
        
        if (!grupo.getProfesorEmail().equalsIgnoreCase(profesorEmail)) {
            throw new EntityNotFoundException("El grupo no pertenece a este profesor.");
        }
        
        List<ResultadoCuestionario> rcs = resultadoCuestionarioRepository.findByGrupoIdAndCuestionario(grupoId, cuestionario);
        
        for (ResultadoCuestionario rc : rcs) {
            rc.setBloqueado(!rc.isBloqueado());
        }
        
        resultadoCuestionarioRepository.saveAll(rcs);
    }

    // =====================================================================================
    // OBTENER RESULTADOS GRUPO CUESTIONARIO
    // =====================================================================================
    public ResultadoGrupoDTO obtenerResultadosGrupoCuestionario(Long cuestionarioId, Long grupoId, String profesorEmail) {
        Cuestionario cuestionario = cuestionarioRepository.findById(cuestionarioId)
                .orElseThrow(() -> new EntityNotFoundException("No existe el cuestionario con id " + cuestionarioId));
        
        GrupoDTO grupo = groupClient.obtenerGrupoPorId(grupoId);
        
        if (!grupo.getProfesorEmail().equalsIgnoreCase(profesorEmail)) {
            throw new EntityNotFoundException("El grupo no pertenece a este profesor.");
        }
        
        List<ResultadoCuestionario> rcs = resultadoCuestionarioRepository.findByGrupoIdAndCuestionario(grupoId, cuestionario);
        
        if (rcs.size() == 0) {
            throw new EntityNotFoundException("Este cuestionario no ha sido asignado a ningun estudiante.");
        }
        
        int cnt = 0;
        ResultadoGrupoDTO res = new ResultadoGrupoDTO();
        
        res.setCuestionario(CuestionarioResumidoDTO.from(cuestionario));
        res.setGrupo(GrupoResumidoDTO.from(grupo));
        
        Map<Long, CategoriaResultadoDTO> mp = new TreeMap<>();
        List<CategoriaResultadoDTO> categorias = new LinkedList<>();
        List<ResultadoCuestionarioDTO> estudiantesS = new LinkedList<>();
        List<ResultadoCuestionarioDTO> estudiantesUS = new LinkedList<>();
        
        res.setFechaAplicacion(rcs.get(0).getFechaAplicacion());
        
        for (Categoria categoria : cuestionario.getCategorias()) {
            CategoriaResultadoDTO cr = new CategoriaResultadoDTO();
            cr.setNombre(categoria.getNombre());
            cr.setValor(0d);
            cr.setValorMaximo(categoria.getValorMaximo());
            cr.setValorMinimo(categoria.getValorMinimo());
            mp.put(categoria.getId(), cr);
            categorias.add(cr);
        }
        
        // Cargar estudiantes de una vez para optimizar
        Map<String, EstudianteDTO> estudiantesCache = new HashMap<>();
        for (ResultadoCuestionario rc : rcs) {
            if (!estudiantesCache.containsKey(rc.getEstudianteEmail())) {
                try {
                    EstudianteDTO est = usuarioClient.obtenerEstudiante(rc.getEstudianteEmail());
                    estudiantesCache.put(rc.getEstudianteEmail(), est);
                } catch (Exception e) {
                    // Crear básico si falla
                    EstudianteDTO estBasico = new EstudianteDTO();
                    estBasico.setEmail(rc.getEstudianteEmail());
                    estudiantesCache.put(rc.getEstudianteEmail(), estBasico);
                }
            }
        }
        
        GrupoResumidoDTO grupoResumido = GrupoResumidoDTO.from(grupo);
        
        for (ResultadoCuestionario rc : rcs) {
            EstudianteDTO estudiante = estudiantesCache.get(rc.getEstudianteEmail());
            
            if (rc.getFechaResolucion() != null) {
                cnt++;
                for (ResultadoPregunta rp : rc.getPreguntas()) {
                    Opcion o = rp.getOpcion();
                    Categoria c = o.getCategoria();
                    CategoriaResultadoDTO crdto = mp.get(c.getId());
                    crdto.setValor(crdto.getValor() + o.getValor());
                }
                estudiantesS.add(ResultadoCuestionarioDTO.from(rc, estudiante, grupoResumido));
            } else {
                estudiantesUS.add(ResultadoCuestionarioDTO.from(rc, estudiante, grupoResumido));
            }
        }
        
        for (CategoriaResultadoDTO rca : mp.values()) {
            rca.setValor(rca.getValor() / Double.valueOf(cnt));
        }
        
        res.setCategorias(categorias);
        res.setEstudiantesResuelto(estudiantesS);
        res.setEstudiantesNoResuelto(estudiantesUS);
        
        return res;
    }

    // =====================================================================================
    // OBTENER POR GRUPO
    // =====================================================================================
    public List<ResultadoGrupoResumidoDTO> obtenerPorGrupo(Long grupoId) {
        GrupoDTO grupoDTO = groupClient.obtenerGrupoPorId(grupoId);
        GrupoResumidoDTO grupo = GrupoResumidoDTO.from(grupoDTO);
        
        List<ResultadoCuestionario> cuestos = resultadoCuestionarioRepository.findByGrupoId(grupoId);
        
        List<ResultadoGrupoResumidoDTO> res = new LinkedList<>();
        
        Set<ResultadoCuestionario> dif = new TreeSet<>(new Comparator<ResultadoCuestionario>() {
            @Override
            public int compare(ResultadoCuestionario a, ResultadoCuestionario b) {
                return a.getCuestionario().getId().compareTo(b.getCuestionario().getId());
            }
        });
        
        for (ResultadoCuestionario rc : cuestos) {
            dif.add(rc);
        }
        
        for (ResultadoCuestionario rc : dif) {
            res.add(ResultadoGrupoResumidoDTO.from(rc, grupo));
        }
        
        return res;
    }
}