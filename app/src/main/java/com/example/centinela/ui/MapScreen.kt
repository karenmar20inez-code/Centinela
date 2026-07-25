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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.centinela.BuildConfig
import com.example.centinela.ui.theme.*

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
    val colorPrimario = MidnightBlue
    val colorFondo = BoneWhite
    val colorAlerta = CrimsonRed

    Box(modifier = Modifier.fillMaxSize()) {

        if (estadoUsuario == EstadoSimulacion.REGISTRO_LOCAL) {
            // PANTALLA DE REGISTRO
            Box(
                modifier = Modifier.fillMaxSize().background(colorFondo).padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Centinela CDMX", fontSize = 32.sp, fontWeight = FontWeight.ExtraBold, color = colorPrimario)
                    Text(text = "Protegiendo tus pasos, cuidando tu ciudad 🛡️", fontSize = 14.sp, color = SteelGray, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                    Spacer(modifier = Modifier.height(60.dp))

                    when (faseRegistro) {
                        FaseRegistro.SELECCION -> {
                            Button(
                                onClick = { esRegistro = true; faseRegistro = FaseRegistro.FORMULARIO },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = colorPrimario),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text("REGISTRARSE", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                            OutlinedButton(
                                onClick = { esRegistro = false; faseRegistro = FaseRegistro.FORMULARIO },
                                modifier = Modifier.fillMaxWidth().height(60.dp),
                                shape = RoundedCornerShape(16.dp),
                                border = androidx.compose.foundation.BorderStroke(2.dp, colorPrimario)
                            ) {
                                Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, color = colorPrimario)
                            }
                        }
                        FaseRegistro.FORMULARIO -> {
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable { faseRegistro = FaseRegistro.SELECCION }.padding(bottom = 24.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar", tint = colorPrimario)
                                Text(text = if (esRegistro) " Crear Cuenta" else " Entrar", color = colorPrimario, fontWeight = FontWeight.Bold)
                            }

                            LoginTextField(value = nombreUsuario, onValueChange = { nombreUsuario = it }, placeholder = "Nombre Completo", icon = Icons.Default.Person)
                            Spacer(modifier = Modifier.height(16.dp))
                            LoginTextField(value = telefonoUsuario, onValueChange = { telefonoUsuario = it }, placeholder = "Número de Teléfono", icon = Icons.Default.Phone)
                            Spacer(modifier = Modifier.height(32.dp))
                            Button(
                                onClick = { if (puedeRegistrar) estadoUsuario = EstadoSimulacion.MAPA_ACTIVO },
                                modifier = Modifier.fillMaxWidth().height(55.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = if(puedeRegistrar) colorPrimario else SteelGray)
                            ) {
                                Text(if (esRegistro) "COMPLETAR REGISTRO" else "ACCEDER")
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
                        modifier = Modifier.width(300.dp),
                        drawerShape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
                    ) {
                        Box(modifier = Modifier.fillMaxWidth().background(colorPrimario).padding(vertical = 40.dp, horizontal = 24.dp)) {
                            Column {
                                Icon(Icons.Default.AccountCircle, null, tint = Color.White, modifier = Modifier.size(70.dp))
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(text = nombreUsuario, color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 22.sp)
                                Text(text = "Centinela Activo", color = Color.White.copy(0.7f), fontSize = 14.sp)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Contacts, null, tint = colorPrimario) },
                            label = { Text("Contactos de Confianza", fontWeight = FontWeight.Medium) },
                            selected = false,
                            onClick = { scope.launch { drawerState.close() }; onGoToContacts() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.History, null, tint = colorPrimario) },
                            label = { Text("Historial de Rutas", fontWeight = FontWeight.Medium) },
                            selected = false,
                            onClick = { scope.launch { drawerState.close() }; onGoToProfile() },
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.weight(1f))

                        NavigationDrawerItem(
                            icon = { Icon(Icons.AutoMirrored.Filled.Logout, null, tint = colorAlerta) },
                            label = { Text("Cerrar Sesión", color = colorAlerta, fontWeight = FontWeight.Bold) },
                            selected = false,
                            onClick = { 
                                scope.launch { 
                                    drawerState.close()
                                    estadoUsuario = EstadoSimulacion.REGISTRO_LOCAL 
                                    faseRegistro = FaseRegistro.SELECCION
                                    rutaTrazada = false
                                } 
                            },
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // ... (Mapa se queda igual)
                    AndroidView(
                        factory = { context ->
                            val mapInitOptions = MapInitOptions(
                                context = context,
                                resourceOptions = ResourceOptions.Builder()
                                    .accessToken(mapboxToken)
                                    .build()
                            )
                            MapView(context, mapInitOptions).apply {
                                getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS)
                                getMapboxMap().setCamera(CameraOptions.Builder().center(Point.fromLngLat(-99.1332, 19.4326)).zoom(14.0).build())
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Capa de ruta con color EmeraldGreen
                    if (rutaTrazada) {
                        Canvas(modifier = Modifier.fillMaxSize().zIndex(1f)) {
                            drawLine(
                                color = EmeraldGreen,
                                start = Offset(size.width * 0.5f, size.height * 0.6f),
                                end = Offset(size.width * 0.5f, size.height * 0.3f),
                                strokeWidth = 18f,
                                cap = StrokeCap.Round
                            )
                            drawCircle(color = colorAlerta, radius = 14f, center = Offset(size.width * 0.5f, size.height * 0.55f))
                        }
                    }

                    // Mensaje de Bienvenida
                    AnimatedVisibility(
                        visible = mostrarBienvenida,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier.align(Alignment.TopCenter).padding(top = 100.dp).zIndex(10f)
                    ) {
                        Surface(color = colorPrimario, shape = RoundedCornerShape(30.dp), shadowElevation = 10.dp) {
                            Text(text = "¡Bienvenido, $nombreUsuario! 🛡️", color = Color.White, modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp), fontWeight = FontWeight.Bold)
                        }
                    }

                    // Botón Menú Moderno
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.statusBarsPadding().padding(16.dp).size(50.dp).background(Color.White, CircleShape).shadow(4.dp, CircleShape).zIndex(11f)
                    ) {
                        Icon(Icons.Default.Menu, null, tint = colorPrimario)
                    }

                    // Planificador Minimalista
                    if (planificadorVisible) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 80.dp, start = 20.dp, end = 20.dp).zIndex(2f),
                            shape = RoundedCornerShape(24.dp),
                            shadowElevation = 12.dp,
                            color = Color.White.copy(0.98f)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text("Planificador Seguro", fontWeight = FontWeight.ExtraBold, color = colorPrimario, fontSize = 18.sp)
                                Spacer(modifier = Modifier.height(12.dp))
                                OutlinedTextField(value = origen, onValueChange = { origen = it }, placeholder = { Text("Origen") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(value = destino, onValueChange = { destino = it }, placeholder = { Text("Destino") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true)
                                Button(
                                    onClick = {
                                        if (origen.isNotBlank() && destino.isNotBlank()) {
                                            analizandoRuta = true
                                            scope.launch { delay(2000); analizandoRuta = false; rutaTrazada = true; planificadorVisible = false }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp).height(50.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorPrimario)
                                ) { Text("TRAZAR RUTA SEGURA", fontWeight = FontWeight.Bold) }
                            }
                        }
                    }

                    if (!planificadorVisible) {
                        FloatingActionButton(
                            onClick = { planificadorVisible = true },
                            containerColor = colorPrimario,
                            contentColor = Color.White,
                            modifier = Modifier.padding(16.dp).align(Alignment.TopEnd).statusBarsPadding().size(50.dp).zIndex(2f),
                            shape = CircleShape
                        ) { Icon(Icons.Default.Search, "Buscar") }
                    }

                    // Sello de Seguridad Minimalista
                    AnimatedVisibility(
                        visible = rutaTrazada,
                        modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 130.dp).zIndex(1f)
                    ) {
                        Surface(color = EmeraldGreen, shape = RoundedCornerShape(16.dp), shadowElevation = 6.dp) {
                            Row(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, null, tint = Color.White, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("PROTEGIDO POR C5", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }

                    if (analizandoRuta) {
                        Box(modifier = Modifier.fillMaxSize().background(DeepOcean.copy(0.7f)).zIndex(15f), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White, strokeWidth = 4.dp)
                                Spacer(modifier = Modifier.height(20.dp))
                                Text("Escaneando zonas seguras...", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Botón SOS Minimalista Elegante
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 40.dp).zIndex(10f)) {
                        Button(
                            onClick = { mostrarAlerta = true },
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = colorAlerta),
                            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
                            border = androidx.compose.foundation.BorderStroke(4.dp, Color.White.copy(0.3f))
                        ) { Text("SOS", fontWeight = FontWeight.ExtraBold, fontSize = 20.sp) }
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