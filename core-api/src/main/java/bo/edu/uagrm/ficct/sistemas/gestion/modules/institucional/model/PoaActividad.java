package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "poa_actividades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoaActividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "poa_gestion_id", nullable = false)
    @JsonIgnore
    private PoaGestion poaGestion;

    @Column(name = "codigo_actividad", nullable = false, length = 30)
    private String codigoActividad;

    @Column(name = "nombre_actividad", nullable = false, length = 250)
    private String nombreActividad;

    @Column(name = "indicador_meta", nullable = false, length = 200)
    private String indicadorMeta;

    @Column(name = "presupuesto_asignado", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal presupuestoAsignado = BigDecimal.ZERO;

    @Column(name = "porcentaje_avance", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajeAvance = BigDecimal.ZERO;
}

