package com.gutierrez.murocampus.data

import com.gutierrez.murocampus.data.remote.PostApi
import com.gutierrez.murocampus.model.CambioTitulo
import com.gutierrez.murocampus.model.Comentario
import com.gutierrez.murocampus.model.NuevoPost
import com.gutierrez.murocampus.model.Post
import kotlinx.coroutines.CancellationException

interface PostRepository {
    suspend fun listar(): Result<List<Post>>
    suspend fun detalle(id: Int): Result<Post>
    suspend fun comentarios(id: Int): Result<List<Comentario>>
    suspend fun porUsuario(userId: Int): Result<List<Post>>
    suspend fun crear(nuevo: NuevoPost): Result<Post>
    suspend fun editar(id: Int, nuevoTitulo: String): Result<Post>
}

class PostRepositoryRemote(private val api: PostApi) : PostRepository {

    override suspend fun listar(): Result<List<Post>> = ejecutarSeguro { api.listar() }

    override suspend fun detalle(id: Int): Result<Post> = ejecutarSeguro { api.detalle(id) }

    override suspend fun comentarios(id: Int): Result<List<Comentario>> =
        ejecutarSeguro { api.comentarios(id) }

    override suspend fun porUsuario(userId: Int): Result<List<Post>> =
        ejecutarSeguro { api.porUsuario(userId) }

    override suspend fun crear(nuevo: NuevoPost): Result<Post> = ejecutarSeguro { api.crear(nuevo) }

    override suspend fun editar(id: Int, nuevoTitulo: String): Result<Post> =
        ejecutarSeguro { api.editar(id, CambioTitulo(nuevoTitulo)) }

    // Un solo try/catch para las cinco funciones
    private suspend fun <T> ejecutarSeguro(bloque: suspend () -> T): Result<T> =
        try {
            Result.success(bloque())
        } catch (e: CancellationException) {
            throw e // nunca se traga la cancelación
        } catch (e: Exception) {
            Result.failure(e)
        }
}
