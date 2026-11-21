
package br.com.taskflow.controller

import br.com.taskflow.dto.ComentarioRequest
import br.com.taskflow.dto.ComentarioResponse
import br.com.taskflow.service.ComentarioService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.security.Principal

@RestController
@RequestMapping("/api/tarefas/{tarefaId}/comentarios")
class ComentarioController(
    private val comentarioService: ComentarioService
) {

    // "principal" é o usuário logado. Com JWT, principal.name é o e-mail guardado no token.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(
        @PathVariable tarefaId: Long,
        @Valid @RequestBody dados: ComentarioRequest,
        principal: Principal
    ): ComentarioResponse {
        return comentarioService.criar(tarefaId, dados, principal.name)
    }

    @GetMapping
    fun listar(@PathVariable tarefaId: Long): List<ComentarioResponse> {
        return comentarioService.listar(tarefaId)
    }
}
