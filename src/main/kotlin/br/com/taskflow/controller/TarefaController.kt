package br.com.taskflow.controller

import br.com.taskflow.dto.TarefaRequest
import br.com.taskflow.dto.TarefaResponse
import br.com.taskflow.service.TarefaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/tarefas")
class TarefaController(
    private val tarefaService: TarefaService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody dados: TarefaRequest): TarefaResponse {
        return tarefaService.criar(dados)
    }

    @GetMapping
    fun listar(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) titulo: String?,
        @RequestParam(defaultValue = "id") ordenarPor: String,
        @RequestParam(defaultValue = "asc") direcao: String
    ): List<TarefaResponse> {
        return tarefaService.listar(status, titulo, ordenarPor, direcao)
    }

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): TarefaResponse {
        return tarefaService.buscarPorId(id)
    }

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody dados: TarefaRequest
    ): TarefaResponse {
        return tarefaService.atualizar(id, dados)
    }

    // Somente ADMIN
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) {
        tarefaService.excluir(id)
    }

    @PostMapping("/{id}/categorias/{categoriaId}")
    fun adicionarCategoria(
        @PathVariable id: Long,
        @PathVariable categoriaId: Long
    ): TarefaResponse {
        return tarefaService.adicionarCategoria(id, categoriaId)
    }

    // Remover uma categoria de uma tarefa (somente ADMIN, pois é DELETE)
    @DeleteMapping("/{id}/categorias/{categoriaId}")
    fun removerCategoria(
        @PathVariable id: Long,
        @PathVariable categoriaId: Long
    ): TarefaResponse {
        return tarefaService.removerCategoria(id, categoriaId)
    }
}
