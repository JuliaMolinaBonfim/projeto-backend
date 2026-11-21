package br.com.taskflow.dto

import br.com.taskflow.model.Categoria
import jakarta.validation.constraints.NotBlank

data class CategoriaRequest(
    @field:NotBlank(message = "O nome da categoria é obrigatório")
    val nome: String
)

data class CategoriaResponse(
    val id: Long?,
    val nome: String
)

fun Categoria.toResponse() = CategoriaResponse(
    id = id,
    nome = nome
)
