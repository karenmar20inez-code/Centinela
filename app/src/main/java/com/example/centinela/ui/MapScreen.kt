package com.example.centinela.ui

import android.annotation.SuppressLint
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
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
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onLogout: () -> Unit = {},
    onGoToContacts: () -> Unit = {},
    onGoToProfile: () -> Unit = {}
) {
    var mostrarAlerta by remember { mutableStateOf(false) }
    var cuentaRegresiva by remember { mutableIntStateOf(0) }
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var analizandoRuta by remember { mutableStateOf(false) }
    var rutaTrazada by remember { mutableStateOf(false) }
    var urlMapa by remember { mutableStateOf("https://www.google.com/maps/@19.4326,-99.1332,15z") }
    
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // --- MOTOR DE MAPA REAL (WEBVIEW) ---
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    loadUrl(urlMapa)
                }
            },
            update = { webView ->
                webView.loadUrl(urlMapa)
            },
            modifier = Modifier.fillMaxSize()
        )

        // --- INTERFAZ DE RUTAS (SUPERIOR) ---
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp,
            color = Color.White.copy(alpha = 0.95f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Planificador de Ruta Segura", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = origen,
                    onValueChange = { origen = it },
                    placeholder = { Text("Origen (ej. Zócalo)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.TripOrigin, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = destino,
                    onValueChange = { destino = it },
                    placeholder = { Text("Destino (ej. Ángel Independencia)") },
                    modifier = Modifier.fillMaxWidth(),
                    leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp)) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Button(
                    onClick = {
                        if (origen.isNotEmpty() && destino.isNotEmpty()) {
                            scope.launch {
                                analizandoRuta = true
                                delay(2000) // Simular pensamiento del algoritmo
                                analizandoRuta = false
                                urlMapa = "https://www.google.com/maps/dir/$origen/$destino"
                                rutaTrazada = true
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("TRAZAR RUTA SEGURA")
                }
            }
        }

        // --- INDICADORES DE ESTADO ---
        if (analizandoRuta) {
            Surface(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 40.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.8f)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Analizando cámaras C5 y zonas de riesgo...", color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }

        // Sello de Seguridad
        AnimatedVisibility(
            visible = rutaTrazada,
            enter = slideInVertically { it } + fadeIn(),
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 100.dp)
        ) {
            Surface(
                color = Color(0xFF4CAF50),
                shape = RoundedCornerShape(20.dp),
                shadowElevation = 4.dp
            ) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("RUTA SEGURA OPTIMIZADA", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- BOTONES LATERALES (FLOTANTES) ---
        Column(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(onClick = onGoToProfile, containerColor = Color.White, modifier = Modifier.size(45.dp)) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
            }
            FloatingActionButton(onClick = onGoToContacts, containerColor = Color.White, modifier = Modifier.size(45.dp)) {
                Icon(Icons.Default.Contacts, contentDescription = "Contactos")
            }
        }

        // --- BOTÓN DE PÁNICO ---
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
                    .padding(bottom = 30.dp)
                    .size(100.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color.White)
                    Text(text = "PÁNICO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 30.dp)
                    .size(100.dp)
                    .background(Color.Black, CircleShape)
                    .clickable { cuentaRegresiva = 0 },
                contentAlignment = Alignment.Center
            ) {
                Text(text = cuentaRegresiva.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Alerta de Emergencia
        if (mostrarAlerta) {
            AlertDialog(
                onDismissRequest = { mostrarAlerta = false },
                confirmButton = {
                    Button(onClick = { mostrarAlerta = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) {
                        Text("ESTOY BIEN")
                    }
                },
                title = { Text("¡ALERTA DE SEGURIDAD!") },
                text = { Text("Ubicación compartida con tus contactos y el C5 de la CDMX. Mantente en un lugar visible.") }
            )
        }
    }
}
