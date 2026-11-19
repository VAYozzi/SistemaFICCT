package bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "invitaciones_acceso")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvitacionAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "correo_destino", nullable = false, length = 150)
    private String correoDestino;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_asignado_id", nullable = false)
    private Role rolAsignado;

    @Column(name = "token_invitacion", nullable = false, unique = true)
    private UUID tokenInvitacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creado_por_usuario_id", nullable = false)
    private Usuario creadoPorUsuario;

    @Column(name = "fecha_expiracion", nullable = false)
    private OffsetDateTime fechaExpiracion;

    @Column(nullable = false, length = 30)
    @Builder.Default
    private String estado = "PENDIENTE"; // PENDIENTE, ACEPTADA, EXPIRADA, REVOCADA

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (tokenInvitacion == null) tokenInvitacion = UUID.randomUUID();
        if (creadoEn == null) creadoEn = OffsetDateTime.now();
        if (estado == null) estado = "PENDIENTE";
    }
}

