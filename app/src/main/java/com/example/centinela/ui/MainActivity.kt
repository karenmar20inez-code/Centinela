package com.example.centinela.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.centinela.ui.theme.CentinelaTheme

class MainActivity : ComponentActivity() {
    
    // Estado global para disparar la alerta desde hardware
    companion object {
        var dispararAlertaGlobal by mutableStateOf(false)
    }

    private var screenOffCount = 0
    private var lastPressTime: Long = 0

    private val powerButtonReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastPressTime > 3000) {
                screenOffCount = 0
            }
            lastPressTime = currentTime
            screenOffCount++

            if (screenOffCount >= 3) {
                dispararAlertaGlobal = true
                screenOffCount = 0
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Registrar detector de botón de apagado (vía eventos de pantalla)
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_ON)
            addAction(Intent.ACTION_SCREEN_OFF)
        }
        registerReceiver(powerButtonReceiver, filter)
        
        enableEdgeToEdge()
        
        setContent {
            CentinelaTheme {
                CentinelaNavigation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(powerButtonReceiver)
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
