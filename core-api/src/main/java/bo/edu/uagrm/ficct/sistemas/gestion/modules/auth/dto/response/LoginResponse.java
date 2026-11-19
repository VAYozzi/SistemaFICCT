package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.dto.response;

public record LoginResponse(
    String token,
    String tipoToken,
    Long usuarioId,
    String nombreCompleto,
    String correoInstitucional,
    String cargo,
    String rolCodigo,
    String rolNombre
) {}

