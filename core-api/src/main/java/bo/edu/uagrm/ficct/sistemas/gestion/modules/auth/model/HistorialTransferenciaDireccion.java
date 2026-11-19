package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "historial_transferencia_direccion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialTransferenciaDireccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_saliente_id", nullable = false)
    private Usuario directorSaliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_entrante_id", nullable = false)
    private Usuario directorEntrante;

    @Column(name = "resolucion_respaldo", nullable = false, length = 150)
    private String resolucionRespaldo;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_transferencia", nullable = false, updatable = false)
    private OffsetDateTime fechaTransferencia;

    @PrePersist
    public void prePersist() {
        if (fechaTransferencia == null) {
            fechaTransferencia = OffsetDateTime.now();
        }
    }
}

