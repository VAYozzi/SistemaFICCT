package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record AsignarDocenteRequest(
    @NotNull(message = "El ID de la oferta de grupo es obligatorio")
    Long ofertaGrupoId,

    @NotNull(message = "El ID del docente es obligatorio")
    Long docenteId,

    String tipoDesignacion, // 'TITULAR', 'INTERINATO', 'SUPLENCIA'
    Integer horasSemanales,
    String resolucionFacultativa,
    LocalDate fechaInicio
) {}

