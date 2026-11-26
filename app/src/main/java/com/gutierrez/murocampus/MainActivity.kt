package com.gutierrez.murocampus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gutierrez.murocampus.data.PostRepositoryRemote
import com.gutierrez.murocampus.data.remote.RetrofitClient
import com.gutierrez.murocampus.ui.CampoBuscar
import com.gutierrez.murocampus.ui.EncabezadoMuro
import com.gutierrez.murocampus.ui.PantallaCrear
import com.gutierrez.murocampus.ui.PantallaDetalle
import com.gutierrez.murocampus.ui.PantallaLista
import com.gutierrez.murocampus.ui.theme.MuroCampusTheme
import com.gutierrez.murocampus.ui.theme.Turquesa
import com.gutierrez.murocampus.viewmodel.PostsViewModel
import com.gutierrez.murocampus.viewmodel.PostsViewModelFactory

class MainActivity : ComponentActivity() {

    private val vm: PostsViewModel by viewModels {
        PostsViewModelFactory(PostRepositoryRemote(RetrofitClient.api))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Íconos de la barra de estado en blanco sobre el encabezado azul
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        setContent {
            MuroCampusTheme {
                AppMuroCampus(vm)
            }
        }
    }
}

@Composable
fun AppMuroCampus(vm: PostsViewModel) {
    var idSeleccionado by rememberSaveable { mutableStateOf<Int?>(null) }
    var creando by rememberSaveable { mutableStateOf(false) }
    var buscando by rememberSaveable { mutableStateOf(false) }
    val busqueda by vm.busqueda.collectAsStateWithLifecycle()

    val volver: () -> Unit = {
        idSeleccionado = null
        creando = false
    }
    BackHandler(enabled = idSeleccionado != null || creando, onBack = volver)

    val id = idSeleccionado
    val pantalla = when {
        creando -> "crear"
        id != null -> "detalle:$id"
        else -> "lista"
    }

    Scaffold(
        topBar = {
            EncabezadoMuro(
                subtitulo = when (pantalla) {
                    "crear" -> "Nueva publicación"
                    "lista" -> "Publicaciones del campus"
                    else -> "Publicación y comentarios"
                },
                onVolver = if (pantalla == "lista") null else volver,
                acciones = {
                    if (pantalla == "lista") {
                        IconButton(onClick = {
                            buscando = !buscando
                            if (!buscando) vm.buscar("")
                        }) {
                            Icon(
                                if (buscando) Icons.Default.Close else Icons.Default.Search,
                                contentDescription = if (buscando) "Cerrar búsqueda" else "Buscar",
                                tint = Color.White
                            )
                        }
                    }
                },
                extra = {
                    if (pantalla == "lista" && buscando) {
                        CampoBuscar(texto = busqueda, onCambio = { vm.buscar(it) })
                    }
                }
            )
        },
        floatingActionButton = {
            if (pantalla == "lista") {
                ExtendedFloatingActionButton(
                    onClick = {
                        vm.reiniciarEnvio()
                        creando = true
                    },
                    icon = { Icon(Icons.Default.Edit, contentDescription = null) },
                    text = { Text("Escribir") },
                    containerColor = Turquesa,
                    contentColor = Color.White
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Crossfade(targetState = pantalla, label = "pantallas", modifier = Modifier.padding(padding)) { p ->
            when {
                p == "crear" -> PantallaCrear(vm)
                p.startsWith("detalle:") -> PantallaDetalle(vm, p.removePrefix("detalle:").toInt())
                else -> PantallaLista(vm, onAbrir = { idSeleccionado = it })
            }
        }
    }
}
