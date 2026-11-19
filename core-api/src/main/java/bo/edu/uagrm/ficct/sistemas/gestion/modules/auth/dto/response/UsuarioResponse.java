package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response;

import java.time.OffsetDateTime;

public record UsuarioResponse(
    Long id,
    String nombreCompleto,
    String correoInstitucional,
    String cargo,
    String rolCodigo,
    String rolNombre,
    Boolean activo,
    OffsetDateTime ultimoAcceso,
    OffsetDateTime creadoEn
) {}

