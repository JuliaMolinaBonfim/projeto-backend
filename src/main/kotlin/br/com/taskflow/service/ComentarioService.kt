package br.com.taskflow.service

import br.com.taskflow.dto.ComentarioRequest
import br.com.taskflow.dto.ComentarioResponse
import br.com.taskflow.dto.toResponse
import br.com.taskflow.model.Comentario
import br.com.taskflow.repository.ComentarioRepository
import br.com.taskflow.repository.TarefaRepository
import br.com.taskflow.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.server.ResponseStatusException

@Service
@Transactional
class ComentarioService(
    private val comentarioRepository: ComentarioRepository,
    private val tarefaRepository: TarefaRepository,
    private val usuarioRepository: UsuarioRepository
) {
    private val logger = LoggerFactory.getLogger(ComentarioService::class.java)

    // O autor é o usuário logado (e-mail que vem do token JWT).
    fun criar(tarefaId: Long, dados: ComentarioRequest, emailAutor: String): ComentarioResponse {
        val tarefa = tarefaRepository.findById(tarefaId).orElseThrow {
            logger.warn("Tarefa não encontrada: {}", tarefaId)
            ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")
        }

        val autor = usuarioRepository.findByEmail(emailAutor)
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado")

        logger.info("Usuário {} comentou na tarefa {}", emailAutor, tarefaId)
        val comentario = Comentario(texto = dados.texto, tarefa = tarefa, autor = autor)
        return comentarioRepository.save(comentario).toResponse()
    }

    @Transactional(readOnly = true)
    fun listar(tarefaId: Long): List<ComentarioResponse> {
        if (!tarefaRepository.existsById(tarefaId)) {
            logger.warn("Tarefa não encontrada: {}", tarefaId)
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Tarefa não encontrada")
        }

        return comentarioRepository.findByTarefaId(tarefaId).map { it.toResponse() }
    }
}
