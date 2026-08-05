# Plan de Implementación - Rutas de Alta Precisión y Tráfico Real

Se mejorará el sistema de navegación para utilizar datos de tráfico en tiempo real, proporcionando estimaciones de tiempo precisas tanto con tráfico actual como en condiciones normales (sin tráfico).

## Proposed Changes

### 1. Estructura de Datos Mejorada

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   Actualizar `RouteInfo` para incluir:
    *   `duration`: Tiempo con tráfico actual (segundos).
    *   `durationTypical`: Tiempo típico sin tráfico (segundos).
    *   `distance`: Distancia total (metros).

### 2. Lógica de Navegación Profesional

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   Cambiar el perfil de ruta a `DirectionsCriteria.PROFILE_DRIVING_TRAFFIC`.
*   Extraer los metadatos de duración y duración típica del objeto `DirectionsRoute`.
*   Asegurar el uso de `GEOMETRY_POLYLINE6` para máxima precisión en las curvas de las calles.

### 3. Interfaz de Usuario (Feedback de Tiempo)

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   Añadir una pequeña tarjeta de información sobre el selector de rutas (Pill) que muestre:
    *   Tiempo estimado (ej: "12 min").
    *   Diferencia de tráfico (ej: "+3 min de tráfico" o "Tráfico fluido").
    *   Distancia total.
*   Aplicar el efecto Glassmorphism a esta nueva tarjeta.

## Verification Plan

### Manual Verification
1.  Trazar una ruta en una zona conocida (ej. Zócalo -> Polanco).
2.  Verificar que aparece la información de tiempo.
3.  Confirmar que al cambiar entre "Segura" y "Rápida", los tiempos se actualizan correctamente.
4.  Observar que la ruta sigue con precisión milimétrica el trazado de las calles.
