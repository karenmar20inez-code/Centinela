# Plan de Implementación - SOS por Patrón de Movimiento Específico (Doble Sacudida)

Se implementará un algoritmo de detección de gestos mediante el acelerómetro para identificar un patrón exacto: **dos sacudidas completas (arriba-abajo, arriba-abajo)**. Esto servirá como disparador físico del SOS, garantizando que movimientos aleatorios no activen la alarma por error.

## Proposed Changes

### Lógica de Detección de Patrón

#### [MODIFICAR] [SosService.kt](file:///C:/Users/lis_0/StudioProjects/Centinela/app/src/main/java/com/example/centinela/ui/SosService.kt)
*   **SensorEventListener**: Implementar el monitoreo constante del `TYPE_ACCELEROMETER`.
*   **Algoritmo de Estado**:
    1.  Detectar un movimiento hacia **arriba** (aceleración positiva fuerte en el eje Y).
    2.  Detectar un movimiento hacia **abajo** (aceleración negativa fuerte en el eje Y).
    3.  Contar esto como **una ciclo completo**.
    4.  Si se completan **dos ciclos** en menos de 2 segundos, disparar la emergencia.
*   **Umbral de Fuerza**: Definir una sensibilidad de aprox. 12-15 m/s² para asegurar que el movimiento sea intencional y no producto de caminar.

### Sincronización y Respuesta
*   Al detectar el patrón de 2 sacudidas, el servicio llamará a `dispararEmergencia()`.
*   Se añadirá una pequeña **vibración de confirmación** táctil para que el usuario sepa que el celular "sintió" la sacudida y está activando el auxilio.

## Verification Plan

### Manual Verification
1.  **En el Emulador**: Ir a `Virtual Sensors` -> `Move`. Realizar movimientos rápidos de arriba a abajo.
2.  **Logs**: Monitorear `SOS_Debug` para ver los mensajes:
    *   `Sacudida detected: Ciclo 1/2`
    *   `Sacudida detected: Ciclo 2/2 -> ¡SOS!`
3.  **Resultado**: La app debe abrirse y enviar los SMS tras el patrón exacto.

> [!IMPORTANT]
> El patrón de "Arriba-Abajo x2" es mucho más seguro que una sacudida simple, ya que imita un gesto de urgencia deliberado.
