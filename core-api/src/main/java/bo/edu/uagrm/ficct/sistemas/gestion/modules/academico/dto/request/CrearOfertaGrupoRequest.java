package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearOfertaGrupoRequest(
    @NotNull(message = "El ID del periodo es obligatorio")
    Long periodoId,

    @NotNull(message = "El ID de la materia es obligatorio")
    Long materiaId,

    @NotBlank(message = "El código de grupo es obligatorio (ej. SA)")
    String codigoGrupo,

    @NotNull(message = "El cupo ofertado es obligatorio")
    Integer cupoOfertado,

    Integer inscritosFinales,
    Integer demandaInsatisfechaEstimada,
    Integer aprobados,
    Integer reprobados,
    Integer abandonos
) {}

