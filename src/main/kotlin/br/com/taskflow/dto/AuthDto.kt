package br.com.taskflow.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

// Dados enviados pelo cliente para fazer login.
data class LoginRequest(
    @field:NotBlank(message = "O e-mail é obrigatório")
    @field:Email(message = "E-mail inválido")
    val email: String,

    @field:NotBlank(message = "A senha é obrigatória")
    val senha: String
)

// Dados enviados pelo cliente para criar uma conta.
data class RegistroRequest(
    @field:NotBlank(message = "O nome é obrigatório")
    val nome: String,

    @field:NotBlank(message = "O e-mail é obrigatório")
    @field:Email(message = "E-mail inválido")
    val email: String,

    @field:NotBlank(message = "A senha é obrigatória")
    @field:Size(min = 8, message = "A senha deve ter no mínimo 8 caracteres")
    val senha: String
)

// Resposta do login: o token JWT que deve ser enviado nas próximas requisições.
data class TokenResponse(
    val token: String,
    val tipo: String = "Bearer",
    val expiraEmSegundos: Long
)
