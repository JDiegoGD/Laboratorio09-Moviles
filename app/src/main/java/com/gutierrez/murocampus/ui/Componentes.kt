package com.gutierrez.murocampus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gutierrez.murocampus.ui.theme.AzulReal
import com.gutierrez.murocampus.ui.theme.GrisTexto
import com.gutierrez.murocampus.ui.theme.Linea
import com.gutierrez.murocampus.ui.theme.Turquesa
import com.gutierrez.murocampus.ui.theme.TurquesaClaro

// Componentes con el diseño de la Fase 2: estilo editorial.

/** Encabezado azul con esquinas inferiores redondeadas. */
@Composable
fun EncabezadoMuro(
    subtitulo: String,
    onVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {},
    extra: @Composable () -> Unit = {}
) {
    Surface(
        color = AzulReal,
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        shadowElevation = 6.dp
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(start = 8.dp, end = 8.dp, top = 6.dp, bottom = 18.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onVolver != null) {
                    IconButton(onClick = onVolver) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = Color.White)
                    }
                } else {
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text("MuroCampus", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Text(
                        subtitulo,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.78f)
                    )
                }
                acciones()
            }
            extra()
        }
    }
}

@Composable
fun CampoBuscar(texto: String, onCambio: (String) -> Unit) {
    OutlinedTextField(
        value = texto,
        onValueChange = onCambio,
        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp, top = 12.dp),
        placeholder = { Text("Buscar por título") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            focusedBorderColor = Color.Transparent,
            unfocusedBorderColor = Color.Transparent
        )
    )
}

/** Número del post en letra monoespaciada: #07 */
@Composable
fun NumeroPost(id: Int, grande: Boolean = false) {
    Text(
        "#" + id.toString().padStart(2, '0'),
        fontFamily = FontFamily.Monospace,
        fontWeight = FontWeight.Bold,
        color = Turquesa,
        style = if (grande) MaterialTheme.typography.headlineMedium else MaterialTheme.typography.labelLarge
    )
}

@Composable
fun EtiquetaUsuario(userId: Int) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = TurquesaClaro,
        border = BorderStroke(1.dp, Turquesa.copy(alpha = 0.35f))
    ) {
        Text(
            "Usuario $userId",
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TarjetaPost(
    postId: Int,
    userId: Int,
    titulo: String,
    cuerpo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val forma = RoundedCornerShape(20.dp)
    Card(
        modifier = modifier.fillMaxWidth().clip(forma).clickable(onClick = onClick),
        shape = forma,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Linea)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                NumeroPost(postId)
                Spacer(Modifier.width(10.dp))
                EtiquetaUsuario(userId)
                Spacer(Modifier.weight(1f))
                Text("Leer", style = MaterialTheme.typography.labelLarge, color = AzulReal)
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = AzulReal,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(
                titulo,
                style = MaterialTheme.typography.titleLarge,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(6.dp))
            Text(
                cuerpo,
                style = MaterialTheme.typography.bodyMedium,
                color = GrisTexto,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun Cargando(texto: String = "Cargando publicaciones…") {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("MuroCampus", style = MaterialTheme.typography.headlineSmall, color = AzulReal)
        Spacer(Modifier.height(16.dp))
        LinearProgressIndicator(
            modifier = Modifier.width(180.dp).clip(RoundedCornerShape(50)),
            color = Turquesa,
            trackColor = TurquesaClaro
        )
        Spacer(Modifier.height(14.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium, color = GrisTexto)
    }
}

@Composable
fun ErrorConReintento(mensaje: String, onReintentar: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.size(84.dp).background(MaterialTheme.colorScheme.errorContainer, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Info,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(38.dp)
            )
        }
        Spacer(Modifier.height(18.dp))
        Text("No pudimos cargar", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text(
            mensaje,
            style = MaterialTheme.typography.bodyLarge,
            color = GrisTexto,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(22.dp))
        Button(
            onClick = onReintentar,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Turquesa)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Reintentar")
        }
    }
}

@Composable
fun Vacio(titulo: String, detalle: String) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("¯\\_(ツ)_/¯", style = MaterialTheme.typography.headlineSmall, color = Turquesa)
        Spacer(Modifier.height(14.dp))
        Text(titulo, style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(6.dp))
        Text(detalle, style = MaterialTheme.typography.bodyMedium, color = GrisTexto, textAlign = TextAlign.Center)
    }
}
