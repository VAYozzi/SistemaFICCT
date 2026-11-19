package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "planes_estudio")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanEstudio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_plan", nullable = false, unique = true, length = 20)
    private String codigoPlan;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "anio_vigencia", nullable = false)
    private Integer anioVigencia;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private OffsetDateTime creadoEn = OffsetDateTime.now();

    @OneToMany(mappedBy = "planEstudio", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Materia> materias = new ArrayList<>();
}

