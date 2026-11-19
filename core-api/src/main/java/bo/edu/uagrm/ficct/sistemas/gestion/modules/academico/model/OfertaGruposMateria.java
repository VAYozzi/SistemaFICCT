package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "oferta_grupos_materia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OfertaGruposMateria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periodo_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private PeriodoAcademico periodo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "materia_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Materia materia;

    @Column(name = "codigo_grupo", nullable = false, length = 10)
    private String codigoGrupo; // 'SA', 'SB', etc.

    @Column(name = "cupo_ofertado", nullable = false)
    private Integer cupoOfertado;

    @Column(name = "inscritos_finales", nullable = false)
    @Builder.Default
    private Integer inscritosFinales = 0;

    @Column(name = "demanda_insatisfecha_estimada", nullable = false)
    @Builder.Default
    private Integer demandaInsatisfechaEstimada = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer aprobados = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer reprobados = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer abandonos = 0;
}

