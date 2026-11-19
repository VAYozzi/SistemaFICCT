package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "correspondencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Correspondencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_cite", nullable = false, unique = true, length = 80)
    private String codigoCite;

    @Column(nullable = false, length = 20)
    private String tipo; // ENTRANTE, SALIENTE, INTERNO

    @Column(name = "remitente_institucion", nullable = false, length = 150)
    private String remitenteInstitucion;

    @Column(name = "remitente_persona", nullable = false, length = 150)
    private String remitentePersona;

    @Column(nullable = false, length = 150)
    private String destinatario;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String referencia;

    @Column(name = "fecha_recepcion_emision", nullable = false)
    private OffsetDateTime fechaRecepcionEmision;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "RECIBIDO"; // RECIBIDO, EN_REVISION, DERIVADO, ARCHIVADO

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_receptor_id", nullable = false)
    private Usuario usuarioReceptor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_adjunto_id")
    private DocumentoDigital documentoAdjunto;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (creadoEn == null) creadoEn = now;
        if (actualizadoEn == null) actualizadoEn = now;
        if (fechaRecepcionEmision == null) fechaRecepcionEmision = now;
        if (estado == null) estado = "RECIBIDO";
    }

    @PreUpdate
    public void preUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}

