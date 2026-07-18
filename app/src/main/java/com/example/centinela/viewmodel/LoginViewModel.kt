package com.example.centinela.viewmodel

import android.app.Activity
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginViewModel : ViewModel() {

    // Instancia de Firebase para manejar la sesión
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Esta función verificará si el usuario ya inició sesión antes
     * para enviarlo directo al mapa sin pedirle sus datos de nuevo.
     */
    fun verificarSesionActiva(): Boolean {
        val usuarioActual = auth.currentUser
        return usuarioActual != null
    }

    /**
     * Envía un mensaje SMS al número proporcionado para iniciar la verificación.
     */
    fun enviarCodigoSms(
        numero: String,
        actividad: Activity,
        callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
        val opciones = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(numero)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(actividad)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(opciones)
    }
}