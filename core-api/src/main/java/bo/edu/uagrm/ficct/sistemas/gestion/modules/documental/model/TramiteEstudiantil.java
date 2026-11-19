package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "tramites_estudiantiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramiteEstudiantil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_tramite", nullable = false, unique = true, length = 50)
    private String codigoTramite;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "expediente_id", nullable = false)
    private ExpedienteEstudiantil expediente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_tramite_id", nullable = false)
    private TipoTramite tipoTramite;

    @Column(name = "estado_actual", nullable = false, length = 40)
    @Builder.Default
    private String estadoActual = "INGRESADO";

    @Column(name = "dictamen_director", columnDefinition = "TEXT")
    private String dictamenDirector;

    @Column(name = "dictamen_coordinador", columnDefinition = "TEXT")
    private String dictamenCoordinador;

    @Column(name = "fecha_ingreso", nullable = false)
    private OffsetDateTime fechaIngreso;

    @Column(name = "fecha_conclusion")
    private OffsetDateTime fechaConclusion;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @Column(name = "actualizado_en", nullable = false)
    private OffsetDateTime actualizadoEn;

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        if (creadoEn == null) creadoEn = now;
        if (actualizadoEn == null) actualizadoEn = now;
        if (fechaIngreso == null) fechaIngreso = now;
        if (estadoActual == null) estadoActual = "INGRESADO";
    }

    @PreUpdate
    public void preUpdate() {
        actualizadoEn = OffsetDateTime.now();
    }
}

