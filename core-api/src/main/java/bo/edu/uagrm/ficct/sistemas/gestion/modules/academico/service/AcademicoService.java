package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.service;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.request.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.response.PrediccionCuposResponseDto;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model.*;
import java.util.List;

public interface AcademicoService {

    PlanEstudio crearPlanEstudio(CrearPlanEstudioRequest request);
    List<PlanEstudio> listarPlanesEstudio();

    Materia agregarMateria(Long planId, CrearMateriaRequest request);
    List<Materia> listarMateriasPorPlan(Long planId);

    PeriodoAcademico crearPeriodo(CrearPeriodoRequest request);
    List<PeriodoAcademico> listarPeriodos();

    OfertaGruposMateria crearOfertaGrupo(CrearOfertaGrupoRequest request);
    List<OfertaGruposMateria> listarOfertasPorPeriodo(Long periodoId);

    CoberturaCargaDocente asignarDocenteAGrupo(AsignarDocenteRequest request);
    List<CoberturaCargaDocente> listarCoberturaPorPeriodo(Long periodoId);

    PrediccionCuposResponseDto predecirDemandaMateria(Long materiaId, Long periodoId);
}

