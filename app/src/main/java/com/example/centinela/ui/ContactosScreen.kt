package com.example.centinela.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.centinela.utils.ContactosManager

@Composable
fun ContactosScreen() {
    val context = LocalContext.current
    val manager = remember { ContactosManager(context) }

    val (nombreGuardado, telGuardado) = remember { manager.obtenerContacto() }

    var nombre by remember { mutableStateOf(if (nombreGuardado != "No registrado") nombreGuardado ?: "" else "") }
    var telefono by remember { mutableStateOf(telGuardado ?: "") }
    var mensajeConfirmacion by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Registro de Contacto de Emergencia",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it },
            label = { Text("Nombre del Familiar") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Teléfono de Emergencia") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                manager.guardarContacto(nombre, telefono)
                mensajeConfirmacion = "Contacto guardado exitosamente"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Guardar Contacto")
        }

        if (mensajeConfirmacion.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = mensajeConfirmacion,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}