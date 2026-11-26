package com.gutierrez.murocampus.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gutierrez.murocampus.data.PostRepository
import com.gutierrez.murocampus.model.Comentario
import com.gutierrez.murocampus.model.NuevoPost
import com.gutierrez.murocampus.model.Post
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DetalleUi(val post: Post, val comentarios: List<Comentario>)

// Solo conoce la interface PostRepository: nada de Retrofit, PostApi ni HttpException
class PostsViewModel(private val repo: PostRepository) : ViewModel() {

    // ---------- Paso 6: lista ----------
    private val _lista = MutableStateFlow<UiState<List<Post>>>(UiState.Cargando)
    val lista: StateFlow<UiState<List<Post>>> = _lista.asStateFlow()

    // ---------- Fase 2: paginación ----------
    private val tamanoPagina = 20
    private var siguienteInicio = 0
    private var jobMas: Job? = null

    private val _hayMas = MutableStateFlow(false)
    val hayMas: StateFlow<Boolean> = _hayMas.asStateFlow()

    private val _cargandoMas = MutableStateFlow(false)
    val cargandoMas: StateFlow<Boolean> = _cargandoMas.asStateFlow()

    private val _errorMas = MutableStateFlow<String?>(null)
    val errorMas: StateFlow<String?> = _errorMas.asStateFlow()

    // ---------- Fase 2: búsqueda por título en lo ya descargado ----------
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    val listaVisible: StateFlow<UiState<List<Post>>> =
        combine(_lista, _busqueda) { estado, texto -> filtrarPorTitulo(estado, texto) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UiState.Cargando)

    // ---------- Paso 8: filtro (null = Todos) ----------
    private val _usuarioFiltro = MutableStateFlow<Int?>(null)
    val usuarioFiltro: StateFlow<Int?> = _usuarioFiltro.asStateFlow()

    // ---------- Paso 7: detalle ----------
    private val _detalle = MutableStateFlow<UiState<DetalleUi>>(UiState.Cargando)
    val detalle: StateFlow<UiState<DetalleUi>> = _detalle.asStateFlow()

    // ---------- Paso 9: crear (null = aún no se envió) ----------
    private val _envio = MutableStateFlow<UiState<Post>?>(null)
    val envio: StateFlow<UiState<Post>?> = _envio.asStateFlow()

    // ---------- Fase 2: editar título (null = sin edición en curso) ----------
    private val _edicion = MutableStateFlow<UiState<Post>?>(null)
    val edicion: StateFlow<UiState<Post>?> = _edicion.asStateFlow()

    private var jobLista: Job? = null
    private var jobDetalle: Job? = null
    private var idDetalle: Int? = null

    init {
        cargar()
    }

    fun cargar() {
        jobLista?.cancel() // si tocas chips rápido, solo vale la última petición
        jobMas?.cancel()
        _cargandoMas.value = false
        _errorMas.value = null
        _hayMas.value = false
        jobLista = viewModelScope.launch {
            _lista.value = UiState.Cargando
            val userId = _usuarioFiltro.value
            if (userId == null) {
                // "Todos": primera página de 20
                repo.pagina(0, tamanoPagina)
                    .onSuccess {
                        siguienteInicio = it.size
                        _hayMas.value = it.size == tamanoPagina
                        _lista.value = UiState.Exito(it)
                    }
                    .onFailure { _lista.value = UiState.Error(mensajeDe(it)) }
                return@launch
            }
            val resultado = if (userId == null) repo.listar() else repo.porUsuario(userId)
            resultado
                .onSuccess { _lista.value = UiState.Exito(it) }
                .onFailure { _lista.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun filtrar(userId: Int?) {
        _usuarioFiltro.value = userId
        cargar()
    }

    fun abrir(id: Int) {
        // Si rotas la pantalla no vuelve a descargar lo mismo
        if (id == idDetalle && _detalle.value is UiState.Exito) return
        idDetalle = id
        jobDetalle?.cancel()
        _detalle.value = UiState.Cargando
        jobDetalle = viewModelScope.launch {
            // Pide el post y los comentarios a la vez
            val (rPost, rComentarios) = coroutineScope {
                val post = async { repo.detalle(id) }
                val comentarios = async { repo.comentarios(id) }
                post.await() to comentarios.await()
            }
            val post = rPost.getOrElse {
                _detalle.value = UiState.Error(mensajeDe(it))
                return@launch
            }
            val comentarios = rComentarios.getOrElse {
                _detalle.value = UiState.Error(mensajeDe(it))
                return@launch
            }
            _detalle.value = UiState.Exito(DetalleUi(post, comentarios))
        }
    }

    fun publicar(titulo: String, texto: String) {
        if (_envio.value is UiState.Cargando) return
        _envio.value = UiState.Cargando
        viewModelScope.launch {
            repo.crear(NuevoPost(userId = 1, title = titulo.trim(), body = texto.trim()))
                .onSuccess { _envio.value = UiState.Exito(it) }
                .onFailure { _envio.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun reiniciarEnvio() {
        _envio.value = null
    }

    fun editarTitulo(id: Int, nuevoTitulo: String) {
        val titulo = nuevoTitulo.trim()
        if (titulo.isEmpty() || _edicion.value is UiState.Cargando) return
        _edicion.value = UiState.Cargando
        viewModelScope.launch {
            repo.editar(id, titulo)
                .onSuccess { actualizado ->
                    // JSONPlaceholder no guarda el cambio: lo reflejamos en memoria
                    val d = _detalle.value
                    if (d is UiState.Exito && d.datos.post.id == id) {
                        _detalle.value = UiState.Exito(
                            d.datos.copy(post = d.datos.post.copy(title = actualizado.title))
                        )
                    }
                    val l = _lista.value
                    if (l is UiState.Exito) {
                        _lista.value = UiState.Exito(
                            l.datos.map { if (it.id == id) it.copy(title = actualizado.title) else it }
                        )
                    }
                    _edicion.value = UiState.Exito(actualizado)
                }
                .onFailure { _edicion.value = UiState.Error(mensajeDe(it)) }
        }
    }

    fun reiniciarEdicion() {
        _edicion.value = null
    }

    fun cargarMas() {
        if (_cargandoMas.value || !_hayMas.value || _usuarioFiltro.value != null) return
        val actual = _lista.value
        if (actual !is UiState.Exito) return
        _cargandoMas.value = true
        _errorMas.value = null
        jobMas = viewModelScope.launch {
            repo.pagina(siguienteInicio, tamanoPagina)
                .onSuccess { nuevos ->
                    siguienteInicio += nuevos.size
                    _hayMas.value = nuevos.size == tamanoPagina
                    _lista.value = UiState.Exito(actual.datos + nuevos)
                }
                .onFailure { _errorMas.value = mensajeDe(it) }
            _cargandoMas.value = false
        }
    }

    fun buscar(texto: String) {
        _busqueda.value = texto
    }

    private fun filtrarPorTitulo(estado: UiState<List<Post>>, texto: String): UiState<List<Post>> {
        val q = texto.trim()
        if (q.isEmpty() || estado !is UiState.Exito) return estado
        return UiState.Exito(estado.datos.filter { it.title.contains(q, ignoreCase = true) })
    }
}
