package br.com.taskflow.dto

import br.com.taskflow.model.Usuario

// Note que a senha NÃO aparece aqui: esse é um dos motivos de usar DTO.
data class UsuarioResponse(
    val id: Long?,
    val nome: String,
    val email: String,
    val perfil: String
)

fun Usuario.toResponse() = UsuarioResponse(
    id = id,
    nome = nome,
    email = email,
    perfil = perfil.name
)
