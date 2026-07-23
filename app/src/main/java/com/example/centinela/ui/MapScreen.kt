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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onGoToContacts: () -> Unit = {},
    onGoToProfile: () -> Unit = {}
) {
    var mostrarAlerta by remember { mutableStateOf(false) }
    var cuentaRegresiva by remember { mutableIntStateOf(0) }
    var origen by remember { mutableStateOf("") }
    var destino by remember { mutableStateOf("") }
    var analizandoRuta by remember { mutableStateOf(false) }
    var rutaTrazada by remember { mutableStateOf(false) }
    var planificadorVisible by remember { mutableStateOf(true) }
    
    // URL inicial: CDMX Zócalo
    var urlMapa by remember { mutableStateOf("https://www.google.com.mx/maps/@19.4326,-99.1332,15z") }
    
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxSize()) {
        // --- MOTOR DE MAPA REAL (WEBVIEW FORZADO A DESKTOP PARA EVITAR ERRORES) ---
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    
                    // FORZAR MODO ESCRITORIO: Esto evita que Google Maps intente abrir la App externa
                    val desktopUserAgent = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
                    settings.userAgentString = desktopUserAgent
                    
                    settings.loadWithOverviewMode = true
                    settings.useWideViewPort = true
                    loadUrl(urlMapa)
                }
            },
            update = { webView ->
                if (webView.url != urlMapa && !analizandoRuta) {
                    webView.loadUrl(urlMapa)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // --- INTERFAZ DE RUTAS (COLAPSABLE) ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .zIndex(1f) // Asegurar que esté arriba del mapa
        ) {
            AnimatedVisibility(
                visible = planificadorVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    shadowElevation = 8.dp,
                    color = Color.White.copy(alpha = 0.95f)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Planificador Seguro", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            IconButton(onClick = { planificadorVisible = false }) {
                                Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Ocultar")
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = origen,
                            onValueChange = { origen = it },
                            placeholder = { Text("Origen (ej. Zócalo)") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.TripOrigin, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        OutlinedTextField(
                            value = destino,
                            onValueChange = { destino = it },
                            placeholder = { Text("Destino (ej. Polanco)") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp)) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = {
                                if (origen.isNotEmpty() && destino.isNotEmpty()) {
                                    scope.launch {
                                        analizandoRuta = true
                                        delay(2000)
                                        analizandoRuta = false
                                        urlMapa = "https://www.google.com.mx/maps/dir/$origen/$destino"
                                        rutaTrazada = true
                                        planificadorVisible = false // Ocultar para ver el mapa
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("BUSCAR RUTA SEGURA")
                        }
                    }
                }
            }

            if (!planificadorVisible) {
                FloatingActionButton(
                    onClick = { planificadorVisible = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(Icons.Default.Search, contentDescription = "Mostrar Buscador")
                }
            }
        }

        // --- INDICADORES ---
        if (analizandoRuta) {
            Surface(
                modifier = Modifier.align(Alignment.Center).padding(horizontal = 40.dp).zIndex(2f),
                shape = RoundedCornerShape(12.dp),
                color = Color.Black.copy(alpha = 0.8f)
            ) {
                Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Color.White)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Calculando ruta con menos incidentes...", color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }

        // Sello de Seguridad
        AnimatedVisibility(
            visible = rutaTrazada,
            modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 120.dp).zIndex(1f)
        ) {
            Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(20.dp), shadowElevation = 4.dp) {
                Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("ZONA PROTEGIDA C5", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // --- BOTONES LATERALES ---
        Column(
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).zIndex(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            FloatingActionButton(onClick = onGoToProfile, containerColor = Color.White, modifier = Modifier.size(45.dp)) {
                Icon(Icons.Default.AccountCircle, contentDescription = "Perfil")
            }
            FloatingActionButton(onClick = onGoToContacts, containerColor = Color.White, modifier = Modifier.size(45.dp)) {
                Icon(Icons.Default.Contacts, contentDescription = "Contactos")
            }
        }

        // --- BOTÓN DE PÁNICO (LA CAPA MÁS ALTA) ---
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 30.dp)
                .zIndex(5f) // Arriba de todo
        ) {
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
                    modifier = Modifier.size(100.dp),
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
                        .size(100.dp)
                        .background(Color.Black, CircleShape)
                        .clickable { cuentaRegresiva = 0 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = cuentaRegresiva.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                }
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
                text = { Text("Simulación: Ubicación compartida con tus 3 contactos y el C5 de la CDMX.") },
                modifier = Modifier.zIndex(10f)
            )
        }
    }
}
