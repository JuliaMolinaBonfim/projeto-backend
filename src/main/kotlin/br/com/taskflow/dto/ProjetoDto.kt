package br.com.taskflow.dto

import br.com.taskflow.model.Projeto
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class ProjetoRequest(
    @field:NotBlank(message = "O nome do projeto é obrigatório")
    val nome: String,

    @field:NotBlank(message = "A descrição é obrigatória")
    val descricao: String,

    @field:NotBlank(message = "O prazo é obrigatório")
    @field:Pattern(
        regexp = "\\d{4}-\\d{2}-\\d{2}",
        message = "O prazo deve estar no formato AAAA-MM-DD (ex: 2026-12-15)"
    )
    val prazo: String
)

data class ProjetoResponse(
    val id: Long?,
    val nome: String,
    val descricao: String,
    val prazo: String
)

fun Projeto.toResponse() = ProjetoResponse(
    id = id,
    nome = nome,
    descricao = descricao,
    prazo = prazo
)
