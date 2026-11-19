package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request.ValidarOcrRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.DocumentoOcrResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.ResultadoBusquedaDocumentalDto;
import java.util.List;

public interface OcrDocumentalService {

    DocumentoOcrResponse procesarOcrEnWorker(Long documentoId);

    DocumentoOcrResponse obtenerDetalleOcr(Long documentoId);

    DocumentoOcrResponse validarTextoOcrHumano(ValidarOcrRequest request);

    List<ResultadoBusquedaDocumentalDto> buscarPorTextoCompleto(String query);
}

