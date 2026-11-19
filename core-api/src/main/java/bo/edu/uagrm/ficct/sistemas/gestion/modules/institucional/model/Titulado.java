package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.DocumentoDigital;
import bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model.ExpedienteEstudiantil;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "titulados")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Titulado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expediente_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private ExpedienteEstudiantil expediente;

    @Column(name = "modalidad_graduacion", nullable = false, length = 50)
    private String modalidadGraduacion; // 'TESIS', 'PROYECTO_GRADO', 'EXAMEN_GRADO', 'EXCELENCIA', 'DIRIGIDO'

    @Column(name = "fecha_defensa", nullable = false)
    private LocalDate fechaDefensa;

    @Column(name = "nota_final", nullable = false, precision = 5, scale = 2)
    private BigDecimal notaFinal;

    @Column(name = "numero_titulo_provision", length = 100)
    private String numeroTituloProvision;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_acta_id")
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private DocumentoDigital documentoActa;

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private OffsetDateTime creadoEn = OffsetDateTime.now();
}

