# Walkthrough - SOS por Patrón de Movimiento (Doble Sacudida)

He implementado una función de seguridad avanzada que permite activar el SOS agitando el teléfono de una manera específica. Esto es ideal para situaciones donde no puedes ver la pantalla o presionar botones con precisión.

## Cambios Realizados

### Detección de Gestos con el Acelerómetro

1.  **Algoritmo "Arriba-Abajo x2"**: He programado un detector que busca un patrón exacto: mover el celular hacia arriba y luego hacia abajo, repetido dos veces.
2.  **Precisión y Seguridad**: El sistema solo se activa si completas los dos ciclos en menos de 2.5 segundos. Esto evita que se dispare por accidente al caminar, correr o si el teléfono se cae una sola vez.
3.  **Confirmación Táctil**: Cada vez que completas una sacudida correcta ("Arriba-Abajo"), el celular emitirá una pequeña vibración. Esto te sirve para saber que la app está contando tus movimientos.

### Integración Total

4.  **Servicio Siempre Activo**: Al igual que el botón de encendido, esta función vive en el `SosService`, por lo que **funciona incluso con la pantalla apagada o el celular bloqueado**.
5.  **Activación Automática**: Al completar las dos sacudidas, el sistema disparará la alerta visual de SOS y enviará los SMS a tus contactos de inmediato.

## Cómo probar el patrón de sacudida

### En el Emulador (PC):
1.  Haz clic en los **tres puntos** (⋮) de la barra lateral del emulador para abrir los "Extended Controls".
2.  Ve a la sección **Virtual Sensors** -> **Move**.
3.  Mueve el control deslizante del eje **Y** (el vertical) rápidamente hacia arriba (valor positivo alto) y luego hacia abajo (valor negativo bajo).
4.  Repite esto una vez más rápido.
5.  **Resultado**: La app de Centinela saltará a la pantalla con la alerta roja.

### En un Celular Real:
*   Simplemente sujeta el teléfono con firmeza y realiza dos movimientos rápidos verticales (como si estuvieras agitando una botella de arriba a abajo dos veces).

> [!TIP]
> Si sientes que el sensor es muy duro de activar, puedo bajar un poco el umbral de fuerza. Por ahora está configurado para requerir un movimiento decidido y claro de emergencia.
