# MuroCampus · Laboratorio 09

**Alumno:** Juan Gutiérrez · Programación en Móviles · 4.º ciclo · Tecsup

MuroCampus es una app Android hecha con Kotlin y Jetpack Compose que se conecta al servicio REST
[JSONPlaceholder](https://jsonplaceholder.typicode.com/). Permite ver las publicaciones, abrir cada
una con sus comentarios, filtrar por usuario y enviar una publicación nueva. Si no hay internet o el
servidor falla, muestra un mensaje y un botón para reintentar en vez de cerrarse.

## Cómo está organizada (MVVM + Repository)

```
Pantallas (ui/)
   │  leen el estado con collectAsStateWithLifecycle()
   ▼
PostsViewModel  ── convierte Result en UiState (Cargando · Exito · Error)
   │  llama solo a la interface
   ▼
PostRepository ◀── PostRepositoryRemote (try/catch → Result)
                         │
                         ▼
               PostApi + RetrofitClient ──▶ https://jsonplaceholder.typicode.com/
```

## Paquetes

```
com.gutierrez.murocampus/
├── MainActivity.kt
├── model/Modelos.kt                 Post, Comentario, NuevoPost, Usuario
├── data/
│   ├── remote/PostApi.kt            @GET, @POST, @Path, @Query, @Body
│   ├── remote/RetrofitClient.kt     OkHttp + logging + kotlinx.serialization
│   └── PostRepository.kt            interface + implementación remota
├── viewmodel/
│   ├── UiState.kt
│   ├── PostsViewModel.kt
│   └── PostsViewModelFactory.kt
└── ui/
    ├── Componentes.kt
    ├── PantallaLista.kt
    ├── PantallaDetalle.kt
    ├── PantallaCrear.kt
    └── theme/
```

## Capturas

| Lista | Detalle | Filtro | Crear | Sin conexión |
|---|---|---|---|---|
| ![](capturas/lista.png) | ![](capturas/detalle.png) | ![](capturas/filtro.png) | ![](capturas/crear.png) | ![](capturas/error.png) |

## Para pensar

**1. GET vs POST; @Body vs @Query.**
GET pide información sin modificar nada; POST manda información para que el servidor cree algo.
Crear necesita enviar un objeto entero, que se serializa a JSON y va en el cuerpo de la petición
(`@Body`). Filtrar por usuario solo agrega un parámetro a la dirección, `posts?userId=3` (`@Query`).

**2. ¿Por qué "posts" y no "/posts"?**
Con la barra inicial Retrofit toma la ruta como absoluta y descarta el camino de la URL base. Sin
la barra, la ruta se pega al final de la URL base, que termina en `/`.

**3. ¿Por qué el Repository devuelve Result?**
Para que los errores de red se manejen en un solo lugar. El ViewModel no necesita saber qué es
una `IOException` o una `HttpException`: solo recibe éxito o fallo. Una excepción que llegara
hasta arriba sin capturar cerraría la app.

**4. Sin permiso INTERNET / sin ignoreUnknownKeys.**
Sin el permiso, Android bloquea la conexión y cada llamada termina en error. Sin
`ignoreUnknownKeys`, cualquier campo nuevo del servidor que no esté en la data class rompe la
lectura del JSON con `SerializationException` ("Respuesta inesperada").

**5. ¿Por qué un when sobre UiState?**
Tres variables separadas permiten estados que no tienen sentido, como "cargando" y "error" al
mismo tiempo. Con una interfaz sellada la pantalla está siempre en un único estado y el compilador
exige atender los tres.

**6. Si el servicio fuera real.**
La publicación creada sí quedaría guardada, así que después de publicar habría que recargar la
lista o agregarla a mano. También se necesitaría iniciar sesión y enviar un token, y manejar
respuestas como 400 o 401.
