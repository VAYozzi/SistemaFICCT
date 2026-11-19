# CODING_STANDARDS.md
# Estándares Unificados de Código y Convenciones de Desarrollo

## 1. Convenciones Globales y Git Commits

### 1.1. Convención de Commits (Conventional Commits 1.0)
Todos los mensajes de confirmación en Git generados por desarrolladores o agentes de IA deben respetar el formato:
```text
<tipo>(<alcance>): <descripción corta en imperativo>
```
* `feat`: Nueva funcionalidad (ej. `feat(tramites): implementar transicion de estado en state machine`).
* `fix`: Corrección de bug (ej. `fix(ocr): corregir rotacion de matriz en deskewing`).
* `docs`: Cambios en especificaciones o documentación (ej. `docs(api): actualizar contrato de ocr`).
* `test`: Adición o refactorización de pruebas (ej. `test(auth): agregar prueba unitaria de transferencia`).
* `refactor`: Cambio de código sin alterar comportamiento (ej. `refactor(db): optimizar indices en bitacora`).

---

## 2. Estándares en Backend Java 17 (`core-api`)

* **Guía Base:** Google Java Style Guide.
* **Nomenclatura:**
  * Clases e Interfaces: `UpperCamelCase` (`TramiteEstudiantilService`, `JwtTokenProvider`).
  * Métodos y Variables: `lowerCamelCase` (`calcularDeficitCupos`, `fechaRegistro`).
  * Constantes: `UPPER_SNAKE_CASE` (`MAX_LOGIN_ATTEMPTS`, `DEFAULT_PAGE_SIZE`).
* **Reglas Específicas de Spring Boot:**
  * Inyección de dependencias **exclusivamente por constructor** mediante `@RequiredArgsConstructor` de Lombok (nunca con `@Autowired` en campos).
  * Uso obligatorio de Java 17 `record` para todo objeto DTO.
  * Prohibido el uso de `@Data` en entidades JPA; utilizar explícitamente `@Getter`, `@Setter`, `@NoArgsConstructor`, `@AllArgsConstructor`, `@Builder`.
  * Todo método que modifique base de datos debe declarar `@Transactional(rollbackFor = Exception.class)`.

---

## 3. Estándares en Microservicio Python 3.10 (`ai-worker-api`)

* **Guía Base:** PEP-8 con formateador `Black` y linter `Ruff`.
* **Tipado Estricto:** Toda función debe declarar tipos de entrada y retorno (`typing` / Python 3.10 union `|`):
  ```python
  def preprocess_image(image_bytes: bytes, dpi: int = 300) -> np.ndarray:
  ```
* **Nomenclatura:**
  * Módulos y funciones: `snake_case` (`extract_text_from_pdf`, `deskew_image`).
  * Clases: `PascalCase` (`OcrEngine`, `RandomForestTrainer`).
* **Regla Crítica de FastAPI:** Las llamadas a librerías de CPU intensivo (OpenCV, Tesseract, Scikit-Learn) deben encapsularse con `asyncio.to_thread(...)`.

---

## 4. Estándares en Frontend React 18 / TypeScript (`web-client`)

* **Tipado Estricto:** Prohibido el uso de `any` (utilizar `unknown` con *type guards* si el tipo es dinámico).
* **Nomenclatura:**
  * Componentes y Páginas: `PascalCase` (`DualOcrViewer.tsx`, `TramiteDetailPage.tsx`).
  * Hooks personalizados: `camelCase` con prefijo `use` (`useTramitesQuery.ts`).
  * Stores de Zustand: `camelCase` con prefijo `use` y sufijo `Store` (`useAuthStore.ts`).
* **Estructura de Componentes:** Componentes funcionales pequeños con tipado explícito de `props`:
  ```tsx
  interface StatusBadgeProps {
    status: "INGRESADO" | "EN_VERIFICACION" | "CON_DICTAMEN" | "APROBADO" | "RECHAZADO";
  }
  export const StatusBadge = ({ status }: StatusBadgeProps) => { ... };
  ```

---

## 5. Estándares en Base de Datos PostgreSQL 15+

* **Nomenclatura SQL:**
  * Tablas: `snake_case` en **plural** (`tramites_estudiantiles`, `procesos_ocr`).
  * Columnas: `snake_case` en **singular** (`id`, `codigo_expediente`, `fecha_creacion`).
  * Palabras clave de SQL en **MAYÚSCULAS** (`SELECT`, `INSERT`, `CREATE TABLE`, `WHERE`).
  * Llaves primarias: siempre llamadas `id` de tipo `BIGSERIAL` o `BIGINT`.
  * Claves foráneas: `<tabla_en_singular>_id` (ej. `estudiante_id`, `usuario_id`).
