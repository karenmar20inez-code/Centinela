package com.example.centinela.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.centinela.viewmodel.LoginViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {}
) {
    var telefono by remember { mutableStateOf("") }
    var mostrarGoogleDialog by remember { mutableStateOf(false) }
    var iniciandoSesion by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = "Centinela CDMX",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Tu ruta segura en la ciudad",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.Gray
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Número de Teléfono") },
            modifier = Modifier.fillMaxWidth(),
            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    iniciandoSesion = true
                    delay(1000)
                    iniciandoSesion = false
                    onLoginSuccess()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !iniciandoSesion
        ) {
            Text(text = "Ingresar con Teléfono")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(text = "o", color = Color.Gray)

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { mostrarGoogleDialog = true },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !iniciandoSesion
        ) {
            Icon(Icons.Default.AccountCircle, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Continuar con Google")
        }
    }

    // --- DIÁLOGO SIMULADO DE SELECCIÓN DE CUENTA GOOGLE ---
    if (mostrarGoogleDialog) {
        AlertDialog(
            onDismissRequest = { mostrarGoogleDialog = false },
            confirmButton = {},
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("Selecciona una cuenta", fontSize = 18.sp, fontWeight = FontWeight.Medium)
                    Text("para continuar a Centinela", fontSize = 14.sp, color = Color.Gray)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Divider()
                    AccountItem(
                        name = "Lisette Usuario",
                        email = "lisette@gmail.com",
                        onClick = {
                            mostrarGoogleDialog = false
                            scope.launch {
                                iniciandoSesion = true
                                delay(1500)
                                onLoginSuccess()
                            }
                        }
                    )
                    AccountItem(
                        name = "Usar otra cuenta",
                        email = "",
                        isAction = true,
                        onClick = { mostrarGoogleDialog = false }
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }

    // Pantalla de carga global
    if (iniciandoSesion) {
        Box(
            modifier = Modifier.fillMaxSize().background(Color.White.copy(alpha = 0.8f)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    }
}

@Composable
fun AccountItem(name: String, email: String, isAction: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(if (isAction) Color.LightGray else MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            if (isAction) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, tint = Color.DarkGray)
            } else {
                Text(text = name.take(1), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(text = name, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            if (email.isNotEmpty()) {
                Text(text = email, fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    com.example.centinela.ui.theme.CentinelaTheme {
        LoginScreen()
    }
}
