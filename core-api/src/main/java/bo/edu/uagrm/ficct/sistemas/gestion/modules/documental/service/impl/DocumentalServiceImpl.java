package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service.impl;

import bo.edu.uagrm.ficct.sistemas.gestion.common.exception.BusinessRuleException;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request.CrearCorrespondenciaRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.request.TransicionTramiteRequest;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.CorrespondenciaResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.dto.response.TramiteEstudiantilResponse;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.repository.*;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service.DocumentalService;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.service.StorageService;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentalServiceImpl implements DocumentalService {

    private final DocumentoDigitalRepository documentoRepository;
    private final CorrespondenciaRepository correspondenciaRepository;
    private final TramiteEstudiantilRepository tramiteRepository;
    private final HistorialEstadoTramiteRepository historialRepository;
    private final StorageService storageService;

    // Mapa formal de transiciones permitidas (RN-05: State Machine de Trámites)
    private static final Map<String, Set<String>> TRANSICIONES_VALIDAS = Map.of(
        "INGRESADO", Set.of("EN_VERIFICACION"),
        "EN_VERIFICACION", Set.of("OBSERVADO", "CON_DICTAMEN"),
        "OBSERVADO", Set.of("EN_VERIFICACION", "RECHAZADO"),
        "CON_DICTAMEN", Set.of("APROBADO", "RECHAZADO"),
        "APROBADO", Set.of("CONCLUIDO"),
        "RECHAZADO", Set.of("CONCLUIDO")
    );

    @Override
    @Transactional
    public DocumentoDigital subirDocumento(MultipartFile file, Usuario usuarioActual) {
        String hashSha256 = storageService.calcularSha256(file);

        // Si ya existe el mismo archivo exacto por hash SHA-256, se retorna para evitar duplicar almacenamiento
        Optional<DocumentoDigital> existente = documentoRepository.findByHashSha256(hashSha256);
        if (existente.isPresent()) {
            return existente.get();
        }

        String extension = "";
        String originalName = file.getOriginalFilename();
        if (originalName != null && originalName.contains(".")) {
            extension = originalName.substring(originalName.lastIndexOf("."));
        }
        String nombreAlmacenamiento = UUID.randomUUID() + extension;
        String rutaRelativa = "documentos/" + nombreAlmacenamiento;

        storageService.almacenarArchivo(file, "documentos", nombreAlmacenamiento);

        DocumentoDigital doc = DocumentoDigital.builder()
            .nombreOriginal(originalName != null ? originalName : "archivo_sin_nombre")
            .nombreAlmacenamiento(nombreAlmacenamiento)
            .rutaAlmacenamiento(rutaRelativa)
            .tipoMime(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
            .tamanioBytes(file.getSize())
            .hashSha256(hashSha256)
            .estadoOcr("PENDIENTE_VALIDACION")
            .subidoPorUsuario(usuarioActual)
            .build();

        return documentoRepository.save(doc);
    }

    @Override
    @Transactional
    public CorrespondenciaResponse registrarCorrespondencia(CrearCorrespondenciaRequest request, Usuario usuarioActual) {
        if (correspondenciaRepository.findByCodigoCite(request.codigoCite()).isPresent()) {
            throw new BusinessRuleException("El código CITE '" + request.codigoCite() + "' ya se encuentra registrado", HttpStatus.CONFLICT);
        }

        DocumentoDigital adjunto = null;
        if (request.documentoAdjuntoId() != null) {
            adjunto = documentoRepository.findById(request.documentoAdjuntoId()).orElse(null);
        }

        Correspondencia correspondencia = Correspondencia.builder()
            .codigoCite(request.codigoCite())
            .tipo(request.tipo())
            .remitenteInstitucion(request.remitenteInstitucion())
            .remitentePersona(request.remitentePersona())
            .destinatario(request.destinatario())
            .referencia(request.referencia())
            .fechaRecepcionEmision(OffsetDateTime.now())
            .estado("RECIBIDO")
            .usuarioReceptor(usuarioActual)
            .documentoAdjunto(adjunto)
            .observaciones(request.observaciones())
            .build();

        Correspondencia guardada = correspondenciaRepository.save(correspondencia);
        return mapToCorrespondenciaResponse(guardada);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CorrespondenciaResponse> listarCorrespondencia(String tipo, Pageable pageable) {
        if (tipo != null && !tipo.isBlank()) {
            return correspondenciaRepository.findByTipo(tipo, pageable).map(this::mapToCorrespondenciaResponse);
        }
        return correspondenciaRepository.findAll(pageable).map(this::mapToCorrespondenciaResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TramiteEstudiantilResponse obtenerTramitePorId(Long tramiteId) {
        TramiteEstudiantil tramite = tramiteRepository.findById(tramiteId)
            .orElseThrow(() -> new BusinessRuleException("Trámite no encontrado con ID: " + tramiteId, HttpStatus.NOT_FOUND));

        return mapToTramiteResponse(tramite);
    }

    @Override
    @Transactional
    public TramiteEstudiantilResponse realizarTransicion(Long tramiteId, TransicionTramiteRequest request, Usuario usuarioActual) {
        TramiteEstudiantil tramite = tramiteRepository.findById(tramiteId)
            .orElseThrow(() -> new BusinessRuleException("Trámite no encontrado con ID: " + tramiteId, HttpStatus.NOT_FOUND));

        String estadoActual = tramite.getEstadoActual();
        String estadoNuevo = request.estadoNuevo();

        // 1. Validar máquina de estados finitos
        Set<String> permitidos = TRANSICIONES_VALIDAS.getOrDefault(estadoActual, Collections.emptySet());
        if (!permitidos.contains(estadoNuevo)) {
            throw new BusinessRuleException(
                "Transición de estado inválida: No se puede pasar de '" + estadoActual + "' a '" + estadoNuevo + "'.",
                HttpStatus.CONFLICT
            );
        }

        // 2. Validar Soberanía Humana y Restricciones por Rol (RN-01 / RN-05)
        String rolUsuario = usuarioActual.getRol().getCodigo();
        if (("APROBADO".equals(estadoNuevo) || "RECHAZADO".equals(estadoNuevo)) && !"ROLE_DIRECTOR".equals(rolUsuario)) {
            throw new BusinessRuleException("Solo el Director de Carrera (ROLE_DIRECTOR) puede APROBAR o RECHAZAR trámites.", HttpStatus.FORBIDDEN);
        }

        if ("CON_DICTAMEN".equals(estadoNuevo) && !"ROLE_COORDINADOR".equals(rolUsuario) && !"ROLE_DIRECTOR".equals(rolUsuario)) {
            throw new BusinessRuleException("La emisión de dictamen preliminar requiere ROLE_COORDINADOR o superior.", HttpStatus.FORBIDDEN);
        }

        // Actualizar trámite
        tramite.setEstadoActual(estadoNuevo);
        if ("APROBADO".equals(estadoNuevo) || "RECHAZADO".equals(estadoNuevo)) {
            tramite.setDictamenDirector(request.observacionODictamen());
        } else if ("CON_DICTAMEN".equals(estadoNuevo)) {
            tramite.setDictamenCoordinador(request.observacionODictamen());
        }
        if ("CONCLUIDO".equals(estadoNuevo)) {
            tramite.setFechaConclusion(OffsetDateTime.now());
        }
        tramiteRepository.save(tramite);

        // 3. Registrar Historial Inmutable de Transición (Auditoría de State Machine)
        HistorialEstadoTramite historial = HistorialEstadoTramite.builder()
            .tramite(tramite)
            .estadoAnterior(estadoActual)
            .estadoNuevo(estadoNuevo)
            .usuario(usuarioActual)
            .rolEnMomento(rolUsuario)
            .observacionODictamen(request.observacionODictamen())
            .fechaHora(OffsetDateTime.now())
            .build();
        historialRepository.save(historial);

        return mapToTramiteResponse(tramite);
    }

    private CorrespondenciaResponse mapToCorrespondenciaResponse(Correspondencia c) {
        return new CorrespondenciaResponse(
            c.getId(),
            c.getCodigoCite(),
            c.getTipo(),
            c.getRemitenteInstitucion(),
            c.getRemitentePersona(),
            c.getDestinatario(),
            c.getReferencia(),
            c.getEstado(),
            c.getUsuarioReceptor() != null ? c.getUsuarioReceptor().getNombreCompleto() : "N/A",
            c.getDocumentoAdjunto() != null ? c.getDocumentoAdjunto().getId() : null,
            c.getDocumentoAdjunto() != null ? c.getDocumentoAdjunto().getNombreOriginal() : null,
            c.getFechaRecepcionEmision(),
            c.getCreadoEn()
        );
    }

    private TramiteEstudiantilResponse mapToTramiteResponse(TramiteEstudiantil t) {
        List<TramiteEstudiantilResponse.HistorialEstadoResponse> historialDtos = historialRepository
            .findByTramiteIdOrderByFechaHoraAsc(t.getId())
            .stream()
            .map(h -> new TramiteEstudiantilResponse.HistorialEstadoResponse(
                h.getId(),
                h.getEstadoAnterior(),
                h.getEstadoNuevo(),
                h.getUsuario() != null ? h.getUsuario().getNombreCompleto() : "N/A",
                h.getRolEnMomento(),
                h.getObservacionODictamen(),
                h.getFechaHora()
            ))
            .toList();

        return new TramiteEstudiantilResponse(
            t.getId(),
            t.getCodigoTramite(),
            t.getExpediente().getId(),
            t.getExpediente().getRegistroEstudiantil(),
            t.getExpediente().getNombres() + " " + t.getExpediente().getApellidos(),
            t.getTipoTramite().getCodigo(),
            t.getTipoTramite().getNombre(),
            t.getEstadoActual(),
            t.getDictamenDirector(),
            t.getDictamenCoordinador(),
            t.getFechaIngreso(),
            t.getFechaConclusion(),
            historialDtos
        );
    }
}

