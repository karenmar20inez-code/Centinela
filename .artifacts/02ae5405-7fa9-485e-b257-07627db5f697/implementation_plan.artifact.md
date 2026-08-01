# Plan de Mejora Estética - Centinela Premium

El objetivo es transformar la interfaz actual en una experiencia más moderna, fluida y visualmente atractiva, manteniendo el enfoque en la seguridad y el minimalismo solicitado.

## Proposed Changes

### Interfaz del Mapa (UX/UI)

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   **Animación SOS**: Implementar un efecto de pulsación infinita en el botón rojo para darle dinamismo.
*   **Glassmorphism**: Ajustar el fondo del Planificador y el Selector de rutas para que sean semi-transparentes (`alpha 0.9`) con bordes más redondeados.
*   **Transiciones Animadas**: Usar `AnimatedVisibility` para que el buscador aparezca con un deslizamiento suave desde arriba.
*   **Estilo de Mapa Premium**: Cambiar a un estilo de mapa más limpio que mejore el contraste con la ruta azul.

### Identidad Visual

#### [MODIFICAR] [MapScreen.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/MapScreen.kt)
*   **Iconos de Ruta**: Añadir marcadores visuales (puntos brillantes o iconos) en los extremos de la ruta para que el inicio y el fin sean inequívocos.

#### [MODIFICAR] [Theme.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/theme/Theme.kt) / [Color.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/theme/Color.kt)
*   Definir un gradiente para los botones de acción principal (Trazar Ruta) para darles un aspecto más moderno.

## Verification Plan

### Manual Verification
1.  Verificar que el botón SOS tiene un efecto de "latido" visual.
2.  Confirmar que al presionar la lupa, el buscador baja suavemente desde arriba.
3.  Comprobar que la ruta azul ahora tiene marcadores claros en sus puntas.
4.  Validar que los cuadros de texto se ven modernos con el efecto de transparencia.
