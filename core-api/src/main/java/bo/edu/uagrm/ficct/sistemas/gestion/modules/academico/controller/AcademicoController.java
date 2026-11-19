package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.controller;

import bo.edu.uagrm.ficct.sistemas.gestion.common.aop.Auditable;
import bo.edu.uagrm.ficct.sistemas.gestion.common.dto.ApiResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.request.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.response.PrediccionCuposResponseDto;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.service.AcademicoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/academico")
@RequiredArgsConstructor
@Tag(name = "Planificación Académica y Predicción de Cupos", description = "Planes de estudio, oferta de materias, asignación docente y modelo Random Forest")
public class AcademicoController {

    private final AcademicoService academicoService;

    // --- Planes de Estudio ---
    @PostMapping("/planes")
    @Auditable(modulo = "ACADEMICO", accion = "CREATE_PLAN", entidadAfectada = "planes_estudio")
    @Operation(summary = "Crear nuevo plan de estudio", description = "Registra malla curricular oficial")
    public ResponseEntity<ApiResponse<PlanEstudio>> crearPlan(@Valid @RequestBody CrearPlanEstudioRequest request) {
        PlanEstudio plan = academicoService.crearPlanEstudio(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Plan de estudio creado", plan));
    }

    @GetMapping("/planes")
    @Operation(summary = "Listar planes de estudio", description = "Recupera todas las mallas curriculares")
    public ResponseEntity<ApiResponse<List<PlanEstudio>>> listarPlanes() {
        return ResponseEntity.ok(ApiResponse.ok("Planes recuperados", academicoService.listarPlanesEstudio()));
    }

    // --- Materias ---
    @PostMapping("/planes/{planId}/materias")
    @Auditable(modulo = "ACADEMICO", accion = "CREATE_MATERIA", entidadAfectada = "materias")
    @Operation(summary = "Agregar materia a un plan", description = "Asocia asignatura con nivel de semestre y prerrequisitos")
    public ResponseEntity<ApiResponse<Materia>> agregarMateria(
        @PathVariable Long planId,
        @Valid @RequestBody CrearMateriaRequest request
    ) {
        Materia mat = academicoService.agregarMateria(planId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Materia agregada", mat));
    }

    @GetMapping("/planes/{planId}/materias")
    @Operation(summary = "Listar materias de un plan", description = "Recupera asignaturas ordenadas por nivel semestral")
    public ResponseEntity<ApiResponse<List<Materia>>> listarMaterias(@PathVariable Long planId) {
        return ResponseEntity.ok(ApiResponse.ok("Materias del plan", academicoService.listarMateriasPorPlan(planId)));
    }

    // --- Periodos Académicos ---
    @PostMapping("/periodos")
    @Auditable(modulo = "ACADEMICO", accion = "CREATE_PERIODO", entidadAfectada = "periodos_academicos")
    @Operation(summary = "Aperturar periodo semestral", description = "Registra semestre académico")
    public ResponseEntity<ApiResponse<PeriodoAcademico>> crearPeriodo(@Valid @RequestBody CrearPeriodoRequest request) {
        PeriodoAcademico periodo = academicoService.crearPeriodo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Periodo aperturado", periodo));
    }

    @GetMapping("/periodos")
    @Operation(summary = "Listar periodos semestrales")
    public ResponseEntity<ApiResponse<List<PeriodoAcademico>>> listarPeriodos() {
        return ResponseEntity.ok(ApiResponse.ok("Periodos académicos", academicoService.listarPeriodos()));
    }

    // --- Oferta de Grupos y Cupos ---
    @PostMapping("/oferta-grupos")
    @Auditable(modulo = "ACADEMICO", accion = "CREATE_OFERTA_GRUPO", entidadAfectada = "oferta_grupos_materia")
    @Operation(summary = "Planificar oferta de grupo y cupos", description = "Abre paralelo y asigna cupo")
    public ResponseEntity<ApiResponse<OfertaGruposMateria>> crearOfertaGrupo(@Valid @RequestBody CrearOfertaGrupoRequest request) {
        OfertaGruposMateria oferta = academicoService.crearOfertaGrupo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Oferta de grupo registrada", oferta));
    }

    @GetMapping("/periodos/{periodoId}/ofertas")
    @Operation(summary = "Listar grupos ofertados en un periodo")
    public ResponseEntity<ApiResponse<List<OfertaGruposMateria>>> listarOfertasPorPeriodo(@PathVariable Long periodoId) {
        return ResponseEntity.ok(ApiResponse.ok("Oferta académica", academicoService.listarOfertasPorPeriodo(periodoId)));
    }

    // --- Cobertura Docente ---
    @PostMapping("/cobertura-docente")
    @Auditable(modulo = "ACADEMICO", accion = "ASSIGN_DOCENTE", entidadAfectada = "cobertura_carga_docente")
    @Operation(summary = "Asignar docente a un grupo ofertado")
    public ResponseEntity<ApiResponse<CoberturaCargaDocente>> asignarDocente(@Valid @RequestBody AsignarDocenteRequest request) {
        CoberturaCargaDocente cob = academicoService.asignarDocenteAGrupo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created("Docente asignado", cob));
    }

    @GetMapping("/periodos/{periodoId}/cobertura")
    @Operation(summary = "Listar cobertura docente de un periodo")
    public ResponseEntity<ApiResponse<List<CoberturaCargaDocente>>> listarCobertura(@PathVariable Long periodoId) {
        return ResponseEntity.ok(ApiResponse.ok("Cobertura docente", academicoService.listarCoberturaPorPeriodo(periodoId)));
    }

    // --- Predicciones de Cupos con Machine Learning (Random Forest) ---
    @GetMapping("/predicciones/materias/{materiaId}/periodos/{periodoId}")
    @Operation(summary = "Predecir demanda y déficit de cupos (Random Forest)", description = "Inferencia supervisada con Random Forest Classifier (K=5 F1 >= 0.80) ejecutada en FastAPI")
    public ResponseEntity<ApiResponse<PrediccionCuposResponseDto>> predecirDemanda(
        @PathVariable Long materiaId,
        @PathVariable Long periodoId
    ) {
        PrediccionCuposResponseDto pred = academicoService.predecirDemandaMateria(materiaId, periodoId);
        return ResponseEntity.ok(ApiResponse.ok("Predicción de cupos generada", pred));
    }
}

