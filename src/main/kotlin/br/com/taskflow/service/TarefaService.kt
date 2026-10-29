
package br.com.taskflow.service

import br.com.taskflow.model.Tarefa
import br.com.taskflow.repository.TarefaRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class TarefaService(
    private val tarefaRepository: TarefaRepository
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
}
