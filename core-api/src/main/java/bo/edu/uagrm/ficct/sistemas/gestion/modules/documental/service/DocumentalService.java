package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request.CrearCorrespondenciaRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request.TransicionTramiteRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.CorrespondenciaResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.TramiteEstudiantilResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.DocumentoDigital;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface DocumentalService {
    DocumentoDigital subirDocumento(MultipartFile file, Usuario usuarioActual);
    CorrespondenciaResponse registrarCorrespondencia(CrearCorrespondenciaRequest request, Usuario usuarioActual);
    Page<CorrespondenciaResponse> listarCorrespondencia(String tipo, Pageable pageable);
    TramiteEstudiantilResponse obtenerTramitePorId(Long tramiteId);
    TramiteEstudiantilResponse realizarTransicion(Long tramiteId, TransicionTramiteRequest request, Usuario usuarioActual);
}

