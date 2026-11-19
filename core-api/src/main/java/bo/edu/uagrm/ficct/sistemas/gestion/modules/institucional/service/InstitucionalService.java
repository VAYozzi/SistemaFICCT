package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.service;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.dto.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model.*;
import java.util.List;

public interface InstitucionalService {

    Titulado registrarTitulado(RegistrarTituladoRequest request);
    List<Titulado> listarTitulados();

    List<Alerta> listarAlertas(String estado, String nivelCriticidad);
    Alerta crearAlerta(Alerta alerta);
    Alerta atenderAlerta(Long alertaId, AtenderAlertaRequest request, Usuario usuarioActual);

    DashboardKpisDto obtenerKpisDashboard();

    DossierGeneradoResponse generarDossierInstitucionalRpa(SolicitudDossierRequest request, Usuario usuarioActual);
}

