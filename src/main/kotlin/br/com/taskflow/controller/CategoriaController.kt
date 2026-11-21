package br.com.taskflow.controller

import br.com.taskflow.dto.CategoriaRequest
import br.com.taskflow.dto.CategoriaResponse
import br.com.taskflow.service.CategoriaService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/categorias")
class CategoriaController(
    private val categoriaService: CategoriaService
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@Valid @RequestBody dados: CategoriaRequest): CategoriaResponse {
        return categoriaService.criar(dados)
    }

    @GetMapping
    fun listar(): List<CategoriaResponse> {
        return categoriaService.listar()
    }

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): CategoriaResponse {
        return categoriaService.buscarPorId(id)
    }

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @Valid @RequestBody dados: CategoriaRequest
    ): CategoriaResponse {
        return categoriaService.atualizar(id, dados)
    }

    // Excluir categoria (somente ADMIN). Não permite se estiver em uso por alguma tarefa.
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) {
        categoriaService.excluir(id)
    }
}
