package com.example.centinela.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.example.centinela.data.EmergencyPrefs
import com.example.centinela.ui.theme.BoneWhite
import com.example.centinela.ui.theme.MidnightBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val prefs = remember { EmergencyPrefs(context) }

    // Estados para los contactos
    var name1 by remember { mutableStateOf(prefs.getContactName(1)) }
    var phone1 by remember { mutableStateOf(prefs.getContactPhone(1)) }
    
    var name2 by remember { mutableStateOf(prefs.getContactName(2)) }
    var phone2 by remember { mutableStateOf(prefs.getContactPhone(2)) }
    
    var name3 by remember { mutableStateOf(prefs.getContactName(3)) }
    var phone3 by remember { mutableStateOf(prefs.getContactPhone(3)) }

    Scaffold(
        containerColor = BoneWhite,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MidnightBlue,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                ),
                title = { Text("Contactos de Emergencia", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Estos contactos recibirán un SMS de auxilio y tu ubicación si presionas 3 veces el botón de encendido.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            ContactInputCard(1, name1, phone1) { n, p -> name1 = n; phone1 = p }
            ContactInputCard(2, name2, phone2) { n, p -> name2 = n; phone2 = p }
            ContactInputCard(3, name3, phone3) { n, p -> name3 = n; phone3 = p }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    prefs.saveContact(1, name1, phone1)
                    prefs.saveContact(2, name2, phone2)
                    prefs.saveContact(3, name3, phone3)
                    onBack()
                },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MidnightBlue)
            ) {
                Text("GUARDAR CONTACTOS", fontWeight = FontWeight.Bold)
            }
            
            Text(
                text = "Nota: El envío de SMS puede generar cargos según tu plan telefónico.",
                style = MaterialTheme.typography.labelSmall,
                color = Color.Gray,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun ContactInputCard(number: Int, name: String, phone: String, onUpdate: (String, String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Contacto $number", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MidnightBlue)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { onUpdate(it, phone) },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { onUpdate(name, it) },
                label = { Text("Teléfono") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }
    }
}
