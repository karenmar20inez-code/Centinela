package com.example.centinela.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.IBinder
import android.os.Vibrator
import android.os.VibrationEffect
import android.util.Log
import android.app.PendingIntent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import com.example.centinela.R

class SosService : Service(), SensorEventListener {

    private var screenOffCount = 0
    private var lastPressTime: Long = 0

    // --- VARIABLES PARA EL SENSOR ---
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    
    // Estados del patrón Arriba-Abajo
    private var cycleCount = 0
    private var waitingForDown = false
    private var lastCycleTime: Long = 0
    private val SHAKE_THRESHOLD = 13.0f // Fuerza del movimiento
    private val PATTERN_TIMEOUT = 2500L // Tiempo máximo para completar las 2 sacudidas

    private val powerButtonReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action ?: return
            Log.d("SOS_Debug", "Broadcast recibido: $action")
            
            if (action == Intent.ACTION_SCREEN_ON || action == Intent.ACTION_SCREEN_OFF || action == "android.media.VOLUME_CHANGED_ACTION") {
                val currentTime = System.currentTimeMillis()
                
                // Reiniciar si pasa mucho tiempo o es un evento distinto
                if (currentTime - lastPressTime > 5000) {
                    screenOffCount = 0
                }
                lastPressTime = currentTime
                screenOffCount++

                Log.d("SOS_Debug", "Conteo SOS ($action): $screenOffCount/3")

                if (screenOffCount >= 3) {
                    dispararEmergencia(context)
                    screenOffCount = 0
                }
            }
        }
    }

    private fun dispararEmergencia(context: Context?) {
        Log.e("SOS_Debug", "Servicio: ¡DISPARANDO EMERGENCIA!")
        MainActivity.dispararAlertaGlobal = true
        
        // Enviar SMS
        context?.let { MainActivity.enviarSmsGlobal(it) }
        
        // Si la app NO está en primer plano, lanzamos la notificación Full Screen
        if (!MainActivity.estaEnPrimerPlano) {
            lanzarNotificacionEmergencia(context)
        }
    }

    private fun lanzarNotificacionEmergencia(context: Context?) {
        if (context == null) return
        
        val notificationManager = context.getSystemService(NotificationManager::class.java)
        
        // Canal de alta prioridad para la alerta
        val channel = NotificationChannel(
            "emergency_alert_channel",
            "Alertas Críticas de Emergencia",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            setSound(null, null)
            enableVibration(true)
            lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        }
        notificationManager.createNotificationChannel(channel)

        val launchIntent = context.packageManager.getLaunchIntentForPackage(context.packageName)
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            launchIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, "emergency_alert_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("¡EMERGENCIA ACTIVADA!")
            .setContentText("Centinela está solicitando ayuda.")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setFullScreenIntent(pendingIntent, true) // ESTO ES LA CLAVE
            .setAutoCancel(true)
            .setOngoing(false)

        notificationManager.notify(911, builder.build())
    }

    override fun onCreate() {
        super.onCreate()
        crearCanalNotificacion()
        
        // Inicializar Sensores
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }

        val notification = crearNotificacion()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            @Suppress("DEPRECATION")
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction("android.media.VOLUME_CHANGED_ACTION")
        }
        
        registerReceiver(powerButtonReceiver, filter, RECEIVER_EXPORTED)
        Log.d("SOS_Debug", "SosService: Iniciado y escuchando hardware (Flag T+).")
    }

    private fun crearCanalNotificacion() {
        val channel = NotificationChannel(
            "sos_service_channel",
            "Protección Centinela",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = "Mantiene activa la detección de emergencia por hardware."
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager?.createNotificationChannel(channel)
    }

    private fun crearNotificacion(): Notification {
        return NotificationCompat.Builder(this, "sos_service_channel")
            .setContentTitle("Centinela está activo")
            .setContentText("Protección de emergencia habilitada.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    // --- IMPLEMENTACIÓN DEL SENSOR ---
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val y = event.values[1] // Eje vertical
            val currentTime = System.currentTimeMillis()

            // Si pasó mucho tiempo desde la última sacudida, reiniciamos el patrón
            if (currentTime - lastCycleTime > PATTERN_TIMEOUT) {
                cycleCount = 0
                waitingForDown = false
            }

            if (!waitingForDown) {
                // Detectar movimiento hacia ARRIBA (Y positivo fuerte)
                if (y > SHAKE_THRESHOLD) {
                    waitingForDown = true
                    lastCycleTime = currentTime
                }
            } else {
                // Detectar movimiento hacia ABAJO (Y negativo fuerte)
                if (y < -SHAKE_THRESHOLD) {
                    waitingForDown = false
                    cycleCount++
                    lastCycleTime = currentTime
                    
                    Log.d("SOS_Debug", "Sacudida detectada: Ciclo $cycleCount/2")
                    vibrarConfirmacion()

                    if (cycleCount >= 2) {
                        Log.e("SOS_Debug", "¡PATRÓN DE MOVIMIENTO COMPLETADO!")
                        dispararEmergencia(this)
                        cycleCount = 0
                    }
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun vibrarConfirmacion() {
        val vibrator = getSystemService(Vibrator::class.java)
        vibrator?.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        try {
            unregisterReceiver(powerButtonReceiver)
        } catch (e: Exception) {
            Log.e("SOS_Debug", "Error desregistrando receptor: ${e.message}")
        }
    }
}
