package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record CrearPeriodoRequest(
    @NotBlank(message = "El código del periodo es obligatorio (ej. 2026-1)")
    String codigo,

    @NotNull(message = "La fecha de inicio es obligatoria")
    LocalDate fechaInicio,

    @NotNull(message = "La fecha de fin es obligatoria")
    LocalDate fechaFin
) {}

