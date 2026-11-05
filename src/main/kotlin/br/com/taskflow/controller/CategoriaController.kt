
package br.com.taskflow.controller

import br.com.taskflow.model.Categoria
import br.com.taskflow.repository.CategoriaRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import org.springframework.web.server.ResponseStatusException

@RestController
@RequestMapping("/api/categorias")
class CategoriaController(
    private val categoriaRepository: CategoriaRepository
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@RequestBody categoria: Categoria): Categoria {
        return categoriaRepository.save(categoria)
    }

    @GetMapping
    fun listar(): List<Categoria> {
        return categoriaRepository.findAll()
    }

    @GetMapping("/{id}")
    fun buscarPorId(@PathVariable id: Long): Categoria {
        return categoriaRepository.findById(id).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoria não encontrada"
            )
        }
    }

    @PutMapping("/{id}")
    fun atualizar(
        @PathVariable id: Long,
        @RequestBody dados: Categoria
    ): Categoria {
        val categoria = categoriaRepository.findById(id).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoria não encontrada"
            )
        }

        val atualizada = Categoria(
            id = categoria.id,
            nome = dados.nome
        )

        return categoriaRepository.save(atualizada)
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun excluir(@PathVariable id: Long) {
        val categoria = categoriaRepository.findById(id).orElseThrow {
            ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Categoria não encontrada"
            )
        }

        categoriaRepository.delete(categoria)
    }


}

