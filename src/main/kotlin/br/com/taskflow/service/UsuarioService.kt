package br.com.taskflow.service

import br.com.taskflow.dto.UsuarioResponse
import br.com.taskflow.dto.toResponse
import br.com.taskflow.repository.UsuarioRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UsuarioService(
    private val usuarioRepository: UsuarioRepository
) {
    private val logger = LoggerFactory.getLogger(UsuarioService::class.java)

    fun listar(): List<UsuarioResponse> {
        logger.info("Listando usuários")
        return usuarioRepository.findAll().map { it.toResponse() }
    }
}
