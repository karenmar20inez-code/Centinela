package com.example.centinela.utils

import android.content.Context

class ContactosManager(context: Context) {
    private val prefs = context.getSharedPreferences("centinela_prefs", Context.MODE_PRIVATE)

    fun guardarContacto(nombre: String, telefono: String) {
        prefs.edit()
            .putString("contacto_nombre", nombre)
            .putString("contacto_tel", telefono)
            .apply()
    }

    fun obtenerContacto(): Pair<String?, String?> {
        val nombre = prefs.getString("contacto_nombre", "No registrado")
        val tel = prefs.getString("contacto_tel", "")
        return Pair(nombre, tel)
    }
}