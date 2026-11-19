package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response;

import java.time.OffsetDateTime;
import java.util.UUID;

public record InvitacionResponse(
    Long id,
    String correoDestino,
    Long rolAsignadoId,
    String rolAsignadoNombre,
    UUID tokenInvitacion,
    String creadoPorNombre,
    OffsetDateTime fechaExpiracion,
    String estado,
    OffsetDateTime creadoEn
) {}

