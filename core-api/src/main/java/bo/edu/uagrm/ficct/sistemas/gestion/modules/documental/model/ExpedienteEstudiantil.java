package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import lombok.*;

@Entity
@Table(name = "expedientes_estudiantiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpedienteEstudiantil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "registro_estudiantil", nullable = false, unique = true, length = 20)
    private String registroEstudiantil;

    @Column(name = "cedula_identidad", nullable = false, unique = true, length = 20)
    private String cedulaIdentidad;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, length = 100)
    @Builder.Default
    private String carrera = "Ingeniería en Sistemas";

    @Column(length = 150)
    private String correo;

    @Column(length = 30)
    private String telefono;

    @Column(name = "estado_academico", nullable = false, length = 30)
    @Builder.Default
    private String estadoAcademico = "REGULAR"; // REGULAR, EGRESADO, TITULADO, SUSPENDIDO

    @Column(name = "creado_en", nullable = false, updatable = false)
    private OffsetDateTime creadoEn;

    @PrePersist
    public void prePersist() {
        if (creadoEn == null) creadoEn = OffsetDateTime.now();
        if (carrera == null) carrera = "Ingeniería en Sistemas";
        if (estadoAcademico == null) estadoAcademico = "REGULAR";
    }
}

