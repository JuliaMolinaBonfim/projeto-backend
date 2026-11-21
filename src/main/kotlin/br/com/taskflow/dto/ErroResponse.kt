package br.com.taskflow.dto

import java.time.Instant

// Formato padrão de todas as respostas de erro da API.
data class ErroResponse(
    val status: Int,
    val mensagem: String,
    val detalhes: List<String> = emptyList(),
    val timestamp: String = Instant.now().toString()
)
