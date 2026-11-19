package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "periodos_academicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PeriodoAcademico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String codigo; // ej. '2026-1'

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String estado = "PLANIFICACION"; // 'PLANIFICACION', 'EN_CURSO', 'CERRADO'

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private OffsetDateTime creadoEn = OffsetDateTime.now();
}

