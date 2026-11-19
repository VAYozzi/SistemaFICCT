package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "alertas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String categoria; // 'PREDICTIVA_CUPOS', 'OPERATIVA_TRAMITE_VENCIDO', 'OPERATIVA_ACEFALIA'

    @Column(name = "nivel_criticidad", nullable = false, length = 20)
    private String nivelCriticidad; // 'ROJO', 'AMARILLO', 'VERDE'

    @Column(nullable = false, length = 200)
    private String titulo;

    @Column(name = "mensaje_detalle", nullable = false, columnDefinition = "TEXT")
    private String mensajeDetalle;

    @Column(name = "entidad_origen", length = 80)
    private String entidadOrigen;

    @Column(name = "entidad_origen_id")
    private Long entidadOrigenId;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "PENDIENTE"; // 'PENDIENTE', 'EN_REVISION', 'ATENDIDA', 'DESCARTADA'

    @Column(name = "fecha_generacion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaGeneracion = OffsetDateTime.now();
}

