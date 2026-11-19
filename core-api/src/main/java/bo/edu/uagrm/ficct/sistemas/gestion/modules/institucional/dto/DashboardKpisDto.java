package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.dto;

import java.math.BigDecimal;
import java.util.Map;

public record DashboardKpisDto(
    long totalEstudiantes,
    long totalTramitesEnCurso,
    long totalTituladosHistorico,
    long totalAlertasPendientes,
    long alertasRojasPendientes,
    Map<String, Long> tituladosPorModalidad,
    BigDecimal presupuestoPoaTotal,
    BigDecimal porcentajeAvancePoaPromedio
) {}

