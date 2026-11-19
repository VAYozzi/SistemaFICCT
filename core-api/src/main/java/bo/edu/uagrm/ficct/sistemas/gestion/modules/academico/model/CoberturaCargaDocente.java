package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Table(name = "cobertura_carga_docente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoberturaCargaDocente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferta_grupo_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private OfertaGruposMateria ofertaGrupo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "docente_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Docente docente;

    @Column(name = "tipo_designacion", nullable = false, length = 30)
    @Builder.Default
    private String tipoDesignacion = "TITULAR"; // 'TITULAR', 'INTERINATO', 'SUPLENCIA'

    @Column(name = "horas_semanales", nullable = false)
    @Builder.Default
    private Integer horasSemanales = 4;

    @Column(name = "resolucion_facultativa", length = 150)
    private String resolucionFacultativa;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "VIGENTE"; // 'VIGENTE', 'CONCLUIDA', 'ACEFALIA'
}

