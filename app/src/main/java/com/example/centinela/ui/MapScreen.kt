package com.example.centinela.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onLogout: () -> Unit = {},
    onGoToContacts: () -> Unit = {},
    onGoToProfile: () -> Unit = {}
) {
    var mostrarAlerta by remember { mutableStateOf(false) }
    var cuentaRegresiva by remember { mutableIntStateOf(0) }
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // --- SIMULACIÓN DEL MAPA (FONDO ESTILIZADO) ---
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5F5))
        ) {
            // "Calles"
            Column(modifier = Modifier.fillMaxSize()) {
                repeat(15) {
                    Divider(color = Color.White, thickness = 2.dp, modifier = Modifier.padding(vertical = 30.dp))
                }
            }
            Row(modifier = Modifier.fillMaxSize()) {
                repeat(8) {
                    Box(modifier = Modifier.fillMaxHeight().width(2.dp).background(Color.White).padding(horizontal = 50.dp))
                }
            }

            // Marcadores simulados de Cámaras C5 (Puntos de seguridad)
            SimularMarcadorPremium(200.dp, 150.dp, "Cámara Zócalo", Color(0xFF1976D2))
            SimularMarcadorPremium(100.dp, 400.dp, "Cámara Ángel", Color(0xFF1976D2))
            SimularMarcadorPremium(300.dp, 500.dp, "Cámara Bellas Artes", Color(0xFF1976D2))
        }

        // --- INTERFAZ SUPERIOR ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Buscar", tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "¿A dónde quieres ir seguro?",
                        color = Color.Gray,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onGoToProfile) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Perfil", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Filtros rápidos
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FiltroRapido("Cámaras C5", Icons.Default.Videocam)
                FiltroRapido("Botones Auxilio", Icons.Default.TouchApp)
            }
        }

        // --- BOTONES LATERALES ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FloatingActionButton(
                onClick = onGoToContacts,
                containerColor = Color.White,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.Contacts, contentDescription = "Contactos")
            }
            
            FloatingActionButton(
                onClick = { /* Mi ubicación */ },
                containerColor = Color.White,
                contentColor = Color.Black,
                modifier = Modifier.size(48.dp)
            ) {
                Icon(Icons.Default.MyLocation, contentDescription = "Ubicación")
            }
        }

        // --- BOTÓN DE PÁNICO (CENTRAL INFERIOR) ---
        if (cuentaRegresiva == 0) {
            Button(
                onClick = {
                    scope.launch {
                        cuentaRegresiva = 3
                        while (cuentaRegresiva > 0) {
                            delay(1000)
                            cuentaRegresiva--
                        }
                        mostrarAlerta = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .size(110.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color.White, modifier = Modifier.size(32.dp))
                    Text(text = "PÁNICO", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        } else {
            // Estado de cuenta regresiva
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 40.dp)
                    .size(110.dp)
                    .background(Color.Black.copy(alpha = 0.8f), CircleShape)
                    .clickable { cuentaRegresiva = 0 }, // Cancelar
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = cuentaRegresiva.toString(), color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Text(text = "CANCELAR", color = Color.White, fontSize = 10.sp)
                }
            }
        }

        // Alerta de Pánico Simulada
        if (mostrarAlerta) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Red.copy(alpha = 0.3f))
            )
            
            AlertDialog(
                onDismissRequest = { mostrarAlerta = false },
                confirmButton = {
                    Button(
                        onClick = { mostrarAlerta = false },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) {
                        Text("ESTOY A SALVO")
                    }
                },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GppBad, contentDescription = null, tint = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "AUXILIO ENVIADO") 
                    }
                },
                text = { 
                    Text(
                        text = "Se ha enviado tu ubicación en tiempo real a:\n" +
                               "1. Mamá (55 1234 5678)\n" +
                               "2. Papá (55 8765 4321)\n" +
                               "3. C5 CDMX\n\n" +
                               "La patrulla más cercana ha sido notificada.",
                        textAlign = TextAlign.Center
                    ) 
                },
                containerColor = Color.White
            )
        }
    }
}

@Composable
fun SimularMarcadorPremium(x: androidx.compose.ui.unit.Dp, y: androidx.compose.ui.unit.Dp, nombre: String, color: Color) {
    Box(
        modifier = Modifier
            .offset(x = x, y = y)
            .size(30.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.1f))
            .clickable { /* Mostrar info */ },
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.LocationOn, contentDescription = nombre, tint = color, modifier = Modifier.size(24.dp))
    }
}

@Composable
fun FiltroRapido(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.DarkGray)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = label, fontSize = 12.sp, color = Color.DarkGray)
        }
    }
}
