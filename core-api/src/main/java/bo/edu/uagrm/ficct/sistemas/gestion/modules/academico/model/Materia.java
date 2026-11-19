package bo.edu.uagrm.ficct.sistemas.gestion.modules.academico.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.*;

@Entity
@Table(name = "materias")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_estudio_id", nullable = false)
    @JsonIgnore
    private PlanEstudio planEstudio;

    @Column(nullable = false, length = 20)
    private String sigla;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(name = "semestre_nivel", nullable = false)
    private Integer semestreNivel;

    @Column(nullable = false)
    private Integer creditos;

    @ManyToMany
    @JoinTable(
        name = "prerrequisitos_materia",
        joinColumns = @JoinColumn(name = "materia_id"),
        inverseJoinColumns = @JoinColumn(name = "prerrequisito_id")
    )
    @Builder.Default
    private Set<Materia> prerrequisitos = new HashSet<>();
}

