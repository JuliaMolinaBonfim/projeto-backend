package br.com.taskflow.dto

import br.com.taskflow.model.Tarefa
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Positive

data class TarefaRequest(
    @field:NotBlank(message = "O título é obrigatório")
    val titulo: String,

    @field:NotBlank(message = "A descrição é obrigatória")
    val descricao: String,

    @field:NotBlank(message = "O status é obrigatório")
    val status: String = "PENDENTE",

    @field:Positive(message = "O projetoId é obrigatório e deve ser positivo")
    val projetoId: Long,

    val categoriaIds: List<Long> = emptyList()
)

data class TarefaResponse(
    val id: Long?,
    val titulo: String,
    val descricao: String,
    val status: String,
    val projetoId: Long?,
    val categorias: List<CategoriaResponse>
)

fun Tarefa.toResponse() = TarefaResponse(
    id = id,
    titulo = titulo,
    descricao = descricao,
    status = status,
    projetoId = projeto.id,
    categorias = categorias.map { it.toResponse() }.sortedBy { it.id }
)
