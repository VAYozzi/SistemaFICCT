package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.dto;

public record DossierGeneradoResponse(
    String nombreArchivo,
    String rutaAlmacenamiento,
    String hashSha256,
    Long documentoId,
    String mensaje
) {}

