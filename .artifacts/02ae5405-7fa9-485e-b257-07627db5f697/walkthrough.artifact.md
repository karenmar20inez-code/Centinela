# Walkthrough - Estabilización Crítica del Sistema SOS

He implementado una "coraza" técnica para asegurar que la aplicación Centinela no se cierre ni se reinicie cuando activas la emergencia. Ahora, el sistema es mucho más inteligente al manejar los eventos de hardware.

## Cambios Realizados

### 1. Sesión Única (Single Instance)
*   He configurado la aplicación para que **siempre exista una sola sesión activa**.
*   **¿Qué significa?**: Cuando presionas los botones de emergencia, Android ya no intentará abrir la app "desde cero" (lo cual causaba que se cerrara o se perdiera la ruta). Ahora, simplemente traerá tu mapa actual al frente, manteniendo tu ruta marcada intacta.

### 2. Lanzamiento Inteligente y Seguro
*   **Flags de Reordenamiento**: He actualizado las órdenes internas para que el SOS use el flag `REORDER_TO_FRONT`. Esto le dice al sistema: "No crees nada nuevo, solo pon lo que ya tengo abierto en primer plano".
*   **Detección de Visibilidad**: Si ya estás viendo el mapa, la app simplemente mostrará el aviso de alerta. Si el teléfono está bloqueado, usará el protocolo de alta prioridad para despertar la pantalla sin interrumpir el proceso.

### 3. Sincronización de Estado Total
*   Se añadió un refuerzo en `onNewIntent` para asegurar que el diálogo de SOS aparezca sí o sí cada vez que el servicio de hardware mande la señal, eliminando fallos visuales.

## Cómo verificar la estabilidad

1.  **Inicia una ruta**: Busca cualquier destino y deja que se dibuje en el mapa.
2.  **Prueba el SOS**: Bloquea la pantalla o sal a la pantalla de inicio del celular.
3.  **Activa el botón**: Presiona 3 veces Volumen Arriba.
4.  **Resultado**: La app aparecerá **exactamente donde la dejaste** (con tu ruta azul trazada) y mostrará el diálogo de emergencia encima. Ya no te sacará de la aplicación.

> [!TIP]
> Esta configuración es la más robusta posible para aplicaciones de seguridad, garantizando que el usuario nunca pierda su información de navegación durante una crisis.
