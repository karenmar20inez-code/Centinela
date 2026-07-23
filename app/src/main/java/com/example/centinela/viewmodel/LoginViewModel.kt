package com.example.centinela.viewmodel

import androidx.lifecycle.ViewModel

/**
 * ViewModel simplificado para la simulación del proyecto.
 * No requiere conexión a Firebase.
 */
class LoginViewModel : ViewModel() {

    /**
     * Simula la verificación de una sesión activa.
     */
    fun verificarSesionActiva(): Boolean {
        return false // Siempre pedimos login para la demostración
    }

    /**
     * Simula el proceso de envío de SMS.
     * En la simulación, esto simplemente devuelve éxito.
     */
    fun simularEnvioSms(numero: String, onExito: () -> Unit) {
        // En una app real aquí iría la lógica de Firebase.
        // Aquí simplemente ejecutamos la navegación al mapa.
        onExito()
    }
}
