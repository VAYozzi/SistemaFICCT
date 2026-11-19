package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "poa_gestiones")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PoaGestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "anio_gestion", nullable = false, unique = true)
    private Integer anioGestion;

    @Column(name = "presupuesto_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal presupuestoTotal = BigDecimal.ZERO;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "EN_EJECUCION"; // 'FORMULADO', 'EN_EJECUCION', 'CERRADO'

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @OneToMany(mappedBy = "poaGestion", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PoaActividad> actividades = new ArrayList<>();
}

