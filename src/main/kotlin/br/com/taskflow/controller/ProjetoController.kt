
package br.com.taskflow.controller

import br.com.taskflow.model.Projeto
import br.com.taskflow.model.Tarefa
import br.com.taskflow.repository.ProjetoRepository
import br.com.taskflow.repository.TarefaRepository
import br.com.taskflow.service.ProjetoService
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/projetos")
class ProjetoController(
    private val projetoRepository: ProjetoRepository,
    private val tarefaRepository: TarefaRepository,
    private val projetoService: ProjetoService
) {

    // Criar projeto
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@RequestBody projeto: Projeto): Projeto {
        return projetoService.criar(projeto)
    }

    // Listar todos os projetos
    @GetMapping
    fun listar(): List<Projeto> {
        return projetoRepository.findAll()
    }

    // Buscar projeto pelo ID
    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): Projeto {
        return projetoService.buscarPorId(id)
    }

    // Atualizar projeto
    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody dados: Projeto
    ): Projeto {
        val projeto = projetoService.buscarPorId(id)

        val atualizado = Projeto(
            id = projeto.id,
            nome = dados.nome,
            descricao = dados.descricao,
            prazo = dados.prazo
        )

        return projetoRepository.save(atualizado)
    }

    // Excluir projeto
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) {
        projetoService.excluir(id)
    }

    // Listar tarefas de um projeto
    @GetMapping("/{id}/tarefas")
    fun listarTarefasDoProjeto(
        @PathVariable id: Long
    ): List<Tarefa> {
        projetoService.buscarPorId(id)
        return tarefaRepository.findByProjetoId(id)
    }
}
