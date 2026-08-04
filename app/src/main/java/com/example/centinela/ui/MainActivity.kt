package com.example.centinela.ui

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.util.Log
import android.view.KeyEvent
import android.provider.Settings
import android.net.Uri
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.centinela.data.EmergencyPrefs
import com.example.centinela.ui.theme.CentinelaTheme

class MainActivity : ComponentActivity() {
    
    companion object {
        var dispararAlertaGlobal by mutableStateOf(value = false)
        var estaEnPrimerPlano = false

        fun enviarSmsGlobal(context: Context) {
            val prefs = EmergencyPrefs(context)
            val telefonos = prefs.getAllPhones()
            
            if (telefonos.isEmpty()) {
                Log.w("SOS_Debug", "No hay contactos guardados para enviar SMS")
                return
            }

            if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                try {
                    val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        context.getSystemService(SmsManager::class.java)!!
                    } else {
                        @Suppress("DEPRECATION")
                        SmsManager.getDefault()
                    }

                    val mensaje = "¡AYUDA! He activado mi alerta SOS de Centinela. Por favor contáctame pronto."
                    
                    for (tel in telefonos) {
                        smsManager.sendTextMessage(tel, null, mensaje, null, null)
                        Log.d("SOS_Debug", "SMS enviado a: $tel")
                    }
                    Toast.makeText(context, "SMS de emergencia enviado", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Log.e("SOS_Debug", "Error enviando SMS: ${e.message}")
                }
            } else {
                Log.e("SOS_Debug", "Sin permiso para enviar SMS")
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val smsGranted = permissions[Manifest.permission.SEND_SMS] ?: false
        if (!smsGranted) {
            Toast.makeText(this, "Se requiere permiso de SMS para la función SOS", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkOverlayPermission()
        configurarPantallaDeBloqueo()
        
        val permisos = mutableListOf(Manifest.permission.SEND_SMS)
        permisos.add(Manifest.permission.POST_NOTIFICATIONS)
        requestPermissionLauncher.launch(permisos.toTypedArray())

        val intent = Intent(this, SosService::class.java)
        startForegroundService(intent)
        
        enableEdgeToEdge()
        
        setContent {
            CentinelaTheme {
                CentinelaNavigation()
            }
        }
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "Centinela requiere el permiso 'Mostrar sobre otras apps' para funcionar correctamente.", Toast.LENGTH_LONG).show()
            val intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:$packageName")
            )
            startActivity(intent)
        }
    }

    private fun configurarPantallaDeBloqueo() {
        setShowWhenLocked(true)
        setTurnScreenOn(true)
        val keyguardManager = getSystemService(KeyguardManager::class.java)
        keyguardManager?.requestDismissKeyguard(this, null)
    }

    override fun onResume() {
        super.onResume()
        estaEnPrimerPlano = true
    }

    override fun onPause() {
        super.onPause()
        estaEnPrimerPlano = false
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        configurarPantallaDeBloqueo()
        dispararAlertaGlobal = true
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (event != null && event.isCtrlPressed && event.isShiftPressed && keyCode == KeyEvent.KEYCODE_P) {
            Log.e("SOS_Debug", "Atajo detectado: Ctrl + Shift + P")
            dispararAlertaGlobal = true
            enviarSmsGlobal(this)
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun CentinelaNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "map") {
        composable("map") {
            MapScreen(
                onGoToContacts = { navController.navigate("contacts") },
                onGoToProfile = { navController.navigate("profile") }
            )
        }
        composable("contacts") {
            ContactsScreen(onBack = { navController.popBackStack() })
        }
        composable("profile") {
            ProfileScreen(
                onBack = { navController.popBackStack() },
                onLogout = {
                    navController.navigate("map") {
                        popUpTo("map") { inclusive = true }
                    }
                }
            )
        }
    }
}
