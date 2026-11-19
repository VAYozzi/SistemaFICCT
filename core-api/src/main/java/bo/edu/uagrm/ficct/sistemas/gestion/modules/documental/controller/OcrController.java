package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.controller;

import bo.edu.uagrm.ficct.sistemas.gestion.common.aop.Auditable;
import bo.edu.uagrm.ficct.sistemas.gestion.common.dto.ApiResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request.ValidarOcrRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.DocumentoOcrResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.ResultadoBusquedaDocumentalDto;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service.OcrDocumentalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/ocr")
@RequiredArgsConstructor
@Tag(name = "OCR y Búsqueda Documental", description = "Extracción con PyTesseract, validación humana y búsqueda de texto completo con tsvector")
public class OcrController {

    private final OcrDocumentalService ocrService;

    @PostMapping("/{documentoId}/procesar")
    @Auditable(modulo = "OCR", accion = "PROCESS_OCR", entidadAfectada = "documentos_digitales")
    @Operation(summary = "Procesar documento con OCR en FastAPI", description = "Ejecuta visión artificial en ai-worker-api y guarda resultados crudos")
    public ResponseEntity<ApiResponse<DocumentoOcrResponse>> procesarOcr(@PathVariable Long documentoId) {
        DocumentoOcrResponse response = ocrService.procesarOcrEnWorker(documentoId);
        return ResponseEntity.ok(ApiResponse.ok("Documento procesado exitosamente por OCR", response));
    }

    @GetMapping("/{documentoId}")
    @Operation(summary = "Obtener páginas y texto OCR de un documento", description = "Devuelve texto crudo, texto corregido y confianza")
    public ResponseEntity<ApiResponse<DocumentoOcrResponse>> obtenerDetalle(@PathVariable Long documentoId) {
        DocumentoOcrResponse response = ocrService.obtenerDetalleOcr(documentoId);
        return ResponseEntity.ok(ApiResponse.ok("Detalle de OCR obtenido exitosamente", response));
    }

    @PutMapping("/validar")
    @Auditable(modulo = "OCR", accion = "VALIDATE_HUMAN_TEXT", entidadAfectada = "documento_paginas_ocr")
    @Operation(summary = "Validación Humana de Texto OCR (Soberanía Humana)", description = "Guarda corrección del usuario y dispara trigger tsvector")
    public ResponseEntity<ApiResponse<DocumentoOcrResponse>> validarTextoHumano(@Valid @RequestBody ValidarOcrRequest request) {
        DocumentoOcrResponse response = ocrService.validarTextoOcrHumano(request);
        return ResponseEntity.ok(ApiResponse.ok("Texto OCR validado y sincronizado con motor de búsqueda", response));
    }

    @GetMapping("/busqueda")
    @Operation(summary = "Búsqueda avanzada de texto completo", description = "Busca palabras clave en documentos validados usando tsvector y genera resaltado")
    public ResponseEntity<ApiResponse<List<ResultadoBusquedaDocumentalDto>>> buscarPorTextoCompleto(@RequestParam("q") String query) {
        List<ResultadoBusquedaDocumentalDto> resultados = ocrService.buscarPorTextoCompleto(query);
        return ResponseEntity.ok(ApiResponse.ok("Búsqueda documental completada", resultados));
    }
}
