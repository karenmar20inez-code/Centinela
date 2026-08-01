# Plan de Implementación - Rutas Dinámicas Minimalistas (Sin Zoom)

El usuario solicita un diseño específico basado en una imagen de referencia: una interfaz limpia con un selector de rutas tipo "pill", una ruta sólida oscura que sigue las calles y, fundamentalmente, **evitar el zoom automático** que aleja la cámara al trazar la ruta.

## Proposed Changes

### Interfaz de Usuario (UX/UI)

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   **Selector de Ruta**: Implementar la "pastilla" (pill) blanca en la parte inferior con las opciones "Segura" y "Rápida".
*   **Ocultar Planificador**: Tras presionar "Trazar", el planificador se ocultará para mostrar el mapa a pantalla completa.
*   **Sello C5**: Reposicionar el sello de seguridad en la esquina inferior izquierda.
*   **Botones Flotantes**: Mantener Menú (arriba-izq) y Búsqueda (arriba-der).

### Lógica de Mapas y Rutas

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   **Trazado Dinámico (Fijar Calles)**: Usar Mapbox Directions con precisión 6 para asegurar que la ruta serpentea por las calles.
*   **Estilo Visual**: Línea de color azul medianoche sólido con un grosor de 8.0 y un borde blanco (casing) para que resalte.
*   **Cámara Estática**: **Eliminar** cualquier comando que mueva o aleje la cámara tras el trazado. La ruta aparecerá en el mapa respetando el zoom actual del usuario.
*   **Depuración Robusta**: Añadir logs en cada etapa (Geocoding -> Directions -> Render) para asegurar que la ruta se procesa correctamente.

## Verification Plan

### Manual Verification
1.  Ingresar origen y destino.
2.  Presionar "Trazar".
3.  Verificar que el planificador desaparece.
4.  Confirmar que la ruta aparece como una línea oscura por las calles **sin que el mapa cambie de zoom**.
5.  Alternar entre "Segura" y "Rápida" en el selector inferior y ver cómo cambia el trazado instantáneamente.
