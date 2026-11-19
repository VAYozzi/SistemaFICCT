package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "docentes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Docente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_docente", nullable = false, unique = true, length = 30)
    private String codigoDocente;

    @Column(name = "nombre_completo", nullable = false, length = 150)
    private String nombreCompleto;

    @Column(name = "correo_institucional", length = 150)
    private String correoInstitucional;

    @Column(nullable = false, length = 50)
    @Builder.Default
    private String categoria = "TITULAR"; // 'TITULAR', 'INTERINO', 'INVITADO'

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private OffsetDateTime creadoEn = OffsetDateTime.now();
}

