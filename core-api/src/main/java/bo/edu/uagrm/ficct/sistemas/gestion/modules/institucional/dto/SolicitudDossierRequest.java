package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.dto;

import java.time.LocalDate;
import java.util.List;

public record SolicitudDossierRequest(
    String periodoCodigo,
    LocalDate fechaDesde,
    LocalDate fechaHasta,
    List<String> incluirModulos // 'CORRESPONDENCIA', 'TRAMITES', 'ALERTAS', 'POA'
) {}

