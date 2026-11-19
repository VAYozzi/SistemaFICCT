package bo.edu.uagrm.ficct.sistemas.gestion.modules.documental.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "documento_paginas_ocr")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentoPaginaOcr {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "documento_id", nullable = false)
    @JsonIgnore
    private DocumentoDigital documento;

    @Column(name = "numero_pagina", nullable = false)
    private Integer numeroPagina;

    @Column(name = "texto_extraido_bruto", columnDefinition = "TEXT")
    private String textoExtraidoBruto;

    @Column(name = "texto_corregido", columnDefinition = "TEXT")
    private String textoCorregido;

    @Column(name = "confianza_pagina", precision = 5, scale = 2)
    private BigDecimal confianzaPagina;

    @Column(name = "tiempo_procesamiento_ms")
    private Integer tiempoProcesamientoMs;

    // Columna vector_busqueda administrada nativamente por el trigger de PostgreSQL
}

