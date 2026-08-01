# Plan de Implementación - Corrección de SOS y Mejora de Rutas

Se abordarán los problemas reportados: la falta de activación del SOS desde el botón físico y el error al encontrar rutas. Además, se pulirá el diseño para que coincida exactamente con la imagen de referencia.

## Proposed Changes

### Lógica de Emergencia

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   **Sincronización SOS**: Añadir un `LaunchedEffect` que observe el estado `dispararAlertaGlobal` de `MainActivity`. Esto permitirá que la alerta aparezca inmediatamente cuando se detecten los 3 toques del botón de encendido.

### Lógica de Navegación

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   **Robustez en Búsqueda**: Mejorar los logs y mensajes de error en `trazarRutaReal`. Si un lugar no se encuentra, el aviso indicará cuál falló (Origen o Destino).
*   **Geocodificación**: Asegurar que la búsqueda priorice resultados locales usando las coordenadas actuales del mapa.

### Interfaz de Usuario (UI)

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   **Ajuste de Capas**: Asegurar que el selector "Pill" y el sello "C5" no se tapen entre sí y coincidan con las alturas de la imagen.
*   **Estilo de Ruta**: Refinar el trazado para que sea de un azul muy oscuro con un borde blanco nítido.

## Verification Plan

### Manual Verification
1.  **SOS**: Bloquear pantalla y presionar 3 veces el botón de encendido. Confirmar que la app muestra la alerta.
2.  **Rutas**: Buscar "Zócalo" y "Bellas Artes". Confirmar que la ruta aparece siguiendo las calles sin alejar el zoom.
3.  **Selector**: Alternar entre "Segura" y "Rápida" y confirmar que el cambio es visual y sin movimiento de cámara.
