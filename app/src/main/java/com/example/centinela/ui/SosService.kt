package com.example.centinela.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.app.PendingIntent
import android.content.pm.ServiceInfo
import androidx.core.app.NotificationCompat
import com.example.centinela.R

class SosService : Service() {

    private var screenOffCount = 0
    private var lastPressTime: Long = 0

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
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Canal de alta prioridad para la alerta
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
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
        }

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
        
        val notification = crearNotificacion()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            @Suppress("DEPRECATION")
            startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            startForeground(1, notification)
        }

        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_USER_PRESENT)
            addAction("android.media.VOLUME_CHANGED_ACTION")
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(powerButtonReceiver, filter, Context.RECEIVER_EXPORTED)
        } else {
            registerReceiver(powerButtonReceiver, filter)
        }
        Log.d("SOS_Debug", "SosService: Iniciado y escuchando hardware (Flag T+).")
    }

    private fun crearCanalNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "sos_service_channel",
                "Protección Centinela",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Mantiene activa la detección de emergencia por hardware."
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
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

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(powerButtonReceiver)
        } catch (e: Exception) {
            Log.e("SOS_Debug", "Error desregistrando receptor: ${e.message}")
        }
    }
}
