package com.example.centinela.ui

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.centinela.viewmodel.LoginViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit = {},
    onWebClick: () -> Unit = {}
) {
    var telefono by remember { mutableStateOf("") }
    val contexto = LocalContext.current // Necesario para Firebase

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
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu ruta segura en la ciudad",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(48.dp))

        OutlinedTextField(
            value = telefono,
            onValueChange = { telefono = it },
            label = { Text("Número a 10 dígitos") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                // 1. Configuramos las respuestas de Firebase
                val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                        Log.d("CentinelaApp", "¡Verificación automática completada!")
                    }
                    override fun onVerificationFailed(e: FirebaseException) {
                        Log.e("CentinelaApp", "Error al enviar SMS: ${e.message}")
                    }
                    override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                        Log.d("CentinelaApp", "¡SMS Enviado! ID: $verificationId")
                        // TODO: Aquí navegaremos a la pantalla para escribir el código
                    }
                }

                // 2. Armamos el número y disparamos la función del ViewModel
                val numeroCompleto = "+52$telefono"
                viewModel.enviarCodigoSms(numeroCompleto, contexto as Activity, callbacks)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Ingresar con Teléfono")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = { /* Aquí irá la conexión a Firebase Google */ },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Ingresar con Google")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // BOTÓN TEMPORAL PARA IR AL MAPA (Solo desarrollo)
        TextButton(onClick = onLoginSuccess) {
            Text(text = "DEBUG: Ir al Mapa directamente")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // BOTÓN PARA ABRIR EL WEBVIEW
        TextButton(onClick = onWebClick) {
            Text(text = "Ver términos o sitio web")
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