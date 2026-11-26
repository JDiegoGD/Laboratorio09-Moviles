package com.gutierrez.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gutierrez.murocampus.ui.theme.AzulReal
import com.gutierrez.murocampus.ui.theme.GrisTexto
import com.gutierrez.murocampus.ui.theme.Linea
import com.gutierrez.murocampus.ui.theme.Turquesa
import com.gutierrez.murocampus.ui.theme.TurquesaClaro
import com.gutierrez.murocampus.viewmodel.PostsViewModel
import com.gutierrez.murocampus.viewmodel.UiState

@Composable
fun PantallaCrear(vm: PostsViewModel, modifier: Modifier = Modifier) {
    var titulo by rememberSaveable { mutableStateOf("") }
    var texto by rememberSaveable { mutableStateOf("") }
    val envio by vm.envio.collectAsStateWithLifecycle()

    // Si se publicó bien, limpia el formulario. Si falló, conserva lo escrito.
    LaunchedEffect(envio) {
        if (envio is UiState.Exito) {
            titulo = ""
            texto = ""
        }
    }

    val enviando = envio is UiState.Cargando
    val colores = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = Color.White,
        unfocusedContainerColor = Color.White,
        unfocusedBorderColor = Linea
    )

    Column(
        modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("Escribe tu publicación", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Se enviará con POST como Usuario 1.",
            style = MaterialTheme.typography.bodyMedium,
            color = GrisTexto
        )
        OutlinedTextField(
            value = titulo,
            onValueChange = { titulo = it },
            label = { Text("Título") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors = colores,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = texto,
            onValueChange = { texto = it },
            label = { Text("Contenido") },
            minLines = 5,
            shape = RoundedCornerShape(14.dp),
            colors = colores,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = { vm.publicar(titulo, texto) },
            enabled = titulo.isNotBlank() && texto.isNotBlank() && !enviando,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AzulReal),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            if (enviando) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp, color = Color.White)
                Spacer(Modifier.width(10.dp))
                Text("Publicando…")
            } else {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(10.dp))
                Text("Publicar")
            }
        }

        when (val e = envio) {
            null, UiState.Cargando -> Unit
            is UiState.Exito -> Surface(
                color = TurquesaClaro,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Turquesa)
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Publicado con id ${e.datos.id}",
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            is UiState.Error -> Surface(
                color = MaterialTheme.colorScheme.errorContainer,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(e.mensaje, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
            }
        }

        // Vista previa en vivo con la misma tarjeta de la lista
        if (titulo.isNotBlank() || texto.isNotBlank()) {
            Text("Vista previa", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(top = 8.dp))
            TarjetaPost(
                postId = 101,
                userId = 1,
                titulo = titulo.ifBlank { "Tu título" },
                cuerpo = texto.ifBlank { "Tu contenido" },
                onClick = {}
            )
        }
    }
}
