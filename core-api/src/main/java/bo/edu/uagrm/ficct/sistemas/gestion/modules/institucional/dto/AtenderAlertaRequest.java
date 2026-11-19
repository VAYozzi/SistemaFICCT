package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AtenderAlertaRequest(
    @NotBlank(message = "La resolución o acción adoptada es obligatoria (Soberanía Humana)")
    String accionTomada
) {}

