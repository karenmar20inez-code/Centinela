package com.example.centinela.ui

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.centinela.utils.ContactosManager

@Composable
fun PanicButtonScreen() {
    val context = LocalContext.current
    val manager = remember { ContactosManager(context) }
    var mostrarDialogo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Button(
            onClick = {
                val (_, tel) = manager.obtenerContacto()
                val numeroDestino = if (!tel.isNullOfEmpty()) tel else "911"

                val intent = Intent(Intent.ACTION_DIAL).apply {
                    data = Uri.parse("tel:$numeroDestino")
                }
                context.startActivity(intent)

                mostrarDialogo = true
            },
            modifier = Modifier
                .size(200.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            )
        ) {
            Text(
                text = "PÁNICO",
                fontSize = 24.sp,
                color = MaterialTheme.colorScheme.onError
            )
        }

        if (mostrarDialogo) {
            AlertDialog(
                onDismissRequest = { mostrarDialogo = false },
                title = { Text("Alerta de Emergencia") },
                text = { Text("Alerta enviada exitosamente a tus contactos.") },
                confirmButton = {
                    TextButton(onClick = { mostrarDialogo = false }) {
                        Text("Aceptar")
                    }
                }
            )
        }
    }
}

private fun String?.isNullOfEmpty(): Boolean = this == null || this.isEmpty()