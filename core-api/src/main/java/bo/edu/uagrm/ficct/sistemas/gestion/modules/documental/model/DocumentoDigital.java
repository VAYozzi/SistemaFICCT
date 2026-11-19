package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model;

import bo.edu.uagrm.ficct.sistemas.gestion.modules.auth.model.Usuario;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "documentos_digitales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoDigital {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nombre_original", nullable = false)
    private String nombreOriginal;

    @Column(name = "nombre_almacenamiento", nullable = false, unique = true)
    private String nombreAlmacenamiento;

    @Column(name = "ruta_almacenamiento", nullable = false)
    private String rutaAlmacenamiento;

    @Column(name = "tipo_mime", nullable = false, length = 100)
    private String tipoMime;

    @Column(name = "tamanio_bytes", nullable = false)
    private Long tamanioBytes;

    @Column(name = "hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    @Column(name = "estado_ocr", nullable = false, length = 30)
    @Builder.Default
    private String estadoOcr = "PENDIENTE_VALIDACION"; // PENDIENTE_VALIDACION, VALIDADO, CORREGIDO, RECHAZADO

    @Column(name = "confianza_promedio_ocr", precision = 5, scale = 2)
    private BigDecimal confianzaPromedioOcr;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subido_por_usuario_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({"hibernateLazyInitializer", "handler", "passwordHash"})
    private Usuario subidoPorUsuario;

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (creadoEn == null) creadoEn = OffsetDateTime.now();
        if (estadoOcr == null) estadoOcr = "PENDIENTE_VALIDACION";
    }
}

