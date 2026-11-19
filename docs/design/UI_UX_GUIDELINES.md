# UI_UX_GUIDELINES.md
# Directrices de Diseño Visual y Experiencia de Usuario (UI/UX)

## 1. Filosofía de Diseño
El diseño de la interfaz de usuario de la **Dirección de Carrera de Ingeniería en Sistemas (FICCT - UAGRM)** responde a los siguientes pilares:
1. **Identidad Institucional:** Refleja la formalidad, sobriedad y prestigio de la Universidad Autónoma Gabriel René Moreno.
2. **Eficiencia Administrativa:** Diseñada para funcionarios que operan el sistema de 4 a 8 horas diarias; prioriza la legibilidad, reduce la fatiga visual y minimiza la cantidad de clics para completar un trámite.
3. **Ergonomía en Tareas Críticas:** Interfaces especializadas para tareas cognitivamente demandantes, como la revisión comparativa de documentos OCR y la toma de decisiones sobre alertas semafóricas.
4. **Accesibilidad Web (WCAG 2.1 AA):** Ratios de contraste de color superiores a 4.5:1 para texto normal y estados interactivos claros (hover, focus, disabled).

---

## 2. Paleta de Colores Oficial (Identidad UAGRM / FICCT)

### 2.1. Colores de Marca Institucional
* **Azul Institucional Primario (FICCT):**
  * `Primary Dark`: `#0f2942` (Barra lateral principal, títulos ejecutivos).
  * `Primary Default`: `#1e3a8a` (Botones de acción primaria, enlaces activos).
  * `Primary Light`: `#3b82f6` (Bordes de foco, estados hover).
* **Rojo UAGRM (Acento y Alerta Máxima):**
  * `Accent Red`: `#dc2626` (Badges de criticidad roja, botones de acciones destructivas/rechazo).
  * `Accent Red Light`: `#fee2e2` (Fondos de tarjetas de alerta crítica).

### 2.2. Colores Semafóricos de Decisión
* 🔴 **Semáforo Crítico (Rojo):**
  * Texto / Icono: `#b91c1c` (`text-red-700`)
  * Fondo: `#fef2f2` (`bg-red-50`)
  * Borde: `#fecaca` (`border-red-200`)
* 🟡 **Semáforo Advertencia (Ámbar):**
  * Texto / Icono: `#b45309` (`text-amber-700`)
  * Fondo: `#fffbeb` (`bg-amber-50`)
  * Borde: `#fde68a` (`border-amber-200`)
* 🟢 **Semáforo Óptimo / Éxito (Verde):**
  * Texto / Icono: `#047857` (`text-emerald-700`)
  * Fondo: `#ecfdf5` (`bg-emerald-50`)
  * Borde: `#a7f3d0` (`border-emerald-200`)

### 2.3. Fondos y Superficies Neutras
* **Fondo de Aplicación:** `#f8fafc` (`slate-50`).
* **Tarjetas y Paneles Blancos:** `#ffffff` (`white`).
* **Bordes de Separación Suave:** `#e2e8f0` (`slate-200`).
* **Texto Principal:** `#0f172a` (`slate-900`).
* **Texto Secundario / Metadatos:** `#64748b` (`slate-500`).

---

## 3. Tipografía y Jerarquía Visual

* **Familia Tipográfica:** `Inter`, `system-ui`, `-apple-system`, `sans-serif`.
* **Escala Tipográfica:**
  * **H1 (Encabezados de Página):** `text-2xl font-bold text-slate-900 tracking-tight` (24px).
  * **H2 (Títulos de Sección / Modales):** `text-lg font-semibold text-slate-800` (18px).
  * **H3 (Títulos de Tarjetas / Tablas):** `text-base font-medium text-slate-700` (16px).
  * **Body (Texto de Contenido):** `text-sm font-normal text-slate-600 leading-relaxed` (14px).
  * **Captions / Metadatos:** `text-xs font-medium text-slate-400` (12px).

---

## 4. Retícula y Espaciado (Grid System)

* **Grid Base:** Múltiplos de 4px y 8px (estándar de Tailwind CSS).
* **Contenedores de Trabajo:** Máximo ancho de `max-w-7xl` centrado, con relleno horizontal `px-6 py-6`.
* **Disposición Responsiva de Escritorio:**
  * Optimizado primariamente para pantallas de **1366x768**, **1600x900** y **1920x1080** (resoluciones habituales de los equipos de oficina en la FICCT).
