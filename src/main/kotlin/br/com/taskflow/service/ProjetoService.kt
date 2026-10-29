
package br.com.taskflow.service

import br.com.taskflow.model.Projeto
import br.com.taskflow.repository.ProjetoRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class ProjetoService(
    private val projetoRepository: ProjetoRepository
) {
    private val logger = LoggerFactory.getLogger(ProjetoService::class.java)

    fun criar(projeto: Projeto): Projeto {
        logger.info("Criando projeto: {}", projeto.nome)
        return projetoRepository.save(projeto)
    }

    fun buscarPorId(id: Long): Projeto {
        return projetoRepository.findById(id).orElseThrow {
            logger.warn("Projeto não encontrado: {}", id)
            ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado")
        }
    }

    fun excluir(id: Long) {
        buscarPorId(id)
        logger.warn("Excluindo projeto: {}", id)
        projetoRepository.deleteById(id)
    }
}
