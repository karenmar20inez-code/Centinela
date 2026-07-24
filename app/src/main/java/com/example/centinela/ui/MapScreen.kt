package com.example.centinela.ui

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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
fun MapScreen() {
    val scope = rememberCoroutineScope()
    
    // --- ESTADOS PARA EL REGISTRO SIMULADO ---
    var estadoUsuario by rememberSaveable { mutableStateOf(EstadoSimulacion.REGISTRO_LOCAL) }
    var faseRegistro by rememberSaveable { mutableStateOf(FaseRegistro.SELECCION) }
    var esRegistro by rememberSaveable { mutableStateOf(true) }
    
    var nombreUsuario by rememberSaveable { mutableStateOf("") }
    var telefonoUsuario by rememberSaveable { mutableStateOf("") }

    val puedeRegistrar = nombreUsuario.isNotBlank() && telefonoUsuario.isNotBlank()

    var mostrarAlerta by rememberSaveable { mutableStateOf(false) }
    var origen by rememberSaveable { mutableStateOf("") }
    var destino by rememberSaveable { mutableStateOf("") }
    var analizandoRuta by rememberSaveable { mutableStateOf(false) }
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
            // MAPA SIMULADO (Placeholder)
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

            ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    ModalDrawerSheet {
                        Box(modifier = Modifier.fillMaxWidth().background(colorAzulMarino).padding(24.dp)) {
                            Text(text = "Hola, $nombreUsuario", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                        NavigationDrawerItem(icon = { Icon(Icons.Default.Logout, null) }, label = { Text("Cerrar Sesión") }, selected = false, onClick = { scope.launch { drawerState.close(); estadoUsuario = EstadoSimulacion.REGISTRO_LOCAL } })
                    }
                }
            ) {
                Box(modifier = Modifier.fillMaxSize().background(Color.LightGray)) {
                    
                    Text(
                        text = "Mapa de la CDMX (Placeholder)\nEl servicio de Mapbox ha sido removido.",
                        modifier = Modifier.align(Alignment.Center),
                        color = Color.DarkGray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )

                    // Botón Menú
                    IconButton(
                        onClick = { scope.launch { drawerState.open() } },
                        modifier = Modifier.statusBarsPadding().padding(16.dp).background(Color.White, CircleShape)
                    ) {
                        Icon(Icons.Default.Menu, null, tint = colorAzulMarino)
                    }

                    // Planificador
                    if (planificadorVisible) {
                        Surface(
                            modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(top = 70.dp, start = 16.dp, end = 16.dp),
                            shape = RoundedCornerShape(16.dp),
                            shadowElevation = 8.dp
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Rutas Seguras CDMX", fontWeight = FontWeight.Bold)
                                OutlinedTextField(value = origen, onValueChange = { origen = it }, placeholder = { Text("¿Dónde estás?") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(value = destino, onValueChange = { destino = it }, placeholder = { Text("¿A dónde vas?") }, modifier = Modifier.fillMaxWidth())
                                Button(
                                    onClick = { 
                                        analizandoRuta = true
                                        scope.launch {
                                            delay(2000)
                                            analizandoRuta = false
                                            planificadorVisible = false
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                                ) { Text("TRAZAR RUTA POR CALLES") }
                            }
                        }
                    }

                    if (analizandoRuta) {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(0.4f)), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = Color.White)
                        }
                    }

                    // Botón SOS
                    Box(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 30.dp)) {
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
                            text = { Text("Tu ubicación en la CDMX ha sido compartida con autoridades.") }
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
    OutlinedTextField(value = value, onValueChange = onValueChange, placeholder = { Text(placeholder) }, modifier = Modifier.fillMaxWidth(), leadingIcon = { Icon(icon, contentDescription = null) }, singleLine = true, shape = RoundedCornerShape(8.dp))
}
