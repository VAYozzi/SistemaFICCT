package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferirDireccionRequest(
    @NotNull(message = "El ID del nuevo director es obligatorio")
    Long nuevoDirectorUsuarioId,

    @NotBlank(message = "La resolución de respaldo es obligatoria")
    String resolucionRespaldo,

    String observaciones,

    @NotBlank(message = "La contraseña actual de confirmación es obligatoria")
    String passwordConfirmacionActual
) {}

