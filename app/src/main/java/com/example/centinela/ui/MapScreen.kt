package com.example.centinela.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Composable
fun MapScreen() {
    // Placeholder de Mapa ya que se eliminó Google Maps
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "El mapa de Google ha sido removido.\nAquí se implementará el nuevo mapa.",
            style = MaterialTheme.typography.bodyLarge,
            color = Color.DarkGray
        )
    }
}