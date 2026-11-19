package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response;

import java.time.OffsetDateTime;

public record BitacoraAuditoriaResponse(
    Long id,
    Long usuarioId,
    String correoActor,
    String direccionIp,
    String modulo,
    String accion,
    String entidadAfectada,
    String entidadId,
    String datosAnteriores,
    String datosNuevos,
    OffsetDateTime fechaHora
) {}

