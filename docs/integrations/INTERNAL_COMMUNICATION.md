# INTERNAL_COMMUNICATION.md
# Protocolo de Comunicación Interna Inter-Servicios (`core-api` $\leftrightarrow$ `ai-worker-api`)

## 1. Topología y Filosofía de Comunicación
La comunicación entre el backend transaccional (`core-api`, Java 17 / Spring Boot) y el microservicio de inteligencia artificial (`ai-worker-api`, Python 3.10 / FastAPI) se realiza mediante peticiones **HTTP RESTful sincrónicas/asincrónicas** confinadas estrictamente a la red interna de Docker (`ficct_network`).

### Reglas de Diseño:
1. **Frontera Segura:** El microservicio `ai-worker-api` no expone puertos públicos al host exterior. Solo escucha en la red privada en `http://ai-worker-api:8000`.
2. **Autenticación Compartida:** Todas las peticiones deben incluir obligatoriamente la cabecera secreta:
   ```http
   X-Internal-Api-Key: <INTERNAL_SHARED_SECRET>
   ```
3. **Cero Transferencia de Binarios Pesados:** Las llamadas REST únicamente intercambian identificadores, metadatos y SAS URLs de Azure Blob (o rutas en el volumen compartido `/app/storage`), evitando saturar el socket HTTP con archivos binarios de varios megabytes.

---

## 2. Configuración del Cliente Reactivo en Spring Boot (`WebClient`)

En `core-api`, las llamadas se gestionan mediante un Bean singleton de `WebClient` configurado con Netty para soportar procesamiento reactivo y control estricto de timeouts:

```java
@Configuration
public class WebClientConfig {

    @Value("${ai-worker.base-url:http://ai-worker-api:8000/internal/v1}")
    private String aiWorkerBaseUrl;

    @Value("${ai-worker.api-key}")
    private String internalApiKey;

    @Bean
    public WebClient aiWorkerWebClient() {
        HttpClient httpClient = HttpClient.create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000) // 10 segundos
            .responseTimeout(Duration.ofSeconds(120))            // 120 segundos para tareas pesadas
            .doOnConnected(conn -> 
                conn.addHandlerLast(new ReadTimeoutHandler(120))
                    .addHandlerLast(new WriteTimeoutHandler(10)));

        return WebClient.builder()
            .baseUrl(aiWorkerBaseUrl)
            .defaultHeader("X-Internal-Api-Key", internalApiKey)
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(new ReactorClientHttpConnector(httpClient))
            .build();
    }
}
```

---

## 3. Resiliencia, Timeouts y Política de Reintentos

* **Tareas Rápidas (NLP Query, Predict ML):** Timeout de respuesta de **10 segundos**. En caso de fallo transitorio (`502 Bad Gateway`, `503 Service Unavailable`, `504 Gateway Timeout`), se ejecutan hasta 3 reintentos automáticos con backoff exponencial ($1\text{s}, 2\text{s}, 4\text{s}$).
* **Tareas Pesadas (OCR de PDFs multi-página, Train ML, RPA):** Timeout de respuesta de **120 segundos**. No se reintenta automáticamente el entrenamiento si este ya comenzó a ejecutarse, para evitar sobrecargar la CPU.

---

## 4. Contratos de Comunicación por Capacidad

### 4.1. Extracción OCR (`POST /internal/v1/ocr/extract`)
* **Request (`core-api` $\rightarrow$ `ai-worker-api`):**
  ```json
  {
    "documento_id": 104,
    "azure_read_sas_url": "https://ficctstorage.blob.core.windows.net/.../carta.pdf?sp=r&sig=...",
    "ruta_archivo_local": "/app/storage/tramites/2026/11/carta.pdf",
    "tipo_mime": "application/pdf",
    "idioma": "spa"
  }
  ```
* **Response 200 OK (`ai-worker-api` $\rightarrow$ `core-api`):**
  ```json
  {
    "documento_id": 104,
    "exito": true,
    "texto_extraido_crudo": "SOLICITUD DE CASO ESPECIAL...",
    "nivel_confianza": 92.45,
    "paginas_procesadas": 1,
    "pipeline_aplicado": ["grayscale", "gaussian_blur_3x3", "otsu_binarization", "deskew"],
    "error_detalle": null
  }
  ```

### 4.2. Inferencia Predictiva de Cupos (`POST /internal/v1/ml/predict`)
* **Request:** Identificador del periodo semestral objetivo (`periodo_objetivo_id: 12`).
* **Response 200 OK:** Lista de asignaturas evaluadas con `demanda_proyectada_estudiantes`, `cupos_actuales_planificados`, `deficit_estimado`, `probabilidad_sobredemanda` y `nivel_criticidad_sugerido` (`ROJO`, `AMARILLO`, `VERDE`).

### 4.3. Consulta Asistente NLP (`POST /internal/v1/assistant/query`)
* **Request:** `usuario_id`, `rol_usuario`, `consulta_usuario` y `canal_entrada` (`TEXTO`/`VOZ`).
* **Response 200 OK:** `respuesta_generada`, `fuentes_consultadas` y `tiempo_respuesta_ms`.

### 4.4. Lanzamiento de Tarea RPA (`POST /internal/v1/rpa/execute`)
* **Request:** `ejecucion_id`, `tipo_flujo`, parámetros del periodo y `azure_write_sas_url`.
* **Response 200 OK:** `estado`, `registros_procesados`, `registros_con_error`, `hash_sha256` y `log_incidencias`.
