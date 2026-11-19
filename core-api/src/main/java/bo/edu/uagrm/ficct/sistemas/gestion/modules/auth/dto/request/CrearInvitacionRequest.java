package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearInvitacionRequest(
    @NotBlank(message = "El correo de destino es obligatorio")
    @Email(message = "El formato del correo es inválido")
    String correoDestino,

    @NotNull(message = "El ID del rol a asignar es obligatorio")
    Long rolId
) {}

