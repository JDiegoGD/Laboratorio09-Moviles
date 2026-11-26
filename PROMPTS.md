# PROMPTS.md · Fase 2 con Gemini

Proyecto: MuroCampus · Alumno: Juan Gutiérrez · Rama: `mejora-ia`

Cómo trabajé: le pedí cada mejora a Gemini, leí la respuesta completa, la probé en el emulador y
corregí lo que rompía las reglas (nada de Retrofit ni `HttpException` en el ViewModel, ninguna
pantalla llama a la API).

| # | Prompt que escribí | Qué respondió Gemini | Qué cambié o descarté | Por qué |
|---|---|---|---|---|
| 1 | «En mi `PostApi` de Retrofit quiero editar solo el título de una publicación de JSONPlaceholder. ¿Uso `@PUT` o `@PATCH`? Dame la función y cómo llamarla desde mi `PostRepository` que devuelve `Result`.» | Recomendó `@PATCH` y propuso enviar un `Map<String, String>` con `@Body`. | Cambié el `Map` por una `data class CambioTitulo(val title: String)` con `@Serializable`. | Con una data class el compilador revisa el nombre del campo; con un `Map` un error de tipeo ("tittle") pasa sin aviso. |
| 2 | «Después del PATCH, ¿cómo actualizo el título en la pantalla de detalle y en la lista sin volver a descargar?» | Volver a llamar a `abrir(id)` y a `cargar()` después de editar. | Lo descarté: actualizo en memoria con `copy(title = ...)` el `UiState` del detalle y de la lista. | JSONPlaceholder no guarda cambios: si recargo, vuelve el título viejo y parece que la edición falló. |
| 3 | «Quiero paginar la lista de 20 en 20 con `_start` y `_limit` y un botón "Cargar más". ¿Dónde guardo la posición y cómo sé si ya no hay más?» | Guardar el número de página en la pantalla con `remember` y llamar a la API desde el botón. | Moví todo al ViewModel (`siguienteInicio`, `hayMas`, `cargandoMas`) y la llamada al Repository. `hayMas` es falso cuando llegan menos de 20. | La pantalla nunca debe llamar a la API, y con `remember` la posición se pierde al rotar. |
| 4 | «Si el usuario cambia de filtro mientras carga la página siguiente, a veces se mezclan publicaciones. ¿Cómo lo evito?» | Usar un `Mutex`. | Usé un `Job` (`jobMas`) que se cancela al recargar o al cambiar de filtro. | Ya usaba `Job` para la lista y el detalle; cancelar es más simple que bloquear y no deja peticiones viejas. |
| 5 | «Agrega una búsqueda por título y rediseña MuroCampus para que se vea profesional y distinto al Material por defecto.» | Un `SearchBar` de Material 3, filtrar con `remember` en la pantalla y una paleta con colores dinámicos. | Cambié el `SearchBar` por un campo dentro del encabezado que se abre con la lupa. Pasé el filtro al ViewModel con `combine` y `stateIn`. Quité los colores dinámicos y usé títulos con serifa. | El `SearchBar` es experimental y ocupaba toda la pantalla. Filtrar en el ViewModel sobrevive a la rotación. Con colores dinámicos la app pierde su identidad. |
