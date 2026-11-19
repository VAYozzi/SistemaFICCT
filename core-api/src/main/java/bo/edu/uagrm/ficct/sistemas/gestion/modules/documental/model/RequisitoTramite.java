package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "requisitos_tramite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RequisitoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramite_id", nullable = false)
    private TramiteEstudiantil tramite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "documento_id", nullable = false)
    private DocumentoDigital documento;

    @Column(name = "nombre_requisito", nullable = false, length = 150)
    private String nombreRequisito;

    @Column(nullable = false)
    @Builder.Default
    private Boolean cumplido = true;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (creadoEn == null) creadoEn = OffsetDateTime.now();
        if (cumplido == null) cumplido = true;
    }
}

