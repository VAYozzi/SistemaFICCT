package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipos_tramites")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoTramite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "dias_limite_atencion", nullable = false)
    @Builder.Default
    private Integer diasLimiteAtencion = 5;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;
}

