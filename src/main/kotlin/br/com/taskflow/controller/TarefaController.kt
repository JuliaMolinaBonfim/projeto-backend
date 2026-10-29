
package br.com.taskflow.controller

import br.com.taskflow.model.Tarefa
import br.com.taskflow.repository.ProjetoRepository
import br.com.taskflow.repository.TarefaRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class TarefaRequest(
    val titulo: String,
    val descricao: String,
    val status: String = "PENDENTE",
    val projetoId: Long
)

@RestController
@RequestMapping("/api/tarefas")
class TarefaController(
    private val tarefaRepository: TarefaRepository,
    private val projetoRepository: ProjetoRepository
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@RequestBody dados: TarefaRequest): Tarefa {
        val projeto = projetoRepository.findById(dados.projetoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado")
        }

        val tarefa = Tarefa(
            titulo = dados.titulo,
            descricao = dados.descricao,
            status = dados.status,
            projeto = projeto
        )

        return tarefaRepository.save(tarefa)
    }

    @GetMapping
    fun listar(): List<Tarefa> = tarefaRepository.findAll()

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): Tarefa {
        return tarefaRepository.findById(id).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")
        }
    }

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody dados: TarefaRequest
    ): Tarefa {
        val tarefa = buscarPorId(id)

        val projeto = projetoRepository.findById(dados.projetoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado")
        }

        val atualizada = Tarefa(
            id = tarefa.id,
            titulo = dados.titulo,
            descricao = dados.descricao,
            status = dados.status,
            projeto = projeto
        )

        return tarefaRepository.save(atualizada)
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) {
        buscarPorId(id)
        tarefaRepository.deleteById(id)
    }
}
