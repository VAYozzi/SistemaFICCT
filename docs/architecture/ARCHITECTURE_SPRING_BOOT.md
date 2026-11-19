# ARCHITECTURE_SPRING_BOOT.md
# Arquitectura y Estándares Técnicos del Backend Transaccional (`core-api`)

## 1. Propósito y Filosofía del Componente
El componente `core-api` es el **núcleo transaccional, orquestador y guardián de las reglas de negocio** del *Sistema Web de Gestión Administrativa con Alertas Predictivas y OCR para la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)*.

### Principios Rectores:
1. **Monolito Modular por Dominio (DDD-Lite / Package-by-Feature):** Cada módulo funcional encapsula sus controladores, servicios, repositorios, entidades, DTOs y mappers.
2. **Topología en Estrella:** Es el único servicio que interactúa con la base de datos relacional (PostgreSQL) y con el cliente web (`web-client`). Orquesta las peticiones complejas hacia `ai-worker-api`.
3. **Cero Exposición de Entidades:** Ningún controlador REST expone entidades JPA directamente. Todo intercambio de datos se realiza mediante objetos de transferencia inmutables (`DTO` basados en `record` de Java 17).
4. **Soberanía Humana y Trazabilidad:** Todo cambio de estado crítico queda auditado de forma inmutable mediante Programación Orientada a Aspectos (Spring AOP).

---

## 2. Stack Tecnológico Oficial

* **Lenguaje:** Java 17 LTS.
* **Framework:** Spring Boot 3.2+.
* **Persistencia:** Spring Data JPA / Hibernate 6.x.
* **Control de Versiones de BD:** Flyway (migraciones versionadas en `src/main/resources/db/migration`).
* **Seguridad:** Spring Security 6.x + `jjwt` (Java JWT 0.12+).
* **Mapeo:** MapStruct 1.5+.
* **Reducción de Boilerplate:** Lombok.
* **Cliente HTTP Interno:** Spring WebFlux (`WebClient`).
* **Documentación API:** SpringDoc OpenAPI 2.x (Swagger UI v3).
* **Validación:** Jakarta Bean Validation (`hibernate-validator`).

---

## 3. Estructura de Directorios Estricta

Todo el código fuente debe ubicarse bajo el paquete base:  
`bo.edu.uagrm.ficct.sistemas.gestion`

```text
src/main/java/bo/edu/uagrm/ficct/sistemas/gestion/
│
├── config/                                # Configuraciones globales de Spring
│   ├── security/                          # SecurityFilterChain, JwtFilter, PasswordEncoder
│   ├── web/                               # WebMvcConfigurer, CORS, interceptores
│   ├── webclient/                         # Configuración WebClient para ai-worker-api
│   ├── swagger/                           # Configuración OpenAPI 3.0 / Swagger
│   └── async/                             # ThreadPoolTaskExecutor para tareas asíncronas
│
├── common/                                # Componentes transversales compartidos
│   ├── aop/                               # Anotación @Auditable y AuditoriaAspect
│   ├── dto/                               # ApiResponse<T>, PageResponse<T>, ErrorResponse
│   ├── exception/                         # GlobalExceptionHandler, excepciones personalizadas
│   └── util/                              # Clases de utilidad (fechas, hashing SHA-256)
│
└── modules/                               # Módulos Funcionales (Package-by-Feature)
    ├── auth/                              # Autenticación, Usuarios, Roles, Invitaciones, Bitácora
    ├── documental/                        # Correspondencia, Archivos Azure, Trámites (State Machine)
    ├── academico/                         # Periodos, Materias, Grupos, Cupos, Malla, Carga Docente
    ├── institucional/                     # Titulados, Investigaciones, POA, Eventos
    ├── inteligencia/                      # Orquestador OCR, Búsqueda Avanzada/Semántica, Asistente
    ├── analitica/                         # Dashboard Directivo, Alertas Predictivas y Operativas
    └── rpa/                               # Orquestación de bots Playwright y monitor de ejecuciones
```

### Estructura Interna Obligatoria de Cada Módulo:
Dentro de cada submódulo (por ejemplo, `modules/documental/`) se debe respetar estrictamente esta jerarquía de paquetes:

```text
modules/documental/
├── controller/       # Clases @RestController con prefijo de ruta /api/v1/...
├── service/          # Interfaces de servicio (contratos de lógica de negocio)
│   └── impl/         # Implementaciones @Service con demarcación @Transactional
├── repository/       # Interfaces Spring Data JPA (@Repository)
├── model/            # Entidades de base de datos (@Entity)
├── dto/              # Java 17 records para Request y Response
│   ├── request/      # DTOs de entrada con anotaciones Jakarta Validation
│   └── response/     # DTOs de salida inmutables
└── mapper/           # Interfaces MapStruct (@Mapper(componentModel = "spring"))
```

---

## 4. Estándares Detallados por Capa

### 4.1. Capa de Controladores (`controller`)
* Todo controlador debe llevar las anotaciones:
  * `@RestController`
  * `@RequestMapping("/api/v1/<recurso>")`
  * `@Tag(name = "...", description = "...")` para Swagger.
  * `@RequiredArgsConstructor` (inyección de dependencias por constructor).
* **Firma de Métodos:** Deben devolver siempre `ResponseEntity<ApiResponse<T>>`.
* **Validación Obligatoria:** Los DTOs de entrada deben validarse con `@Valid`.
* **Prohibición:** Ningún controlador debe inyectar repositorios directamente ni contener lógica de bifurcación de negocio (ifs, cálculos o mutaciones de estado).

```java
@RestController
@RequestMapping("/api/v1/tramites")
@RequiredArgsConstructor
@Tag(name = "Trámites Estudiantiles", description = "Gestión de expedientes y Máquina de Estados")
public class TramiteEstudiantilController {

    private final TramiteEstudiantilService tramiteService;

    @PostMapping("/{id}/transicion")
    @PreAuthorize("hasAnyRole('ROLE_DIRECTOR', 'ROLE_COORDINADOR', 'ROLE_AUXILIAR')")
    @Operation(summary = "Ejecutar transición de estado sobre un expediente")
    public ResponseEntity<ApiResponse<TramiteDetalleResponse>> ejecutarTransicion(
            @PathVariable Long id,
            @Valid @RequestBody TransicionTramiteRequest request,
            Authentication authentication) {

        UsuarioAutenticado usuario = (UsuarioAutenticado) authentication.getPrincipal();
        TramiteDetalleResponse response = tramiteService.ejecutarTransicion(id, request, usuario);
        
        return ResponseEntity.ok(ApiResponse.success("Transición ejecutada correctamente", response));
    }
}
```

---

### 4.2. Capa de Transferencia de Datos (`dto`)
* **Uso exclusivo de `record` de Java 17:** Proporciona inmutabilidad nativa, getters automáticos, `equals`, `hashCode` y `toString` limpios.
* **Separación de Intenciones:** Siempre separar los modelos en `XRequest` y `XResponse`.
* **Anotaciones Jakarta Validation:** Todo request debe validar tipos y restricciones obligatorias.

```java
// DTO de Entrada (Request)
public record TransicionTramiteRequest(
    @NotBlank(message = "El estado destino es obligatorio")
    @Pattern(regexp = "INGRESADO|EN_VERIFICACION|OBSERVADO|CON_DICTAMEN|APROBADO|RECHAZADO|CONCLUIDO", 
             message = "Estado de destino no válido para la Máquina de Estados")
    String estadoDestino,

    @NotBlank(message = "El comentario o dictamen es obligatorio")
    @Size(min = 10, max = 2000, message = "El comentario debe contener entre 10 y 2000 caracteres")
    String comentarioODictamen,

    Long documentoRespaldoId
) {}

// DTO de Salida (Response)
public record TramiteDetalleResponse(
    Long id,
    String codigoExpediente,
    String nombreEstudiante,
    String registroUniversitario,
    String tipoTramite,
    String estadoActual,
    OffsetDateTime fechaIngreso,
    LocalDate fechaLimiteEstimada,
    List<RequisitoPresentadoResponse> requisitos,
    List<HistorialEstadoResponse> historialTransiciones
) {}
```

---

### 4.3. Capa de Servicios (`service` / `service.impl`)
* Las interfaces residen en `service/` y sus implementaciones en `service/impl/`.
* **Límites Transaccionales (`@Transactional`):**
  * La clase de implementación se anota con `@Transactional(readOnly = true)`.
  * Los métodos que ejecutan operaciones de escritura (`CREATE`, `UPDATE`, `DELETE`) se sobreescriben explícitamente con `@Transactional(rollbackFor = Exception.class)`.
* **Soberanía y Máquina de Estados:** Toda lógica de negocio compleja (como verificar transiciones permitidas o invariantes de rol) se ejecuta en esta capa.

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class TramiteEstudiantilServiceImpl implements TramiteEstudiantilService {

    private final TramiteEstudiantilRepository tramiteRepository;
    private final HistorialEstadosTramiteRepository historialRepository;
    private final TramiteMapper tramiteMapper;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @Auditable(modulo = "TRAMITES", accion = "STATE_CHANGE", entidad = "TramiteEstudiantil")
    public TramiteDetalleResponse ejecutarTransicion(Long tramiteId, TransicionTramiteRequest request, UsuarioAutenticado actor) {
        TramiteEstudiantil tramite = tramiteRepository.findById(tramiteId)
            .orElseThrow(() -> new ResourceNotFoundException("Trámite no encontrado con ID: " + tramiteId));

        EstadoTramite actual = EstadoTramite.valueOf(tramite.getEstadoActual());
        EstadoTramite destino = EstadoTramite.valueOf(request.estadoDestino());

        // Validación estricta de la State Machine
        validarTransicion(actual, destino, actor.getRol());

        // Actualizar estado del trámite
        tramite.setEstadoActual(destino.name());
        if (destino == EstadoTramite.CONCLUIDO) {
            tramite.setFechaCierre(OffsetDateTime.now());
        }
        tramiteRepository.save(tramite);

        // Guardar historial inmutable de la transición
        HistorialEstadosTramite historial = HistorialEstadosTramite.builder()
            .tramite(tramite)
            .estadoAnterior(actual.name())
            .estadoNuevo(destino.name())
            .comentarioODictamen(request.comentarioODictamen())
            .usuarioId(actor.getId())
            .rolEnMomento(actor.getRol())
            .fechaTransicion(OffsetDateTime.now())
            .build();
        historialRepository.save(historial);

        return tramiteMapper.toDetalleResponse(tramite);
    }
}
```

---

### 4.4. Capa de Persistencia y Modelos (`model` / `repository`)
* **Entidades JPA:**
  * Uso de `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor` y `@Builder` de Lombok.
  * **PROHIBICIÓN ESTRICTA:** No usar `@Data` ni `@EqualsAndHashCode` en entidades JPA, ya que provocan bucles recursivos en relaciones bidireccionales y rompen el contrato de identidad de Hibernate.
  * Claves foráneas modeladas con `@ManyToOne(fetch = FetchType.LAZY)`.
* **Repositorios Spring Data:**
  * Extienden `JpaRepository<Entity, Long>` y `JpaSpecificationExecutor<Entity>` para filtros dinámicos.
  * Consultas personalizadas usan JPQL o `@Query(nativeQuery = true)` cuando se aprovechen extensiones nativas de PostgreSQL (como búsquedas `tsvector` o índices parciales).

```java
@Entity
@Table(name = "tramites_estudiantiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TramiteEstudiantil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_expediente", nullable = false, unique = true, length = 50)
    private String codigoExpediente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_tramite_id", nullable = false)
    private TipoTramite tipoTramite;

    @Column(name = "periodo_academico_codigo", nullable = false, length = 20)
    private String periodoAcademicoCodigo;

    @Column(name = "motivo_solicitud", nullable = false, columnDefinition = "TEXT")
    private String motivoSolicitud;

    @Column(name = "estado_actual", nullable = false, length = 30)
    private String estadoActual;

    @Column(name = "fecha_ingreso", nullable = false)
    private OffsetDateTime fechaIngreso;

    @Column(name = "fecha_limite_estimada", nullable = false)
    private LocalDate fechaLimiteEstimada;

    @Column(name = "fecha_cierre")
    private OffsetDateTime fechaCierre;

    @Column(name = "registrado_por_usuario_id", nullable = false)
    private Long registradoPorUsuarioId;
}
```

---

### 4.5. Capa de Mapeo (`mapper`)
* Se utiliza **MapStruct** para evitar el copiado manual de propiedades:
```java
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TramiteMapper {

    @Mapping(target = "nombreEstudiante", source = "estudiante.nombreCompleto")
    @Mapping(target = "registroUniversitario", source = "estudiante.registroUniversitario")
    @Mapping(target = "tipoTramite", source = "tipoTramite.nombre")
    TramiteDetalleResponse toDetalleResponse(TramiteEstudiantil entity);
}
```

---

## 5. Aspectos Transversales (`common/`)

### 5.1. Auditoría Inmutable AOP (`@Auditable`)
Para cumplir la ley de inmutabilidad sin saturar los servicios con código repetitivo, se implementa una anotación y un aspecto AOP:

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Auditable {
    String modulo();
    String accion();
    String entidad();
}
```

El aspecto `AuditoriaAspect` captura automáticamente:
1. La IP del cliente (`HttpServletRequest`).
2. El usuario autenticado en el `SecurityContext`.
3. El estado anterior y nuevo de la entidad (vía JSONB serializado con Jackson).
4. Persiste en `bitacora_auditoria` dentro de una transacción independiente (`Propagation.REQUIRES_NEW`).

### 5.2. Manejo Global de Excepciones (`GlobalExceptionHandler`)
Toda excepción lanzada por la aplicación es interceptada por una clase anotada con `@RestControllerAdvice` y transformada en el formato canónico `ApiResponse<T>`:

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        List<ErrorItem> errors = ex.getBindingResult().getFieldErrors().stream()
            .map(err -> new ErrorItem(err.getField(), err.getDefaultMessage()))
            .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ApiResponse.validationError("Error de validación en la solicitud", errors));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiResponse<Void>> handleBusinessRule(BusinessRuleException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(ex.getMessage(), HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponse.error(ex.getMessage(), HttpStatus.NOT_FOUND.value()));
    }
}
```

---

## 6. Cliente HTTP Interno (`WebClient` hacia `ai-worker-api`)

Para comunicarse con el microservicio Python de IA y OCR:
* Se configura un Bean singleton de `WebClient` apuntando a `http://ai-worker-api:8000/internal/v1`.
* **Inyección automática de cabeceras:** Se inyecta la cabecera secreta `X-Internal-Api-Key`.
* **Timeouts:** 
  * Connect Timeout: 10 segundos.
  * Read Timeout: 120 segundos (para permitir procesamiento pesado de OCR y entrenamiento de ML).
* **Manejo de Reintentos:** Reintentos automáticos (máximo 3 con backoff exponencial) solo en errores transitorios de red (`502`, `503`, `504`).

---

## 7. Pruebas y Control de Calidad Obligatorio
Antes de dar por completado un módulo en `core-api`, la IA debe verificar:
1. **Pruebas Unitarias (JUnit 5 + Mockito):** Cobertura de la lógica de negocio y validación de transiciones de estado en la capa `service`.
2. **Pruebas de Integración (MockMvc + Testcontainers PostgreSQL):** Pruebas de endpoints REST asegurados con roles JWT y persistencia real.
3. **Comando de Verificación:**  
   `./mvnw clean test` (debe compilar y pasar con 0 fallos).
