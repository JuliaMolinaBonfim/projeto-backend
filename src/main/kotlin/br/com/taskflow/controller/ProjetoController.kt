
package br.com.taskflow.controller

import br.com.taskflow.dto.ProjetoRequest
import br.com.taskflow.dto.ProjetoResponse
import br.com.taskflow.dto.TarefaResponse
import br.com.taskflow.service.ProjetoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/projetos")
class ProjetoController(
    private val projetoService: ProjetoService
) {

    // Criar projeto
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody dados: ProjetoRequest): ProjetoResponse {
        return projetoService.criar(dados)
    }

    // Listar todos os projetos
    @GetMapping
    fun listar(): List<ProjetoResponse> {
        return projetoService.listar()
    }

    // Buscar projeto pelo ID
    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): ProjetoResponse {
        return projetoService.buscarPorId(id)
    }

    // Atualizar projeto
    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody dados: ProjetoRequest
    ): ProjetoResponse {
        return projetoService.atualizar(id, dados)
    }

    // Excluir projeto (somente ADMIN)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) {
        projetoService.excluir(id)
    }

    // Listar tarefas de um projeto
    @GetMapping("/{id}/tarefas")
    fun listarTarefasDoProjeto(@PathVariable id: Long): List<TarefaResponse> {
        return projetoService.listarTarefas(id)
    }
}
