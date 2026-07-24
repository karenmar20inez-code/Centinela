package com.example.centinela.ui

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.centinela.R

enum class EstadoSimulacion {
    REGISTRO_LOCAL,
    MAPA_ACTIVO
}

enum class FaseRegistro {
    SELECCION,
    FORMULARIO
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onGoToContacts: () -> Unit = {},
    onGoToProfile: () -> Unit = {}
) {
    // --- ESTADOS PARA EL REGISTRO SIMULADO ---
    var estadoUsuario by rememberSaveable { mutableStateOf(EstadoSimulacion.REGISTRO_LOCAL) }
    var faseRegistro by rememberSaveable { mutableStateOf(FaseRegistro.SELECCION) }
    var esRegistro by rememberSaveable { mutableStateOf(true) }
    
    var nombreUsuario by rememberSaveable { mutableStateOf("") }
    var telefonoUsuario by rememberSaveable { mutableStateOf("") }
    var mostrarDialogoGoogle by remember { mutableStateOf(false) }
    var mostrarBienvenida by remember { mutableStateOf(false) }

    // Validación: Nombre obligatorio + Teléfono
    val puedeRegistrar = nombreUsuario.isNotBlank() && telefonoUsuario.isNotBlank()

    // --- LÓGICA DE BIENVENIDA ---
    LaunchedEffect(estadoUsuario) {
        if (estadoUsuario == EstadoSimulacion.MAPA_ACTIVO) {
            mostrarBienvenida = true
            delay(4000) // Mostrar por 4 segundos
            mostrarBienvenida = false
        }
    }

    // --- ESTADOS DEL MAPA ---
    var mostrarAlerta by rememberSaveable { mutableStateOf(false) }
    var cuentaRegresiva by rememberSaveable { mutableIntStateOf(0) }
    var origen by rememberSaveable { mutableStateOf("") }
    var destino by rememberSaveable { mutableStateOf("") }
    var analizandoRuta by rememberSaveable { mutableStateOf(false) }
    var rutaTrazada by rememberSaveable { mutableStateOf(false) }
    var planificadorVisible by rememberSaveable { mutableStateOf(true) }

    val scope = rememberCoroutineScope()

    // Colores basados en tu diseño
    val colorAzulMarino = Color(0xFF455A8A)

    Box(modifier = Modifier.fillMaxSize()) {

        if (estadoUsuario == EstadoSimulacion.REGISTRO_LOCAL) {

            // =======================================================
            // 1. PANTALLA DE REGISTRO / LOGIN (FLUJO DINÁMICO)
            // =======================================================
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFFAFAFA))
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                AnimatedContent(
                    targetState = faseRegistro,
                    label = "transicion_registro",
                    transitionSpec = {
                        if (targetState == FaseRegistro.FORMULARIO) {
                            slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
                        } else {
                            slideInHorizontally { -it } + fadeIn() togetherWith slideOutHorizontally { it } + fadeOut()
                        }
                    }
                ) { fase ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Título común
                        Text(
                            text = "Centinela CDMX",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = colorAzulMarino
                        )
                        Text(
                            text = "Tu ruta segura en la ciudad",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            modifier = Modifier.padding(top = 4.dp, bottom = 40.dp)
                        )

                        when (fase) {
                            FaseRegistro.SELECCION -> {
                                Spacer(modifier = Modifier.height(20.dp))
                                
                                Button(
                                    onClick = { 
                                        esRegistro = true
                                        faseRegistro = FaseRegistro.FORMULARIO 
                                    },
                                    modifier = Modifier.fillMaxWidth().height(55.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = colorAzulMarino)
                                ) {
                                    Text("REGISTRARSE", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                OutlinedButton(
                                    onClick = { 
                                        esRegistro = false
                                        faseRegistro = FaseRegistro.FORMULARIO 
                                    },
                                    modifier = Modifier.fillMaxWidth().height(55.dp),
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(2.dp, colorAzulMarino),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = colorAzulMarino)
                                ) {
                                    Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                }
                            }

                            FaseRegistro.FORMULARIO -> {
                                Row(
                                    modifier = Modifier.fillMaxWidth().clickable { faseRegistro = FaseRegistro.SELECCION },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.ArrowBack, contentDescription = "Regresar", tint = colorAzulMarino)
                                    Text(
                                        text = if (esRegistro) " Crear nueva cuenta" else " Iniciar sesión",
                                        color = colorAzulMarino,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(24.dp))

                                Text(
                                    text = "Por favor, ingresa tu:",
                                    fontSize = 14.sp,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(bottom = 8.dp).align(Alignment.Start)
                                )

                                // Campo Nombre Completo (Siempre obligatorio para el saludo)
                                LoginTextField(
                                    value = nombreUsuario,
                                    onValueChange = { nombreUsuario = it },
                                    placeholder = "Nombre Completo",
                                    icon = Icons.Default.Person
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                // Campo Teléfono
                                LoginTextField(
                                    value = telefonoUsuario,
                                    onValueChange = { telefonoUsuario = it },
                                    placeholder = "Número de Teléfono",
                                    icon = Icons.Default.Phone
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Botón Acción Principal
                                Button(
                                    onClick = {
                                        if (puedeRegistrar) {
                                            estadoUsuario = EstadoSimulacion.MAPA_ACTIVO
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if(puedeRegistrar) colorAzulMarino else Color.LightGray
                                    )
                                ) {
                                    Text(
                                        text = if (esRegistro) "Completar Registro" else "Entrar a Centinela",
                                        color = Color.White,
                                        fontSize = 16.sp
                                    )
                                }

                                Text("o", color = Color.Gray, fontSize = 12.sp, modifier = Modifier.padding(vertical = 16.dp))

                                // Botón Google
                                OutlinedButton(
                                    onClick = { mostrarDialogoGoogle = true },
                                    modifier = Modifier.fillMaxWidth().height(50.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.DarkGray)
                                ) {
                                    Text("G", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, modifier = Modifier.padding(end = 8.dp))
                                    Text("Continuar con Google")
                                }
                            }
                        }
                    }
                }
            }

            // SIMULACIÓN: Petición de correo y nombre de Google
            if (mostrarDialogoGoogle) {
                var correoGoogle by remember { mutableStateOf("") }
                var nombreGoogle by remember { mutableStateOf("") }
                
                AlertDialog(
                    onDismissRequest = { mostrarDialogoGoogle = false },
                    title = { Text("Inicio de sesión con Google") },
                    text = {
                        Column {
                            Text("Por favor, ingresa tus datos para continuar:")
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            OutlinedTextField(
                                value = nombreGoogle,
                                onValueChange = { nombreGoogle = it },
                                label = { Text("Nombre Completo") },
                                placeholder = { Text("Tu nombre aquí") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedTextField(
                                value = correoGoogle,
                                onValueChange = { correoGoogle = it },
                                label = { Text("Correo Gmail") },
                                placeholder = { Text("ejemplo@gmail.com") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if(correoGoogle.isNotBlank() && nombreGoogle.isNotBlank()) {
                                    nombreUsuario = nombreGoogle
                                    mostrarDialogoGoogle = false
                                    estadoUsuario = EstadoSimulacion.MAPA_ACTIVO
                                }
                            },
                            enabled = correoGoogle.isNotBlank() && nombreGoogle.isNotBlank()
                        ) {
                            Text("Acceder")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { mostrarDialogoGoogle = false }) {
                            Text("Cancelar")
                        }
                    }
                )
            }

        } else {

            // =======================================================
            // 2. MAPA CON MENÚ LATERAL (DRAWER)
            // =======================================================
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet(
                        drawerContainerColor = Color.White,
                        modifier = Modifier.width(300.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(colorAzulMarino)
                                .padding(24.dp)
                        ) {
                            Column {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(64.dp)
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = nombreUsuario,
                                    color = Color.White,
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Centinela Activo",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Contacts, contentDescription = null) },
                            label = { Text("Contactos de Confianza") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onGoToContacts()
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.History, contentDescription = null) },
                            label = { Text("Historial de Rutas") },
                            selected = false,
                            onClick = {
                                scope.launch { drawerState.close() }
                                onGoToProfile()
                            },
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )
                        
                        Spacer(modifier = Modifier.weight(1f))
                        
                        NavigationDrawerItem(
                            icon = { Icon(Icons.Default.Logout, contentDescription = null) },
                            label = { Text("Cerrar Sesión") },
                            selected = false,
                            onClick = {
                                scope.launch { 
                                    drawerState.close()
                                    estadoUsuario = EstadoSimulacion.REGISTRO_LOCAL
                                    faseRegistro = FaseRegistro.SELECCION
                                    nombreUsuario = ""
                                    telefonoUsuario = ""
                                    rutaTrazada = false
                                    planificadorVisible = true
                                }
                            },
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.mapa_cdmx),
                        contentDescription = "Mapa de la CDMX",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    // Botón de Menú (Hamburguesa)
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier
                            .statusBarsPadding()
                            .padding(16.dp)
                            .align(Alignment.TopStart)
                            .background(Color.White, CircleShape)
                            .zIndex(11f)
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú", tint = colorAzulMarino)
                    }

                    // Mensaje de Bienvenida Flotante
                    AnimatedVisibility(
                        visible = mostrarBienvenida,
                        enter = fadeIn() + slideInVertically(),
                        exit = fadeOut() + slideOutVertically(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 100.dp)
                            .zIndex(10f)
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

                    if (rutaTrazada) {
                        Canvas(modifier = Modifier.fillMaxSize().zIndex(1f)) {
                            drawLine(color = Color(0xFF2196F3), start = Offset(size.width * 0.5f, size.height * 0.7f), end = Offset(size.width * 0.5f, size.height * 0.2f), strokeWidth = 20f, cap = StrokeCap.Round)
                            drawCircle(color = Color.Red, radius = 18f, center = Offset(size.width * 0.5f, size.height * 0.65f))
                            drawCircle(color = Color.Red, radius = 18f, center = Offset(size.width * 0.5f, size.height * 0.45f))
                            drawCircle(color = Color.Red, radius = 18f, center = Offset(size.width * 0.5f, size.height * 0.25f))
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(top = 70.dp, start = 16.dp, end = 16.dp)
                            .zIndex(2f)
                    ) {
                        AnimatedVisibility(visible = planificadorVisible, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                            Surface(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), shadowElevation = 8.dp, color = Color.White.copy(alpha = 0.95f)) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                                        Icon(Icons.Default.Route, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(text = "Planificador Seguro", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                        IconButton(onClick = { planificadorVisible = false }) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Ocultar") }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(value = origen, onValueChange = { origen = it }, placeholder = { Text("Origen") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.TripOrigin, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp)) }, singleLine = true, shape = RoundedCornerShape(12.dp))
                                    Spacer(modifier = Modifier.height(4.dp))
                                    OutlinedTextField(value = destino, onValueChange = { destino = it }, placeholder = { Text("Destino") }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Red, modifier = Modifier.size(18.dp)) }, singleLine = true, shape = RoundedCornerShape(12.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            if (origen.isNotEmpty() && destino.isNotEmpty()) {
                                                scope.launch {
                                                    analizandoRuta = true; rutaTrazada = false
                                                    delay(2000)
                                                    analizandoRuta = false; rutaTrazada = true; planificadorVisible = false
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)
                                    ) { Text("BUSCAR RUTA SEGURA") }
                                }
                            }
                        }
                        if (!planificadorVisible) {
                            FloatingActionButton(onClick = { planificadorVisible = true }, containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White, modifier = Modifier.size(40.dp)) { Icon(Icons.Default.Search, contentDescription = "Mostrar") }
                        }
                    }

                    if (analizandoRuta) {
                        Surface(modifier = Modifier.align(Alignment.Center).padding(horizontal = 40.dp).zIndex(3f), shape = RoundedCornerShape(12.dp), color = Color.Black.copy(alpha = 0.8f)) {
                            Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Color.White)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Analizando cámaras C5 y zonas seguras...", color = Color.White, textAlign = TextAlign.Center)
                            }
                        }
                    }

                    AnimatedVisibility(visible = rutaTrazada, modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 120.dp).zIndex(2f)) {
                        Surface(color = Color(0xFF4CAF50), shape = RoundedCornerShape(20.dp), shadowElevation = 4.dp) {
                            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("ZONA PROTEGIDA C5", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Botones laterales (Ya no es necesario el de Perfil porque está en el Drawer, pero dejamos Contactos como acceso rápido si quieres)
                    Column(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp).zIndex(2f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        FloatingActionButton(onClick = onGoToContacts, containerColor = Color.White, modifier = Modifier.size(45.dp)) { Icon(Icons.Default.Contacts, contentDescription = "Contactos") }
                    }

                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp).zIndex(10f)) {
                        if (cuentaRegresiva == 0) {
                            Button(
                                onClick = { scope.launch { cuentaRegresiva = 3; while (cuentaRegresiva > 0) { delay(1000); cuentaRegresiva-- }; mostrarAlerta = true } },
                                modifier = Modifier.size(100.dp), shape = CircleShape, colors = ButtonDefaults.buttonColors(containerColor = Color.Red), elevation = ButtonDefaults.buttonElevation(defaultElevation = 10.dp)
                            ) { Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.ReportProblem, contentDescription = null, tint = Color.White); Text(text = "PÁNICO", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold) } }
                        } else {
                            Box(modifier = Modifier.size(100.dp).background(Color.Black, CircleShape).clickable { cuentaRegresiva = 0 }, contentAlignment = Alignment.Center) { Text(text = cuentaRegresiva.toString(), color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Bold) }
                        }
                    }

                    if (mostrarAlerta) {
                        AlertDialog(
                            onDismissRequest = { mostrarAlerta = false },
                            confirmButton = { Button(onClick = { mostrarAlerta = false }, colors = ButtonDefaults.buttonColors(containerColor = Color.Red)) { Text("ESTOY BIEN") } },
                            title = { Text("¡ALERTA DE SEGURIDAD, $nombreUsuario!") },
                            text = { 
                                Text("Simulación: Se le está enviando la ubicación a los contactos de confianza y se ha iniciado una llamada. " +
                                     "Se le ha notificado a una patrulla que está cerca de ti y al C5 de la CDMX.") 
                            },
                            modifier = Modifier.zIndex(20f)
                        )
                    }
                }
            }
        }
    }
}

// Composable ajustado para ser idéntico a tu diseño
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        modifier = Modifier.fillMaxWidth().background(Color.White, RoundedCornerShape(8.dp)),
        leadingIcon = { Icon(icon, contentDescription = null, tint = Color.DarkGray, modifier = Modifier.size(20.dp)) },
        singleLine = true,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color(0xFF455A8A),
            unfocusedBorderColor = Color.LightGray
        )
    )
}