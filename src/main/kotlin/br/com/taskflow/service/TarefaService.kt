package br.com.taskflow.service

import br.com.taskflow.model.Tarefa
import br.com.taskflow.repository.CategoriaRepository
import br.com.taskflow.repository.TarefaRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
class TarefaService(
    private val tarefaRepository: TarefaRepository,
    private val categoriaRepository: CategoriaRepository
) {
    private val logger = LoggerFactory.getLogger(TarefaService::class.java)

    fun criar(tarefa: Tarefa): Tarefa {
        logger.info("Criando tarefa: {}", tarefa.titulo)
        return tarefaRepository.save(tarefa)
    }

    fun buscarPorId(id: Long): Tarefa {
        return tarefaRepository.findById(id).orElseThrow {
            logger.warn("Tarefa não encontrada: {}", id)
            ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")
        }
    }

    fun excluir(id: Long) {
        buscarPorId(id)
        logger.warn("Excluindo tarefa: {}", id)
        tarefaRepository.deleteById(id)
    }

    // Associa uma categoria já existente a uma tarefa já existente
    @Transactional
    fun adicionarCategoria(tarefaId: Long, categoriaId: Long): Tarefa {
        val tarefa = buscarPorId(tarefaId)

        val categoria = categoriaRepository.findById(categoriaId).orElseThrow {
            logger.warn("Categoria não encontrada: {}", categoriaId)
            ResponseStatusException(HttpStatus.NOT_FOUND, "Categoria não encontrada")
        }

        if (tarefa.categorias.any { it.id == categoriaId }) {
            logger.warn("Tarefa {} já possui a categoria {}", tarefaId, categoriaId)
            throw ResponseStatusException(HttpStatus.CONFLICT, "A tarefa já possui esta categoria")
        }

        tarefa.categorias.add(categoria)
        logger.info("Categoria {} adicionada à tarefa {}", categoriaId, tarefaId)
        return tarefaRepository.save(tarefa)
    }

    // Remove a associação entre a tarefa e a categoria (não apaga a categoria)
    @Transactional
    fun removerCategoria(tarefaId: Long, categoriaId: Long): Tarefa {
        val tarefa = buscarPorId(tarefaId)

        val removeu = tarefa.categorias.removeIf { it.id == categoriaId }
        if (!removeu) {
            logger.warn("Tarefa {} não possui a categoria {}", tarefaId, categoriaId)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "A tarefa não possui esta categoria")
        }

        logger.warn("Categoria {} removida da tarefa {}", categoriaId, tarefaId)
        return tarefaRepository.save(tarefa)
    }
}