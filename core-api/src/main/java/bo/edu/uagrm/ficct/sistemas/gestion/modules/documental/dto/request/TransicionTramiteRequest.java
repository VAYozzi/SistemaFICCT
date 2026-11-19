package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransicionTramiteRequest(
    @NotBlank(message = "El estado nuevo es obligatorio")
    String estadoNuevo,

    String observacionODictamen
) {}

