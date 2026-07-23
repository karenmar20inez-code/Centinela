package com.example.centinela.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onBack: () -> Unit, onLogout: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mi Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen de perfil simulada
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(60.dp), tint = MaterialTheme.colorScheme.onPrimaryContainer)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(text = "Ciudadano Seguro", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(text = "Nivel: Centinela Dorado", color = Color.Gray)
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Text(
                text = "Historial de Rutas Seguras",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Start)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(rutasHistorial) { ruta ->
                    RutaItem(ruta)
                }
            }
            
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("CERRAR SESIÓN")
            }
        }
    }
}

@Composable
fun RutaItem(ruta: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Shield, contentDescription = null, tint = Color(0xFF4CAF50))
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = ruta, fontWeight = FontWeight.Medium)
                Text(text = "Estatus: Sin Incidentes", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}

val rutasHistorial = listOf(
    "Polanco -> Condesa",
    "Zócalo -> Bellas Artes",
    "Coyoacán -> UNAM",
    "Santa Fe -> Interlomas",
    "Reforma -> Chapultepec"
)
