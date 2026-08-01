package com.example.centinela.data

import android.content.Context
import android.content.SharedPreferences

class EmergencyPrefs(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("emergency_contacts", Context.MODE_PRIVATE)

    fun saveContact(index: Int, name: String, phone: String) {
        prefs.edit().apply {
            putString("name_$index", name)
            putString("phone_$index", phone)
            apply()
        }
    }

    fun getContactName(index: Int): String {
        val default = when(index) {
            1 -> "Mamá"
            2 -> "Papá"
            3 -> "Amigo Confianza"
            else -> ""
        }
        return prefs.getString("name_$index", default) ?: default
    }

    fun getContactPhone(index: Int): String {
        val default = when(index) {
            1 -> "5512345678"
            2 -> "5587654321"
            3 -> "5599887766"
            else -> ""
        }
        return prefs.getString("phone_$index", default) ?: default
    }

    fun getAllPhones(): List<String> {
        return listOf(
            getContactPhone(1),
            getContactPhone(2),
            getContactPhone(3)
        ).filter { it.isNotBlank() }
    }
}
