# ARCHITECTURE_REACT_TS.md
# Arquitectura y Estándares Técnicos del Frontend Web (`web-client`)

## 1. Propósito y Filosofía del Componente
El componente `web-client` es la **interfaz de usuario web institucional** del *Sistema Web de Gestión Administrativa con Alertas Predictivas y OCR para la Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)*.

### Principios Rectores:
1. **Sistema de Uso Interno Exclusivo (Intranet Directiva):** No existe portal público para estudiantes. La interfaz está diseñada para optimizar los flujos de trabajo administrativos de tres tipos de funcionarios: **Director de Carrera**, **Coordinador Académico** y **Auxiliar de Ventanilla/Secretaría**.
2. **Arquitectura Orientada a Características (Feature-Driven Architecture):** Los componentes, hooks, esquemas y páginas se organizan por módulos de negocio, permitiendo un desarrollo desacoplado y escalable.
3. **Separación Estricta de Estados:**
   * **Estado del Servidor (Server State):** Gestionado exclusivamente con `@tanstack/react-query` (caché, reintentos, refetching e invalidación tras mutaciones).
   * **Estado de la Aplicación Cliente (Client State):** Gestionado de forma liviana con `zustand` (sesión JWT, rol activo, estado de la barra lateral, preferencias del Visor Dual).
4. **Soberanía Humana y Ergonomía Visual:** Las decisiones críticas (validación de texto OCR, resolución de alertas predictivas y transiciones de trámites) disponen de interfaces visuales especializadas (visor dual comparativo, semáforos de criticidad y modales de dictamen con confirmación).

---

## 2. Stack Tecnológico Oficial

* **Core:** React 18, TypeScript 5.x, Vite (bundler ultrarrápido).
* **Estilos y Componentes Primitivos:**
  * `tailwindcss` (estilos utilitarios y diseño responsivo institucional).
  * `shadcn/ui` (componentes accesibles basados en Radix UI: Dialog, Table, Form, Badge, Tabs, Dropdown, etc.).
  * `lucide-react` (iconografía limpia, moderna e institucional).
* **Gestión de Estado y Servidor:**
  * `@tanstack/react-query` (v5) para fetching y caché reactiva.
  * `zustand` para el estado global del cliente.
* **Formularios y Validación:**
  * `react-hook-form` + `@hookform/resolvers/zod` + `zod` (validaciones cliente sincronizadas con los DTOs de Spring Boot).
* **Visualización y Tablas:**
  * `@tanstack/react-table` (tablas avanzadas con paginación, filtros por columna y ordenamiento).
  * `recharts` (gráficos ejecutivos de demanda vs. cupos, KPIs y avance del POA).
* **Visor Especializado Dual OCR:**
  * `react-pdf` (renderizado nativo de documentos PDF en el navegador).
  * `react-zoom-pan-pinch` (zoom, arrastre y rotación fluida de documentos digitalizados).
* **Cliente HTTP:** `axios` con interceptores globales para inyección de token JWT y control de expiración de sesión (HTTP 401/403).

---

## 3. Estructura de Directorios Estricta

```text
web-client/src/
│
├── assets/                                # Identidad institucional (logos UAGRM, FICCT, sellos)
├── components/
│   ├── ui/                                # Componentes base de shadcn/ui (Button, Dialog, Input...)
│   └── shared/                            # Componentes transversales reutilizables
│       ├── Header.tsx                     # Barra superior con datos del funcionario y rol activo
│       ├── Sidebar.tsx                    # Menú lateral dinámico filtrado según RBAC
│       ├── StatusBadge.tsx                # Pastilla de estado de trámites y alertas con semáforo
│       ├── DataTable.tsx                  # Envoltorio genérico de TanStack Table con paginación
│       └── ConfirmDialog.tsx              # Modal de confirmación para acciones destructivas/críticas
│
├── layouts/                               # Plantillas de estructura de pantalla
│   ├── AuthLayout.tsx                     # Pantalla de Login e ingreso con invitación
│   └── DashboardLayout.tsx                # Contenedor principal con Sidebar, Header y Breadcrumbs
│
├── routes/                                # Sistema de navegación y guardas de seguridad
│   ├── AppRoutes.tsx                      # Definición central de rutas con react-router-dom v6
│   └── RoleGuard.tsx                      # Componente de protección de rutas según rol activo
│
├── stores/                                # Estados globales livianos con Zustand
│   ├── useAuthStore.ts                    # Token JWT, datos de usuario, rol activo y logout
│   ├── useUiStore.ts                      # Colapso de barra lateral, temas y notificaciones
│   └── useOcrViewerStore.ts               # Nivel de zoom, rotación y coordenadas del visor OCR
│
├── lib/                                   # Utilidades e instancias globales
│   ├── axios.ts                           # Instancia Axios con interceptores de Auth
│   ├── queryClient.ts                     # Configuración global de React Query (tiempos de caché)
│   └── utils.ts                           # Utilidad cn() para combinación de clases Tailwind
│
└── features/                              # Módulos Funcionales del Negocio (Feature-Driven)
    ├── auth/                              # Login, recuperación de contraseña, aceptar invitaciones
    ├── documental/                        # Correspondencia recibida/emitida y subida de archivos
    ├── tramites/                          # Expedientes, requisitos y Máquina de Estados
    ├── ocr/                               # Visor Comparativo Dual y validación humana
    ├── academico/                         # Planificación semestral, materias, grupos y carga docente
    ├── analitica/                         # Dashboard ejecutivo, semaforización predictiva de cupos
    ├── institucional/                     # Titulados, investigaciones, POA y eventos
    └── rpa/                               # Monitor de bots Playwright y descargas consolidadas
```

### Estructura Interna Obligatoria de Cada Feature:
Dentro de cada módulo en `features/` (por ejemplo, `features/tramites/`):

```text
features/tramites/
├── api/                  # Hooks personalizados de React Query (queries y mutaciones)
│   ├── useTramitesQuery.ts
│   └── useTransicionMutation.ts
├── components/           # Componentes exclusivos de este módulo
│   ├── TramiteTimeline.tsx       # Renderizado visual del Stepper de la State Machine
│   ├── TransicionModal.tsx       # Modal dinámico con transiciones válidas según rol
│   └── RequisitosChecklist.tsx   # Lista de requisitos con estado CONFORME/OBSERVADO
├── schemas/              # Esquemas Zod y tipos TypeScript
│   └── tramiteSchemas.ts
└── pages/                # Pantallas completas enlazadas a las rutas
    ├── TramitesListPage.tsx
    └── TramiteDetailPage.tsx
```

---

## 4. Control de Acceso Basado en Roles (RBAC en el Frontend)

Las pantallas y elementos de navegación se adaptan dinámicamente al rol del usuario en sesión (`ROLE_DIRECTOR`, `ROLE_COORDINADOR`, `ROLE_AUXILIAR`).

### Componente de Protección de Rutas (`RoleGuard.tsx`):
```tsx
import { Navigate, Outlet } from "react-router-dom";
import { useAuthStore } from "@/stores/useAuthStore";

interface RoleGuardProps {
  allowedRoles: Array<"ROLE_DIRECTOR" | "ROLE_COORDINADOR" | "ROLE_AUXILIAR">;
}

export const RoleGuard = ({ allowedRoles }: RoleGuardProps) => {
  const { isAuthenticated, user } = useAuthStore();

  if (!isAuthenticated || !user) {
    return <Navigate to="/login" replace />;
  }

  if (!allowedRoles.includes(user.rol)) {
    return <Navigate to="/unauthorized" replace />;
  }

  return <Outlet />;
};
```

---

## 5. Especificación de Componentes Críticos Especializados

### 5.1. Visor Comparativo Dual OCR (`features/ocr/components/DualOcrViewer.tsx`)
Este componente materializa el principio de **Soberanía Humana Obligatoria**:
* **Distribución de Pantalla:** Disposición dividida 50% / 50% (pantalla partida en escritorio):
  * **Panel Izquierdo (Documento Original):**
    * Renderizado nativo del PDF o imagen con `react-pdf`.
    * Barra de herramientas con: Zoom (+ / - / Reset), Ajuste al ancho, Rotación (90° a la derecha) y Navegación entre páginas (`react-zoom-pan-pinch`).
  * **Panel Derecho (Editor de Validación Humana):**
    * Encabezado con: Nivel de confianza promedio devuelto por Tesseract (ej. `92.4% Confiable` en verde o `68.1% Requiere Atención` en ámbar).
    * Editor de texto enriquecido o textarea autoajustable con el texto extraído (`textoExtraidoCrudo`).
    * Resaltado visual de caracteres de baja confianza.
    * Botón de acción principal: **"Aprobar y Validar Texto"** (deshabilita el botón mientras se ejecuta la mutación `PUT /api/v1/ocr/{id}/validar`, que dispara el trigger en PostgreSQL para poblar el `tsvector`).

---

### 5.2. Línea de Tiempo de la Máquina de Estados (`TramiteTimeline.tsx`)
* Representa gráficamente los estados del expediente:
  `INGRESADO` $\rightarrow$ `EN_VERIFICACION` $\rightarrow$ (`OBSERVADO` $\leftrightarrow$ `EN_VERIFICACION`) $\rightarrow$ `CON_DICTAMEN` $\rightarrow$ (`APROBADO` | `RECHAZADO`) $\rightarrow$ `CONCLUIDO`.
* Muestra claramente:
  * El estado actual con indicador pulsante.
  * Quién ejecutó cada transición previa, fecha/hora y el dictamen/comentario correspondiente.
  * Botón de acción **"Ejecutar Siguiente Actuación"** que despliega el modal con únicamente los estados válidos según el rol del usuario autenticado (ej. solo el Director verá las opciones de `APROBADO` o `RECHAZADO`).

---

### 5.3. Dashboard Predictivo y Semáforos de Cupos (`features/analitica/`)
* **Tarjetas KPI Superiores:** Total de trámites activos, alertas pendientes, correspondencia sin responder y porcentaje global de avance del POA.
* **Tabla de Proyección Semestral de Cupos:**
  * Muestra por materia: Cupos planificados, demanda estimada por Machine Learning y déficit proyectado.
  * **Semaforización Visual Estricta:**
    * 🔴 **Rojo ($P \ge 0.75$):** Sobredemanda crítica inminente.
    * 🟡 **Amarillo ($0.50 \le P < 0.75$):** Capacidad en límite de alerta.
    * 🟢 **Verde ($P < 0.50$):** Capacidad suficiente.
  * Botón directo en la fila: **"Registrar Medida Administrativa"** (abre modal para que el Director autorice apertura de grupo paralelo o ampliación de cupos, resolviendo la alerta mediante `POST /api/v1/alertas/{id}/atender`).

---

## 6. Cliente HTTP y Gestión de Sesión (`lib/axios.ts`)

* **Configuración del Cliente Axios:**
  * Inyección automática del token JWT en la cabecera `Authorization: Bearer <token>`.
  * Manejo global de expiración de sesión: si el backend devuelve `HTTP 401 Unauthorized`, el interceptor elimina automáticamente el token del store de Zustand y redirige al usuario a `/login` con un mensaje amigable ("Su sesión ha expirado").

```typescript
import axios from "axios";
import { useAuthStore } from "@/stores/useAuthStore";

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || "/api/v1",
  headers: {
    "Content-Type": "application/json",
  },
});

apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token;
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      useAuthStore.getState().logout();
      window.location.href = "/login";
    }
    return Promise.reject(error);
  }
);
```

---

## 7. Paleta Institucional y Guía de Estilos

Para reflejar la identidad formal de la **Facultad de Ingeniería en Ciencias de la Computación y Telecomunicaciones (FICCT - UAGRM)**:
* **Color Primario (Azul Institucional):** `#0f2942` (Dark Blue) / `#1e3a8a` (Blue 900) para barras laterales, botones principales y encabezados.
* **Color Secundario (Acento):** `#dc2626` (Rojo UAGRM) para insignias de alerta crítica, cancelaciones o acciones de alta atención.
* **Fondos y Paneles:** `#f8fafc` (Slate 50) para áreas de trabajo y `#ffffff` para tarjetas con bordes suaves (`border-slate-200`).
* **Tipografía:** Inter o Roboto con pesos visuales claros (`font-medium` y `font-semibold`).

---

## 8. Pruebas y Control de Calidad Obligatorio
Antes de dar por completado un módulo en `web-client`, la IA debe verificar:
1. **Tipado Estricto de TypeScript:** Compilación con 0 errores de tipo (`tsc --noEmit`).
2. **Pruebas de Componentes (Vitest + React Testing Library):** Renderizado correcto de tablas, validaciones de formularios Zod y protección de rutas `RoleGuard`.
3. **Comando de Verificación:**  
   `npm run build` (debe generar el bundle de producción sin advertencias críticas ni fallos de compilación).
