package com.gutierrez.murocampus.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.text.style.TextOverflow
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
fun PantallaDetalle(
    vm: PostsViewModel,
    id: Int,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(id) { vm.abrir(id) }
    val estado by vm.detalle.collectAsStateWithLifecycle()
    val edicion by vm.edicion.collectAsStateWithLifecycle()
    var editando by rememberSaveable { mutableStateOf(false) }

    // Si el PATCH salió bien, cierra el diálogo
    LaunchedEffect(edicion) {
        if (edicion is UiState.Exito) {
            editando = false
            vm.reiniciarEdicion()
        }
    }

    Box(modifier.fillMaxSize()) {
        when (val e = estado) {
            UiState.Cargando -> Cargando("Cargando publicación…")
            is UiState.Error -> ErrorConReintento(e.mensaje, onReintentar = { vm.abrir(id) })
            is UiState.Exito -> {
                val post = e.datos.post
                val comentarios = e.datos.comentarios
                LazyColumn(contentPadding = PaddingValues(16.dp)) {
                    item {
                        Card(
                            Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Linea)
                        ) {
                            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    NumeroPost(post.id, grande = true)
                                    Spacer(Modifier.weight(1f))
                                    EtiquetaUsuario(post.userId)
                                }
                                Text(post.title, style = MaterialTheme.typography.headlineSmall)
                                Text(
                                    post.body,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f)
                                )
                                FilledTonalButton(
                                    onClick = { editando = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Editar título")
                                }
                            }
                        }
                    }
                    item {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 4.dp, top = 24.dp, bottom = 14.dp)
                        ) {
                            Text("Comentarios (${comentarios.size})", style = MaterialTheme.typography.titleLarge)
                            Spacer(Modifier.width(10.dp))
                            Surface(color = TurquesaClaro, shape = RoundedCornerShape(50)) {
                                Text(
                                    "hilo",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Turquesa,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    // Comentarios como línea de tiempo
                    itemsIndexed(comentarios, key = { _, c -> c.id }) { i, c ->
                        val ultimo = i == comentarios.lastIndex
                        Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
                            Column(
                                Modifier.width(28.dp).fillMaxHeight(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(Modifier.height(4.dp))
                                Box(Modifier.size(12.dp).background(Turquesa, CircleShape))
                                if (!ultimo) {
                                    Box(Modifier.width(2.dp).weight(1f).background(Linea))
                                }
                            }
                            Column(
                                Modifier.padding(start = 6.dp, bottom = 20.dp),
                                verticalArrangement = Arrangement.spacedBy(3.dp)
                            ) {
                                Text(c.email, style = MaterialTheme.typography.labelLarge, color = AzulReal)
                                Text(
                                    c.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(c.body, style = MaterialTheme.typography.bodyMedium, color = GrisTexto)
                            }
                        }
                    }
                }

                if (editando) {
                    DialogoEditarTitulo(
                        postId = post.id,
                        tituloActual = post.title,
                        edicion = edicion,
                        onGuardar = { vm.editarTitulo(post.id, it) },
                        onCerrar = {
                            editando = false
                            vm.reiniciarEdicion()
                        }
                    )
                }
            }
        }
    }
}
