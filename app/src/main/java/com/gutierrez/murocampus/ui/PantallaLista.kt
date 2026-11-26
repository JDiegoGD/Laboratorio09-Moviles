package com.gutierrez.murocampus.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gutierrez.murocampus.ui.theme.AzulReal
import com.gutierrez.murocampus.ui.theme.GrisTexto
import com.gutierrez.murocampus.ui.theme.Turquesa
import com.gutierrez.murocampus.viewmodel.PostsViewModel
import com.gutierrez.murocampus.viewmodel.UiState

@Composable
fun PantallaLista(
    vm: PostsViewModel,
    onAbrir: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by vm.listaVisible.collectAsStateWithLifecycle()
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()
    val filtro by vm.usuarioFiltro.collectAsStateWithLifecycle()
    val hayMas by vm.hayMas.collectAsStateWithLifecycle()
    val cargandoMas by vm.cargandoMas.collectAsStateWithLifecycle()
    val errorMas by vm.errorMas.collectAsStateWithLifecycle()

    Column(modifier.fillMaxSize()) {
        FilaFiltros(seleccionado = filtro, onElegir = { vm.filtrar(it) })

        Crossfade(targetState = estado, label = "lista", modifier = Modifier.weight(1f)) { e ->
            when (e) {
                UiState.Cargando -> Cargando()
                is UiState.Error -> ErrorConReintento(e.mensaje, onReintentar = { vm.cargar() })
                is UiState.Exito -> {
                    if (e.datos.isEmpty()) {
                        if (busqueda.isNotBlank()) {
                            Vacio("Sin resultados", "Ningún título cargado contiene «${busqueda.trim()}».")
                        } else {
                            Vacio("Sin publicaciones", "Este usuario todavía no ha publicado.")
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 100.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                val n = e.datos.size
                                val detalle = when {
                                    busqueda.isNotBlank() -> "coinciden con la búsqueda"
                                    filtro == null -> "cargadas de 100"
                                    else -> "del Usuario $filtro"
                                }
                                Text(
                                    "$n $detalle",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GrisTexto,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                            items(e.datos, key = { it.id }) { post ->
                                TarjetaPost(
                                    postId = post.id,
                                    userId = post.userId,
                                    titulo = post.title,
                                    cuerpo = post.body,
                                    onClick = { onAbrir(post.id) },
                                    modifier = Modifier.animateItem()
                                )
                            }
                            if (hayMas && filtro == null && busqueda.isBlank()) {
                                item {
                                    BotonCargarMas(cargando = cargandoMas, onClick = { vm.cargarMas() })
                                }
                            }
                            errorMas?.let { msg ->
                                item {
                                    Text(
                                        msg,
                                        color = MaterialTheme.colorScheme.error,
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(start = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BotonCargarMas(cargando: Boolean, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        enabled = !cargando,
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, AzulReal),
        modifier = Modifier.fillMaxWidth().height(50.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(10.dp))
            Text("Cargando…")
        } else {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null)
            Spacer(Modifier.width(6.dp))
            Text("Cargar 20 más")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilaFiltros(seleccionado: Int?, onElegir: (Int?) -> Unit) {
    val opciones: List<Int?> = listOf<Int?>(null) + (1..10).toList()
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(opciones) { id ->
            FilterChip(
                selected = seleccionado == id,
                onClick = { onElegir(id) },
                label = { Text(if (id == null) "Todos" else "Usuario $id") },
                shape = RoundedCornerShape(12.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Color.White,
                    selectedContainerColor = Turquesa,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}
