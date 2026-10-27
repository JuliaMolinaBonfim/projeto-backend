
package br.com.taskflow.controller

import br.com.taskflow.model.Projeto
import br.com.taskflow.repository.ProjetoRepository
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/projetos")
class ProjetoController(
    private val projetoRepository: ProjetoRepository
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun criar(@RequestBody projeto: Projeto): Projeto {
        return projetoRepository.save(projeto)
    }

    @GetMapping
    fun listar(): List<Projeto> {
        return projetoRepository.findAll()
    }
}
