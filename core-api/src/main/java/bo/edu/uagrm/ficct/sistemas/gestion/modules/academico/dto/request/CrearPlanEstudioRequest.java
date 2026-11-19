package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearPlanEstudioRequest(
    @NotBlank(message = "El código del plan es obligatorio (ej. 187-3)")
    String codigoPlan,

    @NotBlank(message = "El nombre del plan es obligatorio")
    String nombre,

    @NotNull(message = "El año de vigencia es obligatorio")
    Integer anioVigencia
) {}

