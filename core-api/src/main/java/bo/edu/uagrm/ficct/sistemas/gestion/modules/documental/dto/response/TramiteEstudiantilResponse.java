package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

public record TramiteEstudiantilResponse(
    Long id,
    String codigoTramite,
    Long expedienteId,
    String estudianteRegistro,
    String estudianteNombreCompleto,
    String tipoTramiteCodigo,
    String tipoTramiteNombre,
    String estadoActual,
    String dictamenDirector,
    String dictamenCoordinador,
    OffsetDateTime fechaIngreso,
    OffsetDateTime fechaConclusion,
    List<HistorialEstadoResponse> historial
) {
    public record HistorialEstadoResponse(
        Long id,
        String estadoAnterior,
        String estadoNuevo,
        String usuarioNombre,
        String rolEnMomento,
        String observacionODictamen,
        OffsetDateTime fechaHora
    ) {}
}

