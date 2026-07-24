package com.example.centinela.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.centinela.ui.theme.CentinelaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Desactivamos el splash predeterminado si es que causa conflicto en el emulador
        enableEdgeToEdge()
        
        setContent {
            CentinelaTheme {
                CentinelaNavigation()
            }
        }
    }
}

@Composable
fun CentinelaNavigation() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "map") {
        composable("map") {
            MapScreen()
        }
    }
}
