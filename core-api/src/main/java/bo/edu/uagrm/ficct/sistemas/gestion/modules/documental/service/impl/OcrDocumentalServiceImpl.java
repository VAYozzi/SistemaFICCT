package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service.impl;

import bo.edu.uagrm.ficct.sistemas.gestion.common.exception.BusinessRuleException;
import bo.edu.uagrm.ficct.sistemas.gestion.common.exception.ResourceNotFoundException;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request.ValidarOcrRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.DocumentoOcrResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.ResultadoBusquedaDocumentalDto;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.DocumentoDigital;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.DocumentoPaginaOcr;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository.DocumentoDigitalRepository;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository.DocumentoPaginaOcrRepository;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service.OcrDocumentalService;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class OcrDocumentalServiceImpl implements OcrDocumentalService {

    private final DocumentoDigitalRepository documentoRepository;
    private final DocumentoPaginaOcrRepository paginaOcrRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${ai.worker.url:http://127.0.0.1:8000}")
    private String aiWorkerUrl;

    @Value("${ai.worker.internal-key:ficct_super_secret_internal_key_2026}")
    private String internalApiKey;

    @Override
    @Transactional
    public DocumentoOcrResponse procesarOcrEnWorker(Long documentoId) {
        DocumentoDigital doc = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + documentoId));

        // Invocación a FastAPI
        String url = aiWorkerUrl + "/internal/v1/ocr/extract";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-Internal-Api-Key", internalApiKey);

        Map<String, String> requestBody = Map.of(
            "ruta_relativa", doc.getRutaAlmacenamiento(),
            "idioma", "spa"
        );

        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map body = response.getBody();
                List<Map<String, Object>> paginasRaw = (List<Map<String, Object>>) body.get("paginas");

                // Eliminar páginas previas si existían y vaciar la sesión JPA
                List<DocumentoPaginaOcr> existentes = paginaOcrRepository.findByDocumentoIdOrderByNumeroPaginaAsc(doc.getId());
                if (!existentes.isEmpty()) {
                    paginaOcrRepository.deleteAll(existentes);
                    paginaOcrRepository.flush();
                }

                List<DocumentoPaginaOcr> nuevasPaginas = new ArrayList<>();
                double sumaConfianza = 0.0;

                for (Map<String, Object> p : paginasRaw) {
                    Integer numPag = (Integer) p.get("numero_pagina");
                    String texto = (String) p.get("texto_extraido");
                    Number conf = (Number) p.get("confianza");
                    BigDecimal confBd = BigDecimal.valueOf(conf != null ? conf.doubleValue() : 0.0);
                    sumaConfianza += confBd.doubleValue();

                    DocumentoPaginaOcr pag = DocumentoPaginaOcr.builder()
                        .documento(doc)
                        .numeroPagina(numPag)
                        .textoExtraidoBruto(texto)
                        .textoCorregido(null) // Soberanía Humana: Nace NULL hasta validación humana
                        .confianzaPagina(confBd)
                        .tiempoProcesamientoMs(150)
                        .build();

                    nuevasPaginas.add(pag);
                }

                paginaOcrRepository.saveAll(nuevasPaginas);

                BigDecimal confPromedio = BigDecimal.valueOf(
                    paginasRaw.isEmpty() ? 0.0 : (sumaConfianza / paginasRaw.size())
                ).setScale(2, RoundingMode.HALF_UP);

                doc.setEstadoOcr("PENDIENTE_VALIDACION");
                doc.setConfianzaPromedioOcr(confPromedio);
                documentoRepository.save(doc);

                return mapearResponse(doc, nuevasPaginas);
            } else {
                throw new BusinessRuleException("El servicio de IA respondió con un estado inválido: " + response.getStatusCode());
            }
        } catch (Exception ex) {
            log.error("Fallo de comunicación con FastAPI ai-worker-api", ex);
            doc.setEstadoOcr("ERROR_PROCESAMIENTO");
            documentoRepository.save(doc);
            throw new BusinessRuleException("Error al procesar OCR con el servicio de IA: " + ex.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentoOcrResponse obtenerDetalleOcr(Long documentoId) {
        DocumentoDigital doc = documentoRepository.findById(documentoId)
            .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + documentoId));

        List<DocumentoPaginaOcr> paginas = paginaOcrRepository.findByDocumentoIdOrderByNumeroPaginaAsc(documentoId);
        return mapearResponse(doc, paginas);
    }

    @Override
    @Transactional
    public DocumentoOcrResponse validarTextoOcrHumano(ValidarOcrRequest request) {
        DocumentoDigital doc = documentoRepository.findById(request.documentoId())
            .orElseThrow(() -> new ResourceNotFoundException("Documento no encontrado con ID: " + request.documentoId()));

        if (request.paginas() != null) {
            for (ValidarOcrRequest.PaginaOcrValidada pReq : request.paginas()) {
                DocumentoPaginaOcr pag = paginaOcrRepository.findByDocumentoIdAndNumeroPagina(doc.getId(), pReq.numeroPagina())
                    .orElseThrow(() -> new ResourceNotFoundException("Página " + pReq.numeroPagina() + " no encontrada para documento ID: " + doc.getId()));

                pag.setTextoCorregido(pReq.textoCorregido() != null ? pReq.textoCorregido().trim() : "");
                paginaOcrRepository.save(pag); // Activa el trigger PostgreSQL trg_documento_paginas_ocr_vector
            }
        }

        doc.setEstadoOcr("VALIDADO");
        documentoRepository.save(doc);

        List<DocumentoPaginaOcr> paginasActualizadas = paginaOcrRepository.findByDocumentoIdOrderByNumeroPaginaAsc(doc.getId());
        return mapearResponse(doc, paginasActualizadas);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ResultadoBusquedaDocumentalDto> buscarPorTextoCompleto(String query) {
        if (query == null || query.trim().isEmpty()) {
            return List.of();
        }

        List<Object[]> resultados = paginaOcrRepository.buscarTextoCompletoConResaltado(query.trim());
        List<ResultadoBusquedaDocumentalDto> dtos = new ArrayList<>();

        for (Object[] row : resultados) {
            Long paginaId = ((Number) row[0]).longValue();
            Long docId = ((Number) row[1]).longValue();
            Integer numPag = ((Number) row[2]).intValue();
            BigDecimal confianza = row[3] != null ? (BigDecimal) row[3] : BigDecimal.ZERO;
            String fragmento = (String) row[4];
            String nombreOrig = (String) row[5];
            String rutaAlm = (String) row[6];
            String mime = (String) row[7];

            dtos.add(new ResultadoBusquedaDocumentalDto(
                paginaId, docId, numPag, confianza, fragmento, nombreOrig, rutaAlm, mime
            ));
        }

        return dtos;
    }

    private DocumentoOcrResponse mapearResponse(DocumentoDigital doc, List<DocumentoPaginaOcr> paginas) {
        List<DocumentoOcrResponse.PaginaOcrDto> pagsDto = paginas.stream()
            .map(p -> new DocumentoOcrResponse.PaginaOcrDto(
                p.getId(),
                p.getNumeroPagina(),
                p.getTextoExtraidoBruto(),
                p.getTextoCorregido(),
                p.getConfianzaPagina(),
                p.getTiempoProcesamientoMs()
            ))
            .toList();

        return new DocumentoOcrResponse(
            doc.getId(),
            doc.getNombreOriginal(),
            doc.getRutaAlmacenamiento(),
            doc.getTipoMime(),
            doc.getEstadoOcr(),
            doc.getConfianzaPromedioOcr(),
            pagsDto
        );
    }
}

