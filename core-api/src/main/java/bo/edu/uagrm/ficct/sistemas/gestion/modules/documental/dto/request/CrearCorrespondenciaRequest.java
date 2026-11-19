package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CrearCorrespondenciaRequest(
    @NotBlank(message = "El código CITE es obligatorio")
    String codigoCite,

    @NotBlank(message = "El tipo de correspondencia es obligatorio (ENTRANTE/SALIENTE/INTERNO)")
    String tipo,

    @NotBlank(message = "La institución remitente es obligatoria")
    String remitenteInstitucion,

    @NotBlank(message = "El nombre del remitente es obligatorio")
    String remitentePersona,

    @NotBlank(message = "El destinatario es obligatorio")
    String destinatario,

    @NotBlank(message = "La referencia es obligatoria")
    String referencia,

    String observaciones,
    Long documentoAdjuntoId
) {}

