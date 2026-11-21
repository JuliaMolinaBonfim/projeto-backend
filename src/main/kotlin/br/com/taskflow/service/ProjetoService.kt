
package br.com.taskflow.service

import br.com.taskflow.dto.ProjetoRequest
import br.com.taskflow.dto.ProjetoResponse
import br.com.taskflow.dto.TarefaResponse
import br.com.taskflow.dto.toResponse
import br.com.taskflow.model.Projeto
import br.com.taskflow.repository.ProjetoRepository
import br.com.taskflow.repository.TarefaRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class ProjetoService(
    private val projetoRepository: ProjetoRepository,
    private val tarefaRepository: TarefaRepository
) {
    private val logger = LoggerFactory.getLogger(ProjetoService::class.java)

    fun criar(dados: ProjetoRequest): ProjetoResponse {
        logger.info("Criando projeto: {}", dados.nome)
        val projeto = Projeto(
            nome = dados.nome,
            descricao = dados.descricao,
            prazo = dados.prazo
        )
        return projetoRepository.save(projeto).toResponse()
    }

    @Transactional(readOnly = true)
    fun listar(): List<ProjetoResponse> {
        return projetoRepository.findAll().map { it.toResponse() }
    }

    @Transactional(readOnly = true)
    fun buscarPorId(id: Long): ProjetoResponse {
        return buscarEntidade(id).toResponse()
    }

    fun atualizar(id: Long, dados: ProjetoRequest): ProjetoResponse {
        val projeto = buscarEntidade(id)
        projeto.nome = dados.nome
        projeto.descricao = dados.descricao
        projeto.prazo = dados.prazo
        logger.info("Atualizando projeto: {}", id)
        return projetoRepository.save(projeto).toResponse()
    }

    fun excluir(id: Long) {
        val projeto = buscarEntidade(id)

        if (tarefaRepository.existsByProjetoId(id)) {
            logger.warn("Projeto {} não pode ser excluído: possui tarefas", id)
            throw ResponseStatusException(
                HttpStatus.CONFLICT,
                "O projeto possui tarefas e não pode ser excluído"
            )
        }

        logger.warn("Excluindo projeto: {}", id)
        projetoRepository.delete(projeto)
    }

    @Transactional(readOnly = true)
    fun listarTarefas(id: Long): List<TarefaResponse> {
        buscarEntidade(id)
        return tarefaRepository.findByProjetoId(id).map { it.toResponse() }
    }

    // Método interno: devolve a entidade (nunca sai desta classe).
    private fun buscarEntidade(id: Long): Projeto {
        return projetoRepository.findById(id).orElseThrow {
            logger.warn("Projeto não encontrado: {}", id)
            ResponseStatusException(HttpStatus.NOT_FOUND, "Projeto não encontrado")
        }
    }
}
