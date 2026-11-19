package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "bitacora_auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BitacoraAuditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "correo_actor", length = 150)
    private String correoActor;

    @Column(name = "direccion_ip", nullable = false, length = 45)
    private String direccionIp;

    @Column(nullable = false, length = 80)
    private String modulo;

    @Column(nullable = false, length = 50)
    private String accion;

    @Column(name = "entidad_afectada", nullable = false, length = 100)
    private String entidadAfectada;

    @Column(name = "entidad_id", length = 50)
    private String entidadId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_anteriores", columnDefinition = "jsonb")
    private String datosAnteriores;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "datos_nuevos", columnDefinition = "jsonb")
    private String datosNuevos;

    @Column(name = "fecha_hora", nullable = false, updatable = false)
    private OffsetDateTime fechaHora;

    @PrePersist
    public void prePersist() {
        if (fechaHora == null) {
            fechaHora = OffsetDateTime.now();
        }
    }
}

