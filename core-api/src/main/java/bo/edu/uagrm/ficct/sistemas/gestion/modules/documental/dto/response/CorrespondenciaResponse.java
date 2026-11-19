package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response;

import java.time.OffsetDateTime;

public record CorrespondenciaResponse(
    Long id,
    String codigoCite,
    String tipo,
    String remitenteInstitucion,
    String remitentePersona,
    String destinatario,
    String referencia,
    String estado,
    String usuarioReceptorNombre,
    Long documentoAdjuntoId,
    String documentoAdjuntoNombre,
    OffsetDateTime fechaRecepcionEmision,
    OffsetDateTime creadoEn
) {}

