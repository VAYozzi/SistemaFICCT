package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record AceptarInvitacionRequest(
    @NotNull(message = "El token de invitación es obligatorio")
    UUID tokenInvitacion,

    @NotBlank(message = "El nombre completo es obligatorio")
    String nombreCompleto,

    @NotBlank(message = "El cargo es obligatorio")
    String cargo,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}

