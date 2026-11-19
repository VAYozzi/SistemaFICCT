# NAVIGATION_AND_LAYOUT.md
# Estructura de Navegación, Disposición y Plantillas (Layouts)

## 1. Disposición General de la Aplicación (App Shell)

La interfaz se estructura bajo un diseño de panel administrativo moderno y fluido:

```text
┌─────────────────────────────────────────────────────────────────────────────┐
│                             TOP HEADER                                      │
│  [Logo FICCT] [Breadcrumbs: Inicio > Trámites > EXP-0012]     [🔔 3] [Perfil]│
├──────────────┬──────────────────────────────────────────────────────────────┤
│              │                                                              │
│   SIDEBAR    │                      MAIN CONTENT AREA                       │
│  NAVEGACIÓN  │                                                              │
│              │   ┌──────────────────────────────────────────────────────┐   │
│   (Filtrada  │   │  TÍTULO DE LA PÁGINA + ACCIÓN PRINCIPAL              │   │
│   según Rol  │   ├──────────────────────────────────────────────────────┤   │
│     RBAC)    │   │                                                      │   │
│              │   │  Contenido, Tablas, Gráficos o Visor Dual            │   │
│              │   │                                                      │   │
│              │   └──────────────────────────────────────────────────────┘   │
│              │                                                              │
└──────────────┴──────────────────────────────────────────────────────────────┘
```

---

## 2. Especificación de la Barra Lateral (`Sidebar.tsx`)

La barra lateral tiene un ancho fijo de `w-64` (256px), fondo institucional `#0f2942` y texto blanco/slate-300 con estados interactivos.

### Elementos de Navegación por Rol:

```text
[ENCABEZADO: Escudo UAGRM + FICCT - Ing. en Sistemas]

--- SECCIÓN GENERAL ---
* Dashboard Directivo          (Icono: LayoutDashboard)   -> Todos los roles
* Correspondencia Oficial      (Icono: Mail)              -> Todos los roles
* Expedientes y Trámites       (Icono: FileText)          -> Todos los roles
* Visor y Búsqueda OCR         (Icono: ScanText)          -> Todos los roles

--- SECCIÓN ACADÉMICA (Visible solo para COORDINADOR y DIRECTOR) ---
* Planificación de Cupos       (Icono: CalendarDays)      -> Coord / Director
* Malla y Prerrequisitos       (Icono: Network)           -> Todos los roles
* Carga Docente y Acefalías    (Icono: GraduationCap)     -> Coord / Director
* Titulados e Investigaciones  (Icono: Award)              -> Todos los roles
* Seguimiento POA              (Icono: Target)             -> Coord / Director

--- SECCIÓN AUTOMATIZACIÓN E INTELIGENCIA ---
* Alertas Predictivas          (Icono: AlertTriangle)     -> Todos los roles
* Monitor de Tareas RPA        (Icono: Bot)               -> Todos los roles

--- SECCIÓN ADMINISTRACIÓN (Exclusiva DIRECTOR) ---
* Personal e Invitaciones      (Icono: Users)             -> Solo Director
* Bitácora de Auditoría        (Icono: ShieldCheck)       -> Solo Director
* Transferir Dirección         (Icono: ArrowRightLeft)    -> Solo Director
```

---

## 3. Especificación de la Barra Superior (`Header.tsx`)

* **Rastro de Migas de Pan (Breadcrumbs):** Indica la ruta jerárquica actual en texto plano y enlaces navegables (ej. `Inicio` / `Trámites Estudiantiles` / `EXP-2026-0045`).
* **Campana de Alertas Rápidas:**
  * Muestra una pastilla roja con el número de alertas de nivel ROJO pendientes de atención humana.
  * Al hacer clic, despliega un menú desplegable flotante con las 3 alertas más críticas y acceso directo a la matriz completa de alertas.
* **Menú de Usuario y Rol Activo:**
  * Muestra el avatar con iniciales, nombre completo del funcionario y una pastilla destacada con su rol activo (`Director`, `Coordinador` o `Auxiliar`).
  * Dropdown con opciones: `Mi Perfil`, `Ayuda / Manual de Usuario` y `Cerrar Sesión`.

---

## 4. Pantallas Especiales de Error y Redirección

### 4.1. Error 403: Acceso Denegado (`UnauthorizedPage.tsx`)
* Se muestra cuando un usuario autenticado intenta acceder a una URL para la cual su rol no tiene privilegios (ej. un Auxiliar intentando abrir `/auditoria`).
* Presenta una ilustración sobria, mensaje claro: *"No cuenta con permisos suficientes para acceder a este recurso institucional"* y un botón para volver al Dashboard.

### 4.2. Error 401: Sesión Expirada
* Interceptado automáticamente por Axios. Redirige a `/login` desplegando un toast emergente: *"Su sesión ha expirado por motivos de seguridad. Por favor, ingrese nuevamente."*

### 4.3. Error 404: Recurso No Encontrado (`NotFoundPage.tsx`)
* Indica que el expediente, correspondencia o página solicitada no existe o fue dada de baja.
