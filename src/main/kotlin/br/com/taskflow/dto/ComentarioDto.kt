package br.com.taskflow.dto

import br.com.taskflow.model.Comentario
import jakarta.validation.constraints.NotBlank

// O autor NÃO vem no corpo da requisição: ele é descoberto pelo token JWT.
// Assim ninguém consegue comentar "em nome de outra pessoa".
data class ComentarioRequest(
    @field:NotBlank(message = "O texto do comentário é obrigatório")
    val texto: String
)

data class ComentarioResponse(
    val id: Long?,
    val texto: String,
    val tarefaId: Long?,
    val autorId: Long?,
    val autorNome: String
)

fun Comentario.toResponse() = ComentarioResponse(
    id = id,
    texto = texto,
    tarefaId = tarefa.id,
    autorId = autor.id,
    autorNome = autor.nome
)
