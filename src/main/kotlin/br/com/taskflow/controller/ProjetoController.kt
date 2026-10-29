
package br.com.taskflow.controller

import br.com.taskflow.model.Projeto
import br.com.taskflow.repository.ProjetoRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException
import br.com.taskflow.repository.TarefaRepository
import br.com.taskflow.model.Tarefa

@RestController
@RequestMapping("/api/projetos")
class ProjetoController(
    private val projetoRepository: ProjetoRepository,
    private val tarefaRepository: TarefaRepository
) {

    // 1. Criar projeto
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@RequestBody projeto: Projeto): Projeto {
        return projetoRepository.save(projeto)
    }

    // 2. Listar todos os projetos
    @GetMapping
    fun listar(): List<Projeto> {
        return projetoRepository.findAll()
    }

    // 3. Buscar projeto pelo ID
    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): Projeto {
        return projetoRepository.findById(id).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Projeto não encontrado"
            )
        }
    }

    // 4. Atualizar projeto
    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody dados: Projeto
    ): Projeto {
        val projeto = buscarPorId(id)

        val atualizado = Projeto(
            id = projeto.id,
            nome = dados.nome,
            descricao = dados.descricao,
            prazo = dados.prazo
        )

        return projetoRepository.save(atualizado)
    }

    // 5. Excluir projeto
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) {
        buscarPorId(id)
        projetoRepository.deleteById(id)
    }

    @GetMapping("/{id}/tarefas")
    fun listarTarefasDoProjeto(@PathVariable id: Long): List<Tarefa> {
        buscarPorId(id)
        return tarefaRepository.findByProjetoId(id)
    }
}

