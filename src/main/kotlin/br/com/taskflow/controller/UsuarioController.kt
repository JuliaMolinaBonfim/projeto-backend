
package br.com.taskflow.controller

import br.com.taskflow.model.Usuario
import br.com.taskflow.repository.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/usuarios")
class UsuarioController(
    private val usuarioRepository: UsuarioRepository
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@RequestBody usuario: Usuario): Usuario {
        return usuarioRepository.save(usuario)
    }

    @GetMapping
    fun listar(): List<Usuario> {
        return usuarioRepository.findAll()
    }
}
