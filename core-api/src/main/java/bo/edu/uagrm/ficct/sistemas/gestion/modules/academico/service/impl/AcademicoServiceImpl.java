package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.service.impl;

import bo.edu.uagrm.ficct.sistemas.gestion.common.exception.BusinessRuleException;
import bo.edu.uagrm.ficct.sistemas.gestion.common.exception.ResourceNotFoundException;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.request.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.response.PrediccionCuposResponseDto;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.repository.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.service.AcademicoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class AcademicoServiceImpl implements AcademicoService {

    private final PlanEstudioRepository planRepository;
    private final MateriaRepository materiaRepository;
    private final PeriodoAcademicoRepository periodoRepository;
    private final DocenteRepository docenteRepository;
    private final OfertaGruposMateriaRepository ofertaRepository;
    private final CoberturaCargaDocenteRepository coberturaRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.worker.url:http://127.0.0.1:8000}")
    private String aiWorkerUrl;

    @Value("${ai.worker.internal-key:ficct_super_secret_internal_key_2026}")
    private String internalApiKey;

    @Override
    @Transactional
    public PlanEstudio crearPlanEstudio(CrearPlanEstudioRequest request) {
        if (planRepository.findByCodigoPlan(request.codigoPlan()).isPresent()) {
            throw new BusinessRuleException("Ya existe un plan de estudio con el código: " + request.codigoPlan());
        }

        PlanEstudio plan = PlanEstudio.builder()
            .codigoPlan(request.codigoPlan())
            .nombre(request.nombre())
            .anioVigencia(request.anioVigencia())
            .activo(true)
            .build();

        return planRepository.save(plan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanEstudio> listarPlanesEstudio() {
        return planRepository.findAll();
    }

    @Override
    @Transactional
    public Materia agregarMateria(Long planId, CrearMateriaRequest request) {
        PlanEstudio plan = planRepository.findById(planId)
            .orElseThrow(() -> new ResourceNotFoundException("Plan de estudio no encontrado con ID: " + planId));

        if (materiaRepository.findByPlanEstudioIdAndSigla(planId, request.sigla()).isPresent()) {
            throw new BusinessRuleException("La sigla " + request.sigla() + " ya existe en este plan de estudios");
        }

        Materia materia = Materia.builder()
            .planEstudio(plan)
            .sigla(request.sigla().toUpperCase())
            .nombre(request.nombre())
            .semestreNivel(request.semestreNivel())
            .creditos(request.creditos())
            .build();

        if (request.prerrequisitosIds() != null && !request.prerrequisitosIds().isEmpty()) {
            List<Materia> prerreqs = materiaRepository.findAllById(request.prerrequisitosIds());
            materia.setPrerrequisitos(new HashSet<>(prerreqs));
        }

        return materiaRepository.save(materia);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Materia> listarMateriasPorPlan(Long planId) {
        return materiaRepository.findByPlanEstudioIdOrderBySemestreNivelAscSiglaAsc(planId);
    }

    @Override
    @Transactional
    public PeriodoAcademico crearPeriodo(CrearPeriodoRequest request) {
        if (periodoRepository.findByCodigo(request.codigo()).isPresent()) {
            throw new BusinessRuleException("El periodo " + request.codigo() + " ya existe");
        }

        PeriodoAcademico periodo = PeriodoAcademico.builder()
            .codigo(request.codigo())
            .fechaInicio(request.fechaInicio())
            .fechaFin(request.fechaFin())
            .estado("PLANIFICACION")
            .build();

        return periodoRepository.save(periodo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PeriodoAcademico> listarPeriodos() {
        return periodoRepository.findAll();
    }

    @Override
    @Transactional
    public OfertaGruposMateria crearOfertaGrupo(CrearOfertaGrupoRequest request) {
        PeriodoAcademico periodo = periodoRepository.findById(request.periodoId())
            .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado con ID: " + request.periodoId()));

        Materia materia = materiaRepository.findById(request.materiaId())
            .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada con ID: " + request.materiaId()));

        if (ofertaRepository.findByPeriodoIdAndMateriaIdAndCodigoGrupo(periodo.getId(), materia.getId(), request.codigoGrupo()).isPresent()) {
            throw new BusinessRuleException("Ya existe el grupo " + request.codigoGrupo() + " para esta materia en el periodo indicado");
        }

        OfertaGruposMateria oferta = OfertaGruposMateria.builder()
            .periodo(periodo)
            .materia(materia)
            .codigoGrupo(request.codigoGrupo().toUpperCase())
            .cupoOfertado(request.cupoOfertado())
            .inscritosFinales(request.inscritosFinales() != null ? request.inscritosFinales() : 0)
            .demandaInsatisfechaEstimada(request.demandaInsatisfechaEstimada() != null ? request.demandaInsatisfechaEstimada() : 0)
            .aprobados(request.aprobados() != null ? request.aprobados() : 0)
            .reprobados(request.reprobados() != null ? request.reprobados() : 0)
            .abandonos(request.abandonos() != null ? request.abandonos() : 0)
            .build();

        return ofertaRepository.save(oferta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OfertaGruposMateria> listarOfertasPorPeriodo(Long periodoId) {
        return ofertaRepository.findByPeriodoId(periodoId);
    }

    @Override
    @Transactional
    public CoberturaCargaDocente asignarDocenteAGrupo(AsignarDocenteRequest request) {
        OfertaGruposMateria oferta = ofertaRepository.findById(request.ofertaGrupoId())
            .orElseThrow(() -> new ResourceNotFoundException("Oferta de grupo no encontrada con ID: " + request.ofertaGrupoId()));

        Docente docente = docenteRepository.findById(request.docenteId())
            .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con ID: " + request.docenteId()));

        CoberturaCargaDocente cobertura = CoberturaCargaDocente.builder()
            .ofertaGrupo(oferta)
            .docente(docente)
            .tipoDesignacion(request.tipoDesignacion() != null ? request.tipoDesignacion() : "TITULAR")
            .horasSemanales(request.horasSemanales() != null ? request.horasSemanales() : 4)
            .resolucionFacultativa(request.resolucionFacultativa())
            .fechaInicio(request.fechaInicio() != null ? request.fechaInicio() : LocalDate.now())
            .estado("VIGENTE")
            .build();

        return coberturaRepository.save(cobertura);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CoberturaCargaDocente> listarCoberturaPorPeriodo(Long periodoId) {
        return coberturaRepository.findByOfertaGrupoPeriodoId(periodoId);
    }

    @Override
    @Transactional(readOnly = true)
    public PrediccionCuposResponseDto predecirDemandaMateria(Long materiaId, Long periodoId) {
        Materia materia = materiaRepository.findById(materiaId)
            .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada"));
        PeriodoAcademico periodo = periodoRepository.findById(periodoId)
            .orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado"));

        List<OfertaGruposMateria> ofertas = ofertaRepository.findByPeriodoIdAndMateriaId(periodoId, materiaId);
        int totalCuposOfertados = ofertas.stream().mapToInt(OfertaGruposMateria::getCupoOfertado).sum();
        if (totalCuposOfertados == 0) {
            totalCuposOfertados = 80; // Valor nominal base si no hay grupos registrados
        }

        // Extracción de métricas para Features del modelo Random Forest
        double tasaReprobacion = 0.35;
        double tasaAbandono = 0.10;
        double demandaInsatisfechaProm = 25.0;
        int estudiantesHabilitados = 180;

        if (!ofertas.isEmpty()) {
            int totalInscritos = ofertas.stream().mapToInt(OfertaGruposMateria::getInscritosFinales).sum();
            int totalReprobados = ofertas.stream().mapToInt(OfertaGruposMateria::getReprobados).sum();
            int totalAbandonos = ofertas.stream().mapToInt(OfertaGruposMateria::getAbandonos).sum();
            int totalDemandaIns = ofertas.stream().mapToInt(OfertaGruposMateria::getDemandaInsatisfechaEstimada).sum();

            if (totalInscritos > 0) {
                tasaReprobacion = (double) totalReprobados / totalInscritos;
                tasaAbandono = (double) totalAbandonos / totalInscritos;
            }
            if (totalDemandaIns > 0) {
                demandaInsatisfechaProm = totalDemandaIns;
            }
        }

        // Invocación a FastAPI
        String url = aiWorkerUrl + "/internal/v1/ml/predict-cupos";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Api-Key", internalApiKey);

        Map<String, Object> payload = Map.of(
            "materia_sigla", materia.getSigla(),
            "demanda_insatisfecha_prom", demandaInsatisfechaProm,
            "tasa_reprobacion_historica", tasaReprobacion,
            "tasa_abandono_historica", tasaAbandono,
            "estudiantes_habilitados_prerreq", estudiantesHabilitados,
            "cupos_ofertados_planificados", totalCuposOfertados
        );

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map body = response.getBody();
                return new PrediccionCuposResponseDto(
                    (String) body.get("materia_sigla"),
                    BigDecimal.valueOf(((Number) body.get("probabilidad_sobredemanda")).doubleValue()),
                    (String) body.get("nivel_alerta"),
                    ((Number) body.get("cupos_ofertados")).intValue(),
                    ((Number) body.get("demanda_proyectada")).intValue(),
                    ((Number) body.get("deficit_estimado")).intValue(),
                    (Boolean) body.get("es_datos_prueba"),
                    (String) body.get("aviso_institucional")
                );
            } else {
                throw new BusinessRuleException("Error al comunicarse con el motor predictivo");
            }
        } catch (Exception ex) {
            log.error("Fallo al predecir cupos con FastAPI", ex);
            throw new BusinessRuleException("Fallo en inferencia predictiva: " + ex.getMessage());
        }
    }
}

