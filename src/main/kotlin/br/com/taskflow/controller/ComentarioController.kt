
package br.com.taskflow.controller

import br.com.taskflow.model.Comentario
import br.com.taskflow.repository.ComentarioRepository
import br.com.taskflow.repository.TarefaRepository
import br.com.taskflow.repository.UsuarioRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class ComentarioRequest(
    val texto: String,
    val usuarioId: Long
)

@RestController
@RequestMapping("/api/tarefas/{tarefaId}/comentarios")
class ComentarioController(
    private val comentarioRepository: ComentarioRepository,
    private val tarefaRepository: TarefaRepository,
    private val usuarioRepository: UsuarioRepository
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(
        @PathVariable tarefaId: Long,
        @RequestBody dados: ComentarioRequest
    ): Comentario {
        val tarefa = tarefaRepository.findById(tarefaId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")
        }

        val usuario = usuarioRepository.findById(dados.usuarioId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")
        }

        val comentario = Comentario(
            texto = dados.texto,
            tarefa = tarefa,
            autor = usuario
        )

        return comentarioRepository.save(comentario)
    }

    @GetMapping
    fun listar(@PathVariable tarefaId: Long): List<Comentario> {
        if (!tarefaRepository.existsById(tarefaId)) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")
        }

        return comentarioRepository.findByTarefaId(tarefaId)
    }
}
