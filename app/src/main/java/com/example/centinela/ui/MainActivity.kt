package com.example.centinela

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.centinela.ui.ContactosScreen
import com.example.centinela.ui.PanicButtonScreen
import com.example.centinela.ui.theme.CentinelaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CentinelaTheme {
                MainAppLayout()
            }
        }
    }
}

@Composable
fun MainAppLayout() {
    var tabSeleccionada by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = tabSeleccionada == 0,
                    onClick = { tabSeleccionada = 0 },
                    label = { Text("Pánico") },
                    icon = { Text("🚨") }
                )
                NavigationBarItem(
                    selected = tabSeleccionada == 1,
                    onClick = { tabSeleccionada = 1 },
                    label = { Text("Contacto") },
                    icon = { Text("👤") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            if (tabSeleccionada == 0) {
                PanicButtonScreen()
            } else {
                ContactosScreen()
            }
        }
    }
}