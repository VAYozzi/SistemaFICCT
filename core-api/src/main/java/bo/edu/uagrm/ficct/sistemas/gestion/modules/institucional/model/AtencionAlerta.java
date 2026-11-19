package bo.edu.uagrm.ficct.sistemas.gestion.modules.institucional.model;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "atencion_alertas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AtencionAlerta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alerta_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Alerta alerta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash"})
    private Usuario usuario;

    @Column(name = "accion_tomada", nullable = false, columnDefinition = "TEXT")
    private String accionTomada;

    @Column(name = "fecha_atencion", nullable = false)
    @Builder.Default
    private OffsetDateTime fechaAtencion = OffsetDateTime.now();
}

