# COMPONENTS_AND_DESIGN_SYSTEM.md
# Sistema de Componentes y Librería de Diseño (Design System)

## 1. Arquitectura del Sistema de Componentes
El sistema de componentes se fundamenta en **`shadcn/ui`** (construido sobre primitivos accesibles de **Radix UI**) y estilizado mediante **Tailwind CSS**.

### Estructura en el Código:
```text
src/components/
├── ui/                 # Componentes base sin modificar de shadcn/ui
│   ├── button.tsx, dialog.tsx, table.tsx, input.tsx, badge.tsx, card.tsx...
└── shared/             # Componentes de negocio compuestos y reutilizables
    ├── DataTable.tsx
    ├── StatusBadge.tsx
    ├── ConfirmModal.tsx
    ├── DualOcrViewer.tsx
    ├── TramiteTimeline.tsx
    └── AlertaSemaphoreCard.tsx
```

---

## 2. Componentes Base Primitivos (`components/ui/`)

### 2.1. Botones (`Button.tsx`)
* **Variantes Disponibles:**
  * `default`: Azul institucional (`bg-blue-900 hover:bg-blue-800 text-white`). Para acciones principales de guardado y avance.
  * `destructive`: Rojo UAGRM (`bg-red-600 hover:bg-red-700 text-white`). Para rechazar trámites, revocar accesos o cancelar procesos.
  * `outline`: Fondo blanco con borde `border-slate-300` y texto `text-slate-700`. Para acciones secundarias y cancelación de modales.
  * `secondary`: Fondo suave `bg-slate-100 hover:bg-slate-200 text-slate-900`.
  * `ghost`: Sin borde ni fondo hasta el hover. Para botones de iconos en barras de herramientas.

---

## 3. Componentes Especializados del Dominio (`components/shared/`)

### 3.1. Visor Comparativo Dual (`DualOcrViewer.tsx`)
* **Propósito:** Interfaz de validación humana donde se contrasta el documento original con el texto extraído por Tesseract.
* **Comportamiento:**
  * Disposición horizontal en escritorio (`grid grid-cols-2 gap-4 h-[calc(100vh-140px)]`).
  * **Panel Izquierdo:** Contenedor de `react-pdf` envuelto en `TransformWrapper` (`react-zoom-pan-pinch`). Barra superior flotante con botones para Zoom In, Zoom Out, Reset (100%), Ajustar al Ancho y Rotar 90°.
  * **Panel Derecho:** Editor de texto con `textarea` enriquecido. Insignia en la cabecera mostrando el porcentaje de confianza:
    * `confianza >= 85%`: Badge verde.
    * `65% <= confianza < 85%`: Badge ámbar.
    * `confianza < 65%`: Badge rojo con advertencia de baja calidad de escaneo.
  * Botón inferior de confirmación: *"Confirmar y Validar Texto"*.

---

### 3.2. Stepper de la Máquina de Estados (`TramiteTimeline.tsx`)
* **Propósito:** Mostrar la progresión visual del expediente a lo largo de sus estados:
  $$\text{INGRESADO} \rightarrow \text{EN\_VERIFICACION} \rightarrow \text{OBSERVADO} \rightarrow \text{CON\_DICTAMEN} \rightarrow \text{APROBADO/RECHAZADO} \rightarrow \text{CONCLUIDO}$$
* **Estados Visuales de los Nodos:**
  * **Completado:** Círculo verde con icono `Check` y línea conectora verde.
  * **Activo:** Círculo azul pulsante con el nombre del estado actual en negrita.
  * **Observado:** Círculo ámbar con icono `AlertCircle` indicando que requiere subsanación.
  * **Pendiente:** Círculo gris suave con borde discontinuo.

---

### 3.3. Tarjeta Semafórica de Alerta (`AlertaSemaphoreCard.tsx`)
* **Propósito:** Renderizar de forma inmediata la gravedad de una alerta operativa o predictiva.
* **Propiedades Visuales:**
  * Borde izquierdo grueso (4px) con el color de la criticidad (`border-l-red-600`, `border-l-amber-500` o `border-l-emerald-500`).
  * Indicador circular de color pulsante.
  * Título de la alerta, materia involucrada, probabilidad $P$ calculada y fecha de detección.
  * Botón contextual: *"Registrar Medida"* (abre modal para atención directiva).

---

### 3.4. Modal de Confirmación Crítica (`ConfirmModal.tsx`)
* **Propósito:** Blindar operaciones irreversibles (aprobación/rechazo de expedientes, eliminación de invitaciones o transferencia de dirección).
* **Elementos:**
  * Icono de advertencia destacado.
  * Título explicativo claro y texto de consecuencias.
  * Botón de confirmación con color condicional y botón de cancelar.
  * Opcional: Campo de confirmación mediante reingreso de contraseña (obligatorio en Transferencia de Dirección).
