package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "El correo institucional es obligatorio")
    @Email(message = "El formato de correo institucional es inválido")
    String correoInstitucional,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}

