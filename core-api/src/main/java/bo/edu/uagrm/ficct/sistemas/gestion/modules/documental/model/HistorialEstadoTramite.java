package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "historial_estados_tramite")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialEstadoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tramite_id", nullable = false)
    private TramiteEstudiantil tramite;

    @Column(name = "estado_anterior", nullable = false, length = 40)
    private String estadoAnterior;

    @Column(name = "estado_nuevo", nullable = false, length = 40)
    private String estadoNuevo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "rol_en_momento", nullable = false, length = 50)
    private String rolEnMomento;

    @Column(name = "observacion_o_dictamen", columnDefinition = "TEXT")
    private String observacionODictamen;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private OffsetDateTime fechaHora;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) fechaHora = OffsetDateTime.now();
    }
}

