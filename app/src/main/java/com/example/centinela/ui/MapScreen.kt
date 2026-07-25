package com.example.centinela.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.centinela.BuildConfig

// --- NUEVOS IMPORTS DE MAPBOX ---
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapInitOptions
import com.mapbox.maps.MapView
import com.mapbox.maps.ResourceOptions
import com.mapbox.maps.Style

// Enums locales para la simulación
enum class EstadoSimulacion {
    REGISTRO_LOCAL,
    MAPA_ACTIVO
}

enum class FaseRegistro {
    SELECCION,
    FORMULARIO
}

@Composable
fun MapScreen(
    onGoToContacts: () -> Unit = {},
    onGoToProfile: () -> Unit = {}
) {
    // --- TU TOKEN DE MAPBOX (Ahora desde BuildConfig) ---
    val mapboxToken = BuildConfig.MAPBOX_ACCESS_TOKEN

    val scope = rememberCoroutineScope()

    // --- ESTADOS PARA EL REGISTRO SIMULADO ---
    var estadoUsuario by rememberSaveable { mutableStateOf(EstadoSimulacion.REGISTRO_LOCAL) }
    var faseRegistro by rememberSaveable { mutableStateOf(FaseRegistro.SELECCION) }
    var esRegistro by rememberSaveable { mutableStateOf(true) }

    var nombreUsuario by rememberSaveable { mutableStateOf("") }
    var telefonoUsuario by rememberSaveable { mutableStateOf("") }

    val puedeRegistrar = nombreUsuario.isNotBlank() && telefonoUsuario.isNotBlank()

    // --- LÓGICA DE BIENVENIDA ---
    var mostrarBienvenida by remember { mutableStateOf(false) }
    LaunchedEffect(estadoUsuario) {
        if (estadoUsuario == EstadoSimulacion.MAPA_ACTIVO) {
            mostrarBienvenida = true
            delay(4000)
            mostrarBienvenida = false
        }
    }

    var mostrarAlerta by rememberSaveable { mutableStateOf(false) }
    var origen by rememberSaveable { mutableStateOf("") }
    var destino by rememberSaveable { mutableStateOf("") }
    var analizandoRuta by rememberSaveable { mutableStateOf(false) }
    var rutaTrazada by rememberSaveable { mutableStateOf(false) }
    var planificadorVisible by rememberSaveable { mutableStateOf(true) }

    // Colores diseño
    val colorAzulMarino = Color(0xFF455A8A)

    Box(modifier = Modifier.fillMaxSize()) {

        if (estadoUsuario == EstadoSimulacion.REGISTRO_LOCAL) {
            // PANTALLA DE REGISTRO
            Box(
                modifier = Modifier.fillMaxSize().background(Color(0xFFFAFAFA)).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Centinela CDMX", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = colorAzulMarino)
                    Spacer(modifier = Modifier.height(40.dp))

                    when (faseRegistro) {
                        FaseRegistro.SELECCION -> {
                            Button(onClick = { esRegistro = true; faseRegistro = FaseRegistro.FORMULARIO }, modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = colorAzulMarino)) {
                                Text("REGISTRARSE")
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(onClick = { esRegistro = false; faseRegistro = FaseRegistro.FORMULARIO }, modifier = Modifier.fillMaxWidth().height(55.dp), shape = RoundedCornerShape(12.dp)) {
                                Text("INICIAR SESIÓN")
                            }
                        }
                        FaseRegistro.FORMULARIO -> {
                            LoginTextField(value = nombreUsuario, onValueChange = { nombreUsuario = it }, placeholder = "Nombre Completo", icon = Icons.Default.Person)
                            Spacer(modifier = Modifier.height(12.dp))
                            LoginTextField(value = telefonoUsuario, onValueChange = { telefonoUsuario = it }, placeholder = "Número de Teléfono", icon = Icons.Default.Phone)
                            Spacer(modifier = Modifier.height(24.dp))
                            Button(onClick = { if (puedeRegistrar) estadoUsuario = EstadoSimulacion.MAPA_ACTIVO }, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                                Text(if (esRegistro) "Completar Registro" else "Entrar")
                            }
                        }
                    }
                }
            }
        } else {
            // ==========================================
            // MAPA ACTIVO CON MAPBOX
            // ==========================================
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color.White,
                        modifier = Modifier.width(300.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().background(colorAzulMarino).padding(24.dp)) {
                            Column {
                                Icon(Icons.Default.AccountCircle, null, tint = Color.White, modifier = Modifier.size(64.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "Hola, $nombreUsuario", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                Text(text = "Centinela Activo", color = Color.White.copy(0.7f), fontSize = 14.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Contacts, null) },
                            label = { Text("Contactos de Confianza") },
                            selected = false,
                            onClick = { scope.launch { drawerState.close() }; onGoToContacts() },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.History, null) },
                            label = { Text("Historial de Rutas") },
                            selected = false,
                            onClick = { scope.launch { drawerState.close() }; onGoToProfile() },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.Logout, null) },
                            label = { Text("Cerrar Sesión") },
                            selected = false,
                            onClick = { 
                                scope.launch { 
                                    drawerState.close()
                                    estadoUsuario = EstadoSimulacion.REGISTRO_LOCAL 
                                    rutaTrazada = false
                                } 
                            },
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {

                    // --- MAPA REAL DE MAPBOX ---
                    AndroidView(
                        factory = { context ->
                            val mapInitOptions = MapInitOptions(
                                context = context,
                                resourceOptions = ResourceOptions.Builder()
                                    .accessToken(mapboxToken)
                                    .build()
                            )
                            MapView(context, mapInitOptions).apply {
                                // Cargar el estilo de calles
                                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)

                                // Centrar la cámara en la CDMX (Zócalo)
                                getMapboxMap().setCamera(
                                    CameraOptions.Builder()
                                        .center(Point.fromLngLat(-99.1332, 19.4326))
                                        .zoom(14.0)
                                        .build()
                                )
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // --- CAPA DE RUTA SEGURA (CANVAS) ---
                    if (rutaTrazada) {
                        Canvas(modifier = Modifier.fillMaxSize().zIndex(1f)) {
                            // Simulamos una ruta sobre el Zócalo
                            drawLine(
                                color = Color(0xFF2196F3),
                                start = Offset(size.width * 0.5f, size.height * 0.6f),
                                end = Offset(size.width * 0.5f, size.height * 0.3f),
                                strokeWidth = 15f,
                                cap = StrokeCap.Round
                            )
                            drawCircle(color = Color.Red, radius = 12f, center = Offset(size.width * 0.5f, size.height * 0.55f))
                            drawCircle(color = Color.Red, radius = 12f, center = Offset(size.width * 0.5f, size.height * 0.45f))
                        }
                    }

                    // Mensaje de Bienvenida Flotante
                    AnimatedVisibility(
                        visible = mostrarBienvenida,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp).zIndex(10f)
                    ) {
                        Surface(
                            color = colorAzulMarino,
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 6.dp
                        ) {
                            Text(
                                text = "¡Bienvenido a Centinela, $nombreUsuario!",
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }

                    // Botón Menú
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.statusBarsPadding().padding(16.dp).background(Color.White, CircleShape).zIndex(11f)
                    ) {
                        Icon(Icons.Default.Menu, null, tint = colorAzulMarino)
                    }

                    // Planificador
                    if (planificadorVisible) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 70.dp, start = 16.dp, end = 16.dp).zIndex(2f),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 8.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Planificador Seguro", fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = origen, onValueChange = { origen = it }, placeholder = { Text("Origen (ej. Zócalo)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                OutlinedTextField(value = destino, onValueChange = { destino = it }, placeholder = { Text("Destino (ej. Bellas Artes)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Button(
                                    onClick = {
                                        if (origen.isNotBlank() && destino.isNotBlank()) {
                                            analizandoRuta = true
                                            scope.launch {
                                                delay(2000)
                                                analizandoRuta = false
                                                rutaTrazada = true
                                                planificadorVisible = false
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) { Text("BUSCAR RUTA SEGURA") }
                            }
                        }
                    }

                    if (!planificadorVisible) {
                        FloatingActionButton(
                            onClick = { planificadorVisible = true },
                            containerColor = colorAzulMarino,
                            contentColor = Color.White,
                            modifier = Modifier.padding(16.dp).align(Alignment.TopEnd).statusBarsPadding().size(45.dp).zIndex(2f)
                        ) { Icon(Icons.Default.Search, "Buscar") }
                    }

                    // Sello de Seguridad
                    AnimatedVisibility(
                        visible = rutaTrazada,
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 120.dp).zIndex(1f)
                    ) {
                        Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(20.dp), shadowElevation = 4.dp) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ZONA PROTEGIDA C5", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (analizandoRuta) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.6f)).zIndex(15f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(16.dp))
                                Text("Analizando cámaras y patrullas...", color = Color.White, fontWeight = FontWeight.Medium)
                            }
                        }
                    }

                    // Botón SOS
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp).zIndex(10f)) {
                        Button(
                            onClick = { mostrarAlerta = true },
                            modifier = Modifier.size(90.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) { Text("SOS", fontWeight = FontWeight.Bold) }
                    }

                    if (mostrarAlerta) {
                        AlertDialog(
                            onDismissRequest = { mostrarAlerta = false },
                            confirmButton = { Button(onClick = { mostrarAlerta = false }) { Text("ESTOY BIEN") } },
                            title = { Text("¡ALERTA ENVIADA!") },
                            text = { 
                                Text("Iniciando llamada a tus contactos de confianza y compartiendo tu ubicación en tiempo real con ellos. " +
                                     "También se ha notificado a una patrulla cercana y al C5 de la CDMX.") 
                            }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(value: String, onValueChange: (String) -> Unit, placeholder: String, icon: ImageVector) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder) },
        modifier = Modifier.fillMaxWidth(),
        leadingIcon = { Icon(icon, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(8.dp)
    )
}