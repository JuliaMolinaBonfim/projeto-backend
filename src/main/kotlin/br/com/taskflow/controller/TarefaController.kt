
package br.com.taskflow.controller

import br.com.taskflow.model.Tarefa
import br.com.taskflow.repository.CategoriaRepository
import br.com.taskflow.repository.ProjetoRepository
import br.com.taskflow.repository.TarefaRepository
import br.com.taskflow.service.TarefaService
import org.springframework.data.domain.Sort
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

data class TarefaRequest(
    val titulo: String,
    val descricao: String,
    val status: String = "PENDENTE",
    val projetoId: Long,
    val categoriaIds: List<Long> = emptyList()
)

@RestController
@RequestMapping("/api/tarefas")
class TarefaController(
    private val tarefaRepository: TarefaRepository,
    private val projetoRepository: ProjetoRepository,
    private val categoriaRepository: CategoriaRepository,
    private val tarefaService: TarefaService
) {

    // Criar tarefa
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@RequestBody dados: TarefaRequest): Tarefa {
        val projeto = projetoRepository.findById(dados.projetoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado")
        }

        val categorias = dados.categoriaIds.map { id ->
            categoriaRepository.findById(id).orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Categoria não encontrada: $id"
                )
            }
        }.toMutableSet()

        val tarefa = Tarefa(
            titulo = dados.titulo,
            descricao = dados.descricao,
            status = dados.status,
            projeto = projeto,
            categorias = categorias
        )

        return tarefaService.criar(tarefa)
    }

    // Listar tarefas com filtros e ordenação
    @GetMapping
    fun listar(
        @RequestParam(required = false) status: String?,
        @RequestParam(required = false) titulo: String?,
        @RequestParam(defaultValue = "id") ordenarPor: String,
        @RequestParam(defaultValue = "asc") direcao: String
    ): List<Tarefa> {

        val camposPermitidos = setOf("id", "titulo", "status")

        if (ordenarPor !in camposPermitidos) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Campo de ordenação inválido"
            )
        }

        if (direcao.lowercase() !in setOf("asc", "desc")) {
            throw ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Direção inválida. Use asc ou desc"
            )
        }

        val ordenacao = Sort.by(
            if (direcao.equals("desc", ignoreCase = true))
                Sort.Direction.DESC
            else
                Sort.Direction.ASC,
            ordenarPor
        )

        return tarefaRepository.findAll(ordenacao)
            .filter {
                status == null || it.status.equals(status, ignoreCase = true)
            }
            .filter {
                titulo == null || it.titulo.contains(titulo, ignoreCase = true)
            }
    }

    // Buscar tarefa pelo ID
    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): Tarefa {
        return tarefaService.buscarPorId(id)
    }

    // Atualizar tarefa
    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody dados: TarefaRequest
    ): Tarefa {
        val tarefa = tarefaService.buscarPorId(id)

        val projeto = projetoRepository.findById(dados.projetoId).orElseThrow {
            ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado")
        }

        val categorias = dados.categoriaIds.map { categoriaId ->
            categoriaRepository.findById(categoriaId).orElseThrow {
                ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Categoria não encontrada: $categoriaId"
                )
            }
        }.toMutableSet()

        val atualizada = Tarefa(
            id = tarefa.id,
            titulo = dados.titulo,
            descricao = dados.descricao,
            status = dados.status,
            projeto = projeto,
            categorias = categorias
        )

        return tarefaRepository.save(atualizada)
    }

    // Excluir tarefa
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) {
        tarefaService.excluir(id)
    }
}
