package br.com.taskflow.service

import br.com.taskflow.dto.CategoriaRequest
import br.com.taskflow.dto.CategoriaResponse
import br.com.taskflow.dto.toResponse
import br.com.taskflow.model.Categoria
import br.com.taskflow.repository.CategoriaRepository
import br.com.taskflow.repository.TarefaRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class CategoriaService(
    private val categoriaRepository: CategoriaRepository,
    private val tarefaRepository: TarefaRepository
) {
    private val logger = LoggerFactory.getLogger(CategoriaService::class.java)

    fun criar(dados: CategoriaRequest): CategoriaResponse {
        logger.info("Criando categoria: {}", dados.nome)
        return categoriaRepository.save(Categoria(nome = dados.nome)).toResponse()
    }

    @Transactional(readOnly = true)
    fun listar(): List<CategoriaResponse> {
        return categoriaRepository.findAll().map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): CategoriaResponse {
        return buscarEntidade(id).toResponse()
    }

    fun atualizar(id: Long, dados: CategoriaRequest): CategoriaResponse {
        val categoria = buscarEntidade(id)
        categoria.nome = dados.nome
        logger.info("Atualizando categoria: {}", id)
        return categoriaRepository.save(categoria).toResponse()
    }

    // Não permite excluir se a categoria estiver em uso por alguma tarefa.
    fun excluir(id: Long) {
        val categoria = buscarEntidade(id)

        if (tarefaRepository.existsByCategoriasId(id)) {
            logger.warn("Categoria {} não pode ser excluída: está em uso por tarefas", id)
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "A categoria está em uso por tarefas e não pode ser excluída"
            )
        }

        logger.warn("Excluindo categoria: {}", id)
        categoriaRepository.delete(categoria)
    }

    private fun buscarEntidade(id: Long): Categoria {
        return categoriaRepository.findById(id).orElseThrow {
            logger.warn("Categoria não encontrada: {}", id)
            ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada")
        }
    }
}
