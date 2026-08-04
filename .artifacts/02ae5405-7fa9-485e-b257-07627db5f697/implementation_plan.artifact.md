# Plan de Estabilización SOS - Prevención de Cierre de App

El análisis de los registros muestra que el SOS se activa correctamente (`DISPARANDO EMERGENCIA`), pero el sistema Android está bloqueando el relanzamiento de la actividad principal (`Background activity launch blocked`) o recreándola, lo que da la sensación de que la app se cierra o te saca.

## Proposed Changes

### 1. Configuración de Manifest (Persistencia)

#### [MODIFICAR] [AndroidManifest.xml](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/AndroidManifest.xml)
*   Cambiar `launchMode` de `MainActivity` a `singleInstance`. Esto asegura que Android no intente crear una copia nueva de la app al activar el SOS, sino que traiga la sesión actual al frente, manteniendo tu ruta y estado.

### 2. Optimización de SosService (Activación Silenciosa)

#### [MODIFICAR] [SosService.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/SosService.kt)
*   **Inteligencia de Lanzamiento**: Antes de llamar a `startActivity`, verificar si la app ya está visible. Si lo está, omitir el lanzamiento para evitar el parpadeo.
*   **Flags de Intento**: Refinar los flags para que solo traigan la app al frente (`FLAG_ACTIVITY_REORDER_TO_FRONT`) en lugar de reiniciarla.

### 3. Mejora de Interfaz (MapScreen)

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   Asegurar que el diálogo de alerta se muestre de forma no intrusiva sobre la ruta activa, permitiendo que el usuario siga viendo el mapa si lo desea.

## Verification Plan

### Manual Verification
1.  Iniciar una ruta en el mapa.
2.  Bloquear la pantalla (`Ctrl + P`).
3.  Presionar 3 veces Volumen Arriba.
4.  **Resultado esperado**: La pantalla se enciende, la app aparece **exactamente donde estaba** (con la ruta marcada) y muestra el diálogo de SOS por encima, sin cerrarse ni reiniciarse.
