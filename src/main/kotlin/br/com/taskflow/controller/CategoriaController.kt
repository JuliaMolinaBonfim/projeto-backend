
package br.com.taskflow.controller

import br.com.taskflow.model.Categoria
import br.com.taskflow.repository.CategoriaRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

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
}
