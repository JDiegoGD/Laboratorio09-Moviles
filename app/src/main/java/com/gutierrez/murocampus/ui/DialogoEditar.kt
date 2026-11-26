package com.gutierrez.murocampus.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gutierrez.murocampus.model.Post
import com.gutierrez.murocampus.viewmodel.UiState

/** Diálogo para cambiar el título con PATCH. No conoce la API: solo avisa con onGuardar. */
@Composable
fun DialogoEditarTitulo(
    postId: Int,
    tituloActual: String,
    edicion: UiState<Post>?,
    onGuardar: (String) -> Unit,
    onCerrar: () -> Unit
) {
    var texto by rememberSaveable(tituloActual) { mutableStateOf(tituloActual) }
    val guardando = edicion is UiState.Cargando

    AlertDialog(
        onDismissRequest = { if (!guardando) onCerrar() },
        shape = RoundedCornerShape(24.dp),
        title = { Text("Editar título") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = texto,
                    onValueChange = { texto = it },
                    label = { Text("Nuevo título") },
                    minLines = 2,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    "Se enviará PATCH posts/$postId solo con el título.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (edicion is UiState.Error) {
                    Text(
                        edicion.mensaje,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onGuardar(texto) },
                enabled = texto.isNotBlank() && !guardando
            ) { Text(if (guardando) "Guardando…" else "Guardar") }
        },
        dismissButton = {
            TextButton(onClick = onCerrar, enabled = !guardando) { Text("Cancelar") }
        }
    )
}
