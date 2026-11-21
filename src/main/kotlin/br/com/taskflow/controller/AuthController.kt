package br.com.taskflow.controller

import br.com.taskflow.dto.LoginRequest
import br.com.taskflow.dto.RegistroRequest
import br.com.taskflow.dto.TokenResponse
import br.com.taskflow.dto.UsuarioResponse
import br.com.taskflow.service.AuthService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val authService: AuthService
) {

    // Cadastro público de usuário comum
    @PostMapping("/registrar")
    @ResponseStatus(HttpStatus.CREATED)
    fun registrar(@Valid @RequestBody dados: RegistroRequest): UsuarioResponse {
        return authService.registrar(dados)
    }

    // Login: devolve o token JWT
    @PostMapping("/login")
    fun login(@Valid @RequestBody dados: LoginRequest): TokenResponse {
        return authService.login(dados)
    }
}
