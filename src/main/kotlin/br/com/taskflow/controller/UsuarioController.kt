
package br.com.taskflow.controller

import br.com.taskflow.dto.UsuarioResponse
import br.com.taskflow.service.UsuarioService
import org.springframework.web.bind.annotation.*

// Somente ADMIN pode listar usuários (a regra está no SecurityConfig).
// O cadastro de novos usuários é feito em POST /api/auth/registrar.
@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    private val usuarioService: UsuarioService
) {

    @GetMapping
    fun listar(): List<UsuarioResponse> {
        return usuarioService.listar()
    }
}
