# CONTEXTO DEL PROYECTO: EcoLim Collect App

## 1. Descripción General
Aplicación móvil nativa para la empresa ECOLIM S.A.C. destinada al registro digital y trazabilidad de residuos sólidos en plantas industriales. La app debe funcionar "Offline-First" (prioridad local) con sincronización posterior a una API REST.

## 2. Tech Stack & Reglas Técnicas
* **IDE:** Android Studio (Ladybug o superior).
* **Lenguaje:** Java (Estricto, no Kotlin).
* **UI:** XML layouts usando Material Design 3 (MDC).
* **Base de Datos Local:** SQLite (usando `SQLiteOpenHelper`).
* **Red:** Retrofit (para consumo de API REST).
* **Inteligencia Artificial:** TensorFlow Lite (para clasificación de imágenes offline).
* **Arquitectura:** MVC o MVVM básico (separación clara de lógica y vistas).

## 3. Estilo y Diseño (UI/UX)
* **Paleta de Colores:**
    * Primario: Verde Industrial (`#2E7D32`).
    * Fondo: Gris Claro (`#F5F5F5`).
    * Superficies: Blanco (`#FFFFFF`).
    * Error: Rojo Material (`#B00020`).
* **Tipografía:** Roboto.
* **Componentes Clave:** `CardView`, `FloatingActionButton`, `TextInputLayout` (Outlined).

## 4. Estructura de Base de Datos (SQLite)

### Tabla: `usuarios`
* `id_usuario` (INTEGER PK AUTOINCREMENT)
* `dni` (TEXT UNIQUE NOT NULL) - *Llave de acceso*
* `nombre` (TEXT NOT NULL)
* `password` (TEXT NOT NULL)
* `rol` (TEXT DEFAULT 'operario')

### Tabla: `residuos`
* `id_residuo` (INTEGER PK AUTOINCREMENT)
* `tipo` (TEXT NOT NULL) - *Valores: ORGANICO, PLASTICO, CARTON, METAL, PELIGROSO*
* `peso` (REAL NOT NULL)
* `fecha_registro` (TEXT NOT NULL) - *Formato ISO-8601*
* `origen_zona` (TEXT)
* `sincronizado` (INTEGER DEFAULT 0) - *0: Pendiente, 1: Enviado*

## 5. Módulos y Reglas de Negocio

### A. Login (Autenticación)
* **Identificador:** DNI (Documento Nacional de Identidad).
* **Validaciones:**
    * DNI debe contener solo números.
    * Longitud exacta: 8 dígitos.
    * Contraseña no vacía.
* **Sesión:** Persistir ID y Nombre en `SharedPreferences`.

### B. Dashboard (Pantalla Principal)
* Mostrar tarjeta resumen: "Total Kg Recolectados Hoy".
* Consulta SQL: `SELECT SUM(peso) FROM residuos WHERE fecha...`.
* Botón flotante (FAB) para ir a "Nuevo Registro".

### C. Registro Inteligente (Cámara + IA)
* **Input de Cámara:** Botón para abrir cámara.
* **Procesamiento:**
    1.  Capturar foto (Bitmap).
    2.  Pasar por modelo `.tflite` (TensorFlow Lite).
    3.  Obtener etiqueta con mayor probabilidad (ej: "PLASTICO").
    4.  **Auto-seleccionar** esa etiqueta en el Spinner del formulario.
* **Formulario Manual:**
    * Spinner "Tipo de Residuo" (obligatorio).
    * EditText "Peso" (Decimal, > 0.1 y < 1000 kg).

### D. Sincronización
* Intentar POST a API (`https://api.ecolim.com/v1/data`).
* Si falla (sin internet), guardar localmente (`sincronizado = 0`).
* Si hay éxito, marcar como (`sincronizado = 1`).

## 6. Prompt para Generación de Código
Si se solicita generar código, priorizar:
1.  Manejo de permisos en tiempo de ejecución (`CAMERA`, `INTERNET`).
2.  Código robusto con `try-catch` para operaciones de base de datos.
3.  Comentarios claros en español explicando la lógica.